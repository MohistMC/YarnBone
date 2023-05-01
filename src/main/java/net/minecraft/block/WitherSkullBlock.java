/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.block;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CarvedPumpkinBlock;
import net.minecraft.block.Material;
import net.minecraft.block.SkullBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.block.pattern.BlockPatternBuilder;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.predicate.block.BlockStatePredicate;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.function.MaterialPredicate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class WitherSkullBlock
extends SkullBlock {
    @Nullable
    private static BlockPattern witherBossPattern;
    @Nullable
    private static BlockPattern witherDispenserPattern;

    protected WitherSkullBlock(AbstractBlock.Settings arg) {
        super(SkullBlock.Type.WITHER_SKELETON, arg);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        BlockEntity lv = world.getBlockEntity(pos);
        if (lv instanceof SkullBlockEntity) {
            WitherSkullBlock.onPlaced(world, pos, (SkullBlockEntity)lv);
        }
    }

    public static void onPlaced(World world, BlockPos pos, SkullBlockEntity blockEntity) {
        boolean bl;
        if (world.isClient) {
            return;
        }
        BlockState lv = blockEntity.getCachedState();
        boolean bl2 = bl = lv.isOf(Blocks.WITHER_SKELETON_SKULL) || lv.isOf(Blocks.WITHER_SKELETON_WALL_SKULL);
        if (!bl || pos.getY() < world.getBottomY() || world.getDifficulty() == Difficulty.PEACEFUL) {
            return;
        }
        BlockPattern.Result lv2 = WitherSkullBlock.getWitherBossPattern().searchAround(world, pos);
        if (lv2 == null) {
            return;
        }
        WitherEntity lv3 = EntityType.WITHER.create(world);
        if (lv3 != null) {
            CarvedPumpkinBlock.breakPatternBlocks(world, lv2);
            BlockPos lv4 = lv2.translate(1, 2, 0).getBlockPos();
            lv3.refreshPositionAndAngles((double)lv4.getX() + 0.5, (double)lv4.getY() + 0.55, (double)lv4.getZ() + 0.5, lv2.getForwards().getAxis() == Direction.Axis.X ? 0.0f : 90.0f, 0.0f);
            lv3.bodyYaw = lv2.getForwards().getAxis() == Direction.Axis.X ? 0.0f : 90.0f;
            lv3.onSummoned();
            for (ServerPlayerEntity lv5 : world.getNonSpectatingEntities(ServerPlayerEntity.class, lv3.getBoundingBox().expand(50.0))) {
                Criteria.SUMMONED_ENTITY.trigger(lv5, lv3);
            }
            world.spawnEntity(lv3);
            CarvedPumpkinBlock.updatePatternBlocks(world, lv2);
        }
    }

    public static boolean canDispense(World world, BlockPos pos, ItemStack stack) {
        if (stack.isOf(Items.WITHER_SKELETON_SKULL) && pos.getY() >= world.getBottomY() + 2 && world.getDifficulty() != Difficulty.PEACEFUL && !world.isClient) {
            return WitherSkullBlock.getWitherDispenserPattern().searchAround(world, pos) != null;
        }
        return false;
    }

    private static BlockPattern getWitherBossPattern() {
        if (witherBossPattern == null) {
            witherBossPattern = BlockPatternBuilder.start().aisle("^^^", "###", "~#~").where('#', pos -> pos.getBlockState().isIn(BlockTags.WITHER_SUMMON_BASE_BLOCKS)).where('^', CachedBlockPosition.matchesBlockState(BlockStatePredicate.forBlock(Blocks.WITHER_SKELETON_SKULL).or(BlockStatePredicate.forBlock(Blocks.WITHER_SKELETON_WALL_SKULL)))).where('~', CachedBlockPosition.matchesBlockState(MaterialPredicate.create(Material.AIR))).build();
        }
        return witherBossPattern;
    }

    private static BlockPattern getWitherDispenserPattern() {
        if (witherDispenserPattern == null) {
            witherDispenserPattern = BlockPatternBuilder.start().aisle("   ", "###", "~#~").where('#', pos -> pos.getBlockState().isIn(BlockTags.WITHER_SUMMON_BASE_BLOCKS)).where('~', CachedBlockPosition.matchesBlockState(MaterialPredicate.create(Material.AIR))).build();
        }
        return witherDispenserPattern;
    }
}

