/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.data.server.advancement;

import java.util.function.Consumer;
import net.minecraft.advancement.Advancement;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

public interface AdvancementTabGenerator {
    public static Advancement createEmptyAdvancement(String id) {
        return Advancement.Builder.create().build(new Identifier(id));
    }

    public void accept(RegistryWrapper.WrapperLookup var1, Consumer<Advancement> var2);
}

