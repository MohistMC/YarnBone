/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.entity.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class AbsorptionStatusEffect
extends StatusEffect {
    protected AbsorptionStatusEffect(StatusEffectCategory arg, int i) {
        super(arg, i);
    }

    @Override
    public void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        entity.setAbsorptionAmount(entity.getAbsorptionAmount() - (float)(4 * (amplifier + 1)));
        super.onRemoved(entity, attributes, amplifier);
    }

    @Override
    public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        entity.setAbsorptionAmount(entity.getAbsorptionAmount() + (float)(4 * (amplifier + 1)));
        super.onApplied(entity, attributes, amplifier);
    }
}

