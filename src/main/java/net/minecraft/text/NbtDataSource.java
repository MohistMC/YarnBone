/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.text;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.stream.Stream;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.ServerCommandSource;

@FunctionalInterface
public interface NbtDataSource {
    public Stream<NbtCompound> get(ServerCommandSource var1) throws CommandSyntaxException;
}

