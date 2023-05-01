/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.entity.projectile.thrown;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Util;
import net.minecraft.world.World;

public abstract class ThrownItemEntity
extends ThrownEntity
implements FlyingItemEntity {
    private static final TrackedData<ItemStack> ITEM = DataTracker.registerData(ThrownItemEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);

    public ThrownItemEntity(EntityType<? extends ThrownItemEntity> arg, World arg2) {
        super((EntityType<? extends ThrownEntity>)arg, arg2);
    }

    public ThrownItemEntity(EntityType<? extends ThrownItemEntity> arg, double d, double e, double f, World arg2) {
        super(arg, d, e, f, arg2);
    }

    public ThrownItemEntity(EntityType<? extends ThrownItemEntity> arg, LivingEntity arg2, World arg3) {
        super(arg, arg2, arg3);
    }

    public void setItem(ItemStack item) {
        if (!item.isOf(this.getDefaultItem()) || item.hasNbt()) {
            this.getDataTracker().set(ITEM, Util.make(item.copy(), stack -> stack.setCount(1)));
        }
    }

    protected abstract Item getDefaultItem();

    protected ItemStack getItem() {
        return this.getDataTracker().get(ITEM);
    }

    @Override
    public ItemStack getStack() {
        ItemStack lv = this.getItem();
        return lv.isEmpty() ? new ItemStack(this.getDefaultItem()) : lv;
    }

    @Override
    protected void initDataTracker() {
        this.getDataTracker().startTracking(ITEM, ItemStack.EMPTY);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        ItemStack lv = this.getItem();
        if (!lv.isEmpty()) {
            nbt.put("Item", lv.writeNbt(new NbtCompound()));
        }
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        ItemStack lv = ItemStack.fromNbt(nbt.getCompound("Item"));
        this.setItem(lv);
    }
}

