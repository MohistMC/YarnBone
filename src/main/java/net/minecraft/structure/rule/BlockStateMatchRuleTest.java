/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.structure.rule;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockState;
import net.minecraft.structure.rule.RuleTest;
import net.minecraft.structure.rule.RuleTestType;
import net.minecraft.util.math.random.Random;

public class BlockStateMatchRuleTest
extends RuleTest {
    public static final Codec<BlockStateMatchRuleTest> CODEC = ((MapCodec)BlockState.CODEC.fieldOf("block_state")).xmap(BlockStateMatchRuleTest::new, ruleTest -> ruleTest.blockState).codec();
    private final BlockState blockState;

    public BlockStateMatchRuleTest(BlockState blockState) {
        this.blockState = blockState;
    }

    @Override
    public boolean test(BlockState state, Random random) {
        return state == this.blockState;
    }

    @Override
    protected RuleTestType<?> getType() {
        return RuleTestType.BLOCKSTATE_MATCH;
    }
}

