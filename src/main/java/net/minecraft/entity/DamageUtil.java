/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.entity;

import net.minecraft.util.math.MathHelper;

public class DamageUtil {
    public static final float field_29962 = 20.0f;
    public static final float field_29963 = 25.0f;
    public static final float field_29964 = 2.0f;
    public static final float field_29965 = 0.2f;
    private static final int field_29966 = 4;

    public static float getDamageLeft(float damage, float armor, float armorToughness) {
        float i = 2.0f + armorToughness / 4.0f;
        float j = MathHelper.clamp(armor - damage / i, armor * 0.2f, 20.0f);
        return damage * (1.0f - j / 25.0f);
    }

    public static float getInflictedDamage(float damageDealt, float protection) {
        float h = MathHelper.clamp(protection, 0.0f, 20.0f);
        return damageDealt * (1.0f - h / 25.0f);
    }
}

