/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.block.entity.DropperBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPointerImpl;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldEvents;

public class DropperBlock
extends DispenserBlock {
    private static final DispenserBehavior BEHAVIOR = new ItemDispenserBehavior();

    public DropperBlock(AbstractBlock.Settings arg) {
        super(arg);
    }

    @Override
    protected DispenserBehavior getBehaviorForItem(ItemStack stack) {
        return BEHAVIOR;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new DropperBlockEntity(pos, state);
    }

    @Override
    protected void dispense(ServerWorld world, BlockPos pos) {
        ItemStack lv6;
        BlockPointerImpl lv = new BlockPointerImpl(world, pos);
        DispenserBlockEntity lv2 = (DispenserBlockEntity)lv.getBlockEntity();
        int i = lv2.chooseNonEmptySlot(world.random);
        if (i < 0) {
            world.syncWorldEvent(WorldEvents.DISPENSER_FAILS, pos, 0);
            return;
        }
        ItemStack lv3 = lv2.getStack(i);
        if (lv3.isEmpty()) {
            return;
        }
        Direction lv4 = world.getBlockState(pos).get(FACING);
        Inventory lv5 = HopperBlockEntity.getInventoryAt(world, pos.offset(lv4));
        if (lv5 == null) {
            lv6 = BEHAVIOR.dispense(lv, lv3);
        } else {
            lv6 = HopperBlockEntity.transfer(lv2, lv5, lv3.copy().split(1), lv4.getOpposite());
            if (lv6.isEmpty()) {
                lv6 = lv3.copy();
                lv6.decrement(1);
            } else {
                lv6 = lv3.copy();
            }
        }
        lv2.setStack(i, lv6);
    }
}

