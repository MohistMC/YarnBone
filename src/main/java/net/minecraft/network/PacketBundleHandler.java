/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.network;

import io.netty.util.AttributeKey;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.BundlePacket;
import net.minecraft.network.packet.BundleSplitterPacket;
import net.minecraft.network.packet.Packet;
import org.jetbrains.annotations.Nullable;

public interface PacketBundleHandler {
    public static final AttributeKey<BundlerGetter> KEY = AttributeKey.valueOf("bundler");
    public static final int MAX_PACKETS = 4096;
    public static final PacketBundleHandler NOOP = new PacketBundleHandler(){

        @Override
        public void forEachPacket(Packet<?> packet, Consumer<Packet<?>> consumer) {
            consumer.accept(packet);
        }

        @Override
        @Nullable
        public Bundler createBundler(Packet<?> splitter) {
            return null;
        }
    };

    public static <T extends PacketListener, P extends BundlePacket<T>> PacketBundleHandler create(final Class<P> bundlePacketType, final Function<Iterable<Packet<T>>, P> bundleFunction, final BundleSplitterPacket<T> splitter) {
        return new PacketBundleHandler(){

            @Override
            public void forEachPacket(Packet<?> packet, Consumer<Packet<?>> consumer) {
                if (packet.getClass() == bundlePacketType) {
                    BundlePacket lv = (BundlePacket)packet;
                    consumer.accept(splitter);
                    lv.getPackets().forEach(consumer);
                    consumer.accept(splitter);
                } else {
                    consumer.accept(packet);
                }
            }

            @Override
            @Nullable
            public Bundler createBundler(Packet<?> splitter2) {
                if (splitter2 == splitter) {
                    return new Bundler(){
                        private final List<Packet<T>> packets = new ArrayList();

                        @Override
                        @Nullable
                        public Packet<?> add(Packet<?> packet) {
                            if (packet == splitter) {
                                return (Packet)bundleFunction.apply(this.packets);
                            }
                            Packet<?> lv = packet;
                            if (this.packets.size() >= 4096) {
                                throw new IllegalStateException("Too many packets in a bundle");
                            }
                            this.packets.add(lv);
                            return null;
                        }
                    };
                }
                return null;
            }
        };
    }

    public void forEachPacket(Packet<?> var1, Consumer<Packet<?>> var2);

    @Nullable
    public Bundler createBundler(Packet<?> var1);

    public static interface BundlerGetter {
        public PacketBundleHandler getBundler(NetworkSide var1);
    }

    public static interface Bundler {
        @Nullable
        public Packet<?> add(Packet<?> var1);
    }
}

