/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.item;

import net.minecraft.recipe.Ingredient;

public interface ToolMaterial {
    public int getDurability();

    public float getMiningSpeedMultiplier();

    public float getAttackDamage();

    public int getMiningLevel();

    public int getEnchantability();

    public Ingredient getRepairIngredient();
}

