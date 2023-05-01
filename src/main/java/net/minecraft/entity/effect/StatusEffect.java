/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.effect;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

public class StatusEffect {
    private final Map<EntityAttribute, EntityAttributeModifier> attributeModifiers = Maps.newHashMap();
    private final StatusEffectCategory category;
    private final int color;
    @Nullable
    private String translationKey;
    private Supplier<StatusEffectInstance.FactorCalculationData> factorCalculationDataSupplier = () -> null;

    @Nullable
    public static StatusEffect byRawId(int rawId) {
        return (StatusEffect)Registries.STATUS_EFFECT.get(rawId);
    }

    public static int getRawId(StatusEffect type) {
        return Registries.STATUS_EFFECT.getRawId(type);
    }

    public static int getRawIdNullable(@Nullable StatusEffect type) {
        return Registries.STATUS_EFFECT.getRawId(type);
    }

    protected StatusEffect(StatusEffectCategory category, int color) {
        this.category = category;
        this.color = color;
    }

    public Optional<StatusEffectInstance.FactorCalculationData> getFactorCalculationDataSupplier() {
        return Optional.ofNullable(this.factorCalculationDataSupplier.get());
    }

    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (this == StatusEffects.REGENERATION) {
            if (entity.getHealth() < entity.getMaxHealth()) {
                entity.heal(1.0f);
            }
        } else if (this == StatusEffects.POISON) {
            if (entity.getHealth() > 1.0f) {
                entity.damage(entity.getDamageSources().magic(), 1.0f);
            }
        } else if (this == StatusEffects.WITHER) {
            entity.damage(entity.getDamageSources().wither(), 1.0f);
        } else if (this == StatusEffects.HUNGER && entity instanceof PlayerEntity) {
            ((PlayerEntity)entity).addExhaustion(0.005f * (float)(amplifier + 1));
        } else if (this == StatusEffects.SATURATION && entity instanceof PlayerEntity) {
            if (!entity.world.isClient) {
                ((PlayerEntity)entity).getHungerManager().add(amplifier + 1, 1.0f);
            }
        } else if (this == StatusEffects.INSTANT_HEALTH && !entity.isUndead() || this == StatusEffects.INSTANT_DAMAGE && entity.isUndead()) {
            entity.heal(Math.max(4 << amplifier, 0));
        } else if (this == StatusEffects.INSTANT_DAMAGE && !entity.isUndead() || this == StatusEffects.INSTANT_HEALTH && entity.isUndead()) {
            entity.damage(entity.getDamageSources().magic(), 6 << amplifier);
        }
    }

    public void applyInstantEffect(@Nullable Entity source, @Nullable Entity attacker, LivingEntity target, int amplifier, double proximity) {
        if (this == StatusEffects.INSTANT_HEALTH && !target.isUndead() || this == StatusEffects.INSTANT_DAMAGE && target.isUndead()) {
            int j = (int)(proximity * (double)(4 << amplifier) + 0.5);
            target.heal(j);
        } else if (this == StatusEffects.INSTANT_DAMAGE && !target.isUndead() || this == StatusEffects.INSTANT_HEALTH && target.isUndead()) {
            int j = (int)(proximity * (double)(6 << amplifier) + 0.5);
            if (source == null) {
                target.damage(target.getDamageSources().magic(), j);
            } else {
                target.damage(target.getDamageSources().indirectMagic(source, attacker), j);
            }
        } else {
            this.applyUpdateEffect(target, amplifier);
        }
    }

    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        if (this == StatusEffects.REGENERATION) {
            int k = 50 >> amplifier;
            if (k > 0) {
                return duration % k == 0;
            }
            return true;
        }
        if (this == StatusEffects.POISON) {
            int k = 25 >> amplifier;
            if (k > 0) {
                return duration % k == 0;
            }
            return true;
        }
        if (this == StatusEffects.WITHER) {
            int k = 40 >> amplifier;
            if (k > 0) {
                return duration % k == 0;
            }
            return true;
        }
        return this == StatusEffects.HUNGER;
    }

    public boolean isInstant() {
        return false;
    }

    protected String loadTranslationKey() {
        if (this.translationKey == null) {
            this.translationKey = Util.createTranslationKey("effect", Registries.STATUS_EFFECT.getId(this));
        }
        return this.translationKey;
    }

    public String getTranslationKey() {
        return this.loadTranslationKey();
    }

    public Text getName() {
        return Text.translatable(this.getTranslationKey());
    }

    public StatusEffectCategory getCategory() {
        return this.category;
    }

    public int getColor() {
        return this.color;
    }

    public StatusEffect addAttributeModifier(EntityAttribute attribute, String uuid, double amount, EntityAttributeModifier.Operation operation) {
        EntityAttributeModifier lv = new EntityAttributeModifier(UUID.fromString(uuid), this::getTranslationKey, amount, operation);
        this.attributeModifiers.put(attribute, lv);
        return this;
    }

    public StatusEffect setFactorCalculationDataSupplier(Supplier<StatusEffectInstance.FactorCalculationData> factorCalculationDataSupplier) {
        this.factorCalculationDataSupplier = factorCalculationDataSupplier;
        return this;
    }

    public Map<EntityAttribute, EntityAttributeModifier> getAttributeModifiers() {
        return this.attributeModifiers;
    }

    public void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        for (Map.Entry<EntityAttribute, EntityAttributeModifier> entry : this.attributeModifiers.entrySet()) {
            EntityAttributeInstance lv = attributes.getCustomInstance(entry.getKey());
            if (lv == null) continue;
            lv.removeModifier(entry.getValue());
        }
    }

    public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        for (Map.Entry<EntityAttribute, EntityAttributeModifier> entry : this.attributeModifiers.entrySet()) {
            EntityAttributeInstance lv = attributes.getCustomInstance(entry.getKey());
            if (lv == null) continue;
            EntityAttributeModifier lv2 = entry.getValue();
            lv.removeModifier(lv2);
            lv.addPersistentModifier(new EntityAttributeModifier(lv2.getId(), this.getTranslationKey() + " " + amplifier, this.adjustModifierAmount(amplifier, lv2), lv2.getOperation()));
        }
    }

    public double adjustModifierAmount(int amplifier, EntityAttributeModifier modifier) {
        return modifier.getValue() * (double)(amplifier + 1);
    }

    public boolean isBeneficial() {
        return this.category == StatusEffectCategory.BENEFICIAL;
    }
}

