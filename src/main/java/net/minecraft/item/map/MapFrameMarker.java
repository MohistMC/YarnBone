/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.item.map;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.math.BlockPos;

public class MapFrameMarker {
    private final BlockPos pos;
    private final int rotation;
    private final int entityId;

    public MapFrameMarker(BlockPos pos, int rotation, int entityId) {
        this.pos = pos;
        this.rotation = rotation;
        this.entityId = entityId;
    }

    public static MapFrameMarker fromNbt(NbtCompound nbt) {
        BlockPos lv = NbtHelper.toBlockPos(nbt.getCompound("Pos"));
        int i = nbt.getInt("Rotation");
        int j = nbt.getInt("EntityId");
        return new MapFrameMarker(lv, i, j);
    }

    public NbtCompound toNbt() {
        NbtCompound lv = new NbtCompound();
        lv.put("Pos", NbtHelper.fromBlockPos(this.pos));
        lv.putInt("Rotation", this.rotation);
        lv.putInt("EntityId", this.entityId);
        return lv;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public int getRotation() {
        return this.rotation;
    }

    public int getEntityId() {
        return this.entityId;
    }

    public String getKey() {
        return MapFrameMarker.getKey(this.pos);
    }

    public static String getKey(BlockPos pos) {
        return "frame-" + pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }
}

