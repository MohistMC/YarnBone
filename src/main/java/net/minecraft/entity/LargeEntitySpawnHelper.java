/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.entity;

import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;

public class LargeEntitySpawnHelper {
    public static <T extends MobEntity> Optional<T> trySpawnAt(EntityType<T> entityType, SpawnReason reason, ServerWorld world, BlockPos pos, int tries, int horizontalRange, int verticalRange, Requirements requirements) {
        BlockPos.Mutable lv = pos.mutableCopy();
        for (int l = 0; l < tries; ++l) {
            MobEntity lv2;
            int m = MathHelper.nextBetween(world.random, -horizontalRange, horizontalRange);
            int n = MathHelper.nextBetween(world.random, -horizontalRange, horizontalRange);
            lv.set(pos, m, verticalRange, n);
            if (!world.getWorldBorder().contains(lv) || !LargeEntitySpawnHelper.findSpawnPos(world, verticalRange, lv, requirements) || (lv2 = (MobEntity)entityType.create(world, null, null, lv, reason, false, false)) == null) continue;
            if (lv2.canSpawn(world, reason) && lv2.canSpawn(world)) {
                world.spawnEntityAndPassengers(lv2);
                return Optional.of(lv2);
            }
            lv2.discard();
        }
        return Optional.empty();
    }

    private static boolean findSpawnPos(ServerWorld world, int verticalRange, BlockPos.Mutable pos, Requirements requirements) {
        BlockPos.Mutable lv = new BlockPos.Mutable().set(pos);
        BlockState lv2 = world.getBlockState(lv);
        for (int j = verticalRange; j >= -verticalRange; --j) {
            pos.move(Direction.DOWN);
            lv.set((Vec3i)pos, Direction.UP);
            BlockState lv3 = world.getBlockState(pos);
            if (requirements.canSpawnOn(world, pos, lv3, lv, lv2)) {
                pos.move(Direction.UP);
                return true;
            }
            lv2 = lv3;
        }
        return false;
    }

    public static interface Requirements {
        public static final Requirements IRON_GOLEM = (world, pos, state, abovePos, aboveState) -> (aboveState.isAir() || aboveState.getMaterial().isLiquid()) && state.getMaterial().blocksLight();
        public static final Requirements WARDEN = (world, pos, state, abovePos, aboveState) -> aboveState.getCollisionShape(world, abovePos).isEmpty() && Block.isFaceFullSquare(state.getCollisionShape(world, pos), Direction.UP);

        public boolean canSpawnOn(ServerWorld var1, BlockPos var2, BlockState var3, BlockPos var4, BlockState var5);
    }
}

