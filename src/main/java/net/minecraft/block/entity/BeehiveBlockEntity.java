/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.block.entity;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.FireBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.annotation.Debug;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class BeehiveBlockEntity
extends BlockEntity {
    public static final String FLOWER_POS_KEY = "FlowerPos";
    public static final String MIN_OCCUPATION_TICKS_KEY = "MinOccupationTicks";
    public static final String ENTITY_DATA_KEY = "EntityData";
    public static final String TICKS_IN_HIVE_KEY = "TicksInHive";
    public static final String HAS_NECTAR_KEY = "HasNectar";
    public static final String BEES_KEY = "Bees";
    private static final List<String> IRRELEVANT_BEE_NBT_KEYS = Arrays.asList("Air", "ArmorDropChances", "ArmorItems", "Brain", "CanPickUpLoot", "DeathTime", "FallDistance", "FallFlying", "Fire", "HandDropChances", "HandItems", "HurtByTimestamp", "HurtTime", "LeftHanded", "Motion", "NoGravity", "OnGround", "PortalCooldown", "Pos", "Rotation", "CannotEnterHiveTicks", "TicksSincePollination", "CropsGrownSincePollination", "HivePos", "Passengers", "Leash", "UUID");
    public static final int MAX_BEE_COUNT = 3;
    private static final int ANGERED_CANNOT_ENTER_HIVE_TICKS = 400;
    private static final int MIN_OCCUPATION_TICKS_WITH_NECTAR = 2400;
    public static final int MIN_OCCUPATION_TICKS_WITHOUT_NECTAR = 600;
    private final List<Bee> bees = Lists.newArrayList();
    @Nullable
    private BlockPos flowerPos;

    public BeehiveBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityType.BEEHIVE, pos, state);
    }

    @Override
    public void markDirty() {
        if (this.isNearFire()) {
            this.angerBees(null, this.world.getBlockState(this.getPos()), BeeState.EMERGENCY);
        }
        super.markDirty();
    }

    public boolean isNearFire() {
        if (this.world == null) {
            return false;
        }
        for (BlockPos lv : BlockPos.iterate(this.pos.add(-1, -1, -1), this.pos.add(1, 1, 1))) {
            if (!(this.world.getBlockState(lv).getBlock() instanceof FireBlock)) continue;
            return true;
        }
        return false;
    }

    public boolean hasNoBees() {
        return this.bees.isEmpty();
    }

    public boolean isFullOfBees() {
        return this.bees.size() == 3;
    }

    public void angerBees(@Nullable PlayerEntity player, BlockState state, BeeState beeState) {
        List<Entity> list = this.tryReleaseBee(state, beeState);
        if (player != null) {
            for (Entity lv : list) {
                if (!(lv instanceof BeeEntity)) continue;
                BeeEntity lv2 = (BeeEntity)lv;
                if (!(player.getPos().squaredDistanceTo(lv.getPos()) <= 16.0)) continue;
                if (!this.isSmoked()) {
                    lv2.setTarget(player);
                    continue;
                }
                lv2.setCannotEnterHiveTicks(400);
            }
        }
    }

    private List<Entity> tryReleaseBee(BlockState state, BeeState beeState) {
        ArrayList<Entity> list = Lists.newArrayList();
        this.bees.removeIf(bee -> BeehiveBlockEntity.releaseBee(this.world, this.pos, state, bee, list, beeState, this.flowerPos));
        if (!list.isEmpty()) {
            super.markDirty();
        }
        return list;
    }

    public void tryEnterHive(Entity entity, boolean hasNectar) {
        this.tryEnterHive(entity, hasNectar, 0);
    }

    @Debug
    public int getBeeCount() {
        return this.bees.size();
    }

    public static int getHoneyLevel(BlockState state) {
        return state.get(BeehiveBlock.HONEY_LEVEL);
    }

    @Debug
    public boolean isSmoked() {
        return CampfireBlock.isLitCampfireInRange(this.world, this.getPos());
    }

    public void tryEnterHive(Entity entity, boolean hasNectar, int ticksInHive) {
        if (this.bees.size() >= 3) {
            return;
        }
        entity.stopRiding();
        entity.removeAllPassengers();
        NbtCompound lv = new NbtCompound();
        entity.saveNbt(lv);
        this.addBee(lv, ticksInHive, hasNectar);
        if (this.world != null) {
            BeeEntity lv2;
            if (entity instanceof BeeEntity && (lv2 = (BeeEntity)entity).hasFlower() && (!this.hasFlowerPos() || this.world.random.nextBoolean())) {
                this.flowerPos = lv2.getFlowerPos();
            }
            BlockPos lv3 = this.getPos();
            this.world.playSound(null, (double)lv3.getX(), (double)lv3.getY(), lv3.getZ(), SoundEvents.BLOCK_BEEHIVE_ENTER, SoundCategory.BLOCKS, 1.0f, 1.0f);
            this.world.emitGameEvent(GameEvent.BLOCK_CHANGE, lv3, GameEvent.Emitter.of(entity, this.getCachedState()));
        }
        entity.discard();
        super.markDirty();
    }

    public void addBee(NbtCompound nbtCompound, int ticksInHive, boolean hasNectar) {
        this.bees.add(new Bee(nbtCompound, ticksInHive, hasNectar ? 2400 : 600));
    }

    private static boolean releaseBee(World world, BlockPos pos, BlockState state2, Bee bee, @Nullable List<Entity> entities, BeeState beeState, @Nullable BlockPos flowerPos) {
        boolean bl;
        if ((world.isNight() || world.isRaining()) && beeState != BeeState.EMERGENCY) {
            return false;
        }
        NbtCompound lv = bee.entityData.copy();
        BeehiveBlockEntity.removeIrrelevantNbtKeys(lv);
        lv.put("HivePos", NbtHelper.fromBlockPos(pos));
        lv.putBoolean("NoGravity", true);
        Direction lv2 = state2.get(BeehiveBlock.FACING);
        BlockPos lv3 = pos.offset(lv2);
        boolean bl2 = bl = !world.getBlockState(lv3).getCollisionShape(world, lv3).isEmpty();
        if (bl && beeState != BeeState.EMERGENCY) {
            return false;
        }
        Entity lv4 = EntityType.loadEntityWithPassengers(lv, world, arg -> arg);
        if (lv4 != null) {
            if (!lv4.getType().isIn(EntityTypeTags.BEEHIVE_INHABITORS)) {
                return false;
            }
            if (lv4 instanceof BeeEntity) {
                BeeEntity lv5 = (BeeEntity)lv4;
                if (flowerPos != null && !lv5.hasFlower() && world.random.nextFloat() < 0.9f) {
                    lv5.setFlowerPos(flowerPos);
                }
                if (beeState == BeeState.HONEY_DELIVERED) {
                    int i;
                    lv5.onHoneyDelivered();
                    if (state2.isIn(BlockTags.BEEHIVES, state -> state.contains(BeehiveBlock.HONEY_LEVEL)) && (i = BeehiveBlockEntity.getHoneyLevel(state2)) < 5) {
                        int j;
                        int n = j = world.random.nextInt(100) == 0 ? 2 : 1;
                        if (i + j > 5) {
                            --j;
                        }
                        world.setBlockState(pos, (BlockState)state2.with(BeehiveBlock.HONEY_LEVEL, i + j));
                    }
                }
                BeehiveBlockEntity.ageBee(bee.ticksInHive, lv5);
                if (entities != null) {
                    entities.add(lv5);
                }
                float f = lv4.getWidth();
                double d = bl ? 0.0 : 0.55 + (double)(f / 2.0f);
                double e = (double)pos.getX() + 0.5 + d * (double)lv2.getOffsetX();
                double g = (double)pos.getY() + 0.5 - (double)(lv4.getHeight() / 2.0f);
                double h = (double)pos.getZ() + 0.5 + d * (double)lv2.getOffsetZ();
                lv4.refreshPositionAndAngles(e, g, h, lv4.getYaw(), lv4.getPitch());
            }
            world.playSound(null, pos, SoundEvents.BLOCK_BEEHIVE_EXIT, SoundCategory.BLOCKS, 1.0f, 1.0f);
            world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(lv4, world.getBlockState(pos)));
            return world.spawnEntity(lv4);
        }
        return false;
    }

    static void removeIrrelevantNbtKeys(NbtCompound compound) {
        for (String string : IRRELEVANT_BEE_NBT_KEYS) {
            compound.remove(string);
        }
    }

    private static void ageBee(int ticks, BeeEntity bee) {
        int j = bee.getBreedingAge();
        if (j < 0) {
            bee.setBreedingAge(Math.min(0, j + ticks));
        } else if (j > 0) {
            bee.setBreedingAge(Math.max(0, j - ticks));
        }
        bee.setLoveTicks(Math.max(0, bee.getLoveTicks() - ticks));
    }

    private boolean hasFlowerPos() {
        return this.flowerPos != null;
    }

    private static void tickBees(World world, BlockPos pos, BlockState state, List<Bee> bees, @Nullable BlockPos flowerPos) {
        boolean bl = false;
        Iterator<Bee> iterator = bees.iterator();
        while (iterator.hasNext()) {
            Bee lv = iterator.next();
            if (lv.ticksInHive > lv.minOccupationTicks) {
                BeeState lv2;
                BeeState beeState = lv2 = lv.entityData.getBoolean(HAS_NECTAR_KEY) ? BeeState.HONEY_DELIVERED : BeeState.BEE_RELEASED;
                if (BeehiveBlockEntity.releaseBee(world, pos, state, lv, null, lv2, flowerPos)) {
                    bl = true;
                    iterator.remove();
                }
            }
            ++lv.ticksInHive;
        }
        if (bl) {
            BeehiveBlockEntity.markDirty(world, pos, state);
        }
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, BeehiveBlockEntity blockEntity) {
        BeehiveBlockEntity.tickBees(world, pos, state, blockEntity.bees, blockEntity.flowerPos);
        if (!blockEntity.bees.isEmpty() && world.getRandom().nextDouble() < 0.005) {
            double d = (double)pos.getX() + 0.5;
            double e = pos.getY();
            double f = (double)pos.getZ() + 0.5;
            world.playSound(null, d, e, f, SoundEvents.BLOCK_BEEHIVE_WORK, SoundCategory.BLOCKS, 1.0f, 1.0f);
        }
        DebugInfoSender.sendBeehiveDebugData(world, pos, state, blockEntity);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.bees.clear();
        NbtList lv = nbt.getList(BEES_KEY, NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < lv.size(); ++i) {
            NbtCompound lv2 = lv.getCompound(i);
            Bee lv3 = new Bee(lv2.getCompound(ENTITY_DATA_KEY), lv2.getInt(TICKS_IN_HIVE_KEY), lv2.getInt(MIN_OCCUPATION_TICKS_KEY));
            this.bees.add(lv3);
        }
        this.flowerPos = null;
        if (nbt.contains(FLOWER_POS_KEY)) {
            this.flowerPos = NbtHelper.toBlockPos(nbt.getCompound(FLOWER_POS_KEY));
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.put(BEES_KEY, this.getBees());
        if (this.hasFlowerPos()) {
            nbt.put(FLOWER_POS_KEY, NbtHelper.fromBlockPos(this.flowerPos));
        }
    }

    public NbtList getBees() {
        NbtList lv = new NbtList();
        for (Bee lv2 : this.bees) {
            NbtCompound lv3 = lv2.entityData.copy();
            lv3.remove("UUID");
            NbtCompound lv4 = new NbtCompound();
            lv4.put(ENTITY_DATA_KEY, lv3);
            lv4.putInt(TICKS_IN_HIVE_KEY, lv2.ticksInHive);
            lv4.putInt(MIN_OCCUPATION_TICKS_KEY, lv2.minOccupationTicks);
            lv.add(lv4);
        }
        return lv;
    }

    public static enum BeeState {
        HONEY_DELIVERED,
        BEE_RELEASED,
        EMERGENCY;

    }

    static class Bee {
        final NbtCompound entityData;
        int ticksInHive;
        final int minOccupationTicks;

        Bee(NbtCompound entityData, int ticksInHive, int minOccupationTicks) {
            BeehiveBlockEntity.removeIrrelevantNbtKeys(entityData);
            this.entityData = entityData;
            this.ticksInHive = ticksInHive;
            this.minOccupationTicks = minOccupationTicks;
        }
    }
}

