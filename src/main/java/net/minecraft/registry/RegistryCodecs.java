/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.registry;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.entry.RegistryElementCodec;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.entry.RegistryEntryListCodec;
import net.minecraft.registry.entry.RegistryFixedCodec;

public class RegistryCodecs {
    private static <T> MapCodec<RegistryManagerEntry<T>> managerEntry(RegistryKey<? extends Registry<T>> registryRef, MapCodec<T> elementCodec) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)RegistryKey.createCodec(registryRef).fieldOf("name")).forGetter(RegistryManagerEntry::key), ((MapCodec)Codec.INT.fieldOf("id")).forGetter(RegistryManagerEntry::rawId), elementCodec.forGetter(RegistryManagerEntry::value)).apply((Applicative<RegistryManagerEntry, ?>)instance, RegistryManagerEntry::new));
    }

    public static <T> Codec<Registry<T>> createRegistryCodec(RegistryKey<? extends Registry<T>> registryRef, Lifecycle lifecycle, Codec<T> elementCodec) {
        return RegistryCodecs.managerEntry(registryRef, elementCodec.fieldOf("element")).codec().listOf().xmap(entries -> {
            SimpleRegistry lv = new SimpleRegistry(registryRef, lifecycle);
            for (RegistryManagerEntry lv2 : entries) {
                lv.set(lv2.rawId(), lv2.key(), lv2.value(), lifecycle);
            }
            return lv;
        }, registry -> {
            ImmutableList.Builder builder = ImmutableList.builder();
            for (Object object : registry) {
                builder.add(new RegistryManagerEntry(registry.getKey(object).get(), registry.getRawId(object), object));
            }
            return builder.build();
        });
    }

    public static <E> Codec<Registry<E>> createKeyedRegistryCodec(RegistryKey<? extends Registry<E>> registryRef, Lifecycle lifecycle, Codec<E> elementCodec) {
        UnboundedMapCodec codec2 = Codec.unboundedMap(RegistryKey.createCodec(registryRef), elementCodec);
        return codec2.xmap(entries -> {
            SimpleRegistry lv = new SimpleRegistry(registryRef, lifecycle);
            entries.forEach((key, value) -> lv.add(key, value, lifecycle));
            return lv.freeze();
        }, registry -> ImmutableMap.copyOf(registry.getEntrySet()));
    }

    public static <E> Codec<RegistryEntryList<E>> entryList(RegistryKey<? extends Registry<E>> registryRef, Codec<E> elementCodec) {
        return RegistryCodecs.entryList(registryRef, elementCodec, false);
    }

    public static <E> Codec<RegistryEntryList<E>> entryList(RegistryKey<? extends Registry<E>> registryRef, Codec<E> elementCodec, boolean alwaysSerializeAsList) {
        return RegistryEntryListCodec.create(registryRef, RegistryElementCodec.of(registryRef, elementCodec), alwaysSerializeAsList);
    }

    public static <E> Codec<RegistryEntryList<E>> entryList(RegistryKey<? extends Registry<E>> registryRef) {
        return RegistryCodecs.entryList(registryRef, false);
    }

    public static <E> Codec<RegistryEntryList<E>> entryList(RegistryKey<? extends Registry<E>> registryRef, boolean alwaysSerializeAsList) {
        return RegistryEntryListCodec.create(registryRef, RegistryFixedCodec.of(registryRef), alwaysSerializeAsList);
    }

    record RegistryManagerEntry<T>(RegistryKey<T> key, int rawId, T value) {
    }
}

