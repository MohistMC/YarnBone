/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.server.command;

import net.minecraft.text.Text;

public interface CommandOutput {
    public static final CommandOutput DUMMY = new CommandOutput(){

        @Override
        public void sendMessage(Text message) {
        }

        @Override
        public boolean shouldReceiveFeedback() {
            return false;
        }

        @Override
        public boolean shouldTrackOutput() {
            return false;
        }

        @Override
        public boolean shouldBroadcastConsoleToOps() {
            return false;
        }
    };

    public void sendMessage(Text var1);

    public boolean shouldReceiveFeedback();

    public boolean shouldTrackOutput();

    public boolean shouldBroadcastConsoleToOps();

    default public boolean cannotBeSilenced() {
        return false;
    }
}

