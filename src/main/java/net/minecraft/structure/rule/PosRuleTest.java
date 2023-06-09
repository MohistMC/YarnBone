/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.structure.rule;

import com.mojang.serialization.Codec;
import net.minecraft.registry.Registries;
import net.minecraft.structure.rule.PosRuleTestType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public abstract class PosRuleTest {
    public static final Codec<PosRuleTest> BASE_CODEC = Registries.POS_RULE_TEST.getCodec().dispatch("predicate_type", PosRuleTest::getType, PosRuleTestType::codec);

    public abstract boolean test(BlockPos var1, BlockPos var2, BlockPos var3, Random var4);

    protected abstract PosRuleTestType<?> getType();
}

