/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.world.gen.structure;

import com.mojang.serialization.Codec;
import net.minecraft.structure.JungleTempleGenerator;
import net.minecraft.world.gen.structure.BasicTempleStructure;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

public class JungleTempleStructure
extends BasicTempleStructure {
    public static final Codec<JungleTempleStructure> CODEC = JungleTempleStructure.createCodec(JungleTempleStructure::new);

    public JungleTempleStructure(Structure.Config arg) {
        super(JungleTempleGenerator::new, 12, 15, arg);
    }

    @Override
    public StructureType<?> getType() {
        return StructureType.JUNGLE_TEMPLE;
    }
}

