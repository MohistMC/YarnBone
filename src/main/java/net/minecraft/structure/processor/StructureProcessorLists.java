/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.structure.processor;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.PaneBlock;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.structure.processor.BlockRotStructureProcessor;
import net.minecraft.structure.processor.ProtectedBlocksStructureProcessor;
import net.minecraft.structure.processor.RuleStructureProcessor;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorList;
import net.minecraft.structure.processor.StructureProcessorRule;
import net.minecraft.structure.rule.AlwaysTrueRuleTest;
import net.minecraft.structure.rule.AxisAlignedLinearPosRuleTest;
import net.minecraft.structure.rule.BlockMatchRuleTest;
import net.minecraft.structure.rule.BlockStateMatchRuleTest;
import net.minecraft.structure.rule.RandomBlockMatchRuleTest;
import net.minecraft.structure.rule.TagMatchRuleTest;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

public class StructureProcessorLists {
    private static final RegistryKey<StructureProcessorList> EMPTY = StructureProcessorLists.of("empty");
    public static final RegistryKey<StructureProcessorList> ZOMBIE_PLAINS = StructureProcessorLists.of("zombie_plains");
    public static final RegistryKey<StructureProcessorList> ZOMBIE_SAVANNA = StructureProcessorLists.of("zombie_savanna");
    public static final RegistryKey<StructureProcessorList> ZOMBIE_SNOWY = StructureProcessorLists.of("zombie_snowy");
    public static final RegistryKey<StructureProcessorList> ZOMBIE_TAIGA = StructureProcessorLists.of("zombie_taiga");
    public static final RegistryKey<StructureProcessorList> ZOMBIE_DESERT = StructureProcessorLists.of("zombie_desert");
    public static final RegistryKey<StructureProcessorList> MOSSIFY_10_PERCENT = StructureProcessorLists.of("mossify_10_percent");
    public static final RegistryKey<StructureProcessorList> MOSSIFY_20_PERCENT = StructureProcessorLists.of("mossify_20_percent");
    public static final RegistryKey<StructureProcessorList> MOSSIFY_70_PERCENT = StructureProcessorLists.of("mossify_70_percent");
    public static final RegistryKey<StructureProcessorList> STREET_PLAINS = StructureProcessorLists.of("street_plains");
    public static final RegistryKey<StructureProcessorList> STREET_SAVANNA = StructureProcessorLists.of("street_savanna");
    public static final RegistryKey<StructureProcessorList> STREET_SNOWY_OR_TAIGA = StructureProcessorLists.of("street_snowy_or_taiga");
    public static final RegistryKey<StructureProcessorList> FARM_PLAINS = StructureProcessorLists.of("farm_plains");
    public static final RegistryKey<StructureProcessorList> FARM_SAVANNA = StructureProcessorLists.of("farm_savanna");
    public static final RegistryKey<StructureProcessorList> FARM_SNOWY = StructureProcessorLists.of("farm_snowy");
    public static final RegistryKey<StructureProcessorList> FARM_TAIGA = StructureProcessorLists.of("farm_taiga");
    public static final RegistryKey<StructureProcessorList> FARM_DESERT = StructureProcessorLists.of("farm_desert");
    public static final RegistryKey<StructureProcessorList> OUTPOST_ROT = StructureProcessorLists.of("outpost_rot");
    public static final RegistryKey<StructureProcessorList> BOTTOM_RAMPART = StructureProcessorLists.of("bottom_rampart");
    public static final RegistryKey<StructureProcessorList> TREASURE_ROOMS = StructureProcessorLists.of("treasure_rooms");
    public static final RegistryKey<StructureProcessorList> HOUSING = StructureProcessorLists.of("housing");
    public static final RegistryKey<StructureProcessorList> SIDE_WALL_DEGRADATION = StructureProcessorLists.of("side_wall_degradation");
    public static final RegistryKey<StructureProcessorList> STABLE_DEGRADATION = StructureProcessorLists.of("stable_degradation");
    public static final RegistryKey<StructureProcessorList> BASTION_GENERIC_DEGRADATION = StructureProcessorLists.of("bastion_generic_degradation");
    public static final RegistryKey<StructureProcessorList> RAMPART_DEGRADATION = StructureProcessorLists.of("rampart_degradation");
    public static final RegistryKey<StructureProcessorList> ENTRANCE_REPLACEMENT = StructureProcessorLists.of("entrance_replacement");
    public static final RegistryKey<StructureProcessorList> BRIDGE = StructureProcessorLists.of("bridge");
    public static final RegistryKey<StructureProcessorList> ROOF = StructureProcessorLists.of("roof");
    public static final RegistryKey<StructureProcessorList> HIGH_WALL = StructureProcessorLists.of("high_wall");
    public static final RegistryKey<StructureProcessorList> HIGH_RAMPART = StructureProcessorLists.of("high_rampart");
    public static final RegistryKey<StructureProcessorList> FOSSIL_ROT = StructureProcessorLists.of("fossil_rot");
    public static final RegistryKey<StructureProcessorList> FOSSIL_COAL = StructureProcessorLists.of("fossil_coal");
    public static final RegistryKey<StructureProcessorList> FOSSIL_DIAMONDS = StructureProcessorLists.of("fossil_diamonds");
    public static final RegistryKey<StructureProcessorList> ANCIENT_CITY_START_DEGRADATION = StructureProcessorLists.of("ancient_city_start_degradation");
    public static final RegistryKey<StructureProcessorList> ANCIENT_CITY_GENERIC_DEGRADATION = StructureProcessorLists.of("ancient_city_generic_degradation");
    public static final RegistryKey<StructureProcessorList> ANCIENT_CITY_WALLS_DEGRADATION = StructureProcessorLists.of("ancient_city_walls_degradation");

