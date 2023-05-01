/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.server.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.ItemSlotArgumentType;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ItemCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class LootCommand {
    public static final SuggestionProvider<ServerCommandSource> SUGGESTION_PROVIDER = (context, builder) -> {
        LootManager lv = ((ServerCommandSource)context.getSource()).getServer().getLootManager();
        return CommandSource.suggestIdentifiers(lv.getTableIds(), builder);
    };
    private static final DynamicCommandExceptionType NO_HELD_ITEMS_EXCEPTION = new DynamicCommandExceptionType(entityName -> Text.translatable("commands.drop.no_held_items", entityName));
    private static final DynamicCommandExceptionType NO_LOOT_TABLE_EXCEPTION = new DynamicCommandExceptionType(entityName -> Text.translatable("commands.drop.no_loot_table", entityName));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess) {
        dispatcher.register(LootCommand.addTargetArguments((LiteralArgumentBuilder)CommandManager.literal("loot").requires(source -> source.hasPermissionLevel(2)), (builder, constructor) -> ((ArgumentBuilder)((ArgumentBuilder)((ArgumentBuilder)builder.then(CommandManager.literal("fish").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("loot_table", IdentifierArgumentType.identifier()).suggests(SUGGESTION_PROVIDER).then((ArgumentBuilder<ServerCommandSource, ?>)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("pos", BlockPosArgumentType.blockPos()).executes(context -> LootCommand.executeFish(context, IdentifierArgumentType.getIdentifier(context, "loot_table"), BlockPosArgumentType.getLoadedBlockPos(context, "pos"), ItemStack.EMPTY, constructor))).then(CommandManager.argument("tool", ItemStackArgumentType.itemStack(commandRegistryAccess)).executes(context -> LootCommand.executeFish(context, IdentifierArgumentType.getIdentifier(context, "loot_table"), BlockPosArgumentType.getLoadedBlockPos(context, "pos"), ItemStackArgumentType.getItemStackArgument(context, "tool").createStack(1, false), constructor)))).then(CommandManager.literal("mainhand").executes(context -> LootCommand.executeFish(context, IdentifierArgumentType.getIdentifier(context, "loot_table"), BlockPosArgumentType.getLoadedBlockPos(context, "pos"), LootCommand.getHeldItem((ServerCommandSource)context.getSource(), EquipmentSlot.MAINHAND), constructor)))).then(CommandManager.literal("offhand").executes(context -> LootCommand.executeFish(context, IdentifierArgumentType.getIdentifier(context, "loot_table"), BlockPosArgumentType.getLoadedBlockPos(context, "pos"), LootCommand.getHeldItem((ServerCommandSource)context.getSource(), EquipmentSlot.OFFHAND), constructor))))))).then(CommandManager.literal("loot").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("loot_table", IdentifierArgumentType.identifier()).suggests(SUGGESTION_PROVIDER).executes(context -> LootCommand.executeLoot(context, IdentifierArgumentType.getIdentifier(context, "loot_table"), constructor))))).then(CommandManager.literal("kill").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("target", EntityArgumentType.entity()).executes(context -> LootCommand.executeKill(context, EntityArgumentType.getEntity(context, "target"), constructor))))).then(CommandManager.literal("mine").then((ArgumentBuilder<ServerCommandSource, ?>)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("pos", BlockPosArgumentType.blockPos()).executes(context -> LootCommand.executeMine(context, BlockPosArgumentType.getLoadedBlockPos(context, "pos"), ItemStack.EMPTY, constructor))).then(CommandManager.argument("tool", ItemStackArgumentType.itemStack(commandRegistryAccess)).executes(context -> LootCommand.executeMine(context, BlockPosArgumentType.getLoadedBlockPos(context, "pos"), ItemStackArgumentType.getItemStackArgument(context, "tool").createStack(1, false), constructor)))).then(CommandManager.literal("mainhand").executes(context -> LootCommand.executeMine(context, BlockPosArgumentType.getLoadedBlockPos(context, "pos"), LootCommand.getHeldItem((ServerCommandSource)context.getSource(), EquipmentSlot.MAINHAND), constructor)))).then(CommandManager.literal("offhand").executes(context -> LootCommand.executeMine(context, BlockPosArgumentType.getLoadedBlockPos(context, "pos"), LootCommand.getHeldItem((ServerCommandSource)context.getSource(), EquipmentSlot.OFFHAND), constructor)))))));
    }

    private static <T extends ArgumentBuilder<ServerCommandSource, T>> T addTargetArguments(T rootArgument, SourceConstructor sourceConstructor) {
        return ((ArgumentBuilder)((ArgumentBuilder)((ArgumentBuilder)rootArgument.then(((LiteralArgumentBuilder)CommandManager.literal("replace").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.literal("entity").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("entities", EntityArgumentType.entities()).then((ArgumentBuilder<ServerCommandSource, ?>)sourceConstructor.construct(CommandManager.argument("slot", ItemSlotArgumentType.itemSlot()), (context, stacks, messageSender) -> LootCommand.executeReplace(EntityArgumentType.getEntities(context, "entities"), ItemSlotArgumentType.getItemSlot(context, "slot"), stacks.size(), stacks, messageSender)).then(sourceConstructor.construct(CommandManager.argument("count", IntegerArgumentType.integer(0)), (context, stacks, messageSender) -> LootCommand.executeReplace(EntityArgumentType.getEntities(context, "entities"), ItemSlotArgumentType.getItemSlot(context, "slot"), IntegerArgumentType.getInteger(context, "count"), stacks, messageSender))))))).then(CommandManager.literal("block").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("targetPos", BlockPosArgumentType.blockPos()).then((ArgumentBuilder<ServerCommandSource, ?>)sourceConstructor.construct(CommandManager.argument("slot", ItemSlotArgumentType.itemSlot()), (context, stacks, messageSender) -> LootCommand.executeBlock((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos(context, "targetPos"), ItemSlotArgumentType.getItemSlot(context, "slot"), stacks.size(), stacks, messageSender)).then(sourceConstructor.construct(CommandManager.argument("count", IntegerArgumentType.integer(0)), (context, stacks, messageSender) -> LootCommand.executeBlock((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos(context, "targetPos"), IntegerArgumentType.getInteger(context, "slot"), IntegerArgumentType.getInteger(context, "count"), stacks, messageSender)))))))).then(CommandManager.literal("insert").then(sourceConstructor.construct(CommandManager.argument("targetPos", BlockPosArgumentType.blockPos()), (context, stacks, messageSender) -> LootCommand.executeInsert((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos(context, "targetPos"), stacks, messageSender))))).then(CommandManager.literal("give").then(sourceConstructor.construct(CommandManager.argument("players", EntityArgumentType.players()), (context, stacks, messageSender) -> LootCommand.executeGive(EntityArgumentType.getPlayers(context, "players"), stacks, messageSender))))).then(CommandManager.literal("spawn").then(sourceConstructor.construct(CommandManager.argument("targetPos", Vec3ArgumentType.vec3()), (context, stacks, messageSender) -> LootCommand.executeSpawn((ServerCommandSource)context.getSource(), Vec3ArgumentType.getVec3(context, "targetPos"), stacks, messageSender))));
    }

    private static Inventory getBlockInventory(ServerCommandSource source, BlockPos pos) throws CommandSyntaxException {
        BlockEntity lv = source.getWorld().getBlockEntity(pos);
        if (!(lv instanceof Inventory)) {
            throw ItemCommand.NOT_A_CONTAINER_TARGET_EXCEPTION.create(pos.getX(), pos.getY(), pos.getZ());
        }
        return (Inventory)((Object)lv);
    }

    private static int executeInsert(ServerCommandSource source, BlockPos targetPos, List<ItemStack> stacks, FeedbackMessage messageSender) throws CommandSyntaxException {
        Inventory lv = LootCommand.getBlockInventory(source, targetPos);
        ArrayList<ItemStack> list2 = Lists.newArrayListWithCapacity(stacks.size());
        for (ItemStack lv2 : stacks) {
            if (!LootCommand.insert(lv, lv2.copy())) continue;
            lv.markDirty();
            list2.add(lv2);
        }
        messageSender.accept(list2);
        return list2.size();
    }

    private static boolean insert(Inventory inventory, ItemStack stack) {
        boolean bl = false;
        for (int i = 0; i < inventory.size() && !stack.isEmpty(); ++i) {
            ItemStack lv = inventory.getStack(i);
            if (!inventory.isValid(i, stack)) continue;
            if (lv.isEmpty()) {
                inventory.setStack(i, stack);
                bl = true;
                break;
            }
            if (!LootCommand.itemsMatch(lv, stack)) continue;
            int j = stack.getMaxCount() - lv.getCount();
            int k = Math.min(stack.getCount(), j);
            stack.decrement(k);
            lv.increment(k);
            bl = true;
        }
        return bl;
    }

    private static int executeBlock(ServerCommandSource source, BlockPos targetPos, int slot, int stackCount, List<ItemStack> stacks, FeedbackMessage messageSender) throws CommandSyntaxException {
        Inventory lv = LootCommand.getBlockInventory(source, targetPos);
        int k = lv.size();
        if (slot < 0 || slot >= k) {
            throw ItemCommand.NO_SUCH_SLOT_TARGET_EXCEPTION.create(slot);
        }
        ArrayList<ItemStack> list2 = Lists.newArrayListWithCapacity(stacks.size());
        for (int l = 0; l < stackCount; ++l) {
            ItemStack lv2;
            int m = slot + l;
            ItemStack itemStack = lv2 = l < stacks.size() ? stacks.get(l) : ItemStack.EMPTY;
            if (!lv.isValid(m, lv2)) continue;
            lv.setStack(m, lv2);
            list2.add(lv2);
        }
        messageSender.accept(list2);
        return list2.size();
    }

    private static boolean itemsMatch(ItemStack first, ItemStack second) {
        return first.isOf(second.getItem()) && first.getDamage() == second.getDamage() && first.getCount() <= first.getMaxCount() && Objects.equals(first.getNbt(), second.getNbt());
    }

    private static int executeGive(Collection<ServerPlayerEntity> players, List<ItemStack> stacks, FeedbackMessage messageSender) throws CommandSyntaxException {
        ArrayList<ItemStack> list2 = Lists.newArrayListWithCapacity(stacks.size());
        for (ItemStack lv : stacks) {
            for (ServerPlayerEntity lv2 : players) {
                if (!lv2.getInventory().insertStack(lv.copy())) continue;
                list2.add(lv);
            }
        }
        messageSender.accept(list2);
        return list2.size();
    }

    private static void replace(Entity entity, List<ItemStack> stacks, int slot, int stackCount, List<ItemStack> addedStacks) {
        for (int k = 0; k < stackCount; ++k) {
            ItemStack lv = k < stacks.size() ? stacks.get(k) : ItemStack.EMPTY;
            StackReference lv2 = entity.getStackReference(slot + k);
            if (lv2 == StackReference.EMPTY || !lv2.set(lv.copy())) continue;
            addedStacks.add(lv);
        }
    }

    private static int executeReplace(Collection<? extends Entity> targets, int slot, int stackCount, List<ItemStack> stacks, FeedbackMessage messageSender) throws CommandSyntaxException {
        ArrayList<ItemStack> list2 = Lists.newArrayListWithCapacity(stacks.size());
        for (Entity entity : targets) {
            if (entity instanceof ServerPlayerEntity) {
                ServerPlayerEntity lv2 = (ServerPlayerEntity)entity;
                LootCommand.replace(entity, stacks, slot, stackCount, list2);
                lv2.currentScreenHandler.sendContentUpdates();
                continue;
            }
            LootCommand.replace(entity, stacks, slot, stackCount, list2);
        }
        messageSender.accept(list2);
        return list2.size();
    }

    private static int executeSpawn(ServerCommandSource source, Vec3d pos, List<ItemStack> stacks, FeedbackMessage messageSender) throws CommandSyntaxException {
        ServerWorld lv = source.getWorld();
        stacks.forEach(stack -> {
            ItemEntity lv = new ItemEntity(lv, arg2.x, arg2.y, arg2.z, stack.copy());
            lv.setToDefaultPickupDelay();
            lv.spawnEntity(lv);
        });
        messageSender.accept(stacks);
        return stacks.size();
    }

    private static void sendDroppedFeedback(ServerCommandSource source, List<ItemStack> stacks) {
        if (stacks.size() == 1) {
            ItemStack lv = stacks.get(0);
            source.sendFeedback(Text.translatable("commands.drop.success.single", lv.getCount(), lv.toHoverableText()), false);
        } else {
            source.sendFeedback(Text.translatable("commands.drop.success.multiple", stacks.size()), false);
        }
    }

    private static void sendDroppedFeedback(ServerCommandSource source, List<ItemStack> stacks, Identifier lootTable) {
        if (stacks.size() == 1) {
            ItemStack lv = stacks.get(0);
            source.sendFeedback(Text.translatable("commands.drop.success.single_with_table", lv.getCount(), lv.toHoverableText(), lootTable), false);
        } else {
            source.sendFeedback(Text.translatable("commands.drop.success.multiple_with_table", stacks.size(), lootTable), false);
        }
    }

    private static ItemStack getHeldItem(ServerCommandSource source, EquipmentSlot slot) throws CommandSyntaxException {
        Entity lv = source.getEntityOrThrow();
        if (lv instanceof LivingEntity) {
            return ((LivingEntity)lv).getEquippedStack(slot);
        }
        throw NO_HELD_ITEMS_EXCEPTION.create(lv.getDisplayName());
    }

    private static int executeMine(CommandContext<ServerCommandSource> context, BlockPos pos, ItemStack stack, Target constructor) throws CommandSyntaxException {
        ServerCommandSource lv = context.getSource();
        ServerWorld lv2 = lv.getWorld();
        BlockState lv3 = lv2.getBlockState(pos);
        BlockEntity lv4 = lv2.getBlockEntity(pos);
        LootContext.Builder lv5 = new LootContext.Builder(lv2).parameter(LootContextParameters.ORIGIN, Vec3d.ofCenter(pos)).parameter(LootContextParameters.BLOCK_STATE, lv3).optionalParameter(LootContextParameters.BLOCK_ENTITY, lv4).optionalParameter(LootContextParameters.THIS_ENTITY, lv.getEntity()).parameter(LootContextParameters.TOOL, stack);
        List<ItemStack> list = lv3.getDroppedStacks(lv5);
        return constructor.accept(context, list, stacks -> LootCommand.sendDroppedFeedback(lv, stacks, lv3.getBlock().getLootTableId()));
    }

    private static int executeKill(CommandContext<ServerCommandSource> context, Entity entity, Target constructor) throws CommandSyntaxException {
        if (!(entity instanceof LivingEntity)) {
            throw NO_LOOT_TABLE_EXCEPTION.create(entity.getDisplayName());
        }
        Identifier lv = ((LivingEntity)entity).getLootTable();
        ServerCommandSource lv2 = context.getSource();
        LootContext.Builder lv3 = new LootContext.Builder(lv2.getWorld());
        Entity lv4 = lv2.getEntity();
        if (lv4 instanceof PlayerEntity) {
            lv3.parameter(LootContextParameters.LAST_DAMAGE_PLAYER, (PlayerEntity)lv4);
        }
        lv3.parameter(LootContextParameters.DAMAGE_SOURCE, entity.getDamageSources().magic());
        lv3.optionalParameter(LootContextParameters.DIRECT_KILLER_ENTITY, lv4);
        lv3.optionalParameter(LootContextParameters.KILLER_ENTITY, lv4);
        lv3.parameter(LootContextParameters.THIS_ENTITY, entity);
        lv3.parameter(LootContextParameters.ORIGIN, lv2.getPosition());
        LootTable lv5 = lv2.getServer().getLootManager().getTable(lv);
        ObjectArrayList<ItemStack> list = lv5.generateLoot(lv3.build(LootContextTypes.ENTITY));
        return constructor.accept(context, list, stacks -> LootCommand.sendDroppedFeedback(lv2, stacks, lv));
    }

    private static int executeLoot(CommandContext<ServerCommandSource> context, Identifier lootTable, Target constructor) throws CommandSyntaxException {
        ServerCommandSource lv = context.getSource();
        LootContext.Builder lv2 = new LootContext.Builder(lv.getWorld()).optionalParameter(LootContextParameters.THIS_ENTITY, lv.getEntity()).parameter(LootContextParameters.ORIGIN, lv.getPosition());
        return LootCommand.getFeedbackMessageSingle(context, lootTable, lv2.build(LootContextTypes.CHEST), constructor);
    }

    private static int executeFish(CommandContext<ServerCommandSource> context, Identifier lootTable, BlockPos pos, ItemStack stack, Target constructor) throws CommandSyntaxException {
        ServerCommandSource lv = context.getSource();
        LootContext lv2 = new LootContext.Builder(lv.getWorld()).parameter(LootContextParameters.ORIGIN, Vec3d.ofCenter(pos)).parameter(LootContextParameters.TOOL, stack).optionalParameter(LootContextParameters.THIS_ENTITY, lv.getEntity()).build(LootContextTypes.FISHING);
        return LootCommand.getFeedbackMessageSingle(context, lootTable, lv2, constructor);
    }

    private static int getFeedbackMessageSingle(CommandContext<ServerCommandSource> context, Identifier lootTable, LootContext lootContext, Target constructor) throws CommandSyntaxException {
        ServerCommandSource lv = context.getSource();
        LootTable lv2 = lv.getServer().getLootManager().getTable(lootTable);
        ObjectArrayList<ItemStack> list = lv2.generateLoot(lootContext);
        return constructor.accept(context, list, stacks -> LootCommand.sendDroppedFeedback(lv, stacks));
    }

    @FunctionalInterface
    static interface SourceConstructor {
        public ArgumentBuilder<ServerCommandSource, ?> construct(ArgumentBuilder<ServerCommandSource, ?> var1, Target var2);
    }

    @FunctionalInterface
    static interface Target {
        public int accept(CommandContext<ServerCommandSource> var1, List<ItemStack> var2, FeedbackMessage var3) throws CommandSyntaxException;
    }

    @FunctionalInterface
    static interface FeedbackMessage {
        public void accept(List<ItemStack> var1) throws CommandSyntaxException;
    }
}

