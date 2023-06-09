/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.block.cauldron;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BannerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public interface CauldronBehavior {
    public static final Map<Item, CauldronBehavior> EMPTY_CAULDRON_BEHAVIOR = CauldronBehavior.createMap();
    public static final Map<Item, CauldronBehavior> WATER_CAULDRON_BEHAVIOR = CauldronBehavior.createMap();
    public static final Map<Item, CauldronBehavior> LAVA_CAULDRON_BEHAVIOR = CauldronBehavior.createMap();
    public static final Map<Item, CauldronBehavior> POWDER_SNOW_CAULDRON_BEHAVIOR = CauldronBehavior.createMap();
    public static final CauldronBehavior FILL_WITH_WATER = (state, world, pos, player, hand, stack) -> CauldronBehavior.fillCauldron(world, pos, player, hand, stack, (BlockState)Blocks.WATER_CAULDRON.getDefaultState().with(LeveledCauldronBlock.LEVEL, 3), SoundEvents.ITEM_BUCKET_EMPTY);
    public static final CauldronBehavior FILL_WITH_LAVA = (state, world, pos, player, hand, stack) -> CauldronBehavior.fillCauldron(world, pos, player, hand, stack, Blocks.LAVA_CAULDRON.getDefaultState(), SoundEvents.ITEM_BUCKET_EMPTY_LAVA);
    public static final CauldronBehavior FILL_WITH_POWDER_SNOW = (state, world, pos, player, hand, stack) -> CauldronBehavior.fillCauldron(world, pos, player, hand, stack, (BlockState)Blocks.POWDER_SNOW_CAULDRON.getDefaultState().with(LeveledCauldronBlock.LEVEL, 3), SoundEvents.ITEM_BUCKET_EMPTY_POWDER_SNOW);
    public static final CauldronBehavior CLEAN_SHULKER_BOX = (state, world, pos, player, hand, stack) -> {
        Block lv = Block.getBlockFromItem(stack.getItem());
        if (!(lv instanceof ShulkerBoxBlock)) {
            return ActionResult.PASS;
        }
        if (!world.isClient) {
            ItemStack lv2 = new ItemStack(Blocks.SHULKER_BOX);
            if (stack.hasNbt()) {
                lv2.setNbt(stack.getNbt().copy());
            }
            player.setStackInHand(hand, lv2);
            player.incrementStat(Stats.CLEAN_SHULKER_BOX);
            LeveledCauldronBlock.decrementFluidLevel(state, world, pos);
        }
        return ActionResult.success(world.isClient);
    };
    public static final CauldronBehavior CLEAN_BANNER = (state, world, pos, player, hand, stack) -> {
        if (BannerBlockEntity.getPatternCount(stack) <= 0) {
            return ActionResult.PASS;
        }
        if (!world.isClient) {
            ItemStack lv = stack.copy();
            lv.setCount(1);
            BannerBlockEntity.loadFromItemStack(lv);
            if (!player.getAbilities().creativeMode) {
                stack.decrement(1);
            }
            if (stack.isEmpty()) {
                player.setStackInHand(hand, lv);
            } else if (player.getInventory().insertStack(lv)) {
                player.playerScreenHandler.syncState();
            } else {
                player.dropItem(lv, false);
            }
            player.incrementStat(Stats.CLEAN_BANNER);
            LeveledCauldronBlock.decrementFluidLevel(state, world, pos);
        }
        return ActionResult.success(world.isClient);
    };
    public static final CauldronBehavior CLEAN_DYEABLE_ITEM = (state, world, pos, player, hand, stack) -> {
        Item lv = stack.getItem();
        if (!(lv instanceof DyeableItem)) {
            return ActionResult.PASS;
        }
        DyeableItem lv2 = (DyeableItem)((Object)lv);
        if (!lv2.hasColor(stack)) {
            return ActionResult.PASS;
        }
        if (!world.isClient) {
            lv2.removeColor(stack);
            player.incrementStat(Stats.CLEAN_ARMOR);
            LeveledCauldronBlock.decrementFluidLevel(state, world, pos);
        }
        return ActionResult.success(world.isClient);
    };

    public static Object2ObjectOpenHashMap<Item, CauldronBehavior> createMap() {
        return Util.make(new Object2ObjectOpenHashMap(), map -> map.defaultReturnValue((state, world, pos, player, hand, stack) -> ActionResult.PASS));
    }

    public ActionResult interact(BlockState var1, World var2, BlockPos var3, PlayerEntity var4, Hand var5, ItemStack var6);

    public static void registerBehavior() {
        CauldronBehavior.registerBucketBehavior(EMPTY_CAULDRON_BEHAVIOR);
        EMPTY_CAULDRON_BEHAVIOR.put(Items.POTION, (state, world, pos, player, hand, stack) -> {
            if (PotionUtil.getPotion(stack) != Potions.WATER) {
                return ActionResult.PASS;
            }
            if (!world.isClient) {
                Item lv = stack.getItem();
                player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
                player.incrementStat(Stats.USE_CAULDRON);
                player.incrementStat(Stats.USED.getOrCreateStat(lv));
                world.setBlockState(pos, Blocks.WATER_CAULDRON.getDefaultState());
                world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0f, 1.0f);
                world.emitGameEvent(null, GameEvent.FLUID_PLACE, pos);
            }
            return ActionResult.success(world.isClient);
        });
        CauldronBehavior.registerBucketBehavior(WATER_CAULDRON_BEHAVIOR);
        WATER_CAULDRON_BEHAVIOR.put(Items.BUCKET, (state2, world, pos, player, hand, stack) -> CauldronBehavior.emptyCauldron(state2, world, pos, player, hand, stack, new ItemStack(Items.WATER_BUCKET), state -> state.get(LeveledCauldronBlock.LEVEL) == 3, SoundEvents.ITEM_BUCKET_FILL));
        WATER_CAULDRON_BEHAVIOR.put(Items.GLASS_BOTTLE, (state, world, pos, player, hand, stack) -> {
            if (!world.isClient) {
                Item lv = stack.getItem();
                player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, PotionUtil.setPotion(new ItemStack(Items.POTION), Potions.WATER)));
                player.incrementStat(Stats.USE_CAULDRON);
                player.incrementStat(Stats.USED.getOrCreateStat(lv));
                LeveledCauldronBlock.decrementFluidLevel(state, world, pos);
                world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS, 1.0f, 1.0f);
                world.emitGameEvent(null, GameEvent.FLUID_PICKUP, pos);
            }
            return ActionResult.success(world.isClient);
        });
        WATER_CAULDRON_BEHAVIOR.put(Items.POTION, (state, world, pos, player, hand, stack) -> {
            if (state.get(LeveledCauldronBlock.LEVEL) == 3 || PotionUtil.getPotion(stack) != Potions.WATER) {
                return ActionResult.PASS;
            }
            if (!world.isClient) {
                player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
                player.incrementStat(Stats.USE_CAULDRON);
                player.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
                world.setBlockState(pos, (BlockState)state.cycle(LeveledCauldronBlock.LEVEL));
                world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0f, 1.0f);
                world.emitGameEvent(null, GameEvent.FLUID_PLACE, pos);
            }
            return ActionResult.success(world.isClient);
        });
        WATER_CAULDRON_BEHAVIOR.put(Items.LEATHER_BOOTS, CLEAN_DYEABLE_ITEM);
        WATER_CAULDRON_BEHAVIOR.put(Items.LEATHER_LEGGINGS, CLEAN_DYEABLE_ITEM);
        WATER_CAULDRON_BEHAVIOR.put(Items.LEATHER_CHESTPLATE, CLEAN_DYEABLE_ITEM);
        WATER_CAULDRON_BEHAVIOR.put(Items.LEATHER_HELMET, CLEAN_DYEABLE_ITEM);
        WATER_CAULDRON_BEHAVIOR.put(Items.LEATHER_HORSE_ARMOR, CLEAN_DYEABLE_ITEM);
        WATER_CAULDRON_BEHAVIOR.put(Items.WHITE_BANNER, CLEAN_BANNER);
        WATER_CAULDRON_BEHAVIOR.put(Items.GRAY_BANNER, CLEAN_BANNER);
        WATER_CAULDRON_BEHAVIOR.put(Items.BLACK_BANNER, CLEAN_BANNER);
        WATER_CAULDRON_BEHAVIOR.put(Items.BLUE_BANNER, CLEAN_BANNER);
        WATER_CAULDRON_BEHAVIOR.put(Items.BROWN_BANNER, CLEAN_BANNER);
        WATER_CAULDRON_BEHAVIOR.put(Items.CYAN_BANNER, CLEAN_BANNER);
        WATER_CAULDRON_BEHAVIOR.put(Items.GREEN_BANNER, CLEAN_BANNER);
        WATER_CAULDRON_BEHAVIOR.put(Items.LIGHT_BLUE_BANNER, CLEAN_BANNER);
        WATER_CAULDRON_BEHAVIOR.put(Items.LIGHT_GRAY_BANNER, CLEAN_BANNER);
        WATER_CAULDRON_BEHAVIOR.put(Items.LIME_BANNER, CLEAN_BANNER);
        WATER_CAULDRON_BEHAVIOR.put(Items.MAGENTA_BANNER, CLEAN_BANNER);
        WATER_CAULDRON_BEHAVIOR.put(Items.ORANGE_BANNER, CLEAN_BANNER);
        WATER_CAULDRON_BEHAVIOR.put(Items.PINK_BANNER, CLEAN_BANNER);
        WATER_CAULDRON_BEHAVIOR.put(Items.PURPLE_BANNER, CLEAN_BANNER);
        WATER_CAULDRON_BEHAVIOR.put(Items.RED_BANNER, CLEAN_BANNER);
        WATER_CAULDRON_BEHAVIOR.put(Items.YELLOW_BANNER, CLEAN_BANNER);
        WATER_CAULDRON_BEHAVIOR.put(Items.WHITE_SHULKER_BOX, CLEAN_SHULKER_BOX);
        WATER_CAULDRON_BEHAVIOR.put(Items.GRAY_SHULKER_BOX, CLEAN_SHULKER_BOX);
        WATER_CAULDRON_BEHAVIOR.put(Items.BLACK_SHULKER_BOX, CLEAN_SHULKER_BOX);
        WATER_CAULDRON_BEHAVIOR.put(Items.BLUE_SHULKER_BOX, CLEAN_SHULKER_BOX);
        WATER_CAULDRON_BEHAVIOR.put(Items.BROWN_SHULKER_BOX, CLEAN_SHULKER_BOX);
        WATER_CAULDRON_BEHAVIOR.put(Items.CYAN_SHULKER_BOX, CLEAN_SHULKER_BOX);
        WATER_CAULDRON_BEHAVIOR.put(Items.GREEN_SHULKER_BOX, CLEAN_SHULKER_BOX);
        WATER_CAULDRON_BEHAVIOR.put(Items.LIGHT_BLUE_SHULKER_BOX, CLEAN_SHULKER_BOX);
        WATER_CAULDRON_BEHAVIOR.put(Items.LIGHT_GRAY_SHULKER_BOX, CLEAN_SHULKER_BOX);
        WATER_CAULDRON_BEHAVIOR.put(Items.LIME_SHULKER_BOX, CLEAN_SHULKER_BOX);
        WATER_CAULDRON_BEHAVIOR.put(Items.MAGENTA_SHULKER_BOX, CLEAN_SHULKER_BOX);
        WATER_CAULDRON_BEHAVIOR.put(Items.ORANGE_SHULKER_BOX, CLEAN_SHULKER_BOX);
        WATER_CAULDRON_BEHAVIOR.put(Items.PINK_SHULKER_BOX, CLEAN_SHULKER_BOX);
        WATER_CAULDRON_BEHAVIOR.put(Items.PURPLE_SHULKER_BOX, CLEAN_SHULKER_BOX);
        WATER_CAULDRON_BEHAVIOR.put(Items.RED_SHULKER_BOX, CLEAN_SHULKER_BOX);
        WATER_CAULDRON_BEHAVIOR.put(Items.YELLOW_SHULKER_BOX, CLEAN_SHULKER_BOX);
        LAVA_CAULDRON_BEHAVIOR.put(Items.BUCKET, (state2, world, pos, player, hand, stack) -> CauldronBehavior.emptyCauldron(state2, world, pos, player, hand, stack, new ItemStack(Items.LAVA_BUCKET), state -> true, SoundEvents.ITEM_BUCKET_FILL_LAVA));
        CauldronBehavior.registerBucketBehavior(LAVA_CAULDRON_BEHAVIOR);
        POWDER_SNOW_CAULDRON_BEHAVIOR.put(Items.BUCKET, (state2, world, pos, player, hand, stack) -> CauldronBehavior.emptyCauldron(state2, world, pos, player, hand, stack, new ItemStack(Items.POWDER_SNOW_BUCKET), state -> state.get(LeveledCauldronBlock.LEVEL) == 3, SoundEvents.ITEM_BUCKET_FILL_POWDER_SNOW));
        CauldronBehavior.registerBucketBehavior(POWDER_SNOW_CAULDRON_BEHAVIOR);
    }

    public static void registerBucketBehavior(Map<Item, CauldronBehavior> behavior) {
        behavior.put(Items.LAVA_BUCKET, FILL_WITH_LAVA);
        behavior.put(Items.WATER_BUCKET, FILL_WITH_WATER);
        behavior.put(Items.POWDER_SNOW_BUCKET, FILL_WITH_POWDER_SNOW);
    }

    public static ActionResult emptyCauldron(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack, ItemStack output, Predicate<BlockState> fullPredicate, SoundEvent soundEvent) {
        if (!fullPredicate.test(state)) {
            return ActionResult.PASS;
        }
        if (!world.isClient) {
            Item lv = stack.getItem();
            player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, output));
            player.incrementStat(Stats.USE_CAULDRON);
            player.incrementStat(Stats.USED.getOrCreateStat(lv));
            world.setBlockState(pos, Blocks.CAULDRON.getDefaultState());
            world.playSound(null, pos, soundEvent, SoundCategory.BLOCKS, 1.0f, 1.0f);
            world.emitGameEvent(null, GameEvent.FLUID_PICKUP, pos);
        }
        return ActionResult.success(world.isClient);
    }

    public static ActionResult fillCauldron(World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack, BlockState state, SoundEvent soundEvent) {
        if (!world.isClient) {
            Item lv = stack.getItem();
            player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(Items.BUCKET)));
            player.incrementStat(Stats.FILL_CAULDRON);
            player.incrementStat(Stats.USED.getOrCreateStat(lv));
            world.setBlockState(pos, state);
            world.playSound(null, pos, soundEvent, SoundCategory.BLOCKS, 1.0f, 1.0f);
            world.emitGameEvent(null, GameEvent.FLUID_PLACE, pos);
        }
        return ActionResult.success(world.isClient);
    }
}

