/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.block.entity;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BlockState;
import net.minecraft.block.SculkCatalystBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.SculkSpreadManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.BlockPositionSource;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.PositionSource;
import net.minecraft.world.event.listener.GameEventListener;

public class SculkCatalystBlockEntity
extends BlockEntity
implements GameEventListener {
    private final BlockPositionSource positionSource;
    private final SculkSpreadManager spreadManager;

    public SculkCatalystBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityType.SCULK_CATALYST, pos, state);
        this.positionSource = new BlockPositionSource(this.pos);
        this.spreadManager = SculkSpreadManager.create();
    }

    @Override
    public PositionSource getPositionSource() {
        return this.positionSource;
    }

    @Override
    public int getRange() {
        return 8;
    }

    @Override
    public GameEventListener.TriggerOrder getTriggerOrder() {
        return GameEventListener.TriggerOrder.BY_DISTANCE;
    }

    @Override
    public boolean listen(ServerWorld world, GameEvent event, GameEvent.Emitter emitter, Vec3d emitterPos) {
        Entity entity;
        if (event == GameEvent.ENTITY_DIE && (entity = emitter.sourceEntity()) instanceof LivingEntity) {
            LivingEntity lv = (LivingEntity)entity;
            if (!lv.isExperienceDroppingDisabled()) {
                int i = lv.getXpToDrop();
                if (lv.shouldDropXp() && i > 0) {
                    this.spreadManager.spread(BlockPos.ofFloored(emitterPos.offset(Direction.UP, 0.5)), i);
                    this.triggerCriteria(lv);
                }
                lv.disableExperienceDropping();
                SculkCatalystBlock.bloom(world, this.pos, this.getCachedState(), world.getRandom());
            }
            return true;
        }
        return false;
    }

    private void triggerCriteria(LivingEntity deadEntity) {
        LivingEntity lv = deadEntity.getAttacker();
        if (lv instanceof ServerPlayerEntity) {
            ServerPlayerEntity lv2 = (ServerPlayerEntity)lv;
            DamageSource lv3 = deadEntity.getRecentDamageSource() == null ? this.world.getDamageSources().playerAttack(lv2) : deadEntity.getRecentDamageSource();
            Criteria.KILL_MOB_NEAR_SCULK_CATALYST.trigger(lv2, deadEntity, lv3);
        }
    }

    public static void tick(World world, BlockPos pos, BlockState state, SculkCatalystBlockEntity blockEntity) {
        blockEntity.spreadManager.tick(world, pos, world.getRandom(), true);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.spreadManager.readNbt(nbt);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        this.spreadManager.writeNbt(nbt);
        super.writeNbt(nbt);
    }

    @VisibleForTesting
    public SculkSpreadManager getSpreadManager() {
        return this.spreadManager;
    }

    private static /* synthetic */ Integer method_41518(SculkSpreadManager.Cursor arg) {
        return 1;
    }
}

