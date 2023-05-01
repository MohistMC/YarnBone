/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.loot.context;

import net.minecraft.util.Identifier;

public class LootContextParameter<T> {
    private final Identifier id;

    public LootContextParameter(Identifier id) {
        this.id = id;
    }

    public Identifier getId() {
        return this.id;
    }

    public String toString() {
        return "<parameter " + this.id + ">";
    }
}

