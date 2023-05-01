/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public class ComparatorBlockEntity
extends BlockEntity {
    private int outputSignal;

    public ComparatorBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityType.COMPARATOR, pos, state);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt("OutputSignal", this.outputSignal);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.outputSignal = nbt.getInt("OutputSignal");
    }

    public int getOutputSignal() {
        return this.outputSignal;
    }

    public void setOutputSignal(int outputSignal) {
        this.outputSignal = outputSignal;
    }
}

