/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.boss.dragon;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathMinHeap;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.boss.dragon.phase.Phase;
import net.minecraft.entity.boss.dragon.phase.PhaseManager;
import net.minecraft.entity.boss.dragon.phase.PhaseType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.gen.feature.EndPortalFeature;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class EnderDragonEntity
extends MobEntity
implements Monster {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final TrackedData<Integer> PHASE_TYPE = DataTracker.registerData(EnderDragonEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TargetPredicate CLOSE_PLAYER_PREDICATE = TargetPredicate.createAttackable().setBaseMaxDistance(64.0);
    private static final int MAX_HEALTH = 200;
    private static final int field_30429 = 400;
    private static final float TAKEOFF_THRESHOLD = 0.25f;
    private static final String DRAGON_DEATH_TIME_KEY = "DragonDeathTime";
    private static final String DRAGON_PHASE_KEY = "DragonPhase";
    public final double[][] segmentCircularBuffer = new double[64][3];
    public int latestSegment = -1;
    private final EnderDragonPart[] parts;
    public final EnderDragonPart head;
    private final EnderDragonPart neck;
    private final EnderDragonPart body;
    private final EnderDragonPart tail1;
    private final EnderDragonPart tail2;
    private final EnderDragonPart tail3;
    private final EnderDragonPart rightWing;
    private final EnderDragonPart leftWing;
    public float prevWingPosition;
    public float wingPosition;
    public boolean slowedDownByBlock;
    public int ticksSinceDeath;
    public float yawAcceleration;
    @Nullable
    public EndCrystalEntity connectedCrystal;
    @Nullable
    private final EnderDragonFight fight;
    private final PhaseManager phaseManager;
    private int ticksUntilNextGrowl = 100;
    private float damageDuringSitting;
    private final PathNode[] pathNodes = new PathNode[24];
    private final int[] pathNodeConnections = new int[24];
    private final PathMinHeap pathHeap = new PathMinHeap();

    public EnderDragonEntity(EntityType<? extends EnderDragonEntity> arg, World arg2) {
        super((EntityType<? extends MobEntity>)EntityType.ENDER_DRAGON, arg2);
        this.head = new EnderDragonPart(this, "head", 1.0f, 1.0f);
        this.neck = new EnderDragonPart(this, "neck", 3.0f, 3.0f);
        this.body = new EnderDragonPart(this, "body", 5.0f, 3.0f);
        this.tail1 = new EnderDragonPart(this, "tail", 2.0f, 2.0f);
        this.tail2 = new EnderDragonPart(this, "tail", 2.0f, 2.0f);
        this.tail3 = new EnderDragonPart(this, "tail", 2.0f, 2.0f);
        this.rightWing = new EnderDragonPart(this, "wing", 4.0f, 2.0f);
        this.leftWing = new EnderDragonPart(this, "wing", 4.0f, 2.0f);
        this.parts = new EnderDragonPart[]{this.head, this.neck, this.body, this.tail1, this.tail2, this.tail3, this.rightWing, this.leftWing};
        this.setHealth(this.getMaxHealth());
        this.noClip = true;
        this.ignoreCameraFrustum = true;
        this.fight = arg2 instanceof ServerWorld ? ((ServerWorld)arg2).getEnderDragonFight() : null;
        this.phaseManager = new PhaseManager(this);
    }

    public static DefaultAttributeContainer.Builder createEnderDragonAttributes() {
        return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 200.0);
    }

    @Override
    public boolean isFlappingWings() {
        float f = MathHelper.cos(this.wingPosition * ((float)Math.PI * 2));
        float g = MathHelper.cos(this.prevWingPosition * ((float)Math.PI * 2));
        return g <= -0.3f && f >= -0.3f;
    }

    @Override
    public void addFlapEffects() {
        if (this.world.isClient && !this.isSilent()) {
            this.world.playSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_ENDER_DRAGON_FLAP, this.getSoundCategory(), 5.0f, 0.8f + this.random.nextFloat() * 0.3f, false);
        }
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.getDataTracker().startTracking(PHASE_TYPE, PhaseType.HOVER.getTypeId());
    }

    public double[] getSegmentProperties(int segmentNumber, float tickDelta) {
        if (this.isDead()) {
            tickDelta = 0.0f;
        }
        tickDelta = 1.0f - tickDelta;
        int j = this.latestSegment - segmentNumber & 0x3F;
        int k = this.latestSegment - segmentNumber - 1 & 0x3F;
        double[] ds = new double[3];
        double d = this.segmentCircularBuffer[j][0];
        double e = MathHelper.wrapDegrees(this.segmentCircularBuffer[k][0] - d);
        ds[0] = d + e * (double)tickDelta;
        d = this.segmentCircularBuffer[j][1];
        e = this.segmentCircularBuffer[k][1] - d;
        ds[1] = d + e * (double)tickDelta;
        ds[2] = MathHelper.lerp((double)tickDelta, this.segmentCircularBuffer[j][2], this.segmentCircularBuffer[k][2]);
        return ds;
    }

    @Override
    public void tickMovement() {
        int ac;
        float q;
        float p;
        float o;
        this.addAirTravelEffects();
        if (this.world.isClient) {
            this.setHealth(this.getHealth());
            if (!this.isSilent() && !this.phaseManager.getCurrent().isSittingOrHovering() && --this.ticksUntilNextGrowl < 0) {
                this.world.playSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_ENDER_DRAGON_GROWL, this.getSoundCategory(), 2.5f, 0.8f + this.random.nextFloat() * 0.3f, false);
                this.ticksUntilNextGrowl = 200 + this.random.nextInt(200);
            }
        }
        this.prevWingPosition = this.wingPosition;
        if (this.isDead()) {
            float f = (this.random.nextFloat() - 0.5f) * 8.0f;
            float g = (this.random.nextFloat() - 0.5f) * 4.0f;
            float h = (this.random.nextFloat() - 0.5f) * 8.0f;
            this.world.addParticle(ParticleTypes.EXPLOSION, this.getX() + (double)f, this.getY() + 2.0 + (double)g, this.getZ() + (double)h, 0.0, 0.0, 0.0);
            return;
        }
        this.tickWithEndCrystals();
        Vec3d lv = this.getVelocity();
        float g = 0.2f / ((float)lv.horizontalLength() * 10.0f + 1.0f);
        this.wingPosition = this.phaseManager.getCurrent().isSittingOrHovering() ? (this.wingPosition += 0.1f) : (this.slowedDownByBlock ? (this.wingPosition += g * 0.5f) : (this.wingPosition += (g *= (float)Math.pow(2.0, lv.y))));
        this.setYaw(MathHelper.wrapDegrees(this.getYaw()));
        if (this.isAiDisabled()) {
            this.wingPosition = 0.5f;
            return;
        }
        if (this.latestSegment < 0) {
            for (int i = 0; i < this.segmentCircularBuffer.length; ++i) {
                this.segmentCircularBuffer[i][0] = this.getYaw();
                this.segmentCircularBuffer[i][1] = this.getY();
            }
        }
        if (++this.latestSegment == this.segmentCircularBuffer.length) {
            this.latestSegment = 0;
        }
        this.segmentCircularBuffer[this.latestSegment][0] = this.getYaw();
        this.segmentCircularBuffer[this.latestSegment][1] = this.getY();
        if (this.world.isClient) {
            if (this.bodyTrackingIncrements > 0) {
                double d = this.getX() + (this.serverX - this.getX()) / (double)this.bodyTrackingIncrements;
                e = this.getY() + (this.serverY - this.getY()) / (double)this.bodyTrackingIncrements;
                j = this.getZ() + (this.serverZ - this.getZ()) / (double)this.bodyTrackingIncrements;
                k = MathHelper.wrapDegrees(this.serverYaw - (double)this.getYaw());
                this.setYaw(this.getYaw() + (float)k / (float)this.bodyTrackingIncrements);
                this.setPitch(this.getPitch() + (float)(this.serverPitch - (double)this.getPitch()) / (float)this.bodyTrackingIncrements);
                --this.bodyTrackingIncrements;
                this.setPosition(d, e, j);
                this.setRotation(this.getYaw(), this.getPitch());
            }
            this.phaseManager.getCurrent().clientTick();
        } else {
            Vec3d lv3;
            Phase lv2 = this.phaseManager.getCurrent();
            lv2.serverTick();
            if (this.phaseManager.getCurrent() != lv2) {
                lv2 = this.phaseManager.getCurrent();
                lv2.serverTick();
            }
            if ((lv3 = lv2.getPathTarget()) != null) {
                e = lv3.x - this.getX();
                j = lv3.y - this.getY();
                k = lv3.z - this.getZ();
                double l = e * e + j * j + k * k;
                float m = lv2.getMaxYAcceleration();
                double n = Math.sqrt(e * e + k * k);
                if (n > 0.0) {
                    j = MathHelper.clamp(j / n, (double)(-m), (double)m);
                }
                this.setVelocity(this.getVelocity().add(0.0, j * 0.01, 0.0));
                this.setYaw(MathHelper.wrapDegrees(this.getYaw()));
                Vec3d lv4 = lv3.subtract(this.getX(), this.getY(), this.getZ()).normalize();
                Vec3d lv5 = new Vec3d(MathHelper.sin(this.getYaw() * ((float)Math.PI / 180)), this.getVelocity().y, -MathHelper.cos(this.getYaw() * ((float)Math.PI / 180))).normalize();
                o = Math.max(((float)lv5.dotProduct(lv4) + 0.5f) / 1.5f, 0.0f);
                if (Math.abs(e) > (double)1.0E-5f || Math.abs(k) > (double)1.0E-5f) {
                    p = MathHelper.clamp(MathHelper.wrapDegrees(180.0f - (float)MathHelper.atan2(e, k) * 57.295776f - this.getYaw()), -50.0f, 50.0f);
                    this.yawAcceleration *= 0.8f;
                    this.yawAcceleration += p * lv2.getYawAcceleration();
                    this.setYaw(this.getYaw() + this.yawAcceleration * 0.1f);
                }
                p = (float)(2.0 / (l + 1.0));
                q = 0.06f;
                this.updateVelocity(0.06f * (o * p + (1.0f - p)), new Vec3d(0.0, 0.0, -1.0));
                if (this.slowedDownByBlock) {
                    this.move(MovementType.SELF, this.getVelocity().multiply(0.8f));
                } else {
                    this.move(MovementType.SELF, this.getVelocity());
                }
                Vec3d lv6 = this.getVelocity().normalize();
                double r = 0.8 + 0.15 * (lv6.dotProduct(lv5) + 1.0) / 2.0;
                this.setVelocity(this.getVelocity().multiply(r, 0.91f, r));
            }
        }
        this.bodyYaw = this.getYaw();
        Vec3d[] lvs = new Vec3d[this.parts.length];
        for (int s = 0; s < this.parts.length; ++s) {
            lvs[s] = new Vec3d(this.parts[s].getX(), this.parts[s].getY(), this.parts[s].getZ());
        }
        float t = (float)(this.getSegmentProperties(5, 1.0f)[1] - this.getSegmentProperties(10, 1.0f)[1]) * 10.0f * ((float)Math.PI / 180);
        float u = MathHelper.cos(t);
        float v = MathHelper.sin(t);
        float w = this.getYaw() * ((float)Math.PI / 180);
        float x = MathHelper.sin(w);
        float y = MathHelper.cos(w);
        this.movePart(this.body, x * 0.5f, 0.0, -y * 0.5f);
        this.movePart(this.rightWing, y * 4.5f, 2.0, x * 4.5f);
        this.movePart(this.leftWing, y * -4.5f, 2.0, x * -4.5f);
        if (!this.world.isClient && this.hurtTime == 0) {
            this.launchLivingEntities(this.world.getOtherEntities(this, this.rightWing.getBoundingBox().expand(4.0, 2.0, 4.0).offset(0.0, -2.0, 0.0), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR));
            this.launchLivingEntities(this.world.getOtherEntities(this, this.leftWing.getBoundingBox().expand(4.0, 2.0, 4.0).offset(0.0, -2.0, 0.0), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR));
            this.damageLivingEntities(this.world.getOtherEntities(this, this.head.getBoundingBox().expand(1.0), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR));
            this.damageLivingEntities(this.world.getOtherEntities(this, this.neck.getBoundingBox().expand(1.0), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR));
        }
        float z = MathHelper.sin(this.getYaw() * ((float)Math.PI / 180) - this.yawAcceleration * 0.01f);
        float aa = MathHelper.cos(this.getYaw() * ((float)Math.PI / 180) - this.yawAcceleration * 0.01f);
        float ab = this.getHeadVerticalMovement();
        this.movePart(this.head, z * 6.5f * u, ab + v * 6.5f, -aa * 6.5f * u);
        this.movePart(this.neck, z * 5.5f * u, ab + v * 5.5f, -aa * 5.5f * u);
        double[] ds = this.getSegmentProperties(5, 1.0f);
        for (ac = 0; ac < 3; ++ac) {
            EnderDragonPart lv7 = null;
            if (ac == 0) {
                lv7 = this.tail1;
            }
            if (ac == 1) {
                lv7 = this.tail2;
            }
            if (ac == 2) {
                lv7 = this.tail3;
            }
            double[] es = this.getSegmentProperties(12 + ac * 2, 1.0f);
            float ad = this.getYaw() * ((float)Math.PI / 180) + this.wrapYawChange(es[0] - ds[0]) * ((float)Math.PI / 180);
            o = MathHelper.sin(ad);
            p = MathHelper.cos(ad);
            q = 1.5f;
            float ae = (float)(ac + 1) * 2.0f;
            this.movePart(lv7, -(x * 1.5f + o * ae) * u, es[1] - ds[1] - (double)((ae + 1.5f) * v) + 1.5, (y * 1.5f + p * ae) * u);
        }
        if (!this.world.isClient) {
            this.slowedDownByBlock = this.destroyBlocks(this.head.getBoundingBox()) | this.destroyBlocks(this.neck.getBoundingBox()) | this.destroyBlocks(this.body.getBoundingBox());
            if (this.fight != null) {
                this.fight.updateFight(this);
            }
        }
        for (ac = 0; ac < this.parts.length; ++ac) {
            this.parts[ac].prevX = lvs[ac].x;
            this.parts[ac].prevY = lvs[ac].y;
            this.parts[ac].prevZ = lvs[ac].z;
            this.parts[ac].lastRenderX = lvs[ac].x;
            this.parts[ac].lastRenderY = lvs[ac].y;
            this.parts[ac].lastRenderZ = lvs[ac].z;
        }
    }

    private void movePart(EnderDragonPart enderDragonPart, double dx, double dy, double dz) {
        enderDragonPart.setPosition(this.getX() + dx, this.getY() + dy, this.getZ() + dz);
    }

    private float getHeadVerticalMovement() {
        if (this.phaseManager.getCurrent().isSittingOrHovering()) {
            return -1.0f;
        }
        double[] ds = this.getSegmentProperties(5, 1.0f);
        double[] es = this.getSegmentProperties(0, 1.0f);
        return (float)(ds[1] - es[1]);
    }

    private void tickWithEndCrystals() {
        if (this.connectedCrystal != null) {
            if (this.connectedCrystal.isRemoved()) {
                this.connectedCrystal = null;
            } else if (this.age % 10 == 0 && this.getHealth() < this.getMaxHealth()) {
                this.setHealth(this.getHealth() + 1.0f);
            }
        }
        if (this.random.nextInt(10) == 0) {
            List<EndCrystalEntity> list = this.world.getNonSpectatingEntities(EndCrystalEntity.class, this.getBoundingBox().expand(32.0));
            EndCrystalEntity lv = null;
            double d = Double.MAX_VALUE;
            for (EndCrystalEntity lv2 : list) {
                double e = lv2.squaredDistanceTo(this);
                if (!(e < d)) continue;
                d = e;
                lv = lv2;
            }
            this.connectedCrystal = lv;
        }
    }

    private void launchLivingEntities(List<Entity> entities) {
        double d = (this.body.getBoundingBox().minX + this.body.getBoundingBox().maxX) / 2.0;
        double e = (this.body.getBoundingBox().minZ + this.body.getBoundingBox().maxZ) / 2.0;
        for (Entity lv : entities) {
            if (!(lv instanceof LivingEntity)) continue;
            double f = lv.getX() - d;
            double g = lv.getZ() - e;
            double h = Math.max(f * f + g * g, 0.1);
            lv.addVelocity(f / h * 4.0, 0.2f, g / h * 4.0);
            if (this.phaseManager.getCurrent().isSittingOrHovering() || ((LivingEntity)lv).getLastAttackedTime() >= lv.age - 2) continue;
            lv.damage(this.getDamageSources().mobAttack(this), 5.0f);
            this.applyDamageEffects(this, lv);
        }
    }

    private void damageLivingEntities(List<Entity> entities) {
        for (Entity lv : entities) {
            if (!(lv instanceof LivingEntity)) continue;
            lv.damage(this.getDamageSources().mobAttack(this), 10.0f);
            this.applyDamageEffects(this, lv);
        }
    }

    private float wrapYawChange(double yawDegrees) {
        return (float)MathHelper.wrapDegrees(yawDegrees);
    }

    private boolean destroyBlocks(Box box) {
        int i = MathHelper.floor(box.minX);
        int j = MathHelper.floor(box.minY);
        int k = MathHelper.floor(box.minZ);
        int l = MathHelper.floor(box.maxX);
        int m = MathHelper.floor(box.maxY);
        int n = MathHelper.floor(box.maxZ);
        boolean bl = false;
        boolean bl2 = false;
        for (int o = i; o <= l; ++o) {
            for (int p = j; p <= m; ++p) {
                for (int q = k; q <= n; ++q) {
                    BlockPos lv = new BlockPos(o, p, q);
                    BlockState lv2 = this.world.getBlockState(lv);
                    if (lv2.isAir() || lv2.isIn(BlockTags.DRAGON_TRANSPARENT)) continue;
                    if (!this.world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING) || lv2.isIn(BlockTags.DRAGON_IMMUNE)) {
                        bl = true;
                        continue;
                    }
                    bl2 = this.world.removeBlock(lv, false) || bl2;
                }
            }
        }
        if (bl2) {
            BlockPos lv3 = new BlockPos(i + this.random.nextInt(l - i + 1), j + this.random.nextInt(m - j + 1), k + this.random.nextInt(n - k + 1));
            this.world.syncWorldEvent(WorldEvents.ENDER_DRAGON_BREAKS_BLOCK, lv3, 0);
        }
        return bl;
    }

    public boolean damagePart(EnderDragonPart part, DamageSource source, float amount) {
        if (this.phaseManager.getCurrent().getType() == PhaseType.DYING) {
            return false;
        }
        amount = this.phaseManager.getCurrent().modifyDamageTaken(source, amount);
        if (part != this.head) {
            amount = amount / 4.0f + Math.min(amount, 1.0f);
        }
        if (amount < 0.01f) {
            return false;
        }
        if (source.getAttacker() instanceof PlayerEntity || source.isIn(DamageTypeTags.ALWAYS_HURTS_ENDER_DRAGONS)) {
            float g = this.getHealth();
            this.parentDamage(source, amount);
            if (this.isDead() && !this.phaseManager.getCurrent().isSittingOrHovering()) {
                this.setHealth(1.0f);
                this.phaseManager.setPhase(PhaseType.DYING);
            }
            if (this.phaseManager.getCurrent().isSittingOrHovering()) {
                this.damageDuringSitting = this.damageDuringSitting + g - this.getHealth();
                if (this.damageDuringSitting > 0.25f * this.getMaxHealth()) {
                    this.damageDuringSitting = 0.0f;
                    this.phaseManager.setPhase(PhaseType.TAKEOFF);
                }
            }
        }
        return true;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (!this.world.isClient) {
            return this.damagePart(this.body, source, amount);
        }
        return false;
    }

    protected boolean parentDamage(DamageSource source, float amount) {
        return super.damage(source, amount);
    }

    @Override
    public void kill() {
        this.remove(Entity.RemovalReason.KILLED);
        this.emitGameEvent(GameEvent.ENTITY_DIE);
        if (this.fight != null) {
            this.fight.updateFight(this);
            this.fight.dragonKilled(this);
        }
    }

    @Override
    protected void updatePostDeath() {
        if (this.fight != null) {
            this.fight.updateFight(this);
        }
        ++this.ticksSinceDeath;
        if (this.ticksSinceDeath >= 180 && this.ticksSinceDeath <= 200) {
            float f = (this.random.nextFloat() - 0.5f) * 8.0f;
            float g = (this.random.nextFloat() - 0.5f) * 4.0f;
            float h = (this.random.nextFloat() - 0.5f) * 8.0f;
            this.world.addParticle(ParticleTypes.EXPLOSION_EMITTER, this.getX() + (double)f, this.getY() + 2.0 + (double)g, this.getZ() + (double)h, 0.0, 0.0, 0.0);
        }
        boolean bl = this.world.getGameRules().getBoolean(GameRules.DO_MOB_LOOT);
        int i = 500;
        if (this.fight != null && !this.fight.hasPreviouslyKilled()) {
            i = 12000;
        }
        if (this.world instanceof ServerWorld) {
            if (this.ticksSinceDeath > 150 && this.ticksSinceDeath % 5 == 0 && bl) {
                ExperienceOrbEntity.spawn((ServerWorld)this.world, this.getPos(), MathHelper.floor((float)i * 0.08f));
            }
            if (this.ticksSinceDeath == 1 && !this.isSilent()) {
                this.world.syncGlobalEvent(WorldEvents.ENDER_DRAGON_DIES, this.getBlockPos(), 0);
            }
        }
        this.move(MovementType.SELF, new Vec3d(0.0, 0.1f, 0.0));
        if (this.ticksSinceDeath == 200 && this.world instanceof ServerWorld) {
            if (bl) {
                ExperienceOrbEntity.spawn((ServerWorld)this.world, this.getPos(), MathHelper.floor((float)i * 0.2f));
            }
            if (this.fight != null) {
                this.fight.dragonKilled(this);
            }
            this.remove(Entity.RemovalReason.KILLED);
            this.emitGameEvent(GameEvent.ENTITY_DIE);
        }
    }

    public int getNearestPathNodeIndex() {
        if (this.pathNodes[0] == null) {
            for (int i = 0; i < 24; ++i) {
                int m;
                int l;
                int j = 5;
                int k = i;
                if (i < 12) {
                    l = MathHelper.floor(60.0f * MathHelper.cos(2.0f * ((float)(-Math.PI) + 0.2617994f * (float)k)));
                    m = MathHelper.floor(60.0f * MathHelper.sin(2.0f * ((float)(-Math.PI) + 0.2617994f * (float)k)));
                } else if (i < 20) {
                    l = MathHelper.floor(40.0f * MathHelper.cos(2.0f * ((float)(-Math.PI) + 0.3926991f * (float)(k -= 12))));
                    m = MathHelper.floor(40.0f * MathHelper.sin(2.0f * ((float)(-Math.PI) + 0.3926991f * (float)k)));
                    j += 10;
                } else {
                    l = MathHelper.floor(20.0f * MathHelper.cos(2.0f * ((float)(-Math.PI) + 0.7853982f * (float)(k -= 20))));
                    m = MathHelper.floor(20.0f * MathHelper.sin(2.0f * ((float)(-Math.PI) + 0.7853982f * (float)k)));
                }
                int n = Math.max(this.world.getSeaLevel() + 10, this.world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, new BlockPos(l, 0, m)).getY() + j);
                this.pathNodes[i] = new PathNode(l, n, m);
            }
            this.pathNodeConnections[0] = 6146;
            this.pathNodeConnections[1] = 8197;
            this.pathNodeConnections[2] = 8202;
            this.pathNodeConnections[3] = 16404;
            this.pathNodeConnections[4] = 32808;
            this.pathNodeConnections[5] = 32848;
            this.pathNodeConnections[6] = 65696;
            this.pathNodeConnections[7] = 131392;
            this.pathNodeConnections[8] = 131712;
            this.pathNodeConnections[9] = 263424;
            this.pathNodeConnections[10] = 526848;
            this.pathNodeConnections[11] = 525313;
            this.pathNodeConnections[12] = 1581057;
            this.pathNodeConnections[13] = 3166214;
            this.pathNodeConnections[14] = 2138120;
            this.pathNodeConnections[15] = 6373424;
            this.pathNodeConnections[16] = 4358208;
            this.pathNodeConnections[17] = 12910976;
            this.pathNodeConnections[18] = 9044480;
            this.pathNodeConnections[19] = 9706496;
            this.pathNodeConnections[20] = 15216640;
            this.pathNodeConnections[21] = 0xD0E000;
            this.pathNodeConnections[22] = 11763712;
            this.pathNodeConnections[23] = 0x7E0000;
        }
        return this.getNearestPathNodeIndex(this.getX(), this.getY(), this.getZ());
    }

    public int getNearestPathNodeIndex(double x, double y, double z) {
        float g = 10000.0f;
        int i = 0;
        PathNode lv = new PathNode(MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z));
        int j = 0;
        if (this.fight == null || this.fight.getAliveEndCrystals() == 0) {
            j = 12;
        }
        for (int k = j; k < 24; ++k) {
            float h;
            if (this.pathNodes[k] == null || !((h = this.pathNodes[k].getSquaredDistance(lv)) < g)) continue;
            g = h;
            i = k;
        }
        return i;
    }

    @Nullable
    public Path findPath(int from, int to, @Nullable PathNode pathNode) {
        PathNode lv;
        for (int k = 0; k < 24; ++k) {
            lv = this.pathNodes[k];
            lv.visited = false;
            lv.heapWeight = 0.0f;
            lv.penalizedPathLength = 0.0f;
            lv.distanceToNearestTarget = 0.0f;
            lv.previous = null;
            lv.heapIndex = -1;
        }
        PathNode lv2 = this.pathNodes[from];
        lv = this.pathNodes[to];
        lv2.penalizedPathLength = 0.0f;
        lv2.heapWeight = lv2.distanceToNearestTarget = lv2.getDistance(lv);
        this.pathHeap.clear();
        this.pathHeap.push(lv2);
        PathNode lv3 = lv2;
        int l = 0;
        if (this.fight == null || this.fight.getAliveEndCrystals() == 0) {
            l = 12;
        }
        while (!this.pathHeap.isEmpty()) {
            int n;
            PathNode lv4 = this.pathHeap.pop();
            if (lv4.equals(lv)) {
                if (pathNode != null) {
                    pathNode.previous = lv;
                    lv = pathNode;
                }
                return this.getPathOfAllPredecessors(lv2, lv);
            }
            if (lv4.getDistance(lv) < lv3.getDistance(lv)) {
                lv3 = lv4;
            }
            lv4.visited = true;
            int m = 0;
            for (n = 0; n < 24; ++n) {
                if (this.pathNodes[n] != lv4) continue;
                m = n;
                break;
            }
            for (n = l; n < 24; ++n) {
                if ((this.pathNodeConnections[m] & 1 << n) <= 0) continue;
                PathNode lv5 = this.pathNodes[n];
                if (lv5.visited) continue;
                float f = lv4.penalizedPathLength + lv4.getDistance(lv5);
                if (lv5.isInHeap() && !(f < lv5.penalizedPathLength)) continue;
                lv5.previous = lv4;
                lv5.penalizedPathLength = f;
                lv5.distanceToNearestTarget = lv5.getDistance(lv);
                if (lv5.isInHeap()) {
                    this.pathHeap.setNodeWeight(lv5, lv5.penalizedPathLength + lv5.distanceToNearestTarget);
                    continue;
                }
                lv5.heapWeight = lv5.penalizedPathLength + lv5.distanceToNearestTarget;
                this.pathHeap.push(lv5);
            }
        }
        if (lv3 == lv2) {
            return null;
        }
        LOGGER.debug("Failed to find path from {} to {}", (Object)from, (Object)to);
        if (pathNode != null) {
            pathNode.previous = lv3;
            lv3 = pathNode;
        }
        return this.getPathOfAllPredecessors(lv2, lv3);
    }

    private Path getPathOfAllPredecessors(PathNode unused, PathNode node) {
        ArrayList<PathNode> list = Lists.newArrayList();
        PathNode lv = node;
        list.add(0, lv);
        while (lv.previous != null) {
            lv = lv.previous;
            list.add(0, lv);
        }
        return new Path(list, new BlockPos(node.x, node.y, node.z), true);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt(DRAGON_PHASE_KEY, this.phaseManager.getCurrent().getType().getTypeId());
        nbt.putInt(DRAGON_DEATH_TIME_KEY, this.ticksSinceDeath);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains(DRAGON_PHASE_KEY)) {
            this.phaseManager.setPhase(PhaseType.getFromId(nbt.getInt(DRAGON_PHASE_KEY)));
        }
        if (nbt.contains(DRAGON_DEATH_TIME_KEY)) {
            this.ticksSinceDeath = nbt.getInt(DRAGON_DEATH_TIME_KEY);
        }
    }

    @Override
    public void checkDespawn() {
    }

    public EnderDragonPart[] getBodyParts() {
        return this.parts;
    }

    @Override
    public boolean canHit() {
        return false;
    }

    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.HOSTILE;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_ENDER_DRAGON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_ENDER_DRAGON_HURT;
    }

    @Override
    protected float getSoundVolume() {
        return 5.0f;
    }

    public float getChangeInNeckPitch(int segmentOffset, double[] segment1, double[] segment2) {
        double e;
        Phase lv = this.phaseManager.getCurrent();
        PhaseType<? extends Phase> lv2 = lv.getType();
        if (lv2 == PhaseType.LANDING || lv2 == PhaseType.TAKEOFF) {
            BlockPos lv3 = this.world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EndPortalFeature.ORIGIN);
            double d = Math.max(Math.sqrt(lv3.getSquaredDistance(this.getPos())) / 4.0, 1.0);
            e = (double)segmentOffset / d;
        } else {
            e = lv.isSittingOrHovering() ? (double)segmentOffset : (segmentOffset == 6 ? 0.0 : segment2[1] - segment1[1]);
        }
        return (float)e;
    }

    public Vec3d getRotationVectorFromPhase(float tickDelta) {
        Vec3d lv4;
        Phase lv = this.phaseManager.getCurrent();
        PhaseType<? extends Phase> lv2 = lv.getType();
        if (lv2 == PhaseType.LANDING || lv2 == PhaseType.TAKEOFF) {
            BlockPos lv3 = this.world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EndPortalFeature.ORIGIN);
            float g = Math.max((float)Math.sqrt(lv3.getSquaredDistance(this.getPos())) / 4.0f, 1.0f);
            float h = 6.0f / g;
            float i = this.getPitch();
            float j = 1.5f;
            this.setPitch(-h * 1.5f * 5.0f);
            lv4 = this.getRotationVec(tickDelta);
            this.setPitch(i);
        } else if (lv.isSittingOrHovering()) {
            float k = this.getPitch();
            float g = 1.5f;
            this.setPitch(-45.0f);
            lv4 = this.getRotationVec(tickDelta);
            this.setPitch(k);
        } else {
            lv4 = this.getRotationVec(tickDelta);
        }
        return lv4;
    }

    public void crystalDestroyed(EndCrystalEntity endCrystal, BlockPos pos, DamageSource source) {
        PlayerEntity lv = source.getAttacker() instanceof PlayerEntity ? (PlayerEntity)source.getAttacker() : this.world.getClosestPlayer(CLOSE_PLAYER_PREDICATE, pos.getX(), pos.getY(), pos.getZ());
        if (endCrystal == this.connectedCrystal) {
            this.damagePart(this.head, this.getDamageSources().explosion(endCrystal, lv), 10.0f);
        }
        this.phaseManager.getCurrent().crystalDestroyed(endCrystal, pos, source, lv);
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        if (PHASE_TYPE.equals(data) && this.world.isClient) {
            this.phaseManager.setPhase(PhaseType.getFromId(this.getDataTracker().get(PHASE_TYPE)));
        }
        super.onTrackedDataSet(data);
    }

    public PhaseManager getPhaseManager() {
        return this.phaseManager;
    }

    @Nullable
    public EnderDragonFight getFight() {
        return this.fight;
    }

    @Override
    public boolean addStatusEffect(StatusEffectInstance effect, @Nullable Entity source) {
        return false;
    }

    @Override
    protected boolean canStartRiding(Entity entity) {
        return false;
    }

    @Override
    public boolean canUsePortals() {
        return false;
    }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        super.onSpawnPacket(packet);
        EnderDragonPart[] lvs = this.getBodyParts();
        for (int i = 0; i < lvs.length; ++i) {
            lvs[i].setId(i + packet.getId());
        }
    }

    @Override
    public boolean canTarget(LivingEntity target) {
        return target.canTakeDamage();
    }

    @Override
    public double getMountedHeightOffset() {
        return this.body.getHeight();
    }
}

