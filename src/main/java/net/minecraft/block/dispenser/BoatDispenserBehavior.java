/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.block.dispenser;

import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.ChestBoatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldEvents;

public class BoatDispenserBehavior
extends ItemDispenserBehavior {
    private final ItemDispenserBehavior itemDispenser = new ItemDispenserBehavior();
    private final BoatEntity.Type boatType;
    private final boolean chest;

    public BoatDispenserBehavior(BoatEntity.Type type) {
        this(type, false);
    }

    public BoatDispenserBehavior(BoatEntity.Type boatType, boolean chest) {
        this.boatType = boatType;
        this.chest = chest;
    }

    @Override
    public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
        double g;
        Direction lv = pointer.getBlockState().get(DispenserBlock.FACING);
        ServerWorld lv2 = pointer.getWorld();
        double d = pointer.getX() + (double)((float)lv.getOffsetX() * 1.125f);
        double e = pointer.getY() + (double)((float)lv.getOffsetY() * 1.125f);
        double f = pointer.getZ() + (double)((float)lv.getOffsetZ() * 1.125f);
        BlockPos lv3 = pointer.getPos().offset(lv);
        if (lv2.getFluidState(lv3).isIn(FluidTags.WATER)) {
            g = 1.0;
        } else if (lv2.getBlockState(lv3).isAir() && lv2.getFluidState(lv3.down()).isIn(FluidTags.WATER)) {
            g = 0.0;
        } else {
            return this.itemDispenser.dispense(pointer, stack);
        }
        BoatEntity lv4 = this.chest ? new ChestBoatEntity(lv2, d, e + g, f) : new BoatEntity(lv2, d, e + g, f);
        lv4.setVariant(this.boatType);
        lv4.setYaw(lv.asRotation());
        lv2.spawnEntity(lv4);
        stack.decrement(1);
        return stack;
    }

    @Override
    protected void playSound(BlockPointer pointer) {
        pointer.getWorld().syncWorldEvent(WorldEvents.DISPENSER_DISPENSES, pointer.getPos(), 0);
    }
}

