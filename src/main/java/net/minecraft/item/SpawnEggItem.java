/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.item;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class SpawnEggItem
extends Item {
    private static final Map<EntityType<? extends MobEntity>, SpawnEggItem> SPAWN_EGGS = Maps.newIdentityHashMap();
    private final int primaryColor;
    private final int secondaryColor;
    private final EntityType<?> type;

    public SpawnEggItem(EntityType<? extends MobEntity> type, int primaryColor, int secondaryColor, Item.Settings settings) {
        super(settings);
        this.type = type;
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
        SPAWN_EGGS.put(type, this);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        BlockEntity lv6;
        World lv = context.getWorld();
        if (!(lv instanceof ServerWorld)) {
            return ActionResult.SUCCESS;
        }
        ItemStack lv2 = context.getStack();
        BlockPos lv3 = context.getBlockPos();
        Direction lv4 = context.getSide();
        BlockState lv5 = lv.getBlockState(lv3);
        if (lv5.isOf(Blocks.SPAWNER) && (lv6 = lv.getBlockEntity(lv3)) instanceof MobSpawnerBlockEntity) {
            MobSpawnerBlockEntity lv7 = (MobSpawnerBlockEntity)lv6;
            EntityType<?> lv8 = this.getEntityType(lv2.getNbt());
            lv7.setEntityType(lv8, lv.getRandom());
            lv6.markDirty();
            lv.updateListeners(lv3, lv5, lv5, Block.NOTIFY_ALL);
            lv.emitGameEvent((Entity)context.getPlayer(), GameEvent.BLOCK_CHANGE, lv3);
            lv2.decrement(1);
            return ActionResult.CONSUME;
        }
        BlockPos lv9 = lv5.getCollisionShape(lv, lv3).isEmpty() ? lv3 : lv3.offset(lv4);
        EntityType<?> lv10 = this.getEntityType(lv2.getNbt());
        if (lv10.spawnFromItemStack((ServerWorld)lv, lv2, context.getPlayer(), lv9, SpawnReason.SPAWN_EGG, true, !Objects.equals(lv3, lv9) && lv4 == Direction.UP) != null) {
            lv2.decrement(1);
            lv.emitGameEvent((Entity)context.getPlayer(), GameEvent.ENTITY_PLACE, lv3);
        }
        return ActionResult.CONSUME;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack lv = user.getStackInHand(hand);
        BlockHitResult lv2 = SpawnEggItem.raycast(world, user, RaycastContext.FluidHandling.SOURCE_ONLY);
        if (((HitResult)lv2).getType() != HitResult.Type.BLOCK) {
            return TypedActionResult.pass(lv);
        }
        if (!(world instanceof ServerWorld)) {
            return TypedActionResult.success(lv);
        }
        BlockHitResult lv3 = lv2;
        BlockPos lv4 = lv3.getBlockPos();
        if (!(world.getBlockState(lv4).getBlock() instanceof FluidBlock)) {
            return TypedActionResult.pass(lv);
        }
        if (!world.canPlayerModifyAt(user, lv4) || !user.canPlaceOn(lv4, lv3.getSide(), lv)) {
            return TypedActionResult.fail(lv);
        }
        EntityType<?> lv5 = this.getEntityType(lv.getNbt());
        Object lv6 = lv5.spawnFromItemStack((ServerWorld)world, lv, user, lv4, SpawnReason.SPAWN_EGG, false, false);
        if (lv6 == null) {
            return TypedActionResult.pass(lv);
        }
        if (!user.getAbilities().creativeMode) {
            lv.decrement(1);
        }
        user.incrementStat(Stats.USED.getOrCreateStat(this));
        world.emitGameEvent((Entity)user, GameEvent.ENTITY_PLACE, ((Entity)lv6).getPos());
        return TypedActionResult.consume(lv);
    }

    public boolean isOfSameEntityType(@Nullable NbtCompound nbt, EntityType<?> type) {
        return Objects.equals(this.getEntityType(nbt), type);
    }

    public int getColor(int tintIndex) {
        return tintIndex == 0 ? this.primaryColor : this.secondaryColor;
    }

    @Nullable
    public static SpawnEggItem forEntity(@Nullable EntityType<?> type) {
        return SPAWN_EGGS.get(type);
    }

    public static Iterable<SpawnEggItem> getAll() {
        return Iterables.unmodifiableIterable(SPAWN_EGGS.values());
    }

    public EntityType<?> getEntityType(@Nullable NbtCompound nbt) {
        NbtCompound lv;
        if (nbt != null && nbt.contains("EntityTag", NbtElement.COMPOUND_TYPE) && (lv = nbt.getCompound("EntityTag")).contains("id", NbtElement.STRING_TYPE)) {
            return EntityType.get(lv.getString("id")).orElse(this.type);
        }
        return this.type;
    }

    @Override
    public FeatureSet getRequiredFeatures() {
        return this.type.getRequiredFeatures();
    }

    public Optional<MobEntity> spawnBaby(PlayerEntity user, MobEntity entity, EntityType<? extends MobEntity> entityType, ServerWorld world, Vec3d pos, ItemStack stack) {
        if (!this.isOfSameEntityType(stack.getNbt(), entityType)) {
            return Optional.empty();
        }
        MobEntity lv = entity instanceof PassiveEntity ? ((PassiveEntity)entity).createChild(world, (PassiveEntity)entity) : entityType.create(world);
        if (lv == null) {
            return Optional.empty();
        }
        lv.setBaby(true);
        if (!lv.isBaby()) {
            return Optional.empty();
        }
        lv.refreshPositionAndAngles(pos.getX(), pos.getY(), pos.getZ(), 0.0f, 0.0f);
        world.spawnEntityAndPassengers(lv);
        if (stack.hasCustomName()) {
            lv.setCustomName(stack.getName());
        }
        if (!user.getAbilities().creativeMode) {
            stack.decrement(1);
        }
        return Optional.of(lv);
    }
}

