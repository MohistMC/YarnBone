/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world.explosion;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.ProtectionEnchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.explosion.EntityExplosionBehavior;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.jetbrains.annotations.Nullable;

public class Explosion {
    private static final ExplosionBehavior DEFAULT_BEHAVIOR = new ExplosionBehavior();
    private static final int field_30960 = 16;
    private final boolean createFire;
    private final DestructionType destructionType;
    private final Random random = Random.create();
    private final World world;
    private final double x;
    private final double y;
    private final double z;
    @Nullable
    private final Entity entity;
    private final float power;
    private final DamageSource damageSource;
    private final ExplosionBehavior behavior;
    private final ObjectArrayList<BlockPos> affectedBlocks = new ObjectArrayList();
    private final Map<PlayerEntity, Vec3d> affectedPlayers = Maps.newHashMap();

    public Explosion(World world, @Nullable Entity entity, double x, double y, double z, float power, List<BlockPos> affectedBlocks) {
        this(world, entity, x, y, z, power, false, DestructionType.DESTROY_WITH_DECAY, affectedBlocks);
    }

    public Explosion(World world, @Nullable Entity entity, double x, double y, double z, float power, boolean createFire, DestructionType destructionType, List<BlockPos> affectedBlocks) {
        this(world, entity, x, y, z, power, createFire, destructionType);
        this.affectedBlocks.addAll((Collection<BlockPos>)affectedBlocks);
    }

    public Explosion(World world, @Nullable Entity entity, double x, double y, double z, float power, boolean createFire, DestructionType destructionType) {
        this(world, entity, null, null, x, y, z, power, createFire, destructionType);
    }

    public Explosion(World world, @Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionBehavior behavior, double x, double y, double z, float power, boolean createFire, DestructionType destructionType) {
        this.world = world;
        this.entity = entity;
        this.power = power;
        this.x = x;
        this.y = y;
        this.z = z;
        this.createFire = createFire;
        this.destructionType = destructionType;
        this.damageSource = damageSource == null ? world.getDamageSources().explosion(this) : damageSource;
        this.behavior = behavior == null ? this.chooseBehavior(entity) : behavior;
    }

    private ExplosionBehavior chooseBehavior(@Nullable Entity entity) {
        return entity == null ? DEFAULT_BEHAVIOR : new EntityExplosionBehavior(entity);
    }

    public static float getExposure(Vec3d source, Entity entity) {
        Box lv = entity.getBoundingBox();
        double d = 1.0 / ((lv.maxX - lv.minX) * 2.0 + 1.0);
        double e = 1.0 / ((lv.maxY - lv.minY) * 2.0 + 1.0);
        double f = 1.0 / ((lv.maxZ - lv.minZ) * 2.0 + 1.0);
        double g = (1.0 - Math.floor(1.0 / d) * d) / 2.0;
        double h = (1.0 - Math.floor(1.0 / f) * f) / 2.0;
        if (d < 0.0 || e < 0.0 || f < 0.0) {
            return 0.0f;
        }
        int i = 0;
        int j = 0;
        for (double k = 0.0; k <= 1.0; k += d) {
            for (double l = 0.0; l <= 1.0; l += e) {
                for (double m = 0.0; m <= 1.0; m += f) {
                    double p;
                    double o;
                    double n = MathHelper.lerp(k, lv.minX, lv.maxX);
                    Vec3d lv2 = new Vec3d(n + g, o = MathHelper.lerp(l, lv.minY, lv.maxY), (p = MathHelper.lerp(m, lv.minZ, lv.maxZ)) + h);
                    if (entity.world.raycast(new RaycastContext(lv2, source, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, entity)).getType() == HitResult.Type.MISS) {
                        ++i;
                    }
                    ++j;
                }
            }
        }
        return (float)i / (float)j;
    }

