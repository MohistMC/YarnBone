/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.world;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DataPool;
import net.minecraft.util.dynamic.Range;

public record MobSpawnerEntry(NbtCompound entity, Optional<CustomSpawnRules> customSpawnRules) {
    public static final String ENTITY_KEY = "entity";
    public static final Codec<MobSpawnerEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)NbtCompound.CODEC.fieldOf(ENTITY_KEY)).forGetter(entry -> entry.entity), CustomSpawnRules.CODEC.optionalFieldOf("custom_spawn_rules").forGetter(entry -> entry.customSpawnRules)).apply((Applicative<MobSpawnerEntry, ?>)instance, MobSpawnerEntry::new));
    public static final Codec<DataPool<MobSpawnerEntry>> DATA_POOL_CODEC = DataPool.createEmptyAllowedCodec(CODEC);

    public MobSpawnerEntry() {
        this(new NbtCompound(), Optional.empty());
    }

    public MobSpawnerEntry {
        if (arg.contains("id")) {
            Identifier lv = Identifier.tryParse(arg.getString("id"));
            if (lv != null) {
                arg.putString("id", lv.toString());
            } else {
                arg.remove("id");
            }
        }
    }

    public NbtCompound getNbt() {
        return this.entity;
    }

    public Optional<CustomSpawnRules> getCustomSpawnRules() {
        return this.customSpawnRules;
    }

    public record CustomSpawnRules(Range<Integer> blockLightLimit, Range<Integer> skyLightLimit) {
        private static final Range<Integer> DEFAULT = new Range<Integer>(0, 15);
        public static final Codec<CustomSpawnRules> CODEC = RecordCodecBuilder.create(instance -> instance.group(Range.CODEC.optionalFieldOf("block_light_limit", DEFAULT).flatXmap(CustomSpawnRules::validate, CustomSpawnRules::validate).forGetter(rules -> rules.blockLightLimit), Range.CODEC.optionalFieldOf("sky_light_limit", DEFAULT).flatXmap(CustomSpawnRules::validate, CustomSpawnRules::validate).forGetter(rules -> rules.skyLightLimit)).apply((Applicative<CustomSpawnRules, ?>)instance, CustomSpawnRules::new));

        private static DataResult<Range<Integer>> validate(Range<Integer> provider) {
            if (!DEFAULT.contains(provider)) {
                return DataResult.error(() -> "Light values must be withing range " + DEFAULT);
            }
            return DataResult.success(provider);
        }
    }
}

