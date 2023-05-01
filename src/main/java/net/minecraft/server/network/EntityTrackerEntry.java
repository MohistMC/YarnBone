/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.server.network;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TrackedPosition;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapState;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityAttachS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityAttributesS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySetHeadYawS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class EntityTrackerEntry {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int field_29767 = 1;
    private final ServerWorld world;
    private final Entity entity;
    private final int tickInterval;
    private final boolean alwaysUpdateVelocity;
    private final Consumer<Packet<?>> receiver;
    private final TrackedPosition trackedPos = new TrackedPosition();
    private int lastYaw;
    private int lastPitch;
    private int lastHeadPitch;
    private Vec3d velocity = Vec3d.ZERO;
    private int trackingTick;
    private int updatesWithoutVehicle;
    private List<Entity> lastPassengers = Collections.emptyList();
    private boolean hadVehicle;
    private boolean lastOnGround;
    @Nullable
    private List<DataTracker.SerializedEntry<?>> changedEntries;

    public EntityTrackerEntry(ServerWorld world, Entity entity, int tickInterval, boolean alwaysUpdateVelocity, Consumer<Packet<?>> receiver) {
        this.world = world;
        this.receiver = receiver;
        this.entity = entity;
        this.tickInterval = tickInterval;
        this.alwaysUpdateVelocity = alwaysUpdateVelocity;
        this.trackedPos.setPos(entity.getSyncedPos());
        this.lastYaw = MathHelper.floor(entity.getYaw() * 256.0f / 360.0f);
        this.lastPitch = MathHelper.floor(entity.getPitch() * 256.0f / 360.0f);
        this.lastHeadPitch = MathHelper.floor(entity.getHeadYaw() * 256.0f / 360.0f);
        this.lastOnGround = entity.isOnGround();
        this.changedEntries = entity.getDataTracker().getChangedEntries();
    }

    public void tick() {
        Entity entity;
        List<Entity> list = this.entity.getPassengerList();
        if (!list.equals(this.lastPassengers)) {
            this.receiver.accept(new EntityPassengersSetS2CPacket(this.entity));
            this.streamChangedPassengers(list, this.lastPassengers).forEach(passenger -> {
                ServerPlayerEntity lv;
                if (passenger instanceof ServerPlayerEntity && !list.contains(lv = (ServerPlayerEntity)passenger)) {
                    lv.networkHandler.requestTeleport(lv.getX(), lv.getY(), lv.getZ(), lv.getYaw(), lv.getPitch());
                }
            });
            this.lastPassengers = list;
        }
        if ((entity = this.entity) instanceof ItemFrameEntity) {
            ItemFrameEntity lv = (ItemFrameEntity)entity;
            if (this.trackingTick % 10 == 0) {
                Integer integer;
                MapState lv3;
                ItemStack lv2 = lv.getHeldItemStack();
                if (lv2.getItem() instanceof FilledMapItem && (lv3 = FilledMapItem.getMapState(integer = FilledMapItem.getMapId(lv2), (World)this.world)) != null) {
                    for (ServerPlayerEntity lv4 : this.world.getPlayers()) {
                        lv3.update(lv4, lv2);
                        Packet<?> lv5 = lv3.getPlayerMarkerPacket(integer, lv4);
                        if (lv5 == null) continue;
                        lv4.networkHandler.sendPacket(lv5);
                    }
                }
                this.syncEntityData();
            }
        }
        if (this.trackingTick % this.tickInterval == 0 || this.entity.velocityDirty || this.entity.getDataTracker().isDirty()) {
            int i;
            if (this.entity.hasVehicle()) {
                boolean bl;
                i = MathHelper.floor(this.entity.getYaw() * 256.0f / 360.0f);
                int j = MathHelper.floor(this.entity.getPitch() * 256.0f / 360.0f);
                boolean bl2 = bl = Math.abs(i - this.lastYaw) >= 1 || Math.abs(j - this.lastPitch) >= 1;
                if (bl) {
                    this.receiver.accept(new EntityS2CPacket.Rotate(this.entity.getId(), (byte)i, (byte)j, this.entity.isOnGround()));
                    this.lastYaw = i;
                    this.lastPitch = j;
                }
                this.trackedPos.setPos(this.entity.getSyncedPos());
                this.syncEntityData();
                this.hadVehicle = true;
            } else {
                Vec3d lv8;
                double d;
                ++this.updatesWithoutVehicle;
                i = MathHelper.floor(this.entity.getYaw() * 256.0f / 360.0f);
                int j = MathHelper.floor(this.entity.getPitch() * 256.0f / 360.0f);
                Vec3d lv6 = this.entity.getSyncedPos();
                boolean bl2 = this.trackedPos.subtract(lv6).lengthSquared() >= 7.62939453125E-6;
                Packet<ClientPlayPacketListener> lv7 = null;
                boolean bl3 = bl2 || this.trackingTick % 60 == 0;
                boolean bl4 = Math.abs(i - this.lastYaw) >= 1 || Math.abs(j - this.lastPitch) >= 1;
                boolean bl5 = false;
                boolean bl6 = false;
                if (this.trackingTick > 0 || this.entity instanceof PersistentProjectileEntity) {
                    boolean bl7;
                    long l = this.trackedPos.getDeltaX(lv6);
                    long m = this.trackedPos.getDeltaY(lv6);
                    long n = this.trackedPos.getDeltaZ(lv6);
                    boolean bl = bl7 = l < -32768L || l > 32767L || m < -32768L || m > 32767L || n < -32768L || n > 32767L;
                    if (bl7 || this.updatesWithoutVehicle > 400 || this.hadVehicle || this.lastOnGround != this.entity.isOnGround()) {
                        this.lastOnGround = this.entity.isOnGround();
                        this.updatesWithoutVehicle = 0;
                        lv7 = new EntityPositionS2CPacket(this.entity);
                        bl5 = true;
                        bl6 = true;
                    } else if (bl3 && bl4 || this.entity instanceof PersistentProjectileEntity) {
                        lv7 = new EntityS2CPacket.RotateAndMoveRelative(this.entity.getId(), (short)l, (short)m, (short)n, (byte)i, (byte)j, this.entity.isOnGround());
                        bl5 = true;
                        bl6 = true;
                    } else if (bl3) {
                        lv7 = new EntityS2CPacket.MoveRelative(this.entity.getId(), (short)l, (short)m, (short)n, this.entity.isOnGround());
                        bl5 = true;
                    } else if (bl4) {
                        lv7 = new EntityS2CPacket.Rotate(this.entity.getId(), (byte)i, (byte)j, this.entity.isOnGround());
                        bl6 = true;
                    }
                }
                if ((this.alwaysUpdateVelocity || this.entity.velocityDirty || this.entity instanceof LivingEntity && ((LivingEntity)this.entity).isFallFlying()) && this.trackingTick > 0 && ((d = (lv8 = this.entity.getVelocity()).squaredDistanceTo(this.velocity)) > 1.0E-7 || d > 0.0 && lv8.lengthSquared() == 0.0)) {
                    this.velocity = lv8;
                    this.receiver.accept(new EntityVelocityUpdateS2CPacket(this.entity.getId(), this.velocity));
                }
                if (lv7 != null) {
                    this.receiver.accept(lv7);
                }
                this.syncEntityData();
                if (bl5) {
                    this.trackedPos.setPos(lv6);
                }
                if (bl6) {
                    this.lastYaw = i;
                    this.lastPitch = j;
                }
                this.hadVehicle = false;
            }
            i = MathHelper.floor(this.entity.getHeadYaw() * 256.0f / 360.0f);
            if (Math.abs(i - this.lastHeadPitch) >= 1) {
                this.receiver.accept(new EntitySetHeadYawS2CPacket(this.entity, (byte)i));
                this.lastHeadPitch = i;
            }
            this.entity.velocityDirty = false;
        }
        ++this.trackingTick;
        if (this.entity.velocityModified) {
            this.sendSyncPacket(new EntityVelocityUpdateS2CPacket(this.entity));
            this.entity.velocityModified = false;
        }
    }

    private Stream<Entity> streamChangedPassengers(List<Entity> passengers, List<Entity> lastPassengers) {
        return Stream.concat(lastPassengers.stream().filter(passenger -> !passengers.contains(passenger)), passengers.stream().filter(passenger -> !lastPassengers.contains(passenger)));
    }

    public void stopTracking(ServerPlayerEntity player) {
        this.entity.onStoppedTrackingBy(player);
        player.networkHandler.sendPacket(new EntitiesDestroyS2CPacket(this.entity.getId()));
    }

    public void startTracking(ServerPlayerEntity player) {
        ArrayList<Packet<ClientPlayPacketListener>> list = new ArrayList<Packet<ClientPlayPacketListener>>();
        this.sendPackets(list::add);
        player.networkHandler.sendPacket(new BundleS2CPacket((Iterable<Packet<ClientPlayPacketListener>>)list));
        this.entity.onStartedTrackingBy(player);
    }

    public void sendPackets(Consumer<Packet<ClientPlayPacketListener>> sender) {
        MobEntity lv6;
        if (this.entity.isRemoved()) {
            LOGGER.warn("Fetching packet for removed entity {}", (Object)this.entity);
        }
        Packet<ClientPlayPacketListener> lv = this.entity.createSpawnPacket();
        this.lastHeadPitch = MathHelper.floor(this.entity.getHeadYaw() * 256.0f / 360.0f);
        sender.accept(lv);
        if (this.changedEntries != null) {
            sender.accept(new EntityTrackerUpdateS2CPacket(this.entity.getId(), this.changedEntries));
        }
        boolean bl = this.alwaysUpdateVelocity;
        if (this.entity instanceof LivingEntity) {
            Collection<EntityAttributeInstance> collection = ((LivingEntity)this.entity).getAttributes().getAttributesToSend();
            if (!collection.isEmpty()) {
                sender.accept(new EntityAttributesS2CPacket(this.entity.getId(), collection));
            }
            if (((LivingEntity)this.entity).isFallFlying()) {
                bl = true;
            }
        }
        this.velocity = this.entity.getVelocity();
        if (bl && !(this.entity instanceof LivingEntity)) {
            sender.accept(new EntityVelocityUpdateS2CPacket(this.entity.getId(), this.velocity));
        }
        if (this.entity instanceof LivingEntity) {
            ArrayList<Pair<EquipmentSlot, ItemStack>> list = Lists.newArrayList();
            for (EquipmentSlot lv2 : EquipmentSlot.values()) {
                ItemStack lv3 = ((LivingEntity)this.entity).getEquippedStack(lv2);
                if (lv3.isEmpty()) continue;
                list.add(Pair.of(lv2, lv3.copy()));
            }
            if (!list.isEmpty()) {
                sender.accept(new EntityEquipmentUpdateS2CPacket(this.entity.getId(), list));
            }
        }
        if (this.entity instanceof LivingEntity) {
            LivingEntity lv4 = (LivingEntity)this.entity;
            for (StatusEffectInstance lv5 : lv4.getStatusEffects()) {
                sender.accept(new EntityStatusEffectS2CPacket(this.entity.getId(), lv5));
            }
        }
        if (!this.entity.getPassengerList().isEmpty()) {
            sender.accept(new EntityPassengersSetS2CPacket(this.entity));
        }
        if (this.entity.hasVehicle()) {
            sender.accept(new EntityPassengersSetS2CPacket(this.entity.getVehicle()));
        }
        if (this.entity instanceof MobEntity && (lv6 = (MobEntity)this.entity).isLeashed()) {
            sender.accept(new EntityAttachS2CPacket(lv6, lv6.getHoldingEntity()));
        }
    }

    private void syncEntityData() {
        DataTracker lv = this.entity.getDataTracker();
        List<DataTracker.SerializedEntry<?>> list = lv.getDirtyEntries();
        if (list != null) {
            this.changedEntries = lv.getChangedEntries();
            this.sendSyncPacket(new EntityTrackerUpdateS2CPacket(this.entity.getId(), list));
        }
        if (this.entity instanceof LivingEntity) {
            Set<EntityAttributeInstance> set = ((LivingEntity)this.entity).getAttributes().getTracked();
            if (!set.isEmpty()) {
                this.sendSyncPacket(new EntityAttributesS2CPacket(this.entity.getId(), set));
            }
            set.clear();
        }
    }

    private void sendSyncPacket(Packet<?> packet) {
        this.receiver.accept(packet);
        if (this.entity instanceof ServerPlayerEntity) {
            ((ServerPlayerEntity)this.entity).networkHandler.sendPacket(packet);
        }
    }
}

