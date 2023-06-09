/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.registry;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.item.trim.ArmorTrimMaterial;
import net.minecraft.item.trim.ArmorTrimPattern;
import net.minecraft.network.message.MessageType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.MutableRegistry;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.structure.StructureSet;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.processor.StructureProcessorList;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.FlatLevelGeneratorPreset;
import net.minecraft.world.gen.WorldPreset;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.structure.Structure;
import org.slf4j.Logger;

public class RegistryLoader {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final List<Entry<?>> DYNAMIC_REGISTRIES = List.of(new Entry<DimensionType>(RegistryKeys.DIMENSION_TYPE, DimensionType.CODEC), new Entry<Biome>(RegistryKeys.BIOME, Biome.CODEC), new Entry<MessageType>(RegistryKeys.MESSAGE_TYPE, MessageType.CODEC), new Entry(RegistryKeys.CONFIGURED_CARVER, ConfiguredCarver.CODEC), new Entry(RegistryKeys.CONFIGURED_FEATURE, ConfiguredFeature.CODEC), new Entry<PlacedFeature>(RegistryKeys.PLACED_FEATURE, PlacedFeature.CODEC), new Entry<Structure>(RegistryKeys.STRUCTURE, Structure.STRUCTURE_CODEC), new Entry<StructureSet>(RegistryKeys.STRUCTURE_SET, StructureSet.CODEC), new Entry<StructureProcessorList>(RegistryKeys.PROCESSOR_LIST, StructureProcessorType.PROCESSORS_CODEC), new Entry<StructurePool>(RegistryKeys.TEMPLATE_POOL, StructurePool.CODEC), new Entry<ChunkGeneratorSettings>(RegistryKeys.CHUNK_GENERATOR_SETTINGS, ChunkGeneratorSettings.CODEC), new Entry<DoublePerlinNoiseSampler.NoiseParameters>(RegistryKeys.NOISE_PARAMETERS, DoublePerlinNoiseSampler.NoiseParameters.CODEC), new Entry<DensityFunction>(RegistryKeys.DENSITY_FUNCTION, DensityFunction.CODEC), new Entry<WorldPreset>(RegistryKeys.WORLD_PRESET, WorldPreset.CODEC), new Entry<FlatLevelGeneratorPreset>(RegistryKeys.FLAT_LEVEL_GENERATOR_PRESET, FlatLevelGeneratorPreset.CODEC), new Entry<ArmorTrimPattern>(RegistryKeys.TRIM_PATTERN, ArmorTrimPattern.CODEC), new Entry<ArmorTrimMaterial>(RegistryKeys.TRIM_MATERIAL, ArmorTrimMaterial.CODEC), new Entry<DamageType>(RegistryKeys.DAMAGE_TYPE, DamageType.CODEC), new Entry<MultiNoiseBiomeSourceParameterList>(RegistryKeys.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST, MultiNoiseBiomeSourceParameterList.CODEC));
    public static final List<Entry<?>> DIMENSION_REGISTRIES = List.of(new Entry<DimensionOptions>(RegistryKeys.DIMENSION, DimensionOptions.CODEC));

    public static DynamicRegistryManager.Immutable load(ResourceManager resourceManager, DynamicRegistryManager baseRegistryManager, List<Entry<?>> entries) {
        HashMap map = new HashMap();
        List<Pair<MutableRegistry<?>, RegistryLoadable>> list2 = entries.stream().map(entry -> entry.getLoader(Lifecycle.stable(), map)).toList();
        RegistryOps.RegistryInfoGetter lv = RegistryLoader.createInfoGetter(baseRegistryManager, list2);
        list2.forEach(loader -> ((RegistryLoadable)loader.getSecond()).load(resourceManager, lv));
        list2.forEach(loader -> {
            Registry lv = (Registry)loader.getFirst();
            try {
                lv.freeze();
            }
            catch (Exception exception) {
                map.put(lv.getKey(), exception);
            }
        });
        if (!map.isEmpty()) {
            RegistryLoader.writeLoadingError(map);
            throw new IllegalStateException("Failed to load registries due to above errors");
        }
        return new DynamicRegistryManager.ImmutableImpl(list2.stream().map(Pair::getFirst).toList()).toImmutable();
    }

