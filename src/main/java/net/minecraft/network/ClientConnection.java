/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.network;

import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.logging.LogUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.MultithreadEventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.local.LocalServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.TimeoutException;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Queue;
import java.util.concurrent.RejectedExecutionException;
import javax.crypto.Cipher;
import net.minecraft.network.DecoderHandler;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.NetworkState;
import net.minecraft.network.OffThreadException;
import net.minecraft.network.PacketBundleHandler;
import net.minecraft.network.PacketBundler;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.PacketDeflater;
import net.minecraft.network.PacketEncoder;
import net.minecraft.network.PacketEncoderException;
import net.minecraft.network.PacketInflater;
import net.minecraft.network.PacketUnbundler;
import net.minecraft.network.SizePrepender;
import net.minecraft.network.SplitterHandler;
import net.minecraft.network.encryption.PacketDecryptor;
import net.minecraft.network.encryption.PacketEncryptor;
import net.minecraft.network.listener.ClientLoginPacketListener;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.listener.TickablePacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.login.LoginDisconnectS2CPacket;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Lazy;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class ClientConnection
extends SimpleChannelInboundHandler<Packet<?>> {
    private static final float CURRENT_PACKET_COUNTER_WEIGHT = 0.75f;
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Marker NETWORK_MARKER = MarkerFactory.getMarker("NETWORK");
    public static final Marker NETWORK_PACKETS_MARKER = Util.make(MarkerFactory.getMarker("NETWORK_PACKETS"), marker -> marker.add(NETWORK_MARKER));
    public static final Marker PACKET_RECEIVED_MARKER = Util.make(MarkerFactory.getMarker("PACKET_RECEIVED"), marker -> marker.add(NETWORK_PACKETS_MARKER));
    public static final Marker PACKET_SENT_MARKER = Util.make(MarkerFactory.getMarker("PACKET_SENT"), marker -> marker.add(NETWORK_PACKETS_MARKER));
    public static final AttributeKey<NetworkState> PROTOCOL_ATTRIBUTE_KEY = AttributeKey.valueOf("protocol");
    public static final Lazy<NioEventLoopGroup> CLIENT_IO_GROUP = new Lazy<NioEventLoopGroup>(() -> new NioEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("Netty Client IO #%d").setDaemon(true).build()));
    public static final Lazy<EpollEventLoopGroup> EPOLL_CLIENT_IO_GROUP = new Lazy<EpollEventLoopGroup>(() -> new EpollEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("Netty Epoll Client IO #%d").setDaemon(true).build()));
    public static final Lazy<DefaultEventLoopGroup> LOCAL_CLIENT_IO_GROUP = new Lazy<DefaultEventLoopGroup>(() -> new DefaultEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("Netty Local Client IO #%d").setDaemon(true).build()));
    private final NetworkSide side;
    private final Queue<QueuedPacket> packetQueue = Queues.newConcurrentLinkedQueue();
    private Channel channel;
    private SocketAddress address;
    private PacketListener packetListener;
    private Text disconnectReason;
    private boolean encrypted;
    private boolean disconnected;
    private int packetsReceivedCounter;
    private int packetsSentCounter;
    private float averagePacketsReceived;
    private float averagePacketsSent;
    private int ticks;
    private boolean errored;

    public ClientConnection(NetworkSide side) {
        this.side = side;
    }

    @Override
    public void channelActive(ChannelHandlerContext context) throws Exception {
        super.channelActive(context);
        this.channel = context.channel();
        this.address = this.channel.remoteAddress();
        try {
            this.setState(NetworkState.HANDSHAKING);
        }
        catch (Throwable throwable) {
            LOGGER.error(LogUtils.FATAL_MARKER, "Failed to change protocol to handshake", throwable);
        }
    }

    public void setState(NetworkState state) {
        this.channel.attr(PROTOCOL_ATTRIBUTE_KEY).set(state);
        this.channel.attr(PacketBundleHandler.KEY).set(state);
        this.channel.config().setAutoRead(true);
        LOGGER.debug("Enabled auto read");
    }

    @Override
    public void channelInactive(ChannelHandlerContext context) {
        this.disconnect(Text.translatable("disconnect.endOfStream"));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable ex) {
        if (ex instanceof PacketEncoderException) {
            LOGGER.debug("Skipping packet due to errors", ex.getCause());
            return;
        }
        boolean bl = !this.errored;
        this.errored = true;
        if (!this.channel.isOpen()) {
            return;
        }
        if (ex instanceof TimeoutException) {
            LOGGER.debug("Timeout", ex);
            this.disconnect(Text.translatable("disconnect.timeout"));
        } else {
            MutableText lv = Text.translatable("disconnect.genericReason", "Internal Exception: " + ex);
            if (bl) {
                LOGGER.debug("Failed to sent packet", ex);
                NetworkState lv2 = this.getState();
                Packet<ClientLoginPacketListener> lv3 = lv2 == NetworkState.LOGIN ? new LoginDisconnectS2CPacket(lv) : new DisconnectS2CPacket(lv);
                this.send(lv3, PacketCallbacks.always(() -> this.disconnect(lv)));
                this.disableAutoRead();
            } else {
                LOGGER.debug("Double fault", ex);
                this.disconnect(lv);
            }
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Packet<?> arg) {
        if (this.channel.isOpen()) {
            try {
                ClientConnection.handlePacket(arg, this.packetListener);
            }
            catch (OffThreadException offThreadException) {
            }
            catch (RejectedExecutionException rejectedExecutionException) {
                this.disconnect(Text.translatable("multiplayer.disconnect.server_shutdown"));
            }
            catch (ClassCastException classCastException) {
                LOGGER.error("Received {} that couldn't be processed", (Object)arg.getClass(), (Object)classCastException);
                this.disconnect(Text.translatable("multiplayer.disconnect.invalid_packet"));
            }
            ++this.packetsReceivedCounter;
        }
    }

    private static <T extends PacketListener> void handlePacket(Packet<T> packet, PacketListener listener) {
        packet.apply(listener);
    }

    public void setPacketListener(PacketListener listener) {
        Validate.notNull(listener, "packetListener", new Object[0]);
        this.packetListener = listener;
    }

    public void send(Packet<?> packet) {
        this.send(packet, null);
    }

    public void send(Packet<?> packet, @Nullable PacketCallbacks callbacks) {
        if (this.isOpen()) {
            this.sendQueuedPackets();
            this.sendImmediately(packet, callbacks);
        } else {
            this.packetQueue.add(new QueuedPacket(packet, callbacks));
        }
    }

    private void sendImmediately(Packet<?> packet, @Nullable PacketCallbacks callbacks) {
        NetworkState lv = NetworkState.getPacketHandlerState(packet);
        NetworkState lv2 = this.getState();
        ++this.packetsSentCounter;
        if (lv2 != lv) {
            if (lv == null) {
                throw new IllegalStateException("Encountered packet without set protocol: " + packet);
            }
            LOGGER.debug("Disabled auto read");
            this.channel.config().setAutoRead(false);
        }
        if (this.channel.eventLoop().inEventLoop()) {
            this.sendInternal(packet, callbacks, lv, lv2);
        } else {
            this.channel.eventLoop().execute(() -> this.sendInternal(packet, callbacks, lv, lv2));
        }
    }

    private void sendInternal(Packet<?> packet, @Nullable PacketCallbacks callbacks, NetworkState packetState, NetworkState currentState) {
        if (packetState != currentState) {
            this.setState(packetState);
        }
        ChannelFuture channelFuture = this.channel.writeAndFlush(packet);
        if (callbacks != null) {
            channelFuture.addListener((GenericFutureListener<? extends Future<? super Void>>)((GenericFutureListener<Future>)future -> {
                if (future.isSuccess()) {
                    callbacks.onSuccess();
                } else {
                    Packet<?> lv = callbacks.getFailurePacket();
                    if (lv != null) {
                        ChannelFuture channelFuture = this.channel.writeAndFlush(lv);
                        channelFuture.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
                    }
                }
            }));
        }
        channelFuture.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    private NetworkState getState() {
        return this.channel.attr(PROTOCOL_ATTRIBUTE_KEY).get();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void sendQueuedPackets() {
        if (this.channel == null || !this.channel.isOpen()) {
            return;
        }
        Queue<QueuedPacket> queue = this.packetQueue;
        synchronized (queue) {
            QueuedPacket lv;
            while ((lv = this.packetQueue.poll()) != null) {
                this.sendImmediately(lv.packet, lv.callbacks);
            }
        }
    }

    public void tick() {
        this.sendQueuedPackets();
        PacketListener packetListener = this.packetListener;
        if (packetListener instanceof TickablePacketListener) {
            TickablePacketListener lv = (TickablePacketListener)packetListener;
            lv.tick();
        }
        if (!this.isOpen() && !this.disconnected) {
            this.handleDisconnection();
        }
        if (this.channel != null) {
            this.channel.flush();
        }
        if (this.ticks++ % 20 == 0) {
            this.updateStats();
        }
    }

    protected void updateStats() {
        this.averagePacketsSent = MathHelper.lerp(0.75f, (float)this.packetsSentCounter, this.averagePacketsSent);
        this.averagePacketsReceived = MathHelper.lerp(0.75f, (float)this.packetsReceivedCounter, this.averagePacketsReceived);
        this.packetsSentCounter = 0;
        this.packetsReceivedCounter = 0;
    }

    public SocketAddress getAddress() {
        return this.address;
    }

    public void disconnect(Text disconnectReason) {
        if (this.channel.isOpen()) {
            this.channel.close().awaitUninterruptibly();
            this.disconnectReason = disconnectReason;
        }
    }

    public boolean isLocal() {
        return this.channel instanceof LocalChannel || this.channel instanceof LocalServerChannel;
    }

    public NetworkSide getSide() {
        return this.side;
    }

    public NetworkSide getOppositeSide() {
        return this.side.getOpposite();
    }

    public static ClientConnection connect(InetSocketAddress address, boolean useEpoll) {
        Lazy<MultithreadEventLoopGroup> lv2;
        Class class_;
        final ClientConnection lv = new ClientConnection(NetworkSide.CLIENTBOUND);
        if (Epoll.isAvailable() && useEpoll) {
            class_ = EpollSocketChannel.class;
            lv2 = EPOLL_CLIENT_IO_GROUP;
        } else {
            class_ = NioSocketChannel.class;
            lv2 = CLIENT_IO_GROUP;
        }
        ((Bootstrap)((Bootstrap)((Bootstrap)new Bootstrap().group(lv2.get())).handler(new ChannelInitializer<Channel>(){

            @Override
            protected void initChannel(Channel channel) {
                try {
                    channel.config().setOption(ChannelOption.TCP_NODELAY, true);
                }
                catch (ChannelException channelException) {
                    // empty catch block
                }
                ChannelPipeline channelPipeline = channel.pipeline().addLast("timeout", (ChannelHandler)new ReadTimeoutHandler(30));
                ClientConnection.addHandlers(channelPipeline, NetworkSide.CLIENTBOUND);
                channelPipeline.addLast("packet_handler", (ChannelHandler)lv);
            }
        })).channel(class_)).connect(address.getAddress(), address.getPort()).syncUninterruptibly();
        return lv;
    }

    public static void addHandlers(ChannelPipeline pipeline, NetworkSide side) {
        NetworkSide lv = side.getOpposite();
        pipeline.addLast("splitter", (ChannelHandler)new SplitterHandler()).addLast("decoder", (ChannelHandler)new DecoderHandler(side)).addLast("prepender", (ChannelHandler)new SizePrepender()).addLast("encoder", (ChannelHandler)new PacketEncoder(lv)).addLast("unbundler", (ChannelHandler)new PacketUnbundler(lv)).addLast("bundler", (ChannelHandler)new PacketBundler(side));
    }

    public static ClientConnection connectLocal(SocketAddress address) {
        final ClientConnection lv = new ClientConnection(NetworkSide.CLIENTBOUND);
        ((Bootstrap)((Bootstrap)((Bootstrap)new Bootstrap().group(LOCAL_CLIENT_IO_GROUP.get())).handler(new ChannelInitializer<Channel>(){

            @Override
            protected void initChannel(Channel channel) {
                ChannelPipeline channelPipeline = channel.pipeline();
                channelPipeline.addLast("packet_handler", (ChannelHandler)lv);
            }
        })).channel(LocalChannel.class)).connect(address).syncUninterruptibly();
        return lv;
    }

    public void setupEncryption(Cipher decryptionCipher, Cipher encryptionCipher) {
        this.encrypted = true;
        this.channel.pipeline().addBefore("splitter", "decrypt", new PacketDecryptor(decryptionCipher));
        this.channel.pipeline().addBefore("prepender", "encrypt", new PacketEncryptor(encryptionCipher));
    }

    public boolean isEncrypted() {
        return this.encrypted;
    }

    public boolean isOpen() {
        return this.channel != null && this.channel.isOpen();
    }

    public boolean isChannelAbsent() {
        return this.channel == null;
    }

    public PacketListener getPacketListener() {
        return this.packetListener;
    }

    @Nullable
    public Text getDisconnectReason() {
        return this.disconnectReason;
    }

    public void disableAutoRead() {
        this.channel.config().setAutoRead(false);
    }

    public void setCompressionThreshold(int compressionThreshold, boolean rejectsBadPackets) {
        if (compressionThreshold >= 0) {
            if (this.channel.pipeline().get("decompress") instanceof PacketInflater) {
                ((PacketInflater)this.channel.pipeline().get("decompress")).setCompressionThreshold(compressionThreshold, rejectsBadPackets);
            } else {
                this.channel.pipeline().addBefore("decoder", "decompress", new PacketInflater(compressionThreshold, rejectsBadPackets));
            }
            if (this.channel.pipeline().get("compress") instanceof PacketDeflater) {
                ((PacketDeflater)this.channel.pipeline().get("compress")).setCompressionThreshold(compressionThreshold);
            } else {
                this.channel.pipeline().addBefore("encoder", "compress", new PacketDeflater(compressionThreshold));
            }
        } else {
            if (this.channel.pipeline().get("decompress") instanceof PacketInflater) {
                this.channel.pipeline().remove("decompress");
            }
            if (this.channel.pipeline().get("compress") instanceof PacketDeflater) {
                this.channel.pipeline().remove("compress");
            }
        }
    }

    public void handleDisconnection() {
        if (this.channel == null || this.channel.isOpen()) {
            return;
        }
        if (this.disconnected) {
            LOGGER.warn("handleDisconnection() called twice");
        } else {
            this.disconnected = true;
            if (this.getDisconnectReason() != null) {
                this.getPacketListener().onDisconnected(this.getDisconnectReason());
            } else if (this.getPacketListener() != null) {
                this.getPacketListener().onDisconnected(Text.translatable("multiplayer.disconnect.generic"));
            }
        }
    }

    public float getAveragePacketsReceived() {
        return this.averagePacketsReceived;
    }

    public float getAveragePacketsSent() {
        return this.averagePacketsSent;
    }

    @Override
    protected /* synthetic */ void channelRead0(ChannelHandlerContext context, Object packet) throws Exception {
        this.channelRead0(context, (Packet)packet);
    }

    static class QueuedPacket {
        final Packet<?> packet;
        @Nullable
        final PacketCallbacks callbacks;

        public QueuedPacket(Packet<?> packet, @Nullable PacketCallbacks callbacks) {
            this.packet = packet;
            this.callbacks = callbacks;
        }
    }
}

