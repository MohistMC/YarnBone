/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DeadCoralWallFanBlock;
import net.minecraft.block.Fertilizable;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.Nullable;

public class BoneMealItem
extends Item {
    public static final int field_30851 = 3;
    public static final int field_30852 = 1;
    public static final int field_30853 = 3;

    public BoneMealItem(Item.Settings arg) {
        super(arg);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World lv = context.getWorld();
        BlockPos lv2 = context.getBlockPos();
        BlockPos lv3 = lv2.offset(context.getSide());
        if (BoneMealItem.useOnFertilizable(context.getStack(), lv, lv2)) {
            if (!lv.isClient) {
                lv.syncWorldEvent(WorldEvents.BONE_MEAL_USED, lv2, 0);
            }
            return ActionResult.success(lv.isClient);
        }
        BlockState lv4 = lv.getBlockState(lv2);
        boolean bl = lv4.isSideSolidFullSquare(lv, lv2, context.getSide());
        if (bl && BoneMealItem.useOnGround(context.getStack(), lv, lv3, context.getSide())) {
            if (!lv.isClient) {
                lv.syncWorldEvent(WorldEvents.BONE_MEAL_USED, lv3, 0);
            }
            return ActionResult.success(lv.isClient);
        }
        return ActionResult.PASS;
    }

    public static boolean useOnFertilizable(ItemStack stack, World world, BlockPos pos) {
        Fertilizable lv2;
        BlockState lv = world.getBlockState(pos);
        if (lv.getBlock() instanceof Fertilizable && (lv2 = (Fertilizable)((Object)lv.getBlock())).isFertilizable(world, pos, lv, world.isClient)) {
            if (world instanceof ServerWorld) {
                if (lv2.canGrow(world, world.random, pos, lv)) {
                    lv2.grow((ServerWorld)world, world.random, pos, lv);
                }
                stack.decrement(1);
            }
            return true;
        }
        return false;
    }

    public static boolean useOnGround(ItemStack stack, World world, BlockPos blockPos, @Nullable Direction facing) {
        if (!world.getBlockState(blockPos).isOf(Blocks.WATER) || world.getFluidState(blockPos).getLevel() != 8) {
            return false;
        }
        if (!(world instanceof ServerWorld)) {
            return true;
        }
        Random lv = world.getRandom();
        block0: for (int i = 0; i < 128; ++i) {
            BlockPos lv2 = blockPos;
            BlockState lv3 = Blocks.SEAGRASS.getDefaultState();
            for (int j = 0; j < i / 16; ++j) {
                if (world.getBlockState(lv2 = lv2.add(lv.nextInt(3) - 1, (lv.nextInt(3) - 1) * lv.nextInt(3) / 2, lv.nextInt(3) - 1)).isFullCube(world, lv2)) continue block0;
            }
            RegistryEntry<Biome> lv4 = world.getBiome(lv2);
            if (lv4.isIn(BiomeTags.PRODUCES_CORALS_FROM_BONEMEAL)) {
                if (i == 0 && facing != null && facing.getAxis().isHorizontal()) {
                    lv3 = Registries.BLOCK.getEntryList(BlockTags.WALL_CORALS).flatMap(blocks -> blocks.getRandom(arg.random)).map(blockEntry -> ((Block)blockEntry.value()).getDefaultState()).orElse(lv3);
                    if (lv3.contains(DeadCoralWallFanBlock.FACING)) {
                        lv3 = (BlockState)lv3.with(DeadCoralWallFanBlock.FACING, facing);
                    }
                } else if (lv.nextInt(4) == 0) {
                    lv3 = Registries.BLOCK.getEntryList(BlockTags.UNDERWATER_BONEMEALS).flatMap(blocks -> blocks.getRandom(arg.random)).map(blockEntry -> ((Block)blockEntry.value()).getDefaultState()).orElse(lv3);
                }
            }
            if (lv3.isIn(BlockTags.WALL_CORALS, state -> state.contains(DeadCoralWallFanBlock.FACING))) {
                for (int k = 0; !lv3.canPlaceAt(world, lv2) && k < 4; ++k) {
                    lv3 = (BlockState)lv3.with(DeadCoralWallFanBlock.FACING, Direction.Type.HORIZONTAL.random(lv));
                }
            }
            if (!lv3.canPlaceAt(world, lv2)) continue;
            BlockState lv5 = world.getBlockState(lv2);
            if (lv5.isOf(Blocks.WATER) && world.getFluidState(lv2).getLevel() == 8) {
                world.setBlockState(lv2, lv3, Block.NOTIFY_ALL);
                continue;
            }
            if (!lv5.isOf(Blocks.SEAGRASS) || lv.nextInt(10) != 0) continue;
            ((Fertilizable)((Object)Blocks.SEAGRASS)).grow((ServerWorld)world, lv, lv2, lv5);
        }
        stack.decrement(1);
        return true;
    }

    public static void createParticles(WorldAccess world, BlockPos pos, int count) {
        double e;
        BlockState lv;
        if (count == 0) {
            count = 15;
        }
        if ((lv = world.getBlockState(pos)).isAir()) {
            return;
        }
        double d = 0.5;
        if (lv.isOf(Blocks.WATER)) {
            count *= 3;
            e = 1.0;
            d = 3.0;
        } else if (lv.isOpaqueFullCube(world, pos)) {
            pos = pos.up();
            count *= 3;
            d = 3.0;
            e = 1.0;
        } else {
            e = lv.getOutlineShape(world, pos).getMax(Direction.Axis.Y);
        }
        world.addParticle(ParticleTypes.HAPPY_VILLAGER, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, 0.0, 0.0, 0.0);
        Random lv2 = world.getRandom();
        for (int j = 0; j < count; ++j) {
            double n;
            double m;
            double f = lv2.nextGaussian() * 0.02;
            double g = lv2.nextGaussian() * 0.02;
            double h = lv2.nextGaussian() * 0.02;
            double k = 0.5 - d;
            double l = (double)pos.getX() + k + lv2.nextDouble() * d * 2.0;
            if (world.getBlockState(BlockPos.ofFloored(l, m = (double)pos.getY() + lv2.nextDouble() * e, n = (double)pos.getZ() + k + lv2.nextDouble() * d * 2.0).down()).isAir()) continue;
            world.addParticle(ParticleTypes.HAPPY_VILLAGER, l, m, n, f, g, h);
        }
    }
}

