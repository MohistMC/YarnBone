/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.server.network;

import net.minecraft.SharedConstants;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkState;
import net.minecraft.network.listener.ServerHandshakePacketListener;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.network.packet.s2c.login.LoginDisconnectS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerMetadata;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.server.network.ServerQueryNetworkHandler;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class ServerHandshakeNetworkHandler
implements ServerHandshakePacketListener {
    private static final Text IGNORING_STATUS_REQUEST_MESSAGE = Text.literal("Ignoring status request");
    private final MinecraftServer server;
    private final ClientConnection connection;

    public ServerHandshakeNetworkHandler(MinecraftServer server, ClientConnection connection) {
        this.server = server;
        this.connection = connection;
    }

    @Override
    public void onHandshake(HandshakeC2SPacket packet) {
        switch (packet.getIntendedState()) {
            case LOGIN: {
                this.connection.setState(NetworkState.LOGIN);
                if (packet.getProtocolVersion() != SharedConstants.getGameVersion().getProtocolVersion()) {
                    MutableText lv = packet.getProtocolVersion() < 754 ? Text.translatable("multiplayer.disconnect.outdated_client", SharedConstants.getGameVersion().getName()) : Text.translatable("multiplayer.disconnect.incompatible", SharedConstants.getGameVersion().getName());
                    this.connection.send(new LoginDisconnectS2CPacket(lv));
                    this.connection.disconnect(lv);
                    break;
                }
                this.connection.setPacketListener(new ServerLoginNetworkHandler(this.server, this.connection));
                break;
            }
            case STATUS: {
                ServerMetadata lv2 = this.server.getServerMetadata();
                if (this.server.acceptsStatusQuery() && lv2 != null) {
                    this.connection.setState(NetworkState.STATUS);
                    this.connection.setPacketListener(new ServerQueryNetworkHandler(lv2, this.connection));
                    break;
                }
                this.connection.disconnect(IGNORING_STATUS_REQUEST_MESSAGE);
                break;
            }
            default: {
                throw new UnsupportedOperationException("Invalid intention " + packet.getIntendedState());
            }
        }
    }

    @Override
    public void onDisconnected(Text reason) {
    }

    @Override
    public boolean isConnectionOpen() {
        return this.connection.isOpen();
    }
}

