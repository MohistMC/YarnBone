/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.text;

import net.minecraft.text.Style;

@FunctionalInterface
public interface CharacterVisitor {
    public boolean accept(int var1, Style var2, int var3);
}

