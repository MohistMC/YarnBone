/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.entity.attribute;

public class EntityAttribute {
    public static final int field_30097 = 64;
    private final double fallback;
    private boolean tracked;
    private final String translationKey;

    protected EntityAttribute(String translationKey, double fallback) {
        this.fallback = fallback;
        this.translationKey = translationKey;
    }

    public double getDefaultValue() {
        return this.fallback;
    }

    public boolean isTracked() {
        return this.tracked;
    }

    public EntityAttribute setTracked(boolean tracked) {
        this.tracked = tracked;
        return this;
    }

    public double clamp(double value) {
        return value;
    }

    public String getTranslationKey() {
        return this.translationKey;
    }
}

