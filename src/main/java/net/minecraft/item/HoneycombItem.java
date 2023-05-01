/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.item;

import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.GameEvent;

public class HoneycombItem
extends Item {
    public static final Supplier<BiMap<Block, Block>> UNWAXED_TO_WAXED_BLOCKS = Suppliers.memoize(() -> ((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)ImmutableBiMap.builder().put(Blocks.COPPER_BLOCK, Blocks.WAXED_COPPER_BLOCK)).put(Blocks.EXPOSED_COPPER, Blocks.WAXED_EXPOSED_COPPER)).put(Blocks.WEATHERED_COPPER, Blocks.WAXED_WEATHERED_COPPER)).put(Blocks.OXIDIZED_COPPER, Blocks.WAXED_OXIDIZED_COPPER)).put(Blocks.CUT_COPPER, Blocks.WAXED_CUT_COPPER)).put(Blocks.EXPOSED_CUT_COPPER, Blocks.WAXED_EXPOSED_CUT_COPPER)).put(Blocks.WEATHERED_CUT_COPPER, Blocks.WAXED_WEATHERED_CUT_COPPER)).put(Blocks.OXIDIZED_CUT_COPPER, Blocks.WAXED_OXIDIZED_CUT_COPPER)).put(Blocks.CUT_COPPER_SLAB, Blocks.WAXED_CUT_COPPER_SLAB)).put(Blocks.EXPOSED_CUT_COPPER_SLAB, Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB)).put(Blocks.WEATHERED_CUT_COPPER_SLAB, Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB)).put(Blocks.OXIDIZED_CUT_COPPER_SLAB, Blocks.WAXED_OXIDIZED_CUT_COPPER_SLAB)).put(Blocks.CUT_COPPER_STAIRS, Blocks.WAXED_CUT_COPPER_STAIRS)).put(Blocks.EXPOSED_CUT_COPPER_STAIRS, Blocks.WAXED_EXPOSED_CUT_COPPER_STAIRS)).put(Blocks.WEATHERED_CUT_COPPER_STAIRS, Blocks.WAXED_WEATHERED_CUT_COPPER_STAIRS)).put(Blocks.OXIDIZED_CUT_COPPER_STAIRS, Blocks.WAXED_OXIDIZED_CUT_COPPER_STAIRS)).build());
    public static final Supplier<BiMap<Block, Block>> WAXED_TO_UNWAXED_BLOCKS = Suppliers.memoize(() -> UNWAXED_TO_WAXED_BLOCKS.get().inverse());

    public HoneycombItem(Item.Settings arg) {
        super(arg);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World lv = context.getWorld();
        BlockPos lv2 = context.getBlockPos();
        BlockState lv3 = lv.getBlockState(lv2);
        return HoneycombItem.getWaxedState(lv3).map(state -> {
            PlayerEntity lv = context.getPlayer();
            ItemStack lv2 = context.getStack();
            if (lv instanceof ServerPlayerEntity) {
                Criteria.ITEM_USED_ON_BLOCK.trigger((ServerPlayerEntity)lv, lv2, lv2);
            }
            lv2.decrement(1);
            lv.setBlockState(lv2, (BlockState)state, Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
            lv.emitGameEvent(GameEvent.BLOCK_CHANGE, lv2, GameEvent.Emitter.of(lv, state));
            lv.syncWorldEvent(lv, WorldEvents.BLOCK_WAXED, lv2, 0);
            return ActionResult.success(arg3.isClient);
        }).orElse(ActionResult.PASS);
    }

    public static Optional<BlockState> getWaxedState(BlockState state) {
        return Optional.ofNullable((Block)UNWAXED_TO_WAXED_BLOCKS.get().get(state.getBlock())).map(block -> block.getStateWithProperties(state));
    }
}

