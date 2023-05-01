/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.advancement.Advancement;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionManager;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionManager;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class IdentifierArgumentType
implements ArgumentType<Identifier> {
    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012");
    private static final DynamicCommandExceptionType UNKNOWN_ADVANCEMENT_EXCEPTION = new DynamicCommandExceptionType(id -> Text.translatable("advancement.advancementNotFound", id));
    private static final DynamicCommandExceptionType UNKNOWN_RECIPE_EXCEPTION = new DynamicCommandExceptionType(id -> Text.translatable("recipe.notFound", id));
    private static final DynamicCommandExceptionType UNKNOWN_PREDICATE_EXCEPTION = new DynamicCommandExceptionType(id -> Text.translatable("predicate.unknown", id));
    private static final DynamicCommandExceptionType UNKNOWN_ITEM_MODIFIER_EXCEPTION = new DynamicCommandExceptionType(id -> Text.translatable("item_modifier.unknown", id));

    public static IdentifierArgumentType identifier() {
        return new IdentifierArgumentType();
    }

    public static Advancement getAdvancementArgument(CommandContext<ServerCommandSource> context, String argumentName) throws CommandSyntaxException {
        Identifier lv = IdentifierArgumentType.getIdentifier(context, argumentName);
        Advancement lv2 = context.getSource().getServer().getAdvancementLoader().get(lv);
        if (lv2 == null) {
            throw UNKNOWN_ADVANCEMENT_EXCEPTION.create(lv);
        }
        return lv2;
    }

    public static Recipe<?> getRecipeArgument(CommandContext<ServerCommandSource> context, String argumentName) throws CommandSyntaxException {
        RecipeManager lv = context.getSource().getServer().getRecipeManager();
        Identifier lv2 = IdentifierArgumentType.getIdentifier(context, argumentName);
        return lv.get(lv2).orElseThrow(() -> UNKNOWN_RECIPE_EXCEPTION.create(lv2));
    }

    public static LootCondition getPredicateArgument(CommandContext<ServerCommandSource> context, String argumentName) throws CommandSyntaxException {
        Identifier lv = IdentifierArgumentType.getIdentifier(context, argumentName);
        LootConditionManager lv2 = context.getSource().getServer().getPredicateManager();
        LootCondition lv3 = lv2.get(lv);
        if (lv3 == null) {
            throw UNKNOWN_PREDICATE_EXCEPTION.create(lv);
        }
        return lv3;
    }

    public static LootFunction getItemModifierArgument(CommandContext<ServerCommandSource> context, String argumentName) throws CommandSyntaxException {
        Identifier lv = IdentifierArgumentType.getIdentifier(context, argumentName);
        LootFunctionManager lv2 = context.getSource().getServer().getItemModifierManager();
        LootFunction lv3 = lv2.get(lv);
        if (lv3 == null) {
            throw UNKNOWN_ITEM_MODIFIER_EXCEPTION.create(lv);
        }
        return lv3;
    }

    public static Identifier getIdentifier(CommandContext<ServerCommandSource> context, String name) {
        return context.getArgument(name, Identifier.class);
    }

    @Override
    public Identifier parse(StringReader stringReader) throws CommandSyntaxException {
        return Identifier.fromCommandInput(stringReader);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    @Override
    public /* synthetic */ Object parse(StringReader reader) throws CommandSyntaxException {
        return this.parse(reader);
    }
}

