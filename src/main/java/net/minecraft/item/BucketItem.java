/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.item;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidDrainable;
import net.minecraft.block.FluidFillable;
import net.minecraft.block.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.FluidModificationItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class BucketItem
extends Item
implements FluidModificationItem {
    private final Fluid fluid;

    public BucketItem(Fluid fluid, Item.Settings settings) {
        super(settings);
        this.fluid = fluid;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack lv = user.getStackInHand(hand);
        BlockHitResult lv2 = BucketItem.raycast(world, user, this.fluid == Fluids.EMPTY ? RaycastContext.FluidHandling.SOURCE_ONLY : RaycastContext.FluidHandling.NONE);
        if (lv2.getType() == HitResult.Type.MISS) {
            return TypedActionResult.pass(lv);
        }
        if (lv2.getType() == HitResult.Type.BLOCK) {
            BlockPos lv10;
            BlockPos lv3 = lv2.getBlockPos();
            Direction lv4 = lv2.getSide();
            BlockPos lv5 = lv3.offset(lv4);
            if (!world.canPlayerModifyAt(user, lv3) || !user.canPlaceOn(lv5, lv4, lv)) {
                return TypedActionResult.fail(lv);
            }
            if (this.fluid == Fluids.EMPTY) {
                FluidDrainable lv7;
                ItemStack lv8;
                BlockState lv6 = world.getBlockState(lv3);
                if (lv6.getBlock() instanceof FluidDrainable && !(lv8 = (lv7 = (FluidDrainable)((Object)lv6.getBlock())).tryDrainFluid(world, lv3, lv6)).isEmpty()) {
                    user.incrementStat(Stats.USED.getOrCreateStat(this));
                    lv7.getBucketFillSound().ifPresent(sound -> user.playSound((SoundEvent)sound, 1.0f, 1.0f));
                    world.emitGameEvent((Entity)user, GameEvent.FLUID_PICKUP, lv3);
                    ItemStack lv9 = ItemUsage.exchangeStack(lv, user, lv8);
                    if (!world.isClient) {
                        Criteria.FILLED_BUCKET.trigger((ServerPlayerEntity)user, lv8);
                    }
                    return TypedActionResult.success(lv9, world.isClient());
                }
                return TypedActionResult.fail(lv);
            }
            BlockState lv6 = world.getBlockState(lv3);
            BlockPos blockPos = lv10 = lv6.getBlock() instanceof FluidFillable && this.fluid == Fluids.WATER ? lv3 : lv5;
            if (this.placeFluid(user, world, lv10, lv2)) {
                this.onEmptied(user, world, lv, lv10);
                if (user instanceof ServerPlayerEntity) {
                    Criteria.PLACED_BLOCK.trigger((ServerPlayerEntity)user, lv10, lv);
                }
                user.incrementStat(Stats.USED.getOrCreateStat(this));
                return TypedActionResult.success(BucketItem.getEmptiedStack(lv, user), world.isClient());
            }
            return TypedActionResult.fail(lv);
        }
        return TypedActionResult.pass(lv);
    }

    public static ItemStack getEmptiedStack(ItemStack stack, PlayerEntity player) {
        if (!player.getAbilities().creativeMode) {
            return new ItemStack(Items.BUCKET);
        }
        return stack;
    }

    @Override
    public void onEmptied(@Nullable PlayerEntity player, World world, ItemStack stack, BlockPos pos) {
    }

    @Override
    public boolean placeFluid(@Nullable PlayerEntity player, World world, BlockPos pos, @Nullable BlockHitResult hitResult) {
        boolean bl2;
        if (!(this.fluid instanceof FlowableFluid)) {
            return false;
        }
        BlockState lv = world.getBlockState(pos);
        Block lv2 = lv.getBlock();
        Material lv3 = lv.getMaterial();
        boolean bl = lv.canBucketPlace(this.fluid);
        boolean bl3 = bl2 = lv.isAir() || bl || lv2 instanceof FluidFillable && ((FluidFillable)((Object)lv2)).canFillWithFluid(world, pos, lv, this.fluid);
        if (!bl2) {
            return hitResult != null && this.placeFluid(player, world, hitResult.getBlockPos().offset(hitResult.getSide()), null);
        }
        if (world.getDimension().ultrawarm() && this.fluid.isIn(FluidTags.WATER)) {
            int i = pos.getX();
            int j = pos.getY();
            int k = pos.getZ();
            world.playSound(player, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5f, 2.6f + (world.random.nextFloat() - world.random.nextFloat()) * 0.8f);
            for (int l = 0; l < 8; ++l) {
                world.addParticle(ParticleTypes.LARGE_SMOKE, (double)i + Math.random(), (double)j + Math.random(), (double)k + Math.random(), 0.0, 0.0, 0.0);
            }
            return true;
        }
        if (lv2 instanceof FluidFillable && this.fluid == Fluids.WATER) {
            ((FluidFillable)((Object)lv2)).tryFillWithFluid(world, pos, lv, ((FlowableFluid)this.fluid).getStill(false));
            this.playEmptyingSound(player, world, pos);
            return true;
        }
        if (!world.isClient && bl && !lv3.isLiquid()) {
            world.breakBlock(pos, true);
        }
        if (world.setBlockState(pos, this.fluid.getDefaultState().getBlockState(), Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD) || lv.getFluidState().isStill()) {
            this.playEmptyingSound(player, world, pos);
            return true;
        }
        return false;
    }

    protected void playEmptyingSound(@Nullable PlayerEntity player, WorldAccess world, BlockPos pos) {
        SoundEvent lv = this.fluid.isIn(FluidTags.LAVA) ? SoundEvents.ITEM_BUCKET_EMPTY_LAVA : SoundEvents.ITEM_BUCKET_EMPTY;
        world.playSound(player, pos, lv, SoundCategory.BLOCKS, 1.0f, 1.0f);
        world.emitGameEvent((Entity)player, GameEvent.FLUID_PLACE, pos);
    }
}

