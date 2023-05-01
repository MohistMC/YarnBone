/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.attribute;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.registry.Registries;
import org.jetbrains.annotations.Nullable;

public class DefaultAttributeContainer {
    private final Map<EntityAttribute, EntityAttributeInstance> instances;

    public DefaultAttributeContainer(Map<EntityAttribute, EntityAttributeInstance> instances) {
        this.instances = ImmutableMap.copyOf(instances);
    }

    private EntityAttributeInstance require(EntityAttribute attribute) {
        EntityAttributeInstance lv = this.instances.get(attribute);
        if (lv == null) {
            throw new IllegalArgumentException("Can't find attribute " + Registries.ATTRIBUTE.getId(attribute));
        }
        return lv;
    }

    public double getValue(EntityAttribute attribute) {
        return this.require(attribute).getValue();
    }

    public double getBaseValue(EntityAttribute attribute) {
        return this.require(attribute).getBaseValue();
    }

    public double getModifierValue(EntityAttribute attribute, UUID uuid) {
        EntityAttributeModifier lv = this.require(attribute).getModifier(uuid);
        if (lv == null) {
            throw new IllegalArgumentException("Can't find modifier " + uuid + " on attribute " + Registries.ATTRIBUTE.getId(attribute));
        }
        return lv.getValue();
    }

    @Nullable
    public EntityAttributeInstance createOverride(Consumer<EntityAttributeInstance> updateCallback, EntityAttribute attribute) {
        EntityAttributeInstance lv = this.instances.get(attribute);
        if (lv == null) {
            return null;
        }
        EntityAttributeInstance lv2 = new EntityAttributeInstance(attribute, updateCallback);
        lv2.setFrom(lv);
        return lv2;
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean has(EntityAttribute type) {
        return this.instances.containsKey(type);
    }

    public boolean hasModifier(EntityAttribute type, UUID uuid) {
        EntityAttributeInstance lv = this.instances.get(type);
        return lv != null && lv.getModifier(uuid) != null;
    }

    public static class Builder {
        private final Map<EntityAttribute, EntityAttributeInstance> instances = Maps.newHashMap();
        private boolean unmodifiable;

        private EntityAttributeInstance checkedAdd(EntityAttribute attribute2) {
            EntityAttributeInstance lv = new EntityAttributeInstance(attribute2, attribute -> {
                if (this.unmodifiable) {
                    throw new UnsupportedOperationException("Tried to change value for default attribute instance: " + Registries.ATTRIBUTE.getId(attribute2));
                }
            });
            this.instances.put(attribute2, lv);
            return lv;
        }

        public Builder add(EntityAttribute attribute) {
            this.checkedAdd(attribute);
            return this;
        }

        public Builder add(EntityAttribute attribute, double baseValue) {
            EntityAttributeInstance lv = this.checkedAdd(attribute);
            lv.setBaseValue(baseValue);
            return this;
        }

        public DefaultAttributeContainer build() {
            this.unmodifiable = true;
            return new DefaultAttributeContainer(this.instances);
        }
    }
}

