/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TallBlockItem
extends BlockItem {
    public TallBlockItem(Block arg, Item.Settings arg2) {
        super(arg, arg2);
    }

    @Override
    protected boolean place(ItemPlacementContext context, BlockState state) {
        BlockPos lv2;
        World lv = context.getWorld();
        BlockState lv3 = lv.isWater(lv2 = context.getBlockPos().up()) ? Blocks.WATER.getDefaultState() : Blocks.AIR.getDefaultState();
        lv.setBlockState(lv2, lv3, Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD | Block.FORCE_STATE);
        return super.place(context, state);
    }
}

