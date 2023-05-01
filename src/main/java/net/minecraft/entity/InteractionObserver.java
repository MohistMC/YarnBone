/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityInteraction;

public interface InteractionObserver {
    public void onInteractionWith(EntityInteraction var1, Entity var2);
}

