/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.item.map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapBannerMarker;
import net.minecraft.item.map.MapFrameMarker;
import net.minecraft.item.map.MapIcon;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.MapUpdateS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class MapState
extends PersistentState {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int field_31832 = 128;
    private static final int field_31833 = 64;
    public static final int MAX_SCALE = 4;
    public static final int MAX_ICONS = 256;
    public final int centerX;
    public final int centerZ;
    public final RegistryKey<World> dimension;
    private final boolean showIcons;
    private final boolean unlimitedTracking;
    public final byte scale;
    public byte[] colors = new byte[16384];
    public final boolean locked;
    private final List<PlayerUpdateTracker> updateTrackers = Lists.newArrayList();
    private final Map<PlayerEntity, PlayerUpdateTracker> updateTrackersByPlayer = Maps.newHashMap();
    private final Map<String, MapBannerMarker> banners = Maps.newHashMap();
    final Map<String, MapIcon> icons = Maps.newLinkedHashMap();
    private final Map<String, MapFrameMarker> frames = Maps.newHashMap();
    private int iconCount;

    private MapState(int centerX, int centerZ, byte scale, boolean showIcons, boolean unlimitedTracking, boolean locked, RegistryKey<World> dimension) {
        this.scale = scale;
        this.centerX = centerX;
        this.centerZ = centerZ;
        this.dimension = dimension;
        this.showIcons = showIcons;
        this.unlimitedTracking = unlimitedTracking;
        this.locked = locked;
        this.markDirty();
    }

    public static MapState of(double centerX, double centerZ, byte scale, boolean showIcons, boolean unlimitedTracking, RegistryKey<World> dimension) {
        int i = 128 * (1 << scale);
        int j = MathHelper.floor((centerX + 64.0) / (double)i);
        int k = MathHelper.floor((centerZ + 64.0) / (double)i);
        int l = j * i + i / 2 - 64;
        int m = k * i + i / 2 - 64;
        return new MapState(l, m, scale, showIcons, unlimitedTracking, false, dimension);
    }

    public static MapState of(byte scale, boolean locked, RegistryKey<World> dimension) {
        return new MapState(0, 0, scale, false, false, locked, dimension);
    }

    public static MapState fromNbt(NbtCompound nbt) {
        RegistryKey<World> lv = DimensionType.worldFromDimensionNbt(new Dynamic<NbtElement>(NbtOps.INSTANCE, nbt.get("dimension"))).resultOrPartial(LOGGER::error).orElseThrow(() -> new IllegalArgumentException("Invalid map dimension: " + nbt.get("dimension")));
        int i = nbt.getInt("xCenter");
        int j = nbt.getInt("zCenter");
        byte b = (byte)MathHelper.clamp(nbt.getByte("scale"), 0, 4);
        boolean bl = !nbt.contains("trackingPosition", NbtElement.BYTE_TYPE) || nbt.getBoolean("trackingPosition");
        boolean bl2 = nbt.getBoolean("unlimitedTracking");
        boolean bl3 = nbt.getBoolean("locked");
        MapState lv2 = new MapState(i, j, b, bl, bl2, bl3, lv);
        byte[] bs = nbt.getByteArray("colors");
        if (bs.length == 16384) {
            lv2.colors = bs;
        }
        NbtList lv3 = nbt.getList("banners", NbtElement.COMPOUND_TYPE);
        for (int k = 0; k < lv3.size(); ++k) {
            MapBannerMarker lv4 = MapBannerMarker.fromNbt(lv3.getCompound(k));
            lv2.banners.put(lv4.getKey(), lv4);
            lv2.addIcon(lv4.getIconType(), null, lv4.getKey(), lv4.getPos().getX(), lv4.getPos().getZ(), 180.0, lv4.getName());
        }
        NbtList lv5 = nbt.getList("frames", NbtElement.COMPOUND_TYPE);
        for (int l = 0; l < lv5.size(); ++l) {
            MapFrameMarker lv6 = MapFrameMarker.fromNbt(lv5.getCompound(l));
            lv2.frames.put(lv6.getKey(), lv6);
            lv2.addIcon(MapIcon.Type.FRAME, null, "frame-" + lv6.getEntityId(), lv6.getPos().getX(), lv6.getPos().getZ(), lv6.getRotation(), null);
        }
        return lv2;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        Identifier.CODEC.encodeStart(NbtOps.INSTANCE, this.dimension.getValue()).resultOrPartial(LOGGER::error).ifPresent(arg2 -> nbt.put("dimension", (NbtElement)arg2));
        nbt.putInt("xCenter", this.centerX);
        nbt.putInt("zCenter", this.centerZ);
        nbt.putByte("scale", this.scale);
        nbt.putByteArray("colors", this.colors);
        nbt.putBoolean("trackingPosition", this.showIcons);
        nbt.putBoolean("unlimitedTracking", this.unlimitedTracking);
        nbt.putBoolean("locked", this.locked);
        NbtList lv = new NbtList();
        for (MapBannerMarker lv2 : this.banners.values()) {
            lv.add(lv2.getNbt());
        }
        nbt.put("banners", lv);
        NbtList lv3 = new NbtList();
        for (MapFrameMarker lv4 : this.frames.values()) {
            lv3.add(lv4.toNbt());
        }
        nbt.put("frames", lv3);
        return nbt;
    }

    public MapState copy() {
        MapState lv = new MapState(this.centerX, this.centerZ, this.scale, this.showIcons, this.unlimitedTracking, true, this.dimension);
        lv.banners.putAll(this.banners);
        lv.icons.putAll(this.icons);
        lv.iconCount = this.iconCount;
        System.arraycopy(this.colors, 0, lv.colors, 0, this.colors.length);
        lv.markDirty();
        return lv;
    }

    public MapState zoomOut(int zoomOutScale) {
        return MapState.of(this.centerX, this.centerZ, (byte)MathHelper.clamp(this.scale + zoomOutScale, 0, 4), this.showIcons, this.unlimitedTracking, this.dimension);
    }

    public void update(PlayerEntity player, ItemStack stack) {
        NbtCompound lv7;
        if (!this.updateTrackersByPlayer.containsKey(player)) {
            PlayerUpdateTracker lv = new PlayerUpdateTracker(player);
            this.updateTrackersByPlayer.put(player, lv);
            this.updateTrackers.add(lv);
        }
        if (!player.getInventory().contains(stack)) {
            this.removeIcon(player.getName().getString());
        }
        for (int i = 0; i < this.updateTrackers.size(); ++i) {
            PlayerUpdateTracker lv2 = this.updateTrackers.get(i);
            String string = lv2.player.getName().getString();
            if (lv2.player.isRemoved() || !lv2.player.getInventory().contains(stack) && !stack.isInFrame()) {
                this.updateTrackersByPlayer.remove(lv2.player);
                this.updateTrackers.remove(lv2);
                this.removeIcon(string);
                continue;
            }
            if (stack.isInFrame() || lv2.player.world.getRegistryKey() != this.dimension || !this.showIcons) continue;
            this.addIcon(MapIcon.Type.PLAYER, lv2.player.world, string, lv2.player.getX(), lv2.player.getZ(), lv2.player.getYaw(), null);
        }
        if (stack.isInFrame() && this.showIcons) {
            ItemFrameEntity lv3 = stack.getFrame();
            BlockPos lv4 = lv3.getDecorationBlockPos();
            MapFrameMarker lv5 = this.frames.get(MapFrameMarker.getKey(lv4));
            if (lv5 != null && lv3.getId() != lv5.getEntityId() && this.frames.containsKey(lv5.getKey())) {
                this.removeIcon("frame-" + lv5.getEntityId());
            }
            MapFrameMarker lv6 = new MapFrameMarker(lv4, lv3.getHorizontalFacing().getHorizontal() * 90, lv3.getId());
            this.addIcon(MapIcon.Type.FRAME, player.world, "frame-" + lv3.getId(), lv4.getX(), lv4.getZ(), lv3.getHorizontalFacing().getHorizontal() * 90, null);
            this.frames.put(lv6.getKey(), lv6);
        }
        if ((lv7 = stack.getNbt()) != null && lv7.contains("Decorations", NbtElement.LIST_TYPE)) {
            NbtList lv8 = lv7.getList("Decorations", NbtElement.COMPOUND_TYPE);
            for (int j = 0; j < lv8.size(); ++j) {
                NbtCompound lv9 = lv8.getCompound(j);
                if (this.icons.containsKey(lv9.getString("id"))) continue;
                this.addIcon(MapIcon.Type.byId(lv9.getByte("type")), player.world, lv9.getString("id"), lv9.getDouble("x"), lv9.getDouble("z"), lv9.getDouble("rot"), null);
            }
        }
    }

    private void removeIcon(String id) {
        MapIcon lv = this.icons.remove(id);
        if (lv != null && lv.getType().shouldUseIconCountLimit()) {
            --this.iconCount;
        }
        this.markIconsDirty();
    }

    public static void addDecorationsNbt(ItemStack stack, BlockPos pos, String id, MapIcon.Type type) {
        NbtList lv;
        if (stack.hasNbt() && stack.getNbt().contains("Decorations", NbtElement.LIST_TYPE)) {
            lv = stack.getNbt().getList("Decorations", NbtElement.COMPOUND_TYPE);
        } else {
            lv = new NbtList();
            stack.setSubNbt("Decorations", lv);
        }
        NbtCompound lv2 = new NbtCompound();
        lv2.putByte("type", type.getId());
        lv2.putString("id", id);
        lv2.putDouble("x", pos.getX());
        lv2.putDouble("z", pos.getZ());
        lv2.putDouble("rot", 180.0);
        lv.add(lv2);
        if (type.hasTintColor()) {
            NbtCompound lv3 = stack.getOrCreateSubNbt("display");
            lv3.putInt("MapColor", type.getTintColor());
        }
    }

    private void addIcon(MapIcon.Type type, @Nullable WorldAccess world, String key, double x, double z, double rotation, @Nullable Text text) {
        MapIcon lv2;
        MapIcon lv;
        byte k;
        int i = 1 << this.scale;
        float g = (float)(x - (double)this.centerX) / (float)i;
        float h = (float)(z - (double)this.centerZ) / (float)i;
        byte b = (byte)((double)(g * 2.0f) + 0.5);
        byte c = (byte)((double)(h * 2.0f) + 0.5);
        int j = 63;
        if (g >= -63.0f && h >= -63.0f && g <= 63.0f && h <= 63.0f) {
            k = (byte)((rotation += rotation < 0.0 ? -8.0 : 8.0) * 16.0 / 360.0);
            if (this.dimension == World.NETHER && world != null) {
                l = (int)(world.getLevelProperties().getTimeOfDay() / 10L);
                k = (byte)(l * l * 34187121 + l * 121 >> 15 & 0xF);
            }
        } else if (type == MapIcon.Type.PLAYER) {
            l = 320;
            if (Math.abs(g) < 320.0f && Math.abs(h) < 320.0f) {
                type = MapIcon.Type.PLAYER_OFF_MAP;
            } else if (this.unlimitedTracking) {
                type = MapIcon.Type.PLAYER_OFF_LIMITS;
            } else {
                this.removeIcon(key);
                return;
            }
            k = 0;
            if (g <= -63.0f) {
                b = -128;
            }
            if (h <= -63.0f) {
                c = -128;
            }
            if (g >= 63.0f) {
                b = 127;
            }
            if (h >= 63.0f) {
                c = 127;
            }
        } else {
            this.removeIcon(key);
            return;
        }
        if (!(lv = new MapIcon(type, b, c, k, text)).equals(lv2 = this.icons.put(key, lv))) {
            if (lv2 != null && lv2.getType().shouldUseIconCountLimit()) {
                --this.iconCount;
            }
            if (type.shouldUseIconCountLimit()) {
                ++this.iconCount;
            }
            this.markIconsDirty();
        }
    }

    @Nullable
    public Packet<?> getPlayerMarkerPacket(int id, PlayerEntity player) {
        PlayerUpdateTracker lv = this.updateTrackersByPlayer.get(player);
        if (lv == null) {
            return null;
        }
        return lv.getPacket(id);
    }

    private void markDirty(int x, int z) {
        this.markDirty();
        for (PlayerUpdateTracker lv : this.updateTrackers) {
            lv.markDirty(x, z);
        }
    }

    private void markIconsDirty() {
        this.markDirty();
        this.updateTrackers.forEach(PlayerUpdateTracker::markIconsDirty);
    }

    public PlayerUpdateTracker getPlayerSyncData(PlayerEntity player) {
        PlayerUpdateTracker lv = this.updateTrackersByPlayer.get(player);
        if (lv == null) {
            lv = new PlayerUpdateTracker(player);
            this.updateTrackersByPlayer.put(player, lv);
            this.updateTrackers.add(lv);
        }
        return lv;
    }

    public boolean addBanner(WorldAccess world, BlockPos pos) {
        double d = (double)pos.getX() + 0.5;
        double e = (double)pos.getZ() + 0.5;
        int i = 1 << this.scale;
        double f = (d - (double)this.centerX) / (double)i;
        double g = (e - (double)this.centerZ) / (double)i;
        int j = 63;
        if (f >= -63.0 && g >= -63.0 && f <= 63.0 && g <= 63.0) {
            MapBannerMarker lv = MapBannerMarker.fromWorldBlock(world, pos);
            if (lv == null) {
                return false;
            }
            if (this.banners.remove(lv.getKey(), lv)) {
                this.removeIcon(lv.getKey());
                return true;
            }
            if (!this.iconCountNotLessThan(256)) {
                this.banners.put(lv.getKey(), lv);
                this.addIcon(lv.getIconType(), world, lv.getKey(), d, e, 180.0, lv.getName());
                return true;
            }
        }
        return false;
    }

    public void removeBanner(BlockView world, int x, int z) {
        Iterator<MapBannerMarker> iterator = this.banners.values().iterator();
        while (iterator.hasNext()) {
            MapBannerMarker lv2;
            MapBannerMarker lv = iterator.next();
            if (lv.getPos().getX() != x || lv.getPos().getZ() != z || lv.equals(lv2 = MapBannerMarker.fromWorldBlock(world, lv.getPos()))) continue;
            iterator.remove();
            this.removeIcon(lv.getKey());
        }
    }

    public Collection<MapBannerMarker> getBanners() {
        return this.banners.values();
    }

    public void removeFrame(BlockPos pos, int id) {
        this.removeIcon("frame-" + id);
        this.frames.remove(MapFrameMarker.getKey(pos));
    }

    public boolean putColor(int x, int z, byte color) {
        byte c = this.colors[x + z * 128];
        if (c != color) {
            this.setColor(x, z, color);
            return true;
        }
        return false;
    }

    public void setColor(int x, int z, byte color) {
        this.colors[x + z * 128] = color;
        this.markDirty(x, z);
    }

    public boolean hasMonumentIcon() {
        for (MapIcon lv : this.icons.values()) {
            if (lv.getType() != MapIcon.Type.MANSION && lv.getType() != MapIcon.Type.MONUMENT) continue;
            return true;
        }
        return false;
    }

    public void replaceIcons(List<MapIcon> icons) {
        this.icons.clear();
        this.iconCount = 0;
        for (int i = 0; i < icons.size(); ++i) {
            MapIcon lv = icons.get(i);
            this.icons.put("icon-" + i, lv);
            if (!lv.getType().shouldUseIconCountLimit()) continue;
            ++this.iconCount;
        }
    }

    public Iterable<MapIcon> getIcons() {
        return this.icons.values();
    }

    public boolean iconCountNotLessThan(int iconCount) {
        return this.iconCount >= iconCount;
    }

    public class PlayerUpdateTracker {
        public final PlayerEntity player;
        private boolean dirty = true;
        private int startX;
        private int startZ;
        private int endX = 127;
        private int endZ = 127;
        private boolean iconsDirty = true;
        private int emptyPacketsRequested;
        public int field_131;

        PlayerUpdateTracker(PlayerEntity player) {
            this.player = player;
        }

        private UpdateData getMapUpdateData() {
            int i = this.startX;
            int j = this.startZ;
            int k = this.endX + 1 - this.startX;
            int l = this.endZ + 1 - this.startZ;
            byte[] bs = new byte[k * l];
            for (int m = 0; m < k; ++m) {
                for (int n = 0; n < l; ++n) {
                    bs[m + n * k] = MapState.this.colors[i + m + (j + n) * 128];
                }
            }
            return new UpdateData(i, j, k, l, bs);
        }

        @Nullable
        Packet<?> getPacket(int mapId) {
            Collection<MapIcon> collection;
            UpdateData lv;
            if (this.dirty) {
                this.dirty = false;
                lv = this.getMapUpdateData();
            } else {
                lv = null;
            }
            if (this.iconsDirty && this.emptyPacketsRequested++ % 5 == 0) {
                this.iconsDirty = false;
                collection = MapState.this.icons.values();
            } else {
                collection = null;
            }
            if (collection != null || lv != null) {
                return new MapUpdateS2CPacket(mapId, MapState.this.scale, MapState.this.locked, collection, lv);
            }
            return null;
        }

        void markDirty(int startX, int startZ) {
            if (this.dirty) {
                this.startX = Math.min(this.startX, startX);
                this.startZ = Math.min(this.startZ, startZ);
                this.endX = Math.max(this.endX, startX);
                this.endZ = Math.max(this.endZ, startZ);
            } else {
                this.dirty = true;
                this.startX = startX;
                this.startZ = startZ;
                this.endX = startX;
                this.endZ = startZ;
            }
        }

        private void markIconsDirty() {
            this.iconsDirty = true;
        }
    }

    public static class UpdateData {
        public final int startX;
        public final int startZ;
        public final int width;
        public final int height;
        public final byte[] colors;

        public UpdateData(int startX, int startZ, int width, int height, byte[] colors) {
            this.startX = startX;
            this.startZ = startZ;
            this.width = width;
            this.height = height;
            this.colors = colors;
        }

        public void setColorsTo(MapState mapState) {
            for (int i = 0; i < this.width; ++i) {
                for (int j = 0; j < this.height; ++j) {
                    mapState.setColor(this.startX + i, this.startZ + j, this.colors[i + j * this.width]);
                }
            }
        }
    }
}

