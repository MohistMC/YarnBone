/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.block;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.SilverfishEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;

public class InfestedBlock
extends Block {
    private final Block regularBlock;
    private static final Map<Block, Block> REGULAR_TO_INFESTED_BLOCK = Maps.newIdentityHashMap();
    private static final Map<BlockState, BlockState> REGULAR_TO_INFESTED_STATE = Maps.newIdentityHashMap();
    private static final Map<BlockState, BlockState> INFESTED_TO_REGULAR_STATE = Maps.newIdentityHashMap();

    public InfestedBlock(Block regularBlock, AbstractBlock.Settings settings) {
        super(settings.hardness(regularBlock.getHardness() / 2.0f).resistance(0.75f));
        this.regularBlock = regularBlock;
        REGULAR_TO_INFESTED_BLOCK.put(regularBlock, this);
    }

    public Block getRegularBlock() {
        return this.regularBlock;
    }

    public static boolean isInfestable(BlockState block) {
        return REGULAR_TO_INFESTED_BLOCK.containsKey(block.getBlock());
    }

    private void spawnSilverfish(ServerWorld world, BlockPos pos) {
        SilverfishEntity lv = EntityType.SILVERFISH.create(world);
        if (lv != null) {
            lv.refreshPositionAndAngles((double)pos.getX() + 0.5, pos.getY(), (double)pos.getZ() + 0.5, 0.0f, 0.0f);
            world.spawnEntity(lv);
            lv.playSpawnEffects();
        }
    }

    @Override
    public void onStacksDropped(BlockState state, ServerWorld world, BlockPos pos, ItemStack tool, boolean dropExperience) {
        super.onStacksDropped(state, world, pos, tool, dropExperience);
        if (world.getGameRules().getBoolean(GameRules.DO_TILE_DROPS) && EnchantmentHelper.getLevel(Enchantments.SILK_TOUCH, tool) == 0) {
            this.spawnSilverfish(world, pos);
        }
    }

    public static BlockState fromRegularState(BlockState regularState) {
        return InfestedBlock.copyProperties(REGULAR_TO_INFESTED_STATE, regularState, () -> REGULAR_TO_INFESTED_BLOCK.get(regularState.getBlock()).getDefaultState());
    }

    public BlockState toRegularState(BlockState infestedState) {
        return InfestedBlock.copyProperties(INFESTED_TO_REGULAR_STATE, infestedState, () -> this.getRegularBlock().getDefaultState());
    }

    private static BlockState copyProperties(Map<BlockState, BlockState> stateMap, BlockState fromState, Supplier<BlockState> toStateSupplier) {
        return stateMap.computeIfAbsent(fromState, infestedState -> {
            BlockState lv = (BlockState)toStateSupplier.get();
            for (Property<?> lv2 : infestedState.getProperties()) {
                lv = lv.contains(lv2) ? (BlockState)lv.with(lv2, infestedState.get(lv2)) : lv;
            }
            return lv;
        });
    }
}

