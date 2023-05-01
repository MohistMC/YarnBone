/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.resource;

import net.minecraft.SharedConstants;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public enum ResourcePackCompatibility {
    TOO_OLD("old"),
    TOO_NEW("new"),
    COMPATIBLE("compatible");

    private final Text notification;
    private final Text confirmMessage;

    private ResourcePackCompatibility(String translationSuffix) {
        this.notification = Text.translatable("pack.incompatible." + translationSuffix).formatted(Formatting.GRAY);
        this.confirmMessage = Text.translatable("pack.incompatible.confirm." + translationSuffix);
    }

    public boolean isCompatible() {
        return this == COMPATIBLE;
    }

    public static ResourcePackCompatibility from(int packVersion, ResourceType type) {
        int j = SharedConstants.getGameVersion().getResourceVersion(type);
        if (packVersion < j) {
            return TOO_OLD;
        }
        if (packVersion > j) {
            return TOO_NEW;
        }
        return COMPATIBLE;
    }

    public Text getNotification() {
        return this.notification;
    }

    public Text getConfirmMessage() {
        return this.confirmMessage;
    }
}

