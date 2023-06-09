/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.stat;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatFormatter;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class StatType<T>
implements Iterable<Stat<T>> {
    private final Registry<T> registry;
    private final Map<T, Stat<T>> stats = new IdentityHashMap<T, Stat<T>>();
    @Nullable
    private Text name;

    public StatType(Registry<T> registry) {
        this.registry = registry;
    }

    public boolean hasStat(T key) {
        return this.stats.containsKey(key);
    }

    public Stat<T> getOrCreateStat(T key, StatFormatter formatter) {
        return this.stats.computeIfAbsent(key, value -> new Stat<Object>(this, value, formatter));
    }

    public Registry<T> getRegistry() {
        return this.registry;
    }

    @Override
    public Iterator<Stat<T>> iterator() {
        return this.stats.values().iterator();
    }

    public Stat<T> getOrCreateStat(T key) {
        return this.getOrCreateStat(key, StatFormatter.DEFAULT);
    }

    public String getTranslationKey() {
        return "stat_type." + Registries.STAT_TYPE.getId(this).toString().replace(':', '.');
    }

    public Text getName() {
        if (this.name == null) {
            this.name = Text.translatable(this.getTranslationKey());
        }
        return this.name;
    }
}

