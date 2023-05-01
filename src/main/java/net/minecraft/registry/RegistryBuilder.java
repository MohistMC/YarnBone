/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.registry;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.mojang.serialization.Lifecycle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.entry.RegistryEntryOwner;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class RegistryBuilder {
    private final List<RegistryInfo<?>> registries = new ArrayList();

    static <T> RegistryEntryLookup<T> toLookup(final RegistryWrapper.Impl<T> wrapper) {
        return new EntryListCreatingLookup<T>(wrapper){

            @Override
            public Optional<RegistryEntry.Reference<T>> getOptional(RegistryKey<T> key) {
                return wrapper.getOptional(key);
            }
        };
    }

    public <T> RegistryBuilder addRegistry(RegistryKey<? extends Registry<T>> registryRef, Lifecycle lifecycle, BootstrapFunction<T> bootstrapFunction) {
        this.registries.add(new RegistryInfo<T>(registryRef, lifecycle, bootstrapFunction));
        return this;
    }

    public <T> RegistryBuilder addRegistry(RegistryKey<? extends Registry<T>> registryRef, BootstrapFunction<T> bootstrapFunction) {
        return this.addRegistry(registryRef, Lifecycle.stable(), bootstrapFunction);
    }

    private Registries createBootstrappedRegistries(DynamicRegistryManager registryManager) {
        Registries lv = Registries.of(registryManager, this.registries.stream().map(RegistryInfo::key));
        this.registries.forEach(registry -> registry.runBootstrap(lv));
        return lv;
    }

    public RegistryWrapper.WrapperLookup createWrapperLookup(DynamicRegistryManager baseRegistryManager) {
        Registries lv = this.createBootstrappedRegistries(baseRegistryManager);
        Stream<RegistryWrapper.Impl> stream = baseRegistryManager.streamAllRegistries().map(entry -> entry.value().getReadOnlyWrapper());
        Stream<RegistryWrapper.Impl> stream2 = this.registries.stream().map(info -> info.init(lv).toWrapper());
        RegistryWrapper.WrapperLookup lv2 = RegistryWrapper.WrapperLookup.of(Stream.concat(stream, stream2.peek(lv::addOwner)));
        lv.validateReferences();
        lv.throwErrors();
        return lv2;
    }

    public RegistryWrapper.WrapperLookup createWrapperLookup(DynamicRegistryManager baseRegistryManager, RegistryWrapper.WrapperLookup wrapperLookup) {
        Registries lv = this.createBootstrappedRegistries(baseRegistryManager);
        HashMap map = new HashMap();
        lv.streamRegistries().forEach(registry -> map.put(registry.key, registry));
        this.registries.stream().map(info -> info.init(lv)).forEach(registry -> map.put(registry.key, registry));
        Stream<RegistryWrapper.Impl> stream = baseRegistryManager.streamAllRegistries().map(entry -> entry.value().getReadOnlyWrapper());
        RegistryWrapper.WrapperLookup lv2 = RegistryWrapper.WrapperLookup.of(Stream.concat(stream, map.values().stream().map(InitializedRegistry::toWrapper).peek(lv::addOwner)));
        lv.setReferenceEntryValues(wrapperLookup);
        lv.validateReferences();
        lv.throwErrors();
        return lv2;
    }

    record RegistryInfo<T>(RegistryKey<? extends Registry<T>> key, Lifecycle lifecycle, BootstrapFunction<T> bootstrap) {
        void runBootstrap(Registries registries) {
            this.bootstrap.run(registries.createRegisterable());
        }

        public InitializedRegistry<T> init(Registries registries) {
            HashMap map = new HashMap();
            Iterator<Map.Entry<RegistryKey<?>, RegisteredValue<?>>> iterator = registries.registeredValues.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<RegistryKey<?>, RegisteredValue<?>> entry = iterator.next();
                RegistryKey<?> lv = entry.getKey();
                if (!lv.isOf(this.key)) continue;
                RegistryKey<?> lv2 = lv;
                RegisteredValue<?> lv3 = entry.getValue();
                RegistryEntry.Reference<Object> lv4 = registries.lookup.keysToEntries.remove(lv);
                map.put(lv2, new EntryAssociatedValue(lv3, Optional.ofNullable(lv4)));
                iterator.remove();
            }
            return new InitializedRegistry(this.key, this.lifecycle, map);
        }
    }

    @FunctionalInterface
    public static interface BootstrapFunction<T> {
        public void run(Registerable<T> var1);
    }

    record Registries(AnyOwner owner, StandAloneEntryCreatingLookup lookup, Map<Identifier, RegistryEntryLookup<?>> registries, Map<RegistryKey<?>, RegisteredValue<?>> registeredValues, List<RuntimeException> errors) {
        public static Registries of(DynamicRegistryManager dynamicRegistryManager, Stream<RegistryKey<? extends Registry<?>>> registryRefs) {
            AnyOwner lv = new AnyOwner();
            ArrayList<RuntimeException> list = new ArrayList<RuntimeException>();
            StandAloneEntryCreatingLookup lv2 = new StandAloneEntryCreatingLookup(lv);
            ImmutableMap.Builder builder = ImmutableMap.builder();
            dynamicRegistryManager.streamAllRegistries().forEach(entry -> builder.put(entry.key().getValue(), RegistryBuilder.toLookup(entry.value().getReadOnlyWrapper())));
            registryRefs.forEach(registryRef -> builder.put(registryRef.getValue(), lv2));
            return new Registries(lv, lv2, builder.build(), new HashMap(), list);
        }

        public <T> Registerable<T> createRegisterable() {
            return new Registerable<T>(){

                @Override
                public RegistryEntry.Reference<T> register(RegistryKey<T> key, T value, Lifecycle lifecycle) {
                    RegisteredValue lv = registeredValues.put(key, new RegisteredValue(value, lifecycle));
                    if (lv != null) {
                        errors.add(new IllegalStateException("Duplicate registration for " + key + ", new=" + value + ", old=" + lv.value));
                    }
                    return lookup.getOrCreate(key);
                }

                @Override
                public <S> RegistryEntryLookup<S> getRegistryLookup(RegistryKey<? extends Registry<? extends S>> registryRef) {
                    return registries.getOrDefault(registryRef.getValue(), lookup);
                }
            };
        }

        public void validateReferences() {
            for (RegistryKey<Object> lv : this.lookup.keysToEntries.keySet()) {
                this.errors.add(new IllegalStateException("Unreferenced key: " + lv));
            }
            this.registeredValues.forEach((key, value) -> this.errors.add(new IllegalStateException("Orpaned value " + value.value + " for key " + key)));
        }

        public void throwErrors() {
            if (!this.errors.isEmpty()) {
                IllegalStateException illegalStateException = new IllegalStateException("Errors during registry creation");
                for (RuntimeException runtimeException : this.errors) {
                    illegalStateException.addSuppressed(runtimeException);
                }
                throw illegalStateException;
            }
        }

        public void addOwner(RegistryEntryOwner<?> owner) {
            this.owner.addOwner(owner);
        }

        public void setReferenceEntryValues(RegistryWrapper.WrapperLookup lookup) {
            HashMap<Identifier, Optional> map = new HashMap<Identifier, Optional>();
            Iterator<Map.Entry<RegistryKey<Object>, RegistryEntry.Reference<Object>>> iterator = this.lookup.keysToEntries.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<RegistryKey<Object>, RegistryEntry.Reference<Object>> entry2 = iterator.next();
                RegistryKey<Object> lv = entry2.getKey();
                RegistryEntry.Reference<Object> lv2 = entry2.getValue();
                map.computeIfAbsent(lv.getRegistry(), registryId -> lookup.getOptionalWrapper(RegistryKey.ofRegistry(registryId))).flatMap(entryLookup -> entryLookup.getOptional(lv)).ifPresent(entry -> {
                    lv2.setValue(entry.value());
                    iterator.remove();
                });
            }
        }

        public Stream<InitializedRegistry<?>> streamRegistries() {
            return this.lookup.keysToEntries.keySet().stream().map(RegistryKey::getRegistry).distinct().map(registry -> new InitializedRegistry(RegistryKey.ofRegistry(registry), Lifecycle.stable(), Map.of()));
        }
    }

    record InitializedRegistry<T>(RegistryKey<? extends Registry<? extends T>> key, Lifecycle lifecycle, Map<RegistryKey<T>, EntryAssociatedValue<T>> values) {
        public RegistryWrapper.Impl<T> toWrapper() {
            return new RegistryWrapper.Impl<T>(){
                private final Map<RegistryKey<T>, RegistryEntry.Reference<T>> keysToEntries;
                {
                    this.keysToEntries = values.entrySet().stream().collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, entry -> {
                        EntryAssociatedValue lv = (EntryAssociatedValue)entry.getValue();
                        RegistryEntry.Reference lv2 = lv.entry().orElseGet(() -> RegistryEntry.Reference.standAlone(this, (RegistryKey)entry.getKey()));
                        lv2.setValue(lv.value().value());
                        return lv2;
                    }));
                }

                @Override
                public RegistryKey<? extends Registry<? extends T>> getRegistryKey() {
                    return key;
                }

                @Override
                public Lifecycle getLifecycle() {
                    return lifecycle;
                }

                @Override
                public Optional<RegistryEntry.Reference<T>> getOptional(RegistryKey<T> key) {
                    return Optional.ofNullable(this.keysToEntries.get(key));
                }

                @Override
                public Stream<RegistryEntry.Reference<T>> streamEntries() {
                    return this.keysToEntries.values().stream();
                }

                @Override
                public Optional<RegistryEntryList.Named<T>> getOptional(TagKey<T> tag) {
                    return Optional.empty();
                }

                @Override
                public Stream<RegistryEntryList.Named<T>> streamTags() {
                    return Stream.empty();
                }
            };
        }
    }

    record EntryAssociatedValue<T>(RegisteredValue<T> value, Optional<RegistryEntry.Reference<T>> entry) {
    }

    record RegisteredValue<T>(T value, Lifecycle lifecycle) {
    }

    static class StandAloneEntryCreatingLookup
    extends EntryListCreatingLookup<Object> {
        final Map<RegistryKey<Object>, RegistryEntry.Reference<Object>> keysToEntries = new HashMap<RegistryKey<Object>, RegistryEntry.Reference<Object>>();

        public StandAloneEntryCreatingLookup(RegistryEntryOwner<Object> arg) {
            super(arg);
        }

        @Override
        public Optional<RegistryEntry.Reference<Object>> getOptional(RegistryKey<Object> key) {
            return Optional.of(this.getOrCreate(key));
        }

        <T> RegistryEntry.Reference<T> getOrCreate(RegistryKey<T> key) {
            return this.keysToEntries.computeIfAbsent(key, key2 -> RegistryEntry.Reference.standAlone(this.entryOwner, key2));
        }
    }

    static class AnyOwner
    implements RegistryEntryOwner<Object> {
        private final Set<RegistryEntryOwner<?>> owners = Sets.newIdentityHashSet();

        AnyOwner() {
        }

        @Override
        public boolean ownerEquals(RegistryEntryOwner<Object> other) {
            return this.owners.contains(other);
        }

        public void addOwner(RegistryEntryOwner<?> owner) {
            this.owners.add(owner);
        }
    }

    static abstract class EntryListCreatingLookup<T>
    implements RegistryEntryLookup<T> {
        protected final RegistryEntryOwner<T> entryOwner;

        protected EntryListCreatingLookup(RegistryEntryOwner<T> entryOwner) {
            this.entryOwner = entryOwner;
        }

        @Override
        public Optional<RegistryEntryList.Named<T>> getOptional(TagKey<T> tag) {
            return Optional.of(RegistryEntryList.of(this.entryOwner, tag));
        }
    }
}

