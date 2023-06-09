/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.structure.processor;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.structure.rule.AlwaysTruePosRuleTest;
import net.minecraft.structure.rule.PosRuleTest;
import net.minecraft.structure.rule.RuleTest;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

public class StructureProcessorRule {
    public static final Codec<StructureProcessorRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)RuleTest.TYPE_CODEC.fieldOf("input_predicate")).forGetter(rule -> rule.inputPredicate), ((MapCodec)RuleTest.TYPE_CODEC.fieldOf("location_predicate")).forGetter(rule -> rule.locationPredicate), PosRuleTest.BASE_CODEC.optionalFieldOf("position_predicate", AlwaysTruePosRuleTest.INSTANCE).forGetter(rule -> rule.positionPredicate), ((MapCodec)BlockState.CODEC.fieldOf("output_state")).forGetter(rule -> rule.outputState), NbtCompound.CODEC.optionalFieldOf("output_nbt").forGetter(rule -> Optional.ofNullable(rule.outputNbt))).apply((Applicative<StructureProcessorRule, ?>)instance, StructureProcessorRule::new));
    private final RuleTest inputPredicate;
    private final RuleTest locationPredicate;
    private final PosRuleTest positionPredicate;
    private final BlockState outputState;
    @Nullable
    private final NbtCompound outputNbt;

    public StructureProcessorRule(RuleTest inputPredicate, RuleTest locationPredicate, BlockState state) {
        this(inputPredicate, locationPredicate, AlwaysTruePosRuleTest.INSTANCE, state, Optional.empty());
    }

    public StructureProcessorRule(RuleTest inputPredicate, RuleTest locationPredicate, PosRuleTest positionPredicate, BlockState state) {
        this(inputPredicate, locationPredicate, positionPredicate, state, Optional.empty());
    }

    public StructureProcessorRule(RuleTest inputPredicate, RuleTest locationPredicate, PosRuleTest positionPredicate, BlockState outputState, Optional<NbtCompound> nbt) {
        this.inputPredicate = inputPredicate;
        this.locationPredicate = locationPredicate;
        this.positionPredicate = positionPredicate;
        this.outputState = outputState;
        this.outputNbt = nbt.orElse(null);
    }

    public boolean test(BlockState input, BlockState currentState, BlockPos originalPos, BlockPos currentPos, BlockPos pivot, Random random) {
        return this.inputPredicate.test(input, random) && this.locationPredicate.test(currentState, random) && this.positionPredicate.test(originalPos, currentPos, pivot, random);
    }

    public BlockState getOutputState() {
        return this.outputState;
    }

    @Nullable
    public NbtCompound getOutputNbt() {
        return this.outputNbt;
    }
}

