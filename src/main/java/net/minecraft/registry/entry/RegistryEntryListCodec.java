/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.registry.entry;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.dynamic.Codecs;

public class RegistryEntryListCodec<E>
implements Codec<RegistryEntryList<E>> {
    private final RegistryKey<? extends Registry<E>> registry;
    private final Codec<RegistryEntry<E>> entryCodec;
    private final Codec<List<RegistryEntry<E>>> directEntryListCodec;
    private final Codec<Either<TagKey<E>, List<RegistryEntry<E>>>> entryListStorageCodec;

    private static <E> Codec<List<RegistryEntry<E>>> createDirectEntryListCodec(Codec<RegistryEntry<E>> entryCodec, boolean alwaysSerializeAsList) {
        Codec<List<RegistryEntry<E>>> codec2 = Codecs.validate(entryCodec.listOf(), Codecs.createEqualTypeChecker(RegistryEntry::getType));
        if (alwaysSerializeAsList) {
            return codec2;
        }
        return Codec.either(codec2, entryCodec).xmap(either -> either.map(entries -> entries, List::of), entries -> entries.size() == 1 ? Either.right((RegistryEntry)entries.get(0)) : Either.left(entries));
    }

    public static <E> Codec<RegistryEntryList<E>> create(RegistryKey<? extends Registry<E>> registryRef, Codec<RegistryEntry<E>> entryCodec, boolean alwaysSerializeAsList) {
        return new RegistryEntryListCodec<E>(registryRef, entryCodec, alwaysSerializeAsList);
    }

    private RegistryEntryListCodec(RegistryKey<? extends Registry<E>> registry, Codec<RegistryEntry<E>> entryCodec, boolean alwaysSerializeAsList) {
        this.registry = registry;
        this.entryCodec = entryCodec;
        this.directEntryListCodec = RegistryEntryListCodec.createDirectEntryListCodec(entryCodec, alwaysSerializeAsList);
        this.entryListStorageCodec = Codec.either(TagKey.codec(registry), this.directEntryListCodec);
    }

    @Override
    public <T> DataResult<Pair<RegistryEntryList<E>, T>> decode(DynamicOps<T> ops, T input) {
        RegistryOps lv;
        Optional optional;
        if (ops instanceof RegistryOps && (optional = (lv = (RegistryOps)ops).getEntryLookup(this.registry)).isPresent()) {
            RegistryEntryLookup lv2 = optional.get();
            return this.entryListStorageCodec.decode(ops, input).map((? super R pair) -> pair.mapFirst(either -> either.map(lv2::getOrThrow, RegistryEntryList::of)));
        }
        return this.decodeDirect(ops, input);
    }

    @Override
    public <T> DataResult<T> encode(RegistryEntryList<E> arg, DynamicOps<T> dynamicOps, T object) {
        RegistryOps lv;
        Optional optional;
        if (dynamicOps instanceof RegistryOps && (optional = (lv = (RegistryOps)dynamicOps).getOwner(this.registry)).isPresent()) {
            if (!arg.ownerEquals(optional.get())) {
                return DataResult.error(() -> "HolderSet " + arg + " is not valid in current registry set");
            }
            return this.entryListStorageCodec.encode(arg.getStorage().mapRight(List::copyOf), dynamicOps, object);
        }
        return this.encodeDirect(arg, dynamicOps, object);
    }

    private <T> DataResult<Pair<RegistryEntryList<E>, T>> decodeDirect(DynamicOps<T> ops, T input) {
        return this.entryCodec.listOf().decode(ops, input).flatMap((? super R pair) -> {
            ArrayList<RegistryEntry.Direct> list = new ArrayList<RegistryEntry.Direct>();
            for (RegistryEntry lv : (List)pair.getFirst()) {
                if (lv instanceof RegistryEntry.Direct) {
                    RegistryEntry.Direct lv2 = (RegistryEntry.Direct)lv;
                    list.add(lv2);
                    continue;
                }
                return DataResult.error(() -> "Can't decode element " + lv + " without registry");
            }
            return DataResult.success(new Pair(RegistryEntryList.of(list), pair.getSecond()));
        });
    }

    private <T> DataResult<T> encodeDirect(RegistryEntryList<E> entryList, DynamicOps<T> ops, T prefix) {
        return this.directEntryListCodec.encode(entryList.stream().toList(), ops, prefix);
    }

    @Override
    public /* synthetic */ DataResult encode(Object entryList, DynamicOps ops, Object prefix) {
        return this.encode((RegistryEntryList)entryList, ops, prefix);
    }
}

