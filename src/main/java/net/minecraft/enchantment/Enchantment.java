/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.enchantment;

import com.google.common.collect.Maps;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

public abstract class Enchantment {
    private final EquipmentSlot[] slotTypes;
    private final Rarity rarity;
    public final EnchantmentTarget target;
    @Nullable
    protected String translationKey;

    @Nullable
    public static Enchantment byRawId(int id) {
        return (Enchantment)Registries.ENCHANTMENT.get(id);
    }

    protected Enchantment(Rarity weight, EnchantmentTarget target, EquipmentSlot[] slotTypes) {
        this.rarity = weight;
        this.target = target;
        this.slotTypes = slotTypes;
    }

    public Map<EquipmentSlot, ItemStack> getEquipment(LivingEntity entity) {
        EnumMap<EquipmentSlot, ItemStack> map = Maps.newEnumMap(EquipmentSlot.class);
        for (EquipmentSlot lv : this.slotTypes) {
            ItemStack lv2 = entity.getEquippedStack(lv);
            if (lv2.isEmpty()) continue;
            map.put(lv, lv2);
        }
        return map;
    }

    public Rarity getRarity() {
        return this.rarity;
    }

    public int getMinLevel() {
        return 1;
    }

    public int getMaxLevel() {
        return 1;
    }

    public int getMinPower(int level) {
        return 1 + level * 10;
    }

    public int getMaxPower(int level) {
        return this.getMinPower(level) + 5;
    }

    public int getProtectionAmount(int level, DamageSource source) {
        return 0;
    }

    public float getAttackDamage(int level, EntityGroup group) {
        return 0.0f;
    }

    public final boolean canCombine(Enchantment other) {
        return this.canAccept(other) && other.canAccept(this);
    }

    protected boolean canAccept(Enchantment other) {
        return this != other;
    }

    protected String getOrCreateTranslationKey() {
        if (this.translationKey == null) {
            this.translationKey = Util.createTranslationKey("enchantment", Registries.ENCHANTMENT.getId(this));
        }
        return this.translationKey;
    }

    public String getTranslationKey() {
        return this.getOrCreateTranslationKey();
    }

    public Text getName(int level) {
        MutableText lv = Text.translatable(this.getTranslationKey());
        if (this.isCursed()) {
            lv.formatted(Formatting.RED);
        } else {
            lv.formatted(Formatting.GRAY);
        }
        if (level != 1 || this.getMaxLevel() != 1) {
            lv.append(ScreenTexts.SPACE).append(Text.translatable("enchantment.level." + level));
        }
        return lv;
    }

    public boolean isAcceptableItem(ItemStack stack) {
        return this.target.isAcceptableItem(stack.getItem());
    }

    public void onTargetDamaged(LivingEntity user, Entity target, int level) {
    }

    public void onUserDamaged(LivingEntity user, Entity attacker, int level) {
    }

    public boolean isTreasure() {
        return false;
    }

    public boolean isCursed() {
        return false;
    }

    public boolean isAvailableForEnchantedBookOffer() {
        return true;
    }

    public boolean isAvailableForRandomSelection() {
        return true;
    }

    public static enum Rarity {
        COMMON(10),
        UNCOMMON(5),
        RARE(2),
        VERY_RARE(1);

        private final int weight;

        private Rarity(int weight) {
            this.weight = weight;
        }

        public int getWeight() {
            return this.weight;
        }
    }
}

