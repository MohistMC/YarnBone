/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world;

import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Map;
import net.minecraft.structure.StructureStart;
import net.minecraft.world.gen.structure.Structure;
import org.jetbrains.annotations.Nullable;

public interface StructureHolder {
    @Nullable
    public StructureStart getStructureStart(Structure var1);

    public void setStructureStart(Structure var1, StructureStart var2);

    public LongSet getStructureReferences(Structure var1);

    public void addStructureReference(Structure var1, long var2);

    public Map<Structure, LongSet> getStructureReferences();

    public void setStructureReferences(Map<Structure, LongSet> var1);
}

