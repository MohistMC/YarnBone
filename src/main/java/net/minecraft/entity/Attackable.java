/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity;

import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public interface Attackable {
    @Nullable
    public LivingEntity getLastAttacker();
}