    private static RegistryOps.RegistryInfoGetter createInfoGetter(DynamicRegistryManager baseRegistryManager, List<Pair<MutableRegistry<?>, RegistryLoadable>> additionalRegistries) {
        final HashMap map = new HashMap();
        baseRegistryManager.streamAllRegistries().forEach(entry -> map.put(entry.key(), RegistryLoader.createInfo(entry.value())));
        additionalRegistries.forEach(pair -> map.put(((MutableRegistry)pair.getFirst()).getKey(), RegistryLoader.createInfo((MutableRegistry)pair.getFirst())));
        return new RegistryOps.RegistryInfoGetter(){

            @Override
            public <T> Optional<RegistryOps.RegistryInfo<T>> getRegistryInfo(RegistryKey<? extends Registry<? extends T>> registryRef) {
                return Optional.ofNullable((RegistryOps.RegistryInfo)map.get(registryRef));
            }
        };
    }

    private static <T> RegistryOps.RegistryInfo<T> createInfo(MutableRegistry<T> registry) {
        return new RegistryOps.RegistryInfo(registry.getReadOnlyWrapper(), registry.createMutableEntryLookup(), registry.getLifecycle());
    }

    private static <T> RegistryOps.RegistryInfo<T> createInfo(Registry<T> registry) {
        return new RegistryOps.RegistryInfo<T>(registry.getReadOnlyWrapper(), registry.getTagCreatingWrapper(), registry.getLifecycle());
    }

    private static void writeLoadingError(Map<RegistryKey<?>, Exception> exceptions) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        Map<Identifier, Map<Identifier, Exception>> map2 = exceptions.entrySet().stream().collect(Collectors.groupingBy(entry -> ((RegistryKey)entry.getKey()).getRegistry(), Collectors.toMap(entry -> ((RegistryKey)entry.getKey()).getValue(), Map.Entry::getValue)));
        map2.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
            printWriter.printf("> Errors in registry %s:%n", entry.getKey());
            ((Map)entry.getValue()).entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(elementEntry -> {
                printWriter.printf(">> Errors in element %s:%n", elementEntry.getKey());
                ((Exception)elementEntry.getValue()).printStackTrace(printWriter);
            });
        });
        printWriter.flush();
        LOGGER.error("Registry loading errors:\n{}", (Object)stringWriter);
    }

    private static String getPath(Identifier id) {
        return id.getPath();
    }

    static <E> void load(RegistryOps.RegistryInfoGetter registryInfoGetter, ResourceManager resourceManager, RegistryKey<? extends Registry<E>> registryRef, MutableRegistry<E> newRegistry, Decoder<E> decoder, Map<RegistryKey<?>, Exception> exceptions) {
        String string = RegistryLoader.getPath(registryRef.getValue());
        ResourceFinder lv = ResourceFinder.json(string);
        RegistryOps<JsonElement> lv2 = RegistryOps.of(JsonOps.INSTANCE, registryInfoGetter);
        for (Map.Entry<Identifier, Resource> entry : lv.findResources(resourceManager).entrySet()) {
            Identifier lv3 = entry.getKey();
            RegistryKey lv4 = RegistryKey.of(registryRef, lv.toResourceId(lv3));
            Resource lv5 = entry.getValue();
            try {
                BufferedReader reader = lv5.getReader();
                try {
                    JsonElement jsonElement = JsonParser.parseReader(reader);
                    DataResult<E> dataResult = decoder.parse(lv2, jsonElement);
                    E object = dataResult.getOrThrow(false, error -> {});
                    newRegistry.add(lv4, object, lv5.isAlwaysStable() ? Lifecycle.stable() : dataResult.lifecycle());
                }
                finally {
                    if (reader == null) continue;
                    ((Reader)reader).close();
                }
            }
            catch (Exception exception) {
                exceptions.put(lv4, new IllegalStateException(String.format(Locale.ROOT, "Failed to parse %s from pack %s", lv3, lv5.getResourcePackName()), exception));
            }
        }
    }

    static interface RegistryLoadable {
        public void load(ResourceManager var1, RegistryOps.RegistryInfoGetter var2);
    }

    public record Entry<T>(RegistryKey<? extends Registry<T>> key, Codec<T> elementCodec) {
        Pair<MutableRegistry<?>, RegistryLoadable> getLoader(Lifecycle lifecycle, Map<RegistryKey<?>, Exception> exceptions) {
            SimpleRegistry lv = new SimpleRegistry(this.key, lifecycle);
            RegistryLoadable lv2 = (resourceManager, registryInfoGetter) -> RegistryLoader.load(registryInfoGetter, resourceManager, this.key, lv, this.elementCodec, exceptions);
            return Pair.of(lv, lv2);
        }
    }
}

