/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.predicate.NumberRange;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class EntitySelector {
    public static final int MAX_VALUE = Integer.MAX_VALUE;
    public static final BiConsumer<Vec3d, List<? extends Entity>> ARBITRARY = (pos, entities) -> {};
    private static final TypeFilter<Entity, ?> PASSTHROUGH_FILTER = new TypeFilter<Entity, Entity>(){

        @Override
        public Entity downcast(Entity arg) {
            return arg;
        }

        @Override
        public Class<? extends Entity> getBaseClass() {
            return Entity.class;
        }
    };
    private final int limit;
    private final boolean includesNonPlayers;
    private final boolean localWorldOnly;
    private final Predicate<Entity> basePredicate;
    private final NumberRange.FloatRange distance;
    private final Function<Vec3d, Vec3d> positionOffset;
    @Nullable
    private final Box box;
    private final BiConsumer<Vec3d, List<? extends Entity>> sorter;
    private final boolean senderOnly;
    @Nullable
    private final String playerName;
    @Nullable
    private final UUID uuid;
    private final TypeFilter<Entity, ?> entityFilter;
    private final boolean usesAt;

    public EntitySelector(int count, boolean includesNonPlayers, boolean localWorldOnly, Predicate<Entity> basePredicate, NumberRange.FloatRange distance, Function<Vec3d, Vec3d> positionOffset, @Nullable Box box, BiConsumer<Vec3d, List<? extends Entity>> sorter, boolean senderOnly, @Nullable String playerName, @Nullable UUID uuid, @Nullable EntityType<?> type, boolean usesAt) {
        this.limit = count;
        this.includesNonPlayers = includesNonPlayers;
        this.localWorldOnly = localWorldOnly;
        this.basePredicate = basePredicate;
        this.distance = distance;
        this.positionOffset = positionOffset;
        this.box = box;
        this.sorter = sorter;
        this.senderOnly = senderOnly;
        this.playerName = playerName;
        this.uuid = uuid;
        this.entityFilter = type == null ? PASSTHROUGH_FILTER : type;
        this.usesAt = usesAt;
    }

    public int getLimit() {
        return this.limit;
    }

    public boolean includesNonPlayers() {
        return this.includesNonPlayers;
    }

    public boolean isSenderOnly() {
        return this.senderOnly;
    }

    public boolean isLocalWorldOnly() {
        return this.localWorldOnly;
    }

    public boolean usesAt() {
        return this.usesAt;
    }

    private void checkSourcePermission(ServerCommandSource source) throws CommandSyntaxException {
        if (this.usesAt && !source.hasPermissionLevel(2)) {
            throw EntityArgumentType.NOT_ALLOWED_EXCEPTION.create();
        }
    }

    public Entity getEntity(ServerCommandSource source) throws CommandSyntaxException {
        this.checkSourcePermission(source);
        List<? extends Entity> list = this.getEntities(source);
        if (list.isEmpty()) {
            throw EntityArgumentType.ENTITY_NOT_FOUND_EXCEPTION.create();
        }
        if (list.size() > 1) {
            throw EntityArgumentType.TOO_MANY_ENTITIES_EXCEPTION.create();
        }
        return list.get(0);
    }

    public List<? extends Entity> getEntities(ServerCommandSource source) throws CommandSyntaxException {
        return this.getUnfilteredEntities(source).stream().filter(entity -> entity.getType().isEnabled(source.getEnabledFeatures())).toList();
    }

    private List<? extends Entity> getUnfilteredEntities(ServerCommandSource source) throws CommandSyntaxException {
        this.checkSourcePermission(source);
        if (!this.includesNonPlayers) {
            return this.getPlayers(source);
        }
        if (this.playerName != null) {
            ServerPlayerEntity lv = source.getServer().getPlayerManager().getPlayer(this.playerName);
            if (lv == null) {
                return Collections.emptyList();
            }
            return Lists.newArrayList(lv);
        }
        if (this.uuid != null) {
            for (ServerWorld lv2 : source.getServer().getWorlds()) {
                Entity lv3 = lv2.getEntity(this.uuid);
                if (lv3 == null) continue;
                return Lists.newArrayList(lv3);
            }
            return Collections.emptyList();
        }
        Vec3d lv4 = this.positionOffset.apply(source.getPosition());
        Predicate<Entity> predicate = this.getPositionPredicate(lv4);
        if (this.senderOnly) {
            if (source.getEntity() != null && predicate.test(source.getEntity())) {
                return Lists.newArrayList(source.getEntity());
            }
            return Collections.emptyList();
        }
        ArrayList<Entity> list = Lists.newArrayList();
        if (this.isLocalWorldOnly()) {
            this.appendEntitiesFromWorld(list, source.getWorld(), lv4, predicate);
        } else {
            for (ServerWorld lv5 : source.getServer().getWorlds()) {
                this.appendEntitiesFromWorld(list, lv5, lv4, predicate);
            }
        }
        return this.getEntities(lv4, list);
    }

    private void appendEntitiesFromWorld(List<Entity> entities, ServerWorld world, Vec3d pos, Predicate<Entity> predicate) {
        int i = this.getAppendLimit();
        if (entities.size() >= i) {
            return;
        }
        if (this.box != null) {
            world.collectEntitiesByType(this.entityFilter, this.box.offset(pos), predicate, entities, i);
        } else {
            world.collectEntitiesByType(this.entityFilter, predicate, entities, i);
        }
    }

    private int getAppendLimit() {
        return this.sorter == ARBITRARY ? this.limit : Integer.MAX_VALUE;
    }

    public ServerPlayerEntity getPlayer(ServerCommandSource source) throws CommandSyntaxException {
        this.checkSourcePermission(source);
        List<ServerPlayerEntity> list = this.getPlayers(source);
        if (list.size() != 1) {
            throw EntityArgumentType.PLAYER_NOT_FOUND_EXCEPTION.create();
        }
        return list.get(0);
    }

    public List<ServerPlayerEntity> getPlayers(ServerCommandSource source) throws CommandSyntaxException {
        List<Object> list;
        this.checkSourcePermission(source);
        if (this.playerName != null) {
            ServerPlayerEntity lv = source.getServer().getPlayerManager().getPlayer(this.playerName);
            if (lv == null) {
                return Collections.emptyList();
            }
            return Lists.newArrayList(lv);
        }
        if (this.uuid != null) {
            ServerPlayerEntity lv = source.getServer().getPlayerManager().getPlayer(this.uuid);
            if (lv == null) {
                return Collections.emptyList();
            }
            return Lists.newArrayList(lv);
        }
        Vec3d lv2 = this.positionOffset.apply(source.getPosition());
        Predicate<Entity> predicate = this.getPositionPredicate(lv2);
        if (this.senderOnly) {
            ServerPlayerEntity lv3;
            if (source.getEntity() instanceof ServerPlayerEntity && predicate.test(lv3 = (ServerPlayerEntity)source.getEntity())) {
                return Lists.newArrayList(lv3);
            }
            return Collections.emptyList();
        }
        int i = this.getAppendLimit();
        if (this.isLocalWorldOnly()) {
            list = source.getWorld().getPlayers(predicate, i);
        } else {
            list = Lists.newArrayList();
            for (ServerPlayerEntity lv4 : source.getServer().getPlayerManager().getPlayerList()) {
                if (!predicate.test(lv4)) continue;
                list.add(lv4);
                if (list.size() < i) continue;
                return list;
            }
        }
        return this.getEntities(lv2, list);
    }

    private Predicate<Entity> getPositionPredicate(Vec3d pos) {
        Predicate<Entity> predicate = this.basePredicate;
        if (this.box != null) {
            Box lv = this.box.offset(pos);
            predicate = predicate.and(entity -> lv.intersects(entity.getBoundingBox()));
        }
        if (!this.distance.isDummy()) {
            predicate = predicate.and(entity -> this.distance.testSqrt(entity.squaredDistanceTo(pos)));
        }
        return predicate;
    }

    private <T extends Entity> List<T> getEntities(Vec3d pos, List<T> entities) {
        if (entities.size() > 1) {
            this.sorter.accept(pos, entities);
        }
        return entities.subList(0, Math.min(this.limit, entities.size()));
    }

    public static Text getNames(List<? extends Entity> entities) {
        return Texts.join(entities, Entity::getDisplayName);
    }
}

