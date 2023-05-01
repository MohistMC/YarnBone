/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.item;

import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.CandleBlock;
import net.minecraft.block.CandleCakeBlock;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class FireChargeItem
extends Item {
    public FireChargeItem(Item.Settings arg) {
        super(arg);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World lv = context.getWorld();
        BlockPos lv2 = context.getBlockPos();
        BlockState lv3 = lv.getBlockState(lv2);
        boolean bl = false;
        if (CampfireBlock.canBeLit(lv3) || CandleBlock.canBeLit(lv3) || CandleCakeBlock.canBeLit(lv3)) {
            this.playUseSound(lv, lv2);
            lv.setBlockState(lv2, (BlockState)lv3.with(Properties.LIT, true));
            lv.emitGameEvent((Entity)context.getPlayer(), GameEvent.BLOCK_CHANGE, lv2);
            bl = true;
        } else if (AbstractFireBlock.canPlaceAt(lv, lv2 = lv2.offset(context.getSide()), context.getHorizontalPlayerFacing())) {
            this.playUseSound(lv, lv2);
            lv.setBlockState(lv2, AbstractFireBlock.getState(lv, lv2));
            lv.emitGameEvent((Entity)context.getPlayer(), GameEvent.BLOCK_PLACE, lv2);
            bl = true;
        }
        if (bl) {
            context.getStack().decrement(1);
            return ActionResult.success(lv.isClient);
        }
        return ActionResult.FAIL;
    }

    private void playUseSound(World world, BlockPos pos) {
        Random lv = world.getRandom();
        world.playSound(null, pos, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 1.0f, (lv.nextFloat() - lv.nextFloat()) * 0.2f + 1.0f);
    }
}