    public void collectBlocksAndDamageEntities() {
        int l;
        int k;
        this.world.emitGameEvent(this.entity, GameEvent.EXPLODE, new Vec3d(this.x, this.y, this.z));
        HashSet<BlockPos> set = Sets.newHashSet();
        int i = 16;
        for (int j = 0; j < 16; ++j) {
            for (k = 0; k < 16; ++k) {
                block2: for (l = 0; l < 16; ++l) {
                    if (j != 0 && j != 15 && k != 0 && k != 15 && l != 0 && l != 15) continue;
                    double d = (float)j / 15.0f * 2.0f - 1.0f;
                    double e = (float)k / 15.0f * 2.0f - 1.0f;
                    double f = (float)l / 15.0f * 2.0f - 1.0f;
                    double g = Math.sqrt(d * d + e * e + f * f);
                    d /= g;
                    e /= g;
                    f /= g;
                    double m = this.x;
                    double n = this.y;
                    double o = this.z;
                    float p = 0.3f;
                    for (float h = this.power * (0.7f + this.world.random.nextFloat() * 0.6f); h > 0.0f; h -= 0.22500001f) {
                        BlockPos lv = BlockPos.ofFloored(m, n, o);
                        BlockState lv2 = this.world.getBlockState(lv);
                        FluidState lv3 = this.world.getFluidState(lv);
                        if (!this.world.isInBuildLimit(lv)) continue block2;
                        Optional<Float> optional = this.behavior.getBlastResistance(this, this.world, lv, lv2, lv3);
                        if (optional.isPresent()) {
                            h -= (optional.get().floatValue() + 0.3f) * 0.3f;
                        }
                        if (h > 0.0f && this.behavior.canDestroyBlock(this, this.world, lv, lv2, h)) {
                            set.add(lv);
                        }
                        m += d * (double)0.3f;
                        n += e * (double)0.3f;
                        o += f * (double)0.3f;
                    }
                }
            }
        }
        this.affectedBlocks.addAll((Collection<BlockPos>)set);
        float q = this.power * 2.0f;
        k = MathHelper.floor(this.x - (double)q - 1.0);
        l = MathHelper.floor(this.x + (double)q + 1.0);
        int r = MathHelper.floor(this.y - (double)q - 1.0);
        int s = MathHelper.floor(this.y + (double)q + 1.0);
        int t = MathHelper.floor(this.z - (double)q - 1.0);
        int u = MathHelper.floor(this.z + (double)q + 1.0);
        List<Entity> list = this.world.getOtherEntities(this.entity, new Box(k, r, t, l, s, u));
        Vec3d lv4 = new Vec3d(this.x, this.y, this.z);
        for (int v = 0; v < list.size(); ++v) {
            PlayerEntity lv8;
            double ad;
            double z;
            double y;
            double x;
            double aa;
            double w;
            Entity lv5 = list.get(v);
            if (lv5.isImmuneToExplosion() || !((w = Math.sqrt(lv5.squaredDistanceTo(lv4)) / (double)q) <= 1.0) || (aa = Math.sqrt((x = lv5.getX() - this.x) * x + (y = (lv5 instanceof TntEntity ? lv5.getY() : lv5.getEyeY()) - this.y) * y + (z = lv5.getZ() - this.z) * z)) == 0.0) continue;
            x /= aa;
            y /= aa;
            z /= aa;
            double ab = Explosion.getExposure(lv4, lv5);
            double ac = (1.0 - w) * ab;
            lv5.damage(this.getDamageSource(), (int)((ac * ac + ac) / 2.0 * 7.0 * (double)q + 1.0));
            if (lv5 instanceof LivingEntity) {
                LivingEntity lv6 = (LivingEntity)lv5;
                ad = ProtectionEnchantment.transformExplosionKnockback(lv6, ac);
            } else {
                ad = ac;
            }
            Vec3d lv7 = new Vec3d(x *= ad, y *= ad, z *= ad);
            lv5.setVelocity(lv5.getVelocity().add(lv7));
            if (!(lv5 instanceof PlayerEntity) || (lv8 = (PlayerEntity)lv5).isSpectator() || lv8.isCreative() && lv8.getAbilities().flying) continue;
            this.affectedPlayers.put(lv8, lv7);
        }
    }

