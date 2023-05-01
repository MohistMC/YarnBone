/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.server.network;

import com.google.common.primitives.Ints;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.logging.LogUtils;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.PrivateKey;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.encryption.NetworkEncryptionException;
import net.minecraft.network.encryption.NetworkEncryptionUtils;
import net.minecraft.network.listener.ServerLoginPacketListener;
import net.minecraft.network.listener.TickablePacketListener;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import net.minecraft.network.packet.c2s.login.LoginKeyC2SPacket;
import net.minecraft.network.packet.c2s.login.LoginQueryResponseC2SPacket;
import net.minecraft.network.packet.s2c.login.LoginCompressionS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginDisconnectS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginHelloS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginSuccessS2CPacket;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Uuids;
import net.minecraft.util.logging.UncaughtExceptionLogger;
import net.minecraft.util.math.random.Random;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ServerLoginNetworkHandler
implements ServerLoginPacketListener,
TickablePacketListener {
    private static final AtomicInteger NEXT_AUTHENTICATOR_THREAD_ID = new AtomicInteger(0);
    static final Logger LOGGER = LogUtils.getLogger();
    private static final int TIMEOUT_TICKS = 600;
    private static final Random RANDOM = Random.create();
    private final byte[] nonce;
    final MinecraftServer server;
    final ClientConnection connection;
    State state = State.HELLO;
    private int loginTicks;
    @Nullable
    GameProfile profile;
    private final String serverId = "";
    @Nullable
    private ServerPlayerEntity delayedPlayer;

    public ServerLoginNetworkHandler(MinecraftServer server, ClientConnection connection) {
        this.server = server;
        this.connection = connection;
        this.nonce = Ints.toByteArray(RANDOM.nextInt());
    }

    @Override
    public void tick() {
        ServerPlayerEntity lv;
        if (this.state == State.READY_TO_ACCEPT) {
            this.acceptPlayer();
        } else if (this.state == State.DELAY_ACCEPT && (lv = this.server.getPlayerManager().getPlayer(this.profile.getId())) == null) {
            this.state = State.READY_TO_ACCEPT;
            this.addToServer(this.delayedPlayer);
            this.delayedPlayer = null;
        }
        if (this.loginTicks++ == 600) {
            this.disconnect(Text.translatable("multiplayer.disconnect.slow_login"));
        }
    }

    @Override
    public boolean isConnectionOpen() {
        return this.connection.isOpen();
    }

    public void disconnect(Text reason) {
        try {
            LOGGER.info("Disconnecting {}: {}", (Object)this.getConnectionInfo(), (Object)reason.getString());
            this.connection.send(new LoginDisconnectS2CPacket(reason));
            this.connection.disconnect(reason);
        }
        catch (Exception exception) {
            LOGGER.error("Error whilst disconnecting player", exception);
        }
    }

    public void acceptPlayer() {
        Text lv;
        if (!this.profile.isComplete()) {
            this.profile = this.toOfflineProfile(this.profile);
        }
        if ((lv = this.server.getPlayerManager().checkCanJoin(this.connection.getAddress(), this.profile)) != null) {
            this.disconnect(lv);
        } else {
            this.state = State.ACCEPTED;
            if (this.server.getNetworkCompressionThreshold() >= 0 && !this.connection.isLocal()) {
                this.connection.send(new LoginCompressionS2CPacket(this.server.getNetworkCompressionThreshold()), PacketCallbacks.always(() -> this.connection.setCompressionThreshold(this.server.getNetworkCompressionThreshold(), true)));
            }
            this.connection.send(new LoginSuccessS2CPacket(this.profile));
            ServerPlayerEntity lv2 = this.server.getPlayerManager().getPlayer(this.profile.getId());
            try {
                ServerPlayerEntity lv3 = this.server.getPlayerManager().createPlayer(this.profile);
                if (lv2 != null) {
                    this.state = State.DELAY_ACCEPT;
                    this.delayedPlayer = lv3;
                } else {
                    this.addToServer(lv3);
                }
            }
            catch (Exception exception) {
                LOGGER.error("Couldn't place player in world", exception);
                MutableText lv4 = Text.translatable("multiplayer.disconnect.invalid_player_data");
                this.connection.send(new DisconnectS2CPacket(lv4));
                this.connection.disconnect(lv4);
            }
        }
    }

    private void addToServer(ServerPlayerEntity player) {
        this.server.getPlayerManager().onPlayerConnect(this.connection, player);
    }

    @Override
    public void onDisconnected(Text reason) {
        LOGGER.info("{} lost connection: {}", (Object)this.getConnectionInfo(), (Object)reason.getString());
    }

    public String getConnectionInfo() {
        if (this.profile != null) {
            return this.profile + " (" + this.connection.getAddress() + ")";
        }
        return String.valueOf(this.connection.getAddress());
    }

    @Override
    public void onHello(LoginHelloC2SPacket packet) {
        Validate.validState(this.state == State.HELLO, "Unexpected hello packet", new Object[0]);
        Validate.validState(ServerLoginNetworkHandler.isValidName(packet.name()), "Invalid characters in username", new Object[0]);
        GameProfile gameProfile = this.server.getHostProfile();
        if (gameProfile != null && packet.name().equalsIgnoreCase(gameProfile.getName())) {
            this.profile = gameProfile;
            this.state = State.READY_TO_ACCEPT;
            return;
        }
        this.profile = new GameProfile(null, packet.name());
        if (this.server.isOnlineMode() && !this.connection.isLocal()) {
            this.state = State.KEY;
            this.connection.send(new LoginHelloS2CPacket("", this.server.getKeyPair().getPublic().getEncoded(), this.nonce));
        } else {
            this.state = State.READY_TO_ACCEPT;
        }
    }

    public static boolean isValidName(String name) {
        return name.chars().filter(c -> c <= 32 || c >= 127).findAny().isEmpty();
    }

    @Override
    public void onKey(LoginKeyC2SPacket packet) {
        String string;
        Validate.validState(this.state == State.KEY, "Unexpected key packet", new Object[0]);
        try {
            PrivateKey privateKey = this.server.getKeyPair().getPrivate();
            if (!packet.verifySignedNonce(this.nonce, privateKey)) {
                throw new IllegalStateException("Protocol error");
            }
            SecretKey secretKey = packet.decryptSecretKey(privateKey);
            Cipher cipher = NetworkEncryptionUtils.cipherFromKey(2, secretKey);
            Cipher cipher2 = NetworkEncryptionUtils.cipherFromKey(1, secretKey);
            string = new BigInteger(NetworkEncryptionUtils.computeServerId("", this.server.getKeyPair().getPublic(), secretKey)).toString(16);
            this.state = State.AUTHENTICATING;
            this.connection.setupEncryption(cipher, cipher2);
        }
        catch (NetworkEncryptionException lv) {
            throw new IllegalStateException("Protocol error", lv);
        }
        Thread thread = new Thread("User Authenticator #" + NEXT_AUTHENTICATOR_THREAD_ID.incrementAndGet()){

            @Override
            public void run() {
                GameProfile gameProfile = ServerLoginNetworkHandler.this.profile;
                try {
                    ServerLoginNetworkHandler.this.profile = ServerLoginNetworkHandler.this.server.getSessionService().hasJoinedServer(new GameProfile(null, gameProfile.getName()), string, this.getClientAddress());
                    if (ServerLoginNetworkHandler.this.profile != null) {
                        LOGGER.info("UUID of player {} is {}", (Object)ServerLoginNetworkHandler.this.profile.getName(), (Object)ServerLoginNetworkHandler.this.profile.getId());
                        ServerLoginNetworkHandler.this.state = State.READY_TO_ACCEPT;
                    } else if (ServerLoginNetworkHandler.this.server.isSingleplayer()) {
                        LOGGER.warn("Failed to verify username but will let them in anyway!");
                        ServerLoginNetworkHandler.this.profile = gameProfile;
                        ServerLoginNetworkHandler.this.state = State.READY_TO_ACCEPT;
                    } else {
                        ServerLoginNetworkHandler.this.disconnect(Text.translatable("multiplayer.disconnect.unverified_username"));
                        LOGGER.error("Username '{}' tried to join with an invalid session", (Object)gameProfile.getName());
                    }
                }
                catch (AuthenticationUnavailableException authenticationUnavailableException) {
                    if (ServerLoginNetworkHandler.this.server.isSingleplayer()) {
                        LOGGER.warn("Authentication servers are down but will let them in anyway!");
                        ServerLoginNetworkHandler.this.profile = gameProfile;
                        ServerLoginNetworkHandler.this.state = State.READY_TO_ACCEPT;
                    }
                    ServerLoginNetworkHandler.this.disconnect(Text.translatable("multiplayer.disconnect.authservers_down"));
                    LOGGER.error("Couldn't verify username because servers are unavailable");
                }
            }

            @Nullable
            private InetAddress getClientAddress() {
                SocketAddress socketAddress = ServerLoginNetworkHandler.this.connection.getAddress();
                return ServerLoginNetworkHandler.this.server.shouldPreventProxyConnections() && socketAddress instanceof InetSocketAddress ? ((InetSocketAddress)socketAddress).getAddress() : null;
            }
        };
        thread.setUncaughtExceptionHandler(new UncaughtExceptionLogger(LOGGER));
        thread.start();
    }

    @Override
    public void onQueryResponse(LoginQueryResponseC2SPacket packet) {
        this.disconnect(Text.translatable("multiplayer.disconnect.unexpected_query_response"));
    }

    protected GameProfile toOfflineProfile(GameProfile profile) {
        UUID uUID = Uuids.getOfflinePlayerUuid(profile.getName());
        return new GameProfile(uUID, profile.getName());
    }

    static enum State {
        HELLO,
        KEY,
        AUTHENTICATING,
        NEGOTIATING,
        READY_TO_ACCEPT,
        DELAY_ACCEPT,
        ACCEPTED;

    }
}