    private static RegistryKey<StructureProcessorList> of(String id) {
        return RegistryKey.of(RegistryKeys.PROCESSOR_LIST, new Identifier(id));
    }

    private static void register(Registerable<StructureProcessorList> processorListRegisterable, RegistryKey<StructureProcessorList> key, List<StructureProcessor> processors) {
        processorListRegisterable.register(key, new StructureProcessorList(processors));
    }

    public static void bootstrap(Registerable<StructureProcessorList> processorListRegisterable) {
        RegistryEntryLookup<Block> lv = processorListRegisterable.getRegistryLookup(RegistryKeys.BLOCK);
        StructureProcessorRule lv2 = new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.BLACKSTONE, 0.01f), AlwaysTrueRuleTest.INSTANCE, Blocks.GILDED_BLACKSTONE.getDefaultState());
        StructureProcessorRule lv3 = new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.GILDED_BLACKSTONE, 0.5f), AlwaysTrueRuleTest.INSTANCE, Blocks.BLACKSTONE.getDefaultState());
        StructureProcessorLists.register(processorListRegisterable, EMPTY, ImmutableList.of());
        StructureProcessorLists.register(processorListRegisterable, ZOMBIE_PLAINS, ImmutableList.of(new RuleStructureProcessor(ImmutableList.of(new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.COBBLESTONE, 0.8f), AlwaysTrueRuleTest.INSTANCE, Blocks.MOSSY_COBBLESTONE.getDefaultState()), new StructureProcessorRule(new TagMatchRuleTest(BlockTags.DOORS), AlwaysTrueRuleTest.INSTANCE, Blocks.AIR.getDefaultState()), new StructureProcessorRule(new BlockMatchRuleTest(Blocks.TORCH), AlwaysTrueRuleTest.INSTANCE, Blocks.AIR.getDefaultState()), new StructureProcessorRule(new BlockMatchRuleTest(Blocks.WALL_TORCH), AlwaysTrueRuleTest.INSTANCE, Blocks.AIR.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.COBBLESTONE, 0.07f), AlwaysTrueRuleTest.INSTANCE, Blocks.COBWEB.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.MOSSY_COBBLESTONE, 0.07f), AlwaysTrueRuleTest.INSTANCE, Blocks.COBWEB.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.WHITE_TERRACOTTA, 0.07f), AlwaysTrueRuleTest.INSTANCE, Blocks.COBWEB.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.OAK_LOG, 0.05f), AlwaysTrueRuleTest.INSTANCE, Blocks.COBWEB.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.OAK_PLANKS, 0.1f), AlwaysTrueRuleTest.INSTANCE, Blocks.COBWEB.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.OAK_STAIRS, 0.1f), AlwaysTrueRuleTest.INSTANCE, Blocks.COBWEB.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.STRIPPED_OAK_LOG, 0.02f), AlwaysTrueRuleTest.INSTANCE, Blocks.COBWEB.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.GLASS_PANE, 0.5f), AlwaysTrueRuleTest.INSTANCE, Blocks.COBWEB.getDefaultState()), new StructureProcessorRule[]{new StructureProcessorRule(new BlockStateMatchRuleTest((BlockState)((BlockState)Blocks.GLASS_PANE.getDefaultState().with(PaneBlock.NORTH, true)).with(PaneBlock.SOUTH, true)), AlwaysTrueRuleTest.INSTANCE, (BlockState)((BlockState)Blocks.BROWN_STAINED_GLASS_PANE.getDefaultState().with(PaneBlock.NORTH, true)).with(PaneBlock.SOUTH, true)), new StructureProcessorRule(new BlockStateMatchRuleTest((BlockState)((BlockState)Blocks.GLASS_PANE.getDefaultState().with(PaneBlock.EAST, true)).with(PaneBlock.WEST, true)), AlwaysTrueRuleTest.INSTANCE, (BlockState)((BlockState)Blocks.BROWN_STAINED_GLASS_PANE.getDefaultState().with(PaneBlock.EAST, true)).with(PaneBlock.WEST, true)), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.WHEAT, 0.3f), AlwaysTrueRuleTest.INSTANCE, Blocks.CARROTS.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.WHEAT, 0.2f), AlwaysTrueRuleTest.INSTANCE, Blocks.POTATOES.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.WHEAT, 0.1f), AlwaysTrueRuleTest.INSTANCE, Blocks.BEETROOTS.getDefaultState())}))));
        StructureProcessorLists.register(processorListRegisterable, ZOMBIE_SAVANNA, ImmutableList.of(new RuleStructureProcessor(ImmutableList.of(new StructureProcessorRule(new TagMatchRuleTest(BlockTags.DOORS), AlwaysTrueRuleTest.INSTANCE, Blocks.AIR.getDefaultState()), new StructureProcessorRule(new BlockMatchRuleTest(Blocks.TORCH), AlwaysTrueRuleTest.INSTANCE, Blocks.AIR.getDefaultState()), new StructureProcessorRule(new BlockMatchRuleTest(Blocks.WALL_TORCH), AlwaysTrueRuleTest.INSTANCE, Blocks.AIR.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.ACACIA_PLANKS, 0.2f), AlwaysTrueRuleTest.INSTANCE, Blocks.COBWEB.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.ACACIA_STAIRS, 0.2f), AlwaysTrueRuleTest.INSTANCE, Blocks.COBWEB.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.ACACIA_LOG, 0.05f), AlwaysTrueRuleTest.INSTANCE, Blocks.COBWEB.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.ACACIA_WOOD, 0.05f), AlwaysTrueRuleTest.INSTANCE, Blocks.COBWEB.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.ORANGE_TERRACOTTA, 0.05f), AlwaysTrueRuleTest.INSTANCE, Blocks.COBWEB.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.YELLOW_TERRACOTTA, 0.05f), AlwaysTrueRuleTest.INSTANCE, Blocks.COBWEB.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.RED_TERRACOTTA, 0.05f), AlwaysTrueRuleTest.INSTANCE, Blocks.COBWEB.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.GLASS_PANE, 0.5f), AlwaysTrueRuleTest.INSTANCE, Blocks.COBWEB.getDefaultState()), new StructureProcessorRule(new BlockStateMatchRuleTest((BlockState)((BlockState)Blocks.GLASS_PANE.getDefaultState().with(PaneBlock.NORTH, true)).with(PaneBlock.SOUTH, true)), AlwaysTrueRuleTest.INSTANCE, (BlockState)((BlockState)Blocks.BROWN_STAINED_GLASS_PANE.getDefaultState().with(PaneBlock.NORTH, true)).with(PaneBlock.SOUTH, true)), new StructureProcessorRule[]{new StructureProcessorRule(new BlockStateMatchRuleTest((BlockState)((BlockState)Blocks.GLASS_PANE.getDefaultState().with(PaneBlock.EAST, true)).with(PaneBlock.WEST, true)), AlwaysTrueRuleTest.INSTANCE, (BlockState)((BlockState)Blocks.BROWN_STAINED_GLASS_PANE.getDefaultState().with(PaneBlock.EAST, true)).with(PaneBlock.WEST, true)), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.WHEAT, 0.1f), AlwaysTrueRuleTest.INSTANCE, Blocks.MELON_STEM.getDefaultState())}))));
        StructureProcessorLists.register(processorListRegisterable, ZOMBIE_SNOWY, ImmutableList.of(new RuleStructureProcessor(ImmutableList.of(new StructureProcessorRule(new TagMatchRuleTest(BlockTags.DOORS), AlwaysTrueRuleTest.INSTANCE, Blocks.AIR.getDefaultState()), new StructureProcessorRule(new BlockMatchRuleTest(Blocks.TORCH), AlwaysTrueRuleTest.INSTANCE, Blocks.AIR.getDefaultState()), new StructureProcessorRule(new BlockMatchRuleTest(Blocks.WALL_TORCH), AlwaysTrueRuleTest.INSTANCE, Blocks.AIR.getDefaultState()), new StructureProcessorRule(new BlockMatchRuleTest(Blocks.LANTERN), AlwaysTrueRuleTest.INSTANCE, Blocks.AIR.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.SPRUCE_PLANKS, 0.2f), AlwaysTrueRuleTest.INSTANCE, Blocks.COBWEB.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.SPRUCE_SLAB, 0.4f), AlwaysTrueRuleTest.INSTANCE, Blocks.COBWEB.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.STRIPPED_SPRUCE_LOG, 0.05f), AlwaysTrueRuleTest.INSTANCE, Blocks.COBWEB.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.STRIPPED_SPRUCE_WOOD, 0.05f), AlwaysTrueRuleTest.INSTANCE, Blocks.COBWEB.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.GLASS_PANE, 0.5f), AlwaysTrueRuleTest.INSTANCE, Blocks.COBWEB.getDefaultState()), new StructureProcessorRule(new BlockStateMatchRuleTest((BlockState)((BlockState)Blocks.GLASS_PANE.getDefaultState().with(PaneBlock.NORTH, true)).with(PaneBlock.SOUTH, true)), AlwaysTrueRuleTest.INSTANCE, (BlockState)((BlockState)Blocks.BROWN_STAINED_GLASS_PANE.getDefaultState().with(PaneBlock.NORTH, true)).with(PaneBlock.SOUTH, true)), new StructureProcessorRule(new BlockStateMatchRuleTest((BlockState)((BlockState)Blocks.GLASS_PANE.getDefaultState().with(PaneBlock.EAST, true)).with(PaneBlock.WEST, true)), AlwaysTrueRuleTest.INSTANCE, (BlockState)((BlockState)Blocks.BROWN_STAINED_GLASS_PANE.getDefaultState().with(PaneBlock.EAST, true)).with(PaneBlock.WEST, true)), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.WHEAT, 0.1f), AlwaysTrueRuleTest.INSTANCE, Blocks.CARROTS.getDefaultState()), new StructureProcessorRule[]{new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.WHEAT, 0.8f), AlwaysTrueRuleTest.INSTANCE, Blocks.POTATOES.getDefaultState())}))));
        StructureProcessorLists.register(processorListRegisterable, ZOMBIE_TAIGA, ImmutableList.of(new RuleStructureProcessor(ImmutableList.of(new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.COBBLESTONE, 0.8f), AlwaysTrueRuleTest.INSTANCE, Blocks.MOSSY_COBBLESTONE.getDefaultState()), new StructureProcessorRule(new TagMatchRuleTest(BlockTags.DOORS), AlwaysTrueRuleTest.INSTANCE, Blocks.AIR.getDefaultState()), new StructureProcessorRule(new BlockMatchRuleTest(Blocks.TORCH), AlwaysTrueRuleTest.INSTANCE, Blocks.AIR.getDefaultState()), new StructureProcessorRule(new BlockMatchRuleTest(Blocks.WALL_TORCH), AlwaysTrueRuleTest.INSTANCE, Blocks.AIR.getDefaultState()), new StructureProcessorRule(new BlockMatchRuleTest(Blocks.CAMPFIRE), AlwaysTrueRuleTest.INSTANCE, (BlockState)Blocks.CAMPFIRE.getDefaultState().with(CampfireBlock.LIT, false)), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.COBBLESTONE, 0.08f), AlwaysTrueRuleTest.INSTANCE, Blocks.COBWEB.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.SPRUCE_LOG, 0.08f), AlwaysTrueRuleTest.INSTANCE, Blocks.COBWEB.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.GLASS_PANE, 0.5f), AlwaysTrueRuleTest.INSTANCE, Blocks.COBWEB.getDefaultState()), new StructureProcessorRule(new BlockStateMatchRuleTest((BlockState)((BlockState)Blocks.GLASS_PANE.getDefaultState().with(PaneBlock.NORTH, true)).with(PaneBlock.SOUTH, true)), AlwaysTrueRuleTest.INSTANCE, (BlockState)((BlockState)Blocks.BROWN_STAINED_GLASS_PANE.getDefaultState().with(PaneBlock.NORTH, true)).with(PaneBlock.SOUTH, true)), new StructureProcessorRule(new BlockStateMatchRuleTest((BlockState)((BlockState)Blocks.GLASS_PANE.getDefaultState().with(PaneBlock.EAST, true)).with(PaneBlock.WEST, true)), AlwaysTrueRuleTest.INSTANCE, (BlockState)((BlockState)Blocks.BROWN_STAINED_GLASS_PANE.getDefaultState().with(PaneBlock.EAST, true)).with(PaneBlock.WEST, true)), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.WHEAT, 0.3f), AlwaysTrueRuleTest.INSTANCE, Blocks.PUMPKIN_STEM.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.WHEAT, 0.2f), AlwaysTrueRuleTest.INSTANCE, Blocks.POTATOES.getDefaultState()), new StructureProcessorRule[0]))));
        StructureProcessorLists.register(processorListRegisterable, ZOMBIE_DESERT, ImmutableList.of(new RuleStructureProcessor(ImmutableList.of(new StructureProcessorRule(new TagMatchRuleTest(BlockTags.DOORS), AlwaysTrueRuleTest.INSTANCE, Blocks.AIR.getDefaultState()), new StructureProcessorRule(new BlockMatchRuleTest(Blocks.TORCH), AlwaysTrueRuleTest.INSTANCE, Blocks.AIR.getDefaultState()), new StructureProcessorRule(new BlockMatchRuleTest(Blocks.WALL_TORCH), AlwaysTrueRuleTest.INSTANCE, Blocks.AIR.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.SMOOTH_SANDSTONE, 0.08f), AlwaysTrueRuleTest.INSTANCE, Blocks.COBWEB.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.CUT_SANDSTONE, 0.1f), AlwaysTrueRuleTest.INSTANCE, Blocks.COBWEB.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.TERRACOTTA, 0.08f), AlwaysTrueRuleTest.INSTANCE, Blocks.COBWEB.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.SMOOTH_SANDSTONE_STAIRS, 0.08f), AlwaysTrueRuleTest.INSTANCE, Blocks.COBWEB.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.SMOOTH_SANDSTONE_SLAB, 0.08f), AlwaysTrueRuleTest.INSTANCE, Blocks.COBWEB.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.WHEAT, 0.2f), AlwaysTrueRuleTest.INSTANCE, Blocks.BEETROOTS.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.WHEAT, 0.1f), AlwaysTrueRuleTest.INSTANCE, Blocks.MELON_STEM.getDefaultState())))));
        StructureProcessorLists.register(processorListRegisterable, MOSSIFY_10_PERCENT, ImmutableList.of(new RuleStructureProcessor(ImmutableList.of(new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.COBBLESTONE, 0.1f), AlwaysTrueRuleTest.INSTANCE, Blocks.MOSSY_COBBLESTONE.getDefaultState())))));
        StructureProcessorLists.register(processorListRegisterable, MOSSIFY_20_PERCENT, ImmutableList.of(new RuleStructureProcessor(ImmutableList.of(new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.COBBLESTONE, 0.2f), AlwaysTrueRuleTest.INSTANCE, Blocks.MOSSY_COBBLESTONE.getDefaultState())))));
        StructureProcessorLists.register(processorListRegisterable, MOSSIFY_70_PERCENT, ImmutableList.of(new RuleStructureProcessor(ImmutableList.of(new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.COBBLESTONE, 0.7f), AlwaysTrueRuleTest.INSTANCE, Blocks.MOSSY_COBBLESTONE.getDefaultState())))));
        StructureProcessorLists.register(processorListRegisterable, STREET_PLAINS, ImmutableList.of(new RuleStructureProcessor(ImmutableList.of(new StructureProcessorRule(new BlockMatchRuleTest(Blocks.DIRT_PATH), new BlockMatchRuleTest(Blocks.WATER), Blocks.OAK_PLANKS.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.DIRT_PATH, 0.1f), AlwaysTrueRuleTest.INSTANCE, Blocks.GRASS_BLOCK.getDefaultState()), new StructureProcessorRule(new BlockMatchRuleTest(Blocks.GRASS_BLOCK), new BlockMatchRuleTest(Blocks.WATER), Blocks.WATER.getDefaultState()), new StructureProcessorRule(new BlockMatchRuleTest(Blocks.DIRT), new BlockMatchRuleTest(Blocks.WATER), Blocks.WATER.getDefaultState())))));
        StructureProcessorLists.register(processorListRegisterable, STREET_SAVANNA, ImmutableList.of(new RuleStructureProcessor(ImmutableList.of(new StructureProcessorRule(new BlockMatchRuleTest(Blocks.DIRT_PATH), new BlockMatchRuleTest(Blocks.WATER), Blocks.ACACIA_PLANKS.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.DIRT_PATH, 0.2f), AlwaysTrueRuleTest.INSTANCE, Blocks.GRASS_BLOCK.getDefaultState()), new StructureProcessorRule(new BlockMatchRuleTest(Blocks.GRASS_BLOCK), new BlockMatchRuleTest(Blocks.WATER), Blocks.WATER.getDefaultState()), new StructureProcessorRule(new BlockMatchRuleTest(Blocks.DIRT), new BlockMatchRuleTest(Blocks.WATER), Blocks.WATER.getDefaultState())))));
        StructureProcessorLists.register(processorListRegisterable, STREET_SNOWY_OR_TAIGA, ImmutableList.of(new RuleStructureProcessor(ImmutableList.of(new StructureProcessorRule(new BlockMatchRuleTest(Blocks.DIRT_PATH), new BlockMatchRuleTest(Blocks.WATER), Blocks.SPRUCE_PLANKS.getDefaultState()), new StructureProcessorRule(new BlockMatchRuleTest(Blocks.DIRT_PATH), new BlockMatchRuleTest(Blocks.ICE), Blocks.SPRUCE_PLANKS.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.DIRT_PATH, 0.2f), AlwaysTrueRuleTest.INSTANCE, Blocks.GRASS_BLOCK.getDefaultState()), new StructureProcessorRule(new BlockMatchRuleTest(Blocks.GRASS_BLOCK), new BlockMatchRuleTest(Blocks.WATER), Blocks.WATER.getDefaultState()), new StructureProcessorRule(new BlockMatchRuleTest(Blocks.DIRT), new BlockMatchRuleTest(Blocks.WATER), Blocks.WATER.getDefaultState())))));
        StructureProcessorLists.register(processorListRegisterable, FARM_PLAINS, ImmutableList.of(new RuleStructureProcessor(ImmutableList.of(new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.WHEAT, 0.3f), AlwaysTrueRuleTest.INSTANCE, Blocks.CARROTS.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.WHEAT, 0.2f), AlwaysTrueRuleTest.INSTANCE, Blocks.POTATOES.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.WHEAT, 0.1f), AlwaysTrueRuleTest.INSTANCE, Blocks.BEETROOTS.getDefaultState())))));
        StructureProcessorLists.register(processorListRegisterable, FARM_SAVANNA, ImmutableList.of(new RuleStructureProcessor(ImmutableList.of(new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.WHEAT, 0.1f), AlwaysTrueRuleTest.INSTANCE, Blocks.MELON_STEM.getDefaultState())))));
        StructureProcessorLists.register(processorListRegisterable, FARM_SNOWY, ImmutableList.of(new RuleStructureProcessor(ImmutableList.of(new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.WHEAT, 0.1f), AlwaysTrueRuleTest.INSTANCE, Blocks.CARROTS.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.WHEAT, 0.8f), AlwaysTrueRuleTest.INSTANCE, Blocks.POTATOES.getDefaultState())))));
        StructureProcessorLists.register(processorListRegisterable, FARM_TAIGA, ImmutableList.of(new RuleStructureProcessor(ImmutableList.of(new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.WHEAT, 0.3f), AlwaysTrueRuleTest.INSTANCE, Blocks.PUMPKIN_STEM.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.WHEAT, 0.2f), AlwaysTrueRuleTest.INSTANCE, Blocks.POTATOES.getDefaultState())))));
        StructureProcessorLists.register(processorListRegisterable, FARM_DESERT, ImmutableList.of(new RuleStructureProcessor(ImmutableList.of(new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.WHEAT, 0.2f), AlwaysTrueRuleTest.INSTANCE, Blocks.BEETROOTS.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.WHEAT, 0.1f), AlwaysTrueRuleTest.INSTANCE, Blocks.MELON_STEM.getDefaultState())))));
        StructureProcessorLists.register(processorListRegisterable, OUTPOST_ROT, ImmutableList.of(new BlockRotStructureProcessor(0.05f)));
        StructureProcessorLists.register(processorListRegisterable, BOTTOM_RAMPART, ImmutableList.of(new RuleStructureProcessor(ImmutableList.of(new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.MAGMA_BLOCK, 0.75f), AlwaysTrueRuleTest.INSTANCE, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS, 0.15f), AlwaysTrueRuleTest.INSTANCE, Blocks.POLISHED_BLACKSTONE_BRICKS.getDefaultState()), lv3, lv2))));
        StructureProcessorLists.register(processorListRegisterable, TREASURE_ROOMS, ImmutableList.of(new RuleStructureProcessor(ImmutableList.of(new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 0.35f), AlwaysTrueRuleTest.INSTANCE, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.CHISELED_POLISHED_BLACKSTONE, 0.1f), AlwaysTrueRuleTest.INSTANCE, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.getDefaultState()), lv3, lv2))));
        StructureProcessorLists.register(processorListRegisterable, HOUSING, ImmutableList.of(new RuleStructureProcessor(ImmutableList.of(new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 0.3f), AlwaysTrueRuleTest.INSTANCE, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.BLACKSTONE, 1.0E-4f), AlwaysTrueRuleTest.INSTANCE, Blocks.AIR.getDefaultState()), lv3, lv2))));
        StructureProcessorLists.register(processorListRegisterable, SIDE_WALL_DEGRADATION, ImmutableList.of(new RuleStructureProcessor(ImmutableList.of(new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.CHISELED_POLISHED_BLACKSTONE, 0.5f), AlwaysTrueRuleTest.INSTANCE, Blocks.AIR.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.GOLD_BLOCK, 0.1f), AlwaysTrueRuleTest.INSTANCE, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.getDefaultState()), lv3, lv2))));
        StructureProcessorLists.register(processorListRegisterable, STABLE_DEGRADATION, ImmutableList.of(new RuleStructureProcessor(ImmutableList.of(new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 0.1f), AlwaysTrueRuleTest.INSTANCE, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.BLACKSTONE, 1.0E-4f), AlwaysTrueRuleTest.INSTANCE, Blocks.AIR.getDefaultState()), lv3, lv2))));
        StructureProcessorLists.register(processorListRegisterable, BASTION_GENERIC_DEGRADATION, ImmutableList.of(new RuleStructureProcessor(ImmutableList.of(new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 0.3f), AlwaysTrueRuleTest.INSTANCE, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.BLACKSTONE, 1.0E-4f), AlwaysTrueRuleTest.INSTANCE, Blocks.AIR.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.GOLD_BLOCK, 0.3f), AlwaysTrueRuleTest.INSTANCE, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.getDefaultState()), lv3, lv2))));
        StructureProcessorLists.register(processorListRegisterable, RAMPART_DEGRADATION, ImmutableList.of(new RuleStructureProcessor(ImmutableList.of(new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 0.4f), AlwaysTrueRuleTest.INSTANCE, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.BLACKSTONE, 0.01f), AlwaysTrueRuleTest.INSTANCE, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 1.0E-4f), AlwaysTrueRuleTest.INSTANCE, Blocks.AIR.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.BLACKSTONE, 1.0E-4f), AlwaysTrueRuleTest.INSTANCE, Blocks.AIR.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.GOLD_BLOCK, 0.3f), AlwaysTrueRuleTest.INSTANCE, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.getDefaultState()), lv3, lv2))));
        StructureProcessorLists.register(processorListRegisterable, ENTRANCE_REPLACEMENT, ImmutableList.of(new RuleStructureProcessor(ImmutableList.of(new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.CHISELED_POLISHED_BLACKSTONE, 0.5f), AlwaysTrueRuleTest.INSTANCE, Blocks.AIR.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.GOLD_BLOCK, 0.6f), AlwaysTrueRuleTest.INSTANCE, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.getDefaultState()), lv3, lv2))));
        StructureProcessorLists.register(processorListRegisterable, BRIDGE, ImmutableList.of(new RuleStructureProcessor(ImmutableList.of(new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 0.3f), AlwaysTrueRuleTest.INSTANCE, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.BLACKSTONE, 1.0E-4f), AlwaysTrueRuleTest.INSTANCE, Blocks.AIR.getDefaultState())))));
        StructureProcessorLists.register(processorListRegisterable, ROOF, ImmutableList.of(new RuleStructureProcessor(ImmutableList.of(new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 0.3f), AlwaysTrueRuleTest.INSTANCE, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 0.15f), AlwaysTrueRuleTest.INSTANCE, Blocks.AIR.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 0.3f), AlwaysTrueRuleTest.INSTANCE, Blocks.BLACKSTONE.getDefaultState())))));
        StructureProcessorLists.register(processorListRegisterable, HIGH_WALL, ImmutableList.of(new RuleStructureProcessor(ImmutableList.of(new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 0.01f), AlwaysTrueRuleTest.INSTANCE, Blocks.AIR.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 0.5f), AlwaysTrueRuleTest.INSTANCE, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.POLISHED_BLACKSTONE_BRICKS, 0.3f), AlwaysTrueRuleTest.INSTANCE, Blocks.BLACKSTONE.getDefaultState()), lv3))));
        StructureProcessorLists.register(processorListRegisterable, HIGH_RAMPART, ImmutableList.of(new RuleStructureProcessor(ImmutableList.of(new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.GOLD_BLOCK, 0.3f), AlwaysTrueRuleTest.INSTANCE, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.getDefaultState()), new StructureProcessorRule(AlwaysTrueRuleTest.INSTANCE, AlwaysTrueRuleTest.INSTANCE, new AxisAlignedLinearPosRuleTest(0.0f, 0.05f, 0, 100, Direction.Axis.Y), Blocks.AIR.getDefaultState()), lv3))));
        StructureProcessorLists.register(processorListRegisterable, FOSSIL_ROT, ImmutableList.of(new BlockRotStructureProcessor(0.9f), new ProtectedBlocksStructureProcessor(BlockTags.FEATURES_CANNOT_REPLACE)));
        StructureProcessorLists.register(processorListRegisterable, FOSSIL_COAL, ImmutableList.of(new BlockRotStructureProcessor(0.1f), new ProtectedBlocksStructureProcessor(BlockTags.FEATURES_CANNOT_REPLACE)));
        StructureProcessorLists.register(processorListRegisterable, FOSSIL_DIAMONDS, ImmutableList.of(new BlockRotStructureProcessor(0.1f), new RuleStructureProcessor(ImmutableList.of(new StructureProcessorRule(new BlockMatchRuleTest(Blocks.COAL_ORE), AlwaysTrueRuleTest.INSTANCE, Blocks.DEEPSLATE_DIAMOND_ORE.getDefaultState()))), new ProtectedBlocksStructureProcessor(BlockTags.FEATURES_CANNOT_REPLACE)));
        StructureProcessorLists.register(processorListRegisterable, ANCIENT_CITY_START_DEGRADATION, ImmutableList.of(new RuleStructureProcessor(ImmutableList.of(new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.DEEPSLATE_BRICKS, 0.3f), AlwaysTrueRuleTest.INSTANCE, Blocks.CRACKED_DEEPSLATE_BRICKS.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.DEEPSLATE_TILES, 0.3f), AlwaysTrueRuleTest.INSTANCE, Blocks.CRACKED_DEEPSLATE_TILES.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.SOUL_LANTERN, 0.05f), AlwaysTrueRuleTest.INSTANCE, Blocks.AIR.getDefaultState()))), new ProtectedBlocksStructureProcessor(BlockTags.FEATURES_CANNOT_REPLACE)));
        StructureProcessorLists.register(processorListRegisterable, ANCIENT_CITY_GENERIC_DEGRADATION, ImmutableList.of(new BlockRotStructureProcessor(lv.getOrThrow(BlockTags.ANCIENT_CITY_REPLACEABLE), 0.95f), new RuleStructureProcessor(ImmutableList.of(new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.DEEPSLATE_BRICKS, 0.3f), AlwaysTrueRuleTest.INSTANCE, Blocks.CRACKED_DEEPSLATE_BRICKS.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.DEEPSLATE_TILES, 0.3f), AlwaysTrueRuleTest.INSTANCE, Blocks.CRACKED_DEEPSLATE_TILES.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.SOUL_LANTERN, 0.05f), AlwaysTrueRuleTest.INSTANCE, Blocks.AIR.getDefaultState()))), new ProtectedBlocksStructureProcessor(BlockTags.FEATURES_CANNOT_REPLACE)));
        StructureProcessorLists.register(processorListRegisterable, ANCIENT_CITY_WALLS_DEGRADATION, ImmutableList.of(new BlockRotStructureProcessor(lv.getOrThrow(BlockTags.ANCIENT_CITY_REPLACEABLE), 0.95f), new RuleStructureProcessor(ImmutableList.of(new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.DEEPSLATE_BRICKS, 0.3f), AlwaysTrueRuleTest.INSTANCE, Blocks.CRACKED_DEEPSLATE_BRICKS.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.DEEPSLATE_TILES, 0.3f), AlwaysTrueRuleTest.INSTANCE, Blocks.CRACKED_DEEPSLATE_TILES.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.DEEPSLATE_TILE_SLAB, 0.3f), AlwaysTrueRuleTest.INSTANCE, Blocks.AIR.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.SOUL_LANTERN, 0.05f), AlwaysTrueRuleTest.INSTANCE, Blocks.AIR.getDefaultState()))), new ProtectedBlocksStructureProcessor(BlockTags.FEATURES_CANNOT_REPLACE)));
    }
}
