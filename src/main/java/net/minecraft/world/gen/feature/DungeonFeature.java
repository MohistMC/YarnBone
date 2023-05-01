/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.world.gen.feature;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.loot.LootTables;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.structure.StructurePiece;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;
import org.slf4j.Logger;

public class DungeonFeature
extends Feature<DefaultFeatureConfig> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final EntityType<?>[] MOB_SPAWNER_ENTITIES = new EntityType[]{EntityType.SKELETON, EntityType.ZOMBIE, EntityType.ZOMBIE, EntityType.SPIDER};
    private static final BlockState AIR = Blocks.CAVE_AIR.getDefaultState();

    public DungeonFeature(Codec<DefaultFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        BlockPos lv4;
        int u;
        int t;
        int s;
        Predicate<BlockState> predicate = Feature.notInBlockTagPredicate(BlockTags.FEATURES_CANNOT_REPLACE);
        BlockPos lv = context.getOrigin();
        Random lv2 = context.getRandom();
        StructureWorldAccess lv3 = context.getWorld();
        int i = 3;
        int j = lv2.nextInt(2) + 2;
        int k = -j - 1;
        int l = j + 1;
        int m = -1;
        int n = 4;
        int o = lv2.nextInt(2) + 2;
        int p = -o - 1;
        int q = o + 1;
        int r = 0;
        for (s = k; s <= l; ++s) {
            for (t = -1; t <= 4; ++t) {
                for (u = p; u <= q; ++u) {
                    lv4 = lv.add(s, t, u);
                    Material lv5 = lv3.getBlockState(lv4).getMaterial();
                    boolean bl = lv5.isSolid();
                    if (t == -1 && !bl) {
                        return false;
                    }
                    if (t == 4 && !bl) {
                        return false;
                    }
                    if (s != k && s != l && u != p && u != q || t != 0 || !lv3.isAir(lv4) || !lv3.isAir(lv4.up())) continue;
                    ++r;
                }
            }
        }
        if (r < 1 || r > 5) {
            return false;
        }
        for (s = k; s <= l; ++s) {
            for (t = 3; t >= -1; --t) {
                for (u = p; u <= q; ++u) {
                    lv4 = lv.add(s, t, u);
                    BlockState lv6 = lv3.getBlockState(lv4);
                    if (s == k || t == -1 || u == p || s == l || t == 4 || u == q) {
                        if (lv4.getY() >= lv3.getBottomY() && !lv3.getBlockState(lv4.down()).getMaterial().isSolid()) {
                            lv3.setBlockState(lv4, AIR, Block.NOTIFY_LISTENERS);
                            continue;
                        }
                        if (!lv6.getMaterial().isSolid() || lv6.isOf(Blocks.CHEST)) continue;
                        if (t == -1 && lv2.nextInt(4) != 0) {
                            this.setBlockStateIf(lv3, lv4, Blocks.MOSSY_COBBLESTONE.getDefaultState(), predicate);
                            continue;
                        }
                        this.setBlockStateIf(lv3, lv4, Blocks.COBBLESTONE.getDefaultState(), predicate);
                        continue;
                    }
                    if (lv6.isOf(Blocks.CHEST) || lv6.isOf(Blocks.SPAWNER)) continue;
                    this.setBlockStateIf(lv3, lv4, AIR, predicate);
                }
            }
        }
        block6: for (s = 0; s < 2; ++s) {
            for (t = 0; t < 3; ++t) {
                int w;
                int v;
                u = lv.getX() + lv2.nextInt(j * 2 + 1) - j;
                BlockPos lv7 = new BlockPos(u, v = lv.getY(), w = lv.getZ() + lv2.nextInt(o * 2 + 1) - o);
                if (!lv3.isAir(lv7)) continue;
                int x = 0;
                for (Direction lv8 : Direction.Type.HORIZONTAL) {
                    if (!lv3.getBlockState(lv7.offset(lv8)).getMaterial().isSolid()) continue;
                    ++x;
                }
                if (x != 1) continue;
                this.setBlockStateIf(lv3, lv7, StructurePiece.orientateChest(lv3, lv7, Blocks.CHEST.getDefaultState()), predicate);
                LootableContainerBlockEntity.setLootTable(lv3, lv2, lv7, LootTables.SIMPLE_DUNGEON_CHEST);
                continue block6;
            }
        }
        this.setBlockStateIf(lv3, lv, Blocks.SPAWNER.getDefaultState(), predicate);
        BlockEntity lv9 = lv3.getBlockEntity(lv);
        if (lv9 instanceof MobSpawnerBlockEntity) {
            MobSpawnerBlockEntity lv10 = (MobSpawnerBlockEntity)lv9;
            lv10.setEntityType(this.getMobSpawnerEntity(lv2), lv2);
        } else {
            LOGGER.error("Failed to fetch mob spawner entity at ({}, {}, {})", lv.getX(), lv.getY(), lv.getZ());
        }
        return true;
    }

    private EntityType<?> getMobSpawnerEntity(Random random) {
        return Util.getRandom(MOB_SPAWNER_ENTITIES, random);
    }
}

