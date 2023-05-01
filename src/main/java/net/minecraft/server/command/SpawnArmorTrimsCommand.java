/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.server.command;

import com.google.common.collect.Maps;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.datafixers.util.Pair;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.trim.ArmorTrim;
import net.minecraft.item.trim.ArmorTrimMaterial;
import net.minecraft.item.trim.ArmorTrimMaterials;
import net.minecraft.item.trim.ArmorTrimPattern;
import net.minecraft.item.trim.ArmorTrimPatterns;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SpawnArmorTrimsCommand {
    private static final Map<Pair<ArmorMaterial, EquipmentSlot>, Item> ARMOR_PIECES = Util.make(Maps.newHashMap(), map -> {
        map.put(Pair.of(ArmorMaterials.CHAIN, EquipmentSlot.HEAD), Items.CHAINMAIL_HELMET);
        map.put(Pair.of(ArmorMaterials.CHAIN, EquipmentSlot.CHEST), Items.CHAINMAIL_CHESTPLATE);
        map.put(Pair.of(ArmorMaterials.CHAIN, EquipmentSlot.LEGS), Items.CHAINMAIL_LEGGINGS);
        map.put(Pair.of(ArmorMaterials.CHAIN, EquipmentSlot.FEET), Items.CHAINMAIL_BOOTS);
        map.put(Pair.of(ArmorMaterials.IRON, EquipmentSlot.HEAD), Items.IRON_HELMET);
        map.put(Pair.of(ArmorMaterials.IRON, EquipmentSlot.CHEST), Items.IRON_CHESTPLATE);
        map.put(Pair.of(ArmorMaterials.IRON, EquipmentSlot.LEGS), Items.IRON_LEGGINGS);
        map.put(Pair.of(ArmorMaterials.IRON, EquipmentSlot.FEET), Items.IRON_BOOTS);
        map.put(Pair.of(ArmorMaterials.GOLD, EquipmentSlot.HEAD), Items.GOLDEN_HELMET);
        map.put(Pair.of(ArmorMaterials.GOLD, EquipmentSlot.CHEST), Items.GOLDEN_CHESTPLATE);
        map.put(Pair.of(ArmorMaterials.GOLD, EquipmentSlot.LEGS), Items.GOLDEN_LEGGINGS);
        map.put(Pair.of(ArmorMaterials.GOLD, EquipmentSlot.FEET), Items.GOLDEN_BOOTS);
        map.put(Pair.of(ArmorMaterials.NETHERITE, EquipmentSlot.HEAD), Items.NETHERITE_HELMET);
        map.put(Pair.of(ArmorMaterials.NETHERITE, EquipmentSlot.CHEST), Items.NETHERITE_CHESTPLATE);
        map.put(Pair.of(ArmorMaterials.NETHERITE, EquipmentSlot.LEGS), Items.NETHERITE_LEGGINGS);
        map.put(Pair.of(ArmorMaterials.NETHERITE, EquipmentSlot.FEET), Items.NETHERITE_BOOTS);
        map.put(Pair.of(ArmorMaterials.DIAMOND, EquipmentSlot.HEAD), Items.DIAMOND_HELMET);
        map.put(Pair.of(ArmorMaterials.DIAMOND, EquipmentSlot.CHEST), Items.DIAMOND_CHESTPLATE);
        map.put(Pair.of(ArmorMaterials.DIAMOND, EquipmentSlot.LEGS), Items.DIAMOND_LEGGINGS);
        map.put(Pair.of(ArmorMaterials.DIAMOND, EquipmentSlot.FEET), Items.DIAMOND_BOOTS);
        map.put(Pair.of(ArmorMaterials.TURTLE, EquipmentSlot.HEAD), Items.TURTLE_HELMET);
    });
    private static final List<RegistryKey<ArmorTrimPattern>> PATTERNS = List.of(ArmorTrimPatterns.SENTRY, ArmorTrimPatterns.DUNE, ArmorTrimPatterns.COAST, ArmorTrimPatterns.WILD, ArmorTrimPatterns.WARD, ArmorTrimPatterns.EYE, ArmorTrimPatterns.VEX, ArmorTrimPatterns.TIDE, ArmorTrimPatterns.SNOUT, ArmorTrimPatterns.RIB, ArmorTrimPatterns.SPIRE);
    private static final List<RegistryKey<ArmorTrimMaterial>> MATERIALS = List.of(ArmorTrimMaterials.QUARTZ, ArmorTrimMaterials.IRON, ArmorTrimMaterials.NETHERITE, ArmorTrimMaterials.REDSTONE, ArmorTrimMaterials.COPPER, ArmorTrimMaterials.GOLD, ArmorTrimMaterials.EMERALD, ArmorTrimMaterials.DIAMOND, ArmorTrimMaterials.LAPIS, ArmorTrimMaterials.AMETHYST);
    private static final ToIntFunction<RegistryKey<ArmorTrimPattern>> PATTERN_INDEX_GETTER = Util.lastIndexGetter(PATTERNS);
    private static final ToIntFunction<RegistryKey<ArmorTrimMaterial>> MATERIAL_INDEX_GETTER = Util.lastIndexGetter(MATERIALS);

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("spawn_armor_trims").requires(source -> source.hasPermissionLevel(2) && source.getWorld().getEnabledFeatures().contains(FeatureFlags.UPDATE_1_20))).executes(context -> SpawnArmorTrimsCommand.execute((ServerCommandSource)context.getSource(), ((ServerCommandSource)context.getSource()).getPlayerOrThrow())));
    }

    private static int execute(ServerCommandSource source, PlayerEntity player) {
        World lv = player.getWorld();
        DefaultedList<ArmorTrim> lv2 = DefaultedList.of();
        Registry<ArmorTrimPattern> lv3 = lv.getRegistryManager().get(RegistryKeys.TRIM_PATTERN);
        Registry<ArmorTrimMaterial> lv4 = lv.getRegistryManager().get(RegistryKeys.TRIM_MATERIAL);
        lv3.stream().sorted(Comparator.comparing(pattern -> PATTERN_INDEX_GETTER.applyAsInt(lv3.getKey((ArmorTrimPattern)pattern).orElse(null)))).forEachOrdered(pattern -> lv4.stream().sorted(Comparator.comparing(material -> MATERIAL_INDEX_GETTER.applyAsInt(lv4.getKey((ArmorTrimMaterial)material).orElse(null)))).forEachOrdered(material -> lv2.add(new ArmorTrim(lv4.getEntry((ArmorTrimMaterial)material), lv3.getEntry((ArmorTrimPattern)pattern)))));
        BlockPos lv5 = player.getBlockPos().offset(player.getHorizontalFacing(), 5);
        int i = ArmorMaterials.values().length - 1;
        double d = 3.0;
        int j = 0;
        int k = 0;
        for (ArmorTrim lv6 : lv2) {
            for (ArmorMaterials lv7 : ArmorMaterials.values()) {
                if (lv7 == ArmorMaterials.LEATHER) continue;
                double e = (double)lv5.getX() + 0.5 - (double)(j % lv4.size()) * 3.0;
                double f = (double)lv5.getY() + 0.5 + (double)(k % i) * 3.0;
                double g = (double)lv5.getZ() + 0.5 + (double)(j / lv4.size() * 10);
                ArmorStandEntity lv8 = new ArmorStandEntity(lv, e, f, g);
                lv8.setYaw(180.0f);
                lv8.setNoGravity(true);
                for (EquipmentSlot lv9 : EquipmentSlot.values()) {
                    ArmorItem lv12;
                    Item lv10 = ARMOR_PIECES.get(Pair.of(lv7, lv9));
                    if (lv10 == null) continue;
                    ItemStack lv11 = new ItemStack(lv10);
                    ArmorTrim.apply(lv.getRegistryManager(), lv11, lv6);
                    lv8.equipStack(lv9, lv11);
                    if (lv10 instanceof ArmorItem && (lv12 = (ArmorItem)lv10).getMaterial() == ArmorMaterials.TURTLE) {
                        lv8.setCustomName(lv6.getPattern().value().getDescription(lv6.getMaterial()).copy().append(" ").append(lv6.getMaterial().value().description()));
                        lv8.setCustomNameVisible(true);
                        continue;
                    }
                    lv8.setInvisible(true);
                }
                lv.spawnEntity(lv8);
                ++k;
            }
            ++j;
        }
        source.sendFeedback(Text.literal("Armorstands with trimmed armor spawned around you"), true);
        return 1;
    }
}

