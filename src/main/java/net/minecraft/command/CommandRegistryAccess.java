/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.command;

import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.featuretoggle.FeatureSet;

public interface CommandRegistryAccess {
    public <T> RegistryWrapper<T> createWrapper(RegistryKey<? extends Registry<T>> var1);

    public static CommandRegistryAccess of(final RegistryWrapper.WrapperLookup wrapperLookup, final FeatureSet enabledFeatures) {
        return new CommandRegistryAccess(){

            @Override
            public <T> RegistryWrapper<T> createWrapper(RegistryKey<? extends Registry<T>> registryRef) {
                return wrapperLookup.getWrapperOrThrow(registryRef).withFeatureFilter(enabledFeatures);
            }
        };
    }

    public static EntryListCreationPolicySettable of(final DynamicRegistryManager registryManager, final FeatureSet enabledFeatures) {
        return new EntryListCreationPolicySettable(){
            EntryListCreationPolicy entryListCreationPolicy = EntryListCreationPolicy.FAIL;

            @Override
            public void setEntryListCreationPolicy(EntryListCreationPolicy entryListCreationPolicy) {
                this.entryListCreationPolicy = entryListCreationPolicy;
            }

            @Override
            public <T> RegistryWrapper<T> createWrapper(RegistryKey<? extends Registry<T>> registryRef) {
                Registry lv = registryManager.get(registryRef);
                final RegistryWrapper.Impl lv2 = lv.getReadOnlyWrapper();
                final RegistryWrapper.Impl lv3 = lv.getTagCreatingWrapper();
                RegistryWrapper.Impl.Delegating lv4 = new RegistryWrapper.Impl.Delegating<T>(){

                    @Override
                    protected RegistryWrapper.Impl<T> getBase() {
                        return switch (entryListCreationPolicy) {
                            default -> throw new IncompatibleClassChangeError();
                            case EntryListCreationPolicy.FAIL -> lv2;
                            case EntryListCreationPolicy.CREATE_NEW -> lv3;
                        };
                    }
                };
                return lv4.withFeatureFilter(enabledFeatures);
            }
        };
    }

    public static interface EntryListCreationPolicySettable
    extends CommandRegistryAccess {
        public void setEntryListCreationPolicy(EntryListCreationPolicy var1);
    }

    public static enum EntryListCreationPolicy {
        CREATE_NEW,
        FAIL;

    }
}

