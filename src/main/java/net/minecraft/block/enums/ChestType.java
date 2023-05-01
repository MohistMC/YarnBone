/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.block.enums;

import net.minecraft.util.StringIdentifiable;

public enum ChestType implements StringIdentifiable
{
    SINGLE("single"),
    LEFT("left"),
    RIGHT("right");

    private final String name;

    private ChestType(String name) {
        this.name = name;
    }

    @Override
    public String asString() {
        return this.name;
    }

    public ChestType getOpposite() {
        return switch (this) {
            default -> throw new IncompatibleClassChangeError();
            case SINGLE -> SINGLE;
            case LEFT -> RIGHT;
            case RIGHT -> LEFT;
        };
    }
}

