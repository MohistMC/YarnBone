/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.data.server.advancement.vanilla;

import java.util.function.Consumer;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.criterion.ChangedDimensionCriterion;
import net.minecraft.advancement.criterion.EnterBlockCriterion;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.advancement.criterion.LevitationCriterion;
import net.minecraft.advancement.criterion.OnKilledCriterion;
import net.minecraft.advancement.criterion.SummonedEntityCriterion;
import net.minecraft.advancement.criterion.TickCriterion;
import net.minecraft.block.Blocks;
import net.minecraft.data.server.advancement.AdvancementTabGenerator;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Items;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.DistancePredicate;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LocationPredicate;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureKeys;

public class VanillaEndTabAdvancementGenerator
implements AdvancementTabGenerator {
    @Override
    public void accept(RegistryWrapper.WrapperLookup lookup, Consumer<Advancement> exporter) {
        Advancement lv = Advancement.Builder.create().display(Blocks.END_STONE, (Text)Text.translatable("advancements.end.root.title"), (Text)Text.translatable("advancements.end.root.description"), new Identifier("textures/gui/advancements/backgrounds/end.png"), AdvancementFrame.TASK, false, false, false).criterion("entered_end", ChangedDimensionCriterion.Conditions.to(World.END)).build(exporter, "end/root");
        Advancement lv2 = Advancement.Builder.create().parent(lv).display(Blocks.DRAGON_HEAD, (Text)Text.translatable("advancements.end.kill_dragon.title"), (Text)Text.translatable("advancements.end.kill_dragon.description"), null, AdvancementFrame.TASK, true, true, false).criterion("killed_dragon", OnKilledCriterion.Conditions.createPlayerKilledEntity(EntityPredicate.Builder.create().type(EntityType.ENDER_DRAGON))).build(exporter, "end/kill_dragon");
        Advancement lv3 = Advancement.Builder.create().parent(lv2).display(Items.ENDER_PEARL, (Text)Text.translatable("advancements.end.enter_end_gateway.title"), (Text)Text.translatable("advancements.end.enter_end_gateway.description"), null, AdvancementFrame.TASK, true, true, false).criterion("entered_end_gateway", EnterBlockCriterion.Conditions.block(Blocks.END_GATEWAY)).build(exporter, "end/enter_end_gateway");
        Advancement.Builder.create().parent(lv2).display(Items.END_CRYSTAL, (Text)Text.translatable("advancements.end.respawn_dragon.title"), (Text)Text.translatable("advancements.end.respawn_dragon.description"), null, AdvancementFrame.GOAL, true, true, false).criterion("summoned_dragon", SummonedEntityCriterion.Conditions.create(EntityPredicate.Builder.create().type(EntityType.ENDER_DRAGON))).build(exporter, "end/respawn_dragon");
        Advancement lv4 = Advancement.Builder.create().parent(lv3).display(Blocks.PURPUR_BLOCK, (Text)Text.translatable("advancements.end.find_end_city.title"), (Text)Text.translatable("advancements.end.find_end_city.description"), null, AdvancementFrame.TASK, true, true, false).criterion("in_city", TickCriterion.Conditions.createLocation(LocationPredicate.feature(StructureKeys.END_CITY))).build(exporter, "end/find_end_city");
        Advancement.Builder.create().parent(lv2).display(Items.DRAGON_BREATH, (Text)Text.translatable("advancements.end.dragon_breath.title"), (Text)Text.translatable("advancements.end.dragon_breath.description"), null, AdvancementFrame.GOAL, true, true, false).criterion("dragon_breath", InventoryChangedCriterion.Conditions.items(Items.DRAGON_BREATH)).build(exporter, "end/dragon_breath");
        Advancement.Builder.create().parent(lv4).display(Items.SHULKER_SHELL, (Text)Text.translatable("advancements.end.levitate.title"), (Text)Text.translatable("advancements.end.levitate.description"), null, AdvancementFrame.CHALLENGE, true, true, false).rewards(AdvancementRewards.Builder.experience(50)).criterion("levitated", LevitationCriterion.Conditions.create(DistancePredicate.y(NumberRange.FloatRange.atLeast(50.0)))).build(exporter, "end/levitate");
        Advancement.Builder.create().parent(lv4).display(Items.ELYTRA, (Text)Text.translatable("advancements.end.elytra.title"), (Text)Text.translatable("advancements.end.elytra.description"), null, AdvancementFrame.GOAL, true, true, false).criterion("elytra", InventoryChangedCriterion.Conditions.items(Items.ELYTRA)).build(exporter, "end/elytra");
        Advancement.Builder.create().parent(lv2).display(Blocks.DRAGON_EGG, (Text)Text.translatable("advancements.end.dragon_egg.title"), (Text)Text.translatable("advancements.end.dragon_egg.description"), null, AdvancementFrame.GOAL, true, true, false).criterion("dragon_egg", InventoryChangedCriterion.Conditions.items(Blocks.DRAGON_EGG)).build(exporter, "end/dragon_egg");
    }
}

