/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.world.entity;

import net.minecraft.entity.Entity;

public interface EntityChangeListener {
    public static final EntityChangeListener NONE = new EntityChangeListener(){

        @Override
        public void updateEntityPosition() {
        }

        @Override
        public void remove(Entity.RemovalReason reason) {
        }
    };

    public void updateEntityPosition();

    public void remove(Entity.RemovalReason var1);
}

