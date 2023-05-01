/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.advancement;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import net.minecraft.advancement.Advancement;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class AdvancementManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Map<Identifier, Advancement> advancements = Maps.newHashMap();
    private final Set<Advancement> roots = Sets.newLinkedHashSet();
    private final Set<Advancement> dependents = Sets.newLinkedHashSet();
    @Nullable
    private Listener listener;

    private void remove(Advancement advancement) {
        for (Advancement lv : advancement.getChildren()) {
            this.remove(lv);
        }
        LOGGER.info("Forgot about advancement {}", (Object)advancement.getId());
        this.advancements.remove(advancement.getId());
        if (advancement.getParent() == null) {
            this.roots.remove(advancement);
            if (this.listener != null) {
                this.listener.onRootRemoved(advancement);
            }
        } else {
            this.dependents.remove(advancement);
            if (this.listener != null) {
                this.listener.onDependentRemoved(advancement);
            }
        }
    }

    public void removeAll(Set<Identifier> advancements) {
        for (Identifier lv : advancements) {
            Advancement lv2 = this.advancements.get(lv);
            if (lv2 == null) {
                LOGGER.warn("Told to remove advancement {} but I don't know what that is", (Object)lv);
                continue;
            }
            this.remove(lv2);
        }
    }

    public void load(Map<Identifier, Advancement.Builder> advancements) {
        HashMap<Identifier, Advancement.Builder> map2 = Maps.newHashMap(advancements);
        while (!map2.isEmpty()) {
            boolean bl = false;
            Iterator iterator = map2.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = iterator.next();
                Identifier lv = (Identifier)entry.getKey();
                Advancement.Builder lv2 = (Advancement.Builder)entry.getValue();
                if (!lv2.findParent(this.advancements::get)) continue;
                Advancement lv3 = lv2.build(lv);
                this.advancements.put(lv, lv3);
                bl = true;
                iterator.remove();
                if (lv3.getParent() == null) {
                    this.roots.add(lv3);
                    if (this.listener == null) continue;
                    this.listener.onRootAdded(lv3);
                    continue;
                }
                this.dependents.add(lv3);
                if (this.listener == null) continue;
                this.listener.onDependentAdded(lv3);
            }
            if (bl) continue;
            for (Map.Entry entry : map2.entrySet()) {
                LOGGER.error("Couldn't load advancement {}: {}", entry.getKey(), entry.getValue());
            }
        }
        LOGGER.info("Loaded {} advancements", (Object)this.advancements.size());
    }

    public void clear() {
        this.advancements.clear();
        this.roots.clear();
        this.dependents.clear();
        if (this.listener != null) {
            this.listener.onClear();
        }
    }

    public Iterable<Advancement> getRoots() {
        return this.roots;
    }

    public Collection<Advancement> getAdvancements() {
        return this.advancements.values();
    }

    @Nullable
    public Advancement get(Identifier id) {
        return this.advancements.get(id);
    }

    public void setListener(@Nullable Listener listener) {
        this.listener = listener;
        if (listener != null) {
            for (Advancement lv : this.roots) {
                listener.onRootAdded(lv);
            }
            for (Advancement lv : this.dependents) {
                listener.onDependentAdded(lv);
            }
        }
    }

    public static interface Listener {
        public void onRootAdded(Advancement var1);

        public void onRootRemoved(Advancement var1);

        public void onDependentAdded(Advancement var1);

        public void onDependentRemoved(Advancement var1);

        public void onClear();
    }
}

