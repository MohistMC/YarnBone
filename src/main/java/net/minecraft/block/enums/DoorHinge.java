/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.block.enums;

import net.minecraft.util.StringIdentifiable;

public enum DoorHinge implements StringIdentifiable
{
    LEFT,
    RIGHT;


    public String toString() {
        return this.asString();
    }

    @Override
    public String asString() {
        return this == LEFT ? "left" : "right";
    }
}

