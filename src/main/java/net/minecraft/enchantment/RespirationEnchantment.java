/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;

public class RespirationEnchantment
extends Enchantment {
    public RespirationEnchantment(Enchantment.Rarity weight, EquipmentSlot ... slotTypes) {
        super(weight, EnchantmentTarget.ARMOR_HEAD, slotTypes);
    }

    @Override
    public int getMinPower(int level) {
        return 10 * level;
    }

    @Override
    public int getMaxPower(int level) {
        return this.getMinPower(level) + 30;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }
}

