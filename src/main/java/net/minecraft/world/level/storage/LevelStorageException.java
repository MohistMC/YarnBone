/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.world.level.storage;

import net.minecraft.text.Text;

public class LevelStorageException
extends RuntimeException {
    private final Text messageText;

    public LevelStorageException(Text messageText) {
        super(messageText.getString());
        this.messageText = messageText;
    }

    public Text getMessageText() {
        return this.messageText;
    }
}

