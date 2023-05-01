/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.world.entity;

import java.util.UUID;
import java.util.stream.Stream;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.entity.EntityChangeListener;

public interface EntityLike {
    public int getId();

    public UUID getUuid();

    public BlockPos getBlockPos();

    public Box getBoundingBox();

    public void setChangeListener(EntityChangeListener var1);

    public Stream<? extends EntityLike> streamSelfAndPassengers();

    public Stream<? extends EntityLike> streamPassengersAndSelf();

    public void setRemoved(Entity.RemovalReason var1);

    public boolean shouldSave();

    public boolean isPlayer();
}

