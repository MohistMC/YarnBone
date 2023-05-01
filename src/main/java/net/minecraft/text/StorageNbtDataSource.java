/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.text;

import java.util.stream.Stream;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.NbtDataSource;
import net.minecraft.util.Identifier;

public record StorageNbtDataSource(Identifier id) implements NbtDataSource
{
    @Override
    public Stream<NbtCompound> get(ServerCommandSource source) {
        NbtCompound lv = source.getServer().getDataCommandStorage().get(this.id);
        return Stream.of(lv);
    }

    @Override
    public String toString() {
        return "storage=" + this.id;
    }
}