    public void affectWorld(boolean particles) {
        if (this.world.isClient) {
            this.world.playSound(this.x, this.y, this.z, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0f, (1.0f + (this.world.random.nextFloat() - this.world.random.nextFloat()) * 0.2f) * 0.7f, false);
        }
        boolean bl2 = this.shouldDestroy();
        if (particles) {
            if (this.power < 2.0f || !bl2) {
                this.world.addParticle(ParticleTypes.EXPLOSION, this.x, this.y, this.z, 1.0, 0.0, 0.0);
            } else {
                this.world.addParticle(ParticleTypes.EXPLOSION_EMITTER, this.x, this.y, this.z, 1.0, 0.0, 0.0);
            }
        }
        if (bl2) {
            ObjectArrayList objectArrayList = new ObjectArrayList();
            boolean bl3 = this.getCausingEntity() instanceof PlayerEntity;
            Util.shuffle(this.affectedBlocks, this.world.random);
            for (BlockPos lv : this.affectedBlocks) {
                World world;
                BlockState lv2 = this.world.getBlockState(lv);
                Block lv3 = lv2.getBlock();
                if (lv2.isAir()) continue;
                BlockPos lv4 = lv.toImmutable();
                this.world.getProfiler().push("explosion_blocks");
                if (lv3.shouldDropItemsOnExplosion(this) && (world = this.world) instanceof ServerWorld) {
                    ServerWorld lv5 = (ServerWorld)world;
                    BlockEntity lv6 = lv2.hasBlockEntity() ? this.world.getBlockEntity(lv) : null;
                    LootContext.Builder lv7 = new LootContext.Builder(lv5).random(this.world.random).parameter(LootContextParameters.ORIGIN, Vec3d.ofCenter(lv)).parameter(LootContextParameters.TOOL, ItemStack.EMPTY).optionalParameter(LootContextParameters.BLOCK_ENTITY, lv6).optionalParameter(LootContextParameters.THIS_ENTITY, this.entity);
                    if (this.destructionType == DestructionType.DESTROY_WITH_DECAY) {
                        lv7.parameter(LootContextParameters.EXPLOSION_RADIUS, Float.valueOf(this.power));
                    }
                    lv2.onStacksDropped(lv5, lv, ItemStack.EMPTY, bl3);
                    lv2.getDroppedStacks(lv7).forEach(stack -> Explosion.tryMergeStack(objectArrayList, stack, lv4));
                }
                this.world.setBlockState(lv, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
                lv3.onDestroyedByExplosion(this.world, lv, this);
                this.world.getProfiler().pop();
            }
            for (Pair pair : objectArrayList) {
                Block.dropStack(this.world, (BlockPos)pair.getSecond(), (ItemStack)pair.getFirst());
            }
        }
        if (this.createFire) {
            for (BlockPos lv8 : this.affectedBlocks) {
                if (this.random.nextInt(3) != 0 || !this.world.getBlockState(lv8).isAir() || !this.world.getBlockState(lv8.down()).isOpaqueFullCube(this.world, lv8.down())) continue;
                this.world.setBlockState(lv8, AbstractFireBlock.getState(this.world, lv8));
            }
        }
    }

    public boolean shouldDestroy() {
        return this.destructionType != DestructionType.KEEP;
    }

    private static void tryMergeStack(ObjectArrayList<Pair<ItemStack, BlockPos>> stacks, ItemStack stack, BlockPos pos) {
        int i = stacks.size();
        for (int j = 0; j < i; ++j) {
            Pair<ItemStack, BlockPos> pair = stacks.get(j);
            ItemStack lv = pair.getFirst();
            if (!ItemEntity.canMerge(lv, stack)) continue;
            ItemStack lv2 = ItemEntity.merge(lv, stack, 16);
            stacks.set(j, Pair.of(lv2, pair.getSecond()));
            if (!stack.isEmpty()) continue;
            return;
        }
        stacks.add(Pair.of(stack, pos));
    }

    public DamageSource getDamageSource() {
        return this.damageSource;
    }

    public Map<PlayerEntity, Vec3d> getAffectedPlayers() {
        return this.affectedPlayers;
    }

    @Nullable
    public LivingEntity getCausingEntity() {
        ProjectileEntity lv3;
        Entity lv4;
        if (this.entity == null) {
            return null;
        }
        Entity entity = this.entity;
        if (entity instanceof TntEntity) {
            TntEntity lv = (TntEntity)entity;
            return lv.getOwner();
        }
        entity = this.entity;
        if (entity instanceof LivingEntity) {
            LivingEntity lv2 = (LivingEntity)entity;
            return lv2;
        }
        entity = this.entity;
        if (entity instanceof ProjectileEntity && (lv4 = (lv3 = (ProjectileEntity)entity).getOwner()) instanceof LivingEntity) {
            LivingEntity lv5 = (LivingEntity)lv4;
            return lv5;
        }
        return null;
    }

    @Nullable
    public Entity getEntity() {
        return this.entity;
    }

    public void clearAffectedBlocks() {
        this.affectedBlocks.clear();
    }

    public List<BlockPos> getAffectedBlocks() {
        return this.affectedBlocks;
    }

    public static enum DestructionType {
        KEEP,
        DESTROY,
        DESTROY_WITH_DECAY;

    }
}

