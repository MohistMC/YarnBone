/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.passive;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SuspiciousStewIngredient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.Shearable;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.VariantHolder;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.item.SuspiciousStewItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

public class MooshroomEntity
extends CowEntity
implements Shearable,
VariantHolder<Type> {
    private static final TrackedData<String> TYPE = DataTracker.registerData(MooshroomEntity.class, TrackedDataHandlerRegistry.STRING);
    private static final int MUTATION_CHANCE = 1024;
    @Nullable
    private StatusEffect stewEffect;
    private int stewEffectDuration;
    @Nullable
    private UUID lightningId;

    public MooshroomEntity(EntityType<? extends MooshroomEntity> arg, World arg2) {
        super((EntityType<? extends CowEntity>)arg, arg2);
    }

    @Override
    public float getPathfindingFavor(BlockPos pos, WorldView world) {
        if (world.getBlockState(pos.down()).isOf(Blocks.MYCELIUM)) {
            return 10.0f;
        }
        return world.getPhototaxisFavor(pos);
    }

    public static boolean canSpawn(EntityType<MooshroomEntity> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        return world.getBlockState(pos.down()).isIn(BlockTags.MOOSHROOMS_SPAWNABLE_ON) && MooshroomEntity.isLightLevelValidForNaturalSpawn(world, pos);
    }

    @Override
    public void onStruckByLightning(ServerWorld world, LightningEntity lightning) {
        UUID uUID = lightning.getUuid();
        if (!uUID.equals(this.lightningId)) {
            this.setVariant(this.getVariant() == Type.RED ? Type.BROWN : Type.RED);
            this.lightningId = uUID;
            this.playSound(SoundEvents.ENTITY_MOOSHROOM_CONVERT, 2.0f, 1.0f);
        }
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(TYPE, Type.RED.name);
    }

    @Override
    public ActionResult interactMob(PlayerEntity player2, Hand hand) {
        ItemStack lv = player2.getStackInHand(hand);
        if (lv.isOf(Items.BOWL) && !this.isBaby()) {
            ItemStack lv2;
            boolean bl = false;
            if (this.stewEffect != null) {
                bl = true;
                lv2 = new ItemStack(Items.SUSPICIOUS_STEW);
                SuspiciousStewItem.addEffectToStew(lv2, this.stewEffect, this.stewEffectDuration);
                this.stewEffect = null;
                this.stewEffectDuration = 0;
            } else {
                lv2 = new ItemStack(Items.MUSHROOM_STEW);
            }
            ItemStack lv3 = ItemUsage.exchangeStack(lv, player2, lv2, false);
            player2.setStackInHand(hand, lv3);
            SoundEvent lv4 = bl ? SoundEvents.ENTITY_MOOSHROOM_SUSPICIOUS_MILK : SoundEvents.ENTITY_MOOSHROOM_MILK;
            this.playSound(lv4, 1.0f, 1.0f);
            return ActionResult.success(this.world.isClient);
        }
        if (lv.isOf(Items.SHEARS) && this.isShearable()) {
            this.sheared(SoundCategory.PLAYERS);
            this.emitGameEvent(GameEvent.SHEAR, player2);
            if (!this.world.isClient) {
                lv.damage(1, player2, player -> player.sendToolBreakStatus(hand));
            }
            return ActionResult.success(this.world.isClient);
        }
        if (this.getVariant() == Type.BROWN && lv.isIn(ItemTags.SMALL_FLOWERS)) {
            if (this.stewEffect != null) {
                for (int i = 0; i < 2; ++i) {
                    this.world.addParticle(ParticleTypes.SMOKE, this.getX() + this.random.nextDouble() / 2.0, this.getBodyY(0.5), this.getZ() + this.random.nextDouble() / 2.0, 0.0, this.random.nextDouble() / 5.0, 0.0);
                }
            } else {
                Optional<Pair<StatusEffect, Integer>> optional = this.getStewEffectFrom(lv);
                if (!optional.isPresent()) {
                    return ActionResult.PASS;
                }
                Pair<StatusEffect, Integer> pair = optional.get();
                if (!player2.getAbilities().creativeMode) {
                    lv.decrement(1);
                }
                for (int j = 0; j < 4; ++j) {
                    this.world.addParticle(ParticleTypes.EFFECT, this.getX() + this.random.nextDouble() / 2.0, this.getBodyY(0.5), this.getZ() + this.random.nextDouble() / 2.0, 0.0, this.random.nextDouble() / 5.0, 0.0);
                }
                this.stewEffect = pair.getLeft();
                this.stewEffectDuration = pair.getRight();
                this.playSound(SoundEvents.ENTITY_MOOSHROOM_EAT, 2.0f, 1.0f);
            }
            return ActionResult.success(this.world.isClient);
        }
        return super.interactMob(player2, hand);
    }

    @Override
    public void sheared(SoundCategory shearedSoundCategory) {
        CowEntity lv;
        this.world.playSoundFromEntity(null, this, SoundEvents.ENTITY_MOOSHROOM_SHEAR, shearedSoundCategory, 1.0f, 1.0f);
        if (!this.world.isClient() && (lv = EntityType.COW.create(this.world)) != null) {
            ((ServerWorld)this.world).spawnParticles(ParticleTypes.EXPLOSION, this.getX(), this.getBodyY(0.5), this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
            this.discard();
            lv.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), this.getYaw(), this.getPitch());
            lv.setHealth(this.getHealth());
            lv.bodyYaw = this.bodyYaw;
            if (this.hasCustomName()) {
                lv.setCustomName(this.getCustomName());
                lv.setCustomNameVisible(this.isCustomNameVisible());
            }
            if (this.isPersistent()) {
                lv.setPersistent();
            }
            lv.setInvulnerable(this.isInvulnerable());
            this.world.spawnEntity(lv);
            for (int i = 0; i < 5; ++i) {
                this.world.spawnEntity(new ItemEntity(this.world, this.getX(), this.getBodyY(1.0), this.getZ(), new ItemStack(this.getVariant().mushroom.getBlock())));
            }
        }
    }

    @Override
    public boolean isShearable() {
        return this.isAlive() && !this.isBaby();
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putString("Type", this.getVariant().asString());
        if (this.stewEffect != null) {
            nbt.putInt("EffectId", StatusEffect.getRawId(this.stewEffect));
            nbt.putInt("EffectDuration", this.stewEffectDuration);
        }
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.setVariant(Type.fromName(nbt.getString("Type")));
        if (nbt.contains("EffectId", NbtElement.BYTE_TYPE)) {
            this.stewEffect = StatusEffect.byRawId(nbt.getInt("EffectId"));
        }
        if (nbt.contains("EffectDuration", NbtElement.INT_TYPE)) {
            this.stewEffectDuration = nbt.getInt("EffectDuration");
        }
    }

    private Optional<Pair<StatusEffect, Integer>> getStewEffectFrom(ItemStack flower) {
        SuspiciousStewIngredient lv = SuspiciousStewIngredient.of(flower.getItem());
        if (lv != null) {
            return Optional.of(Pair.of(lv.getEffectInStew(), lv.getEffectInStewDuration()));
        }
        return Optional.empty();
    }

    @Override
    public void setVariant(Type arg) {
        this.dataTracker.set(TYPE, arg.name);
    }

    @Override
    public Type getVariant() {
        return Type.fromName(this.dataTracker.get(TYPE));
    }

    @Override
    @Nullable
    public MooshroomEntity createChild(ServerWorld arg, PassiveEntity arg2) {
        MooshroomEntity lv = EntityType.MOOSHROOM.create(arg);
        if (lv != null) {
            lv.setVariant(this.chooseBabyType((MooshroomEntity)arg2));
        }
        return lv;
    }

    private Type chooseBabyType(MooshroomEntity mooshroom) {
        Type lv2;
        Type lv = this.getVariant();
        Type lv3 = lv == (lv2 = mooshroom.getVariant()) && this.random.nextInt(1024) == 0 ? (lv == Type.BROWN ? Type.RED : Type.BROWN) : (this.random.nextBoolean() ? lv : lv2);
        return lv3;
    }

    @Override
    @Nullable
    public /* synthetic */ CowEntity createChild(ServerWorld arg, PassiveEntity arg2) {
        return this.createChild(arg, arg2);
    }

    @Override
    @Nullable
    public /* synthetic */ PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return this.createChild(world, entity);
    }

    @Override
    public /* synthetic */ Object getVariant() {
        return this.getVariant();
    }

    public static enum Type implements StringIdentifiable
    {
        RED("red", Blocks.RED_MUSHROOM.getDefaultState()),
        BROWN("brown", Blocks.BROWN_MUSHROOM.getDefaultState());

        public static final StringIdentifiable.Codec<Type> CODEC;
        final String name;
        final BlockState mushroom;

        private Type(String name, BlockState mushroom) {
            this.name = name;
            this.mushroom = mushroom;
        }

        public BlockState getMushroomState() {
            return this.mushroom;
        }

        @Override
        public String asString() {
            return this.name;
        }

        static Type fromName(String name) {
            return CODEC.byId(name, RED);
        }

        static {
            CODEC = StringIdentifiable.createCodec(Type::values);
        }
    }
}

