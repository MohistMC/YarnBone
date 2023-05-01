/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.world.gen.blockpredicate;

import com.mojang.serialization.Codec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.blockpredicate.BlockPredicateType;

class AlwaysTrueBlockPredicate
implements BlockPredicate {
    public static AlwaysTrueBlockPredicate instance = new AlwaysTrueBlockPredicate();
    public static final Codec<AlwaysTrueBlockPredicate> CODEC = Codec.unit(() -> instance);

    private AlwaysTrueBlockPredicate() {
    }

    @Override
    public boolean test(StructureWorldAccess arg, BlockPos arg2) {
        return true;
    }

    @Override
    public BlockPredicateType<?> getType() {
        return BlockPredicateType.TRUE;
    }

    @Override
    public /* synthetic */ boolean test(Object world, Object pos) {
        return this.test((StructureWorldAccess)world, (BlockPos)pos);
    }
}

