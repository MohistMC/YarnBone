/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.datafixer;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import java.util.Set;
import net.minecraft.SharedConstants;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;

public enum DataFixTypes {
    LEVEL(TypeReferences.LEVEL),
    PLAYER(TypeReferences.PLAYER),
    CHUNK(TypeReferences.CHUNK),
    HOTBAR(TypeReferences.HOTBAR),
    OPTIONS(TypeReferences.OPTIONS),
    STRUCTURE(TypeReferences.STRUCTURE),
    STATS(TypeReferences.STATS),
    SAVED_DATA(TypeReferences.SAVED_DATA),
    ADVANCEMENTS(TypeReferences.ADVANCEMENTS),
    POI_CHUNK(TypeReferences.POI_CHUNK),
    WORLD_GEN_SETTINGS(TypeReferences.WORLD_GEN_SETTINGS),
    ENTITY_CHUNK(TypeReferences.ENTITY_CHUNK);

    public static final Set<DSL.TypeReference> REQUIRED_TYPES;
    private final DSL.TypeReference typeReference;

    private DataFixTypes(DSL.TypeReference typeReference) {
        this.typeReference = typeReference;
    }

    private static int getSaveVersionId() {
        return SharedConstants.getGameVersion().getSaveVersion().getId();
    }

    public <T> Dynamic<T> update(DataFixer dataFixer, Dynamic<T> dynamic, int oldVersion, int newVersion) {
        return dataFixer.update(this.typeReference, dynamic, oldVersion, newVersion);
    }

    public <T> Dynamic<T> update(DataFixer dataFixer, Dynamic<T> dynamic, int oldVersion) {
        return this.update(dataFixer, dynamic, oldVersion, DataFixTypes.getSaveVersionId());
    }

    public NbtCompound update(DataFixer dataFixer, NbtCompound nbt, int oldVersion, int newVersion) {
        return this.update(dataFixer, new Dynamic<NbtCompound>(NbtOps.INSTANCE, nbt), oldVersion, newVersion).getValue();
    }

    public NbtCompound update(DataFixer dataFixer, NbtCompound nbt, int oldVersion) {
        return this.update(dataFixer, nbt, oldVersion, DataFixTypes.getSaveVersionId());
    }

    static {
        REQUIRED_TYPES = Set.of(DataFixTypes.LEVEL.typeReference);
    }
}

