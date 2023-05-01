/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.item;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.AbstractPlantStemBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class ShearsItem
extends Item {
    public ShearsItem(Item.Settings arg) {
        super(arg);
    }

    @Override
    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
        if (!world.isClient && !state.isIn(BlockTags.FIRE)) {
            stack.damage(1, miner, e -> e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
        }
        if (state.isIn(BlockTags.LEAVES) || state.isOf(Blocks.COBWEB) || state.isOf(Blocks.GRASS) || state.isOf(Blocks.FERN) || state.isOf(Blocks.DEAD_BUSH) || state.isOf(Blocks.HANGING_ROOTS) || state.isOf(Blocks.VINE) || state.isOf(Blocks.TRIPWIRE) || state.isIn(BlockTags.WOOL)) {
            return true;
        }
        return super.postMine(stack, world, state, pos, miner);
    }

    @Override
    public boolean isSuitableFor(BlockState state) {
        return state.isOf(Blocks.COBWEB) || state.isOf(Blocks.REDSTONE_WIRE) || state.isOf(Blocks.TRIPWIRE);
    }

    @Override
    public float getMiningSpeedMultiplier(ItemStack stack, BlockState state) {
        if (state.isOf(Blocks.COBWEB) || state.isIn(BlockTags.LEAVES)) {
            return 15.0f;
        }
        if (state.isIn(BlockTags.WOOL)) {
            return 5.0f;
        }
        if (state.isOf(Blocks.VINE) || state.isOf(Blocks.GLOW_LICHEN)) {
            return 2.0f;
        }
        return super.getMiningSpeedMultiplier(stack, state);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        AbstractPlantStemBlock lv5;
        BlockPos lv2;
        World lv = context.getWorld();
        BlockState lv3 = lv.getBlockState(lv2 = context.getBlockPos());
        Block lv4 = lv3.getBlock();
        if (lv4 instanceof AbstractPlantStemBlock && !(lv5 = (AbstractPlantStemBlock)lv4).hasMaxAge(lv3)) {
            PlayerEntity lv6 = context.getPlayer();
            ItemStack lv7 = context.getStack();
            if (lv6 instanceof ServerPlayerEntity) {
                Criteria.ITEM_USED_ON_BLOCK.trigger((ServerPlayerEntity)lv6, lv2, lv7);
            }
            lv.playSound(lv6, lv2, SoundEvents.BLOCK_GROWING_PLANT_CROP, SoundCategory.BLOCKS, 1.0f, 1.0f);
            BlockState lv8 = lv5.withMaxAge(lv3);
            lv.setBlockState(lv2, lv8);
            lv.emitGameEvent(GameEvent.BLOCK_CHANGE, lv2, GameEvent.Emitter.of(context.getPlayer(), lv8));
            if (lv6 != null) {
                lv7.damage(1, lv6, player -> player.sendToolBreakStatus(context.getHand()));
            }
            return ActionResult.success(lv.isClient);
        }
        return super.useOnBlock(context);
    }
}

