/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.network.message;

import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public interface SentMessage {
    public Text getContent();

    public void send(ServerPlayerEntity var1, boolean var2, MessageType.Parameters var3);

    public static SentMessage of(SignedMessage message) {
        if (message.isSenderMissing()) {
            return new Profileless(message.getContent());
        }
        return new Chat(message);
    }

    public record Profileless(Text getContent) implements SentMessage
    {
        @Override
        public void send(ServerPlayerEntity sender, boolean filterMaskEnabled, MessageType.Parameters params) {
            sender.networkHandler.sendProfilelessChatMessage(this.getContent, params);
        }
    }

    public record Chat(SignedMessage message) implements SentMessage
    {
        @Override
        public Text getContent() {
            return this.message.getContent();
        }

        @Override
        public void send(ServerPlayerEntity sender, boolean filterMaskEnabled, MessageType.Parameters params) {
            SignedMessage lv = this.message.withFilterMaskEnabled(filterMaskEnabled);
            if (!lv.isFullyFiltered()) {
                sender.networkHandler.sendChatMessage(lv, params);
            }
        }
    }
}

