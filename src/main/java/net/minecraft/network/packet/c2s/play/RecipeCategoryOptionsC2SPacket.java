/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.network.packet.c2s.play;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.recipe.book.RecipeBookCategory;

public class RecipeCategoryOptionsC2SPacket
implements Packet<ServerPlayPacketListener> {
    private final RecipeBookCategory category;
    private final boolean guiOpen;
    private final boolean filteringCraftable;

    public RecipeCategoryOptionsC2SPacket(RecipeBookCategory category, boolean guiOpen, boolean filteringCraftable) {
        this.category = category;
        this.guiOpen = guiOpen;
        this.filteringCraftable = filteringCraftable;
    }

    public RecipeCategoryOptionsC2SPacket(PacketByteBuf buf) {
        this.category = buf.readEnumConstant(RecipeBookCategory.class);
        this.guiOpen = buf.readBoolean();
        this.filteringCraftable = buf.readBoolean();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeEnumConstant(this.category);
        buf.writeBoolean(this.guiOpen);
        buf.writeBoolean(this.filteringCraftable);
    }

    @Override
    public void apply(ServerPlayPacketListener arg) {
        arg.onRecipeCategoryOptions(this);
    }

    public RecipeBookCategory getCategory() {
        return this.category;
    }

    public boolean isGuiOpen() {
        return this.guiOpen;
    }

    public boolean isFilteringCraftable() {
        return this.filteringCraftable;
    }
}

