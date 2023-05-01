/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.attribute;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import org.jetbrains.annotations.Nullable;

public class EntityAttributeInstance {
    private final EntityAttribute type;
    private final Map<EntityAttributeModifier.Operation, Set<EntityAttributeModifier>> operationToModifiers = Maps.newEnumMap(EntityAttributeModifier.Operation.class);
    private final Map<UUID, EntityAttributeModifier> idToModifiers = new Object2ObjectArrayMap<UUID, EntityAttributeModifier>();
    private final Set<EntityAttributeModifier> persistentModifiers = new ObjectArraySet<EntityAttributeModifier>();
    private double baseValue;
    private boolean dirty = true;
    private double value;
    private final Consumer<EntityAttributeInstance> updateCallback;

    public EntityAttributeInstance(EntityAttribute type, Consumer<EntityAttributeInstance> updateCallback) {
        this.type = type;
        this.updateCallback = updateCallback;
        this.baseValue = type.getDefaultValue();
    }

    public EntityAttribute getAttribute() {
        return this.type;
    }

    public double getBaseValue() {
        return this.baseValue;
    }

    public void setBaseValue(double baseValue) {
        if (baseValue == this.baseValue) {
            return;
        }
        this.baseValue = baseValue;
        this.onUpdate();
    }

    public Set<EntityAttributeModifier> getModifiers(EntityAttributeModifier.Operation operation2) {
        return this.operationToModifiers.computeIfAbsent(operation2, operation -> Sets.newHashSet());
    }

    public Set<EntityAttributeModifier> getModifiers() {
        return ImmutableSet.copyOf(this.idToModifiers.values());
    }

    @Nullable
    public EntityAttributeModifier getModifier(UUID uuid) {
        return this.idToModifiers.get(uuid);
    }

    public boolean hasModifier(EntityAttributeModifier modifier) {
        return this.idToModifiers.get(modifier.getId()) != null;
    }

    private void addModifier(EntityAttributeModifier modifier) {
        EntityAttributeModifier lv = this.idToModifiers.putIfAbsent(modifier.getId(), modifier);
        if (lv != null) {
            throw new IllegalArgumentException("Modifier is already applied on this attribute!");
        }
        this.getModifiers(modifier.getOperation()).add(modifier);
        this.onUpdate();
    }

    public void addTemporaryModifier(EntityAttributeModifier modifier) {
        this.addModifier(modifier);
    }

    public void addPersistentModifier(EntityAttributeModifier modifier) {
        this.addModifier(modifier);
        this.persistentModifiers.add(modifier);
    }

    protected void onUpdate() {
        this.dirty = true;
        this.updateCallback.accept(this);
    }

    public void removeModifier(EntityAttributeModifier modifier) {
        this.getModifiers(modifier.getOperation()).remove(modifier);
        this.idToModifiers.remove(modifier.getId());
        this.persistentModifiers.remove(modifier);
        this.onUpdate();
    }

    public void removeModifier(UUID uuid) {
        EntityAttributeModifier lv = this.getModifier(uuid);
        if (lv != null) {
            this.removeModifier(lv);
        }
    }

    public boolean tryRemoveModifier(UUID uuid) {
        EntityAttributeModifier lv = this.getModifier(uuid);
        if (lv != null && this.persistentModifiers.contains(lv)) {
            this.removeModifier(lv);
            return true;
        }
        return false;
    }

    public void clearModifiers() {
        for (EntityAttributeModifier lv : this.getModifiers()) {
            this.removeModifier(lv);
        }
    }

    public double getValue() {
        if (this.dirty) {
            this.value = this.computeValue();
            this.dirty = false;
        }
        return this.value;
    }

    private double computeValue() {
        double d = this.getBaseValue();
        for (EntityAttributeModifier lv : this.getModifiersByOperation(EntityAttributeModifier.Operation.ADDITION)) {
            d += lv.getValue();
        }
        double e = d;
        for (EntityAttributeModifier lv2 : this.getModifiersByOperation(EntityAttributeModifier.Operation.MULTIPLY_BASE)) {
            e += d * lv2.getValue();
        }
        for (EntityAttributeModifier lv2 : this.getModifiersByOperation(EntityAttributeModifier.Operation.MULTIPLY_TOTAL)) {
            e *= 1.0 + lv2.getValue();
        }
        return this.type.clamp(e);
    }

    private Collection<EntityAttributeModifier> getModifiersByOperation(EntityAttributeModifier.Operation operation) {
        return this.operationToModifiers.getOrDefault((Object)operation, Collections.emptySet());
    }

    public void setFrom(EntityAttributeInstance other) {
        this.baseValue = other.baseValue;
        this.idToModifiers.clear();
        this.idToModifiers.putAll(other.idToModifiers);
        this.persistentModifiers.clear();
        this.persistentModifiers.addAll(other.persistentModifiers);
        this.operationToModifiers.clear();
        other.operationToModifiers.forEach((operation, modifiers) -> this.getModifiers((EntityAttributeModifier.Operation)((Object)operation)).addAll((Collection<EntityAttributeModifier>)modifiers));
        this.onUpdate();
    }

    public NbtCompound toNbt() {
        NbtCompound lv = new NbtCompound();
        lv.putString("Name", Registries.ATTRIBUTE.getId(this.type).toString());
        lv.putDouble("Base", this.baseValue);
        if (!this.persistentModifiers.isEmpty()) {
            NbtList lv2 = new NbtList();
            for (EntityAttributeModifier lv3 : this.persistentModifiers) {
                lv2.add(lv3.toNbt());
            }
            lv.put("Modifiers", lv2);
        }
        return lv;
    }

    public void readNbt(NbtCompound nbt) {
        this.baseValue = nbt.getDouble("Base");
        if (nbt.contains("Modifiers", NbtElement.LIST_TYPE)) {
            NbtList lv = nbt.getList("Modifiers", NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < lv.size(); ++i) {
                EntityAttributeModifier lv2 = EntityAttributeModifier.fromNbt(lv.getCompound(i));
                if (lv2 == null) continue;
                this.idToModifiers.put(lv2.getId(), lv2);
                this.getModifiers(lv2.getOperation()).add(lv2);
                this.persistentModifiers.add(lv2);
            }
        }
        this.onUpdate();
    }
}

