/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.network.packet.c2s.login;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import javax.crypto.SecretKey;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.encryption.NetworkEncryptionException;
import net.minecraft.network.encryption.NetworkEncryptionUtils;
import net.minecraft.network.listener.ServerLoginPacketListener;
import net.minecraft.network.packet.Packet;

public class LoginKeyC2SPacket
implements Packet<ServerLoginPacketListener> {
    private final byte[] encryptedSecretKey;
    private final byte[] nonce;

    public LoginKeyC2SPacket(SecretKey secretKey, PublicKey publicKey, byte[] nonce) throws NetworkEncryptionException {
        this.encryptedSecretKey = NetworkEncryptionUtils.encrypt(publicKey, secretKey.getEncoded());
        this.nonce = NetworkEncryptionUtils.encrypt(publicKey, nonce);
    }

    public LoginKeyC2SPacket(PacketByteBuf buf) {
        this.encryptedSecretKey = buf.readByteArray();
        this.nonce = buf.readByteArray();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeByteArray(this.encryptedSecretKey);
        buf.writeByteArray(this.nonce);
    }

    @Override
    public void apply(ServerLoginPacketListener arg) {
        arg.onKey(this);
    }

    public SecretKey decryptSecretKey(PrivateKey privateKey) throws NetworkEncryptionException {
        return NetworkEncryptionUtils.decryptSecretKey(privateKey, this.encryptedSecretKey);
    }

    public boolean verifySignedNonce(byte[] nonce, PrivateKey privateKey) {
        try {
            return Arrays.equals(nonce, NetworkEncryptionUtils.decrypt(privateKey, this.nonce));
        }
        catch (NetworkEncryptionException lv) {
            return false;
        }
    }
}

