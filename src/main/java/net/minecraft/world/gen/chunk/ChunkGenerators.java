/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.world.gen.chunk;

import com.mojang.serialization.Codec;
import net.minecraft.registry.Registry;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.DebugChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGenerator;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;

public class ChunkGenerators {
    public static Codec<? extends ChunkGenerator> registerAndGetDefault(Registry<Codec<? extends ChunkGenerator>> registry) {
        Registry.register(registry, "noise", NoiseChunkGenerator.CODEC);
        Registry.register(registry, "flat", FlatChunkGenerator.CODEC);
        return Registry.register(registry, "debug", DebugChunkGenerator.CODEC);
    }
}

