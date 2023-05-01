/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.entity.projectile;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.ExplosiveProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Util;
import net.minecraft.world.World;

public abstract class AbstractFireballEntity
extends ExplosiveProjectileEntity
implements FlyingItemEntity {
    private static final TrackedData<ItemStack> ITEM = DataTracker.registerData(AbstractFireballEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);

    public AbstractFireballEntity(EntityType<? extends AbstractFireballEntity> arg, World arg2) {
        super((EntityType<? extends ExplosiveProjectileEntity>)arg, arg2);
    }

    public AbstractFireballEntity(EntityType<? extends AbstractFireballEntity> arg, double d, double e, double f, double g, double h, double i, World arg2) {
        super(arg, d, e, f, g, h, i, arg2);
    }

    public AbstractFireballEntity(EntityType<? extends AbstractFireballEntity> arg, LivingEntity arg2, double d, double e, double f, World arg3) {
        super(arg, arg2, d, e, f, arg3);
    }

    public void setItem(ItemStack stack2) {
        if (!stack2.isOf(Items.FIRE_CHARGE) || stack2.hasNbt()) {
            this.getDataTracker().set(ITEM, Util.make(stack2.copy(), stack -> stack.setCount(1)));
        }
    }

    protected ItemStack getItem() {
        return this.getDataTracker().get(ITEM);
    }

    @Override
    public ItemStack getStack() {
        ItemStack lv = this.getItem();
        return lv.isEmpty() ? new ItemStack(Items.FIRE_CHARGE) : lv;
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

