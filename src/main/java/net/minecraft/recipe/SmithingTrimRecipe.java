/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.recipe;

import com.google.gson.JsonObject;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.trim.ArmorTrim;
import net.minecraft.item.trim.ArmorTrimMaterial;
import net.minecraft.item.trim.ArmorTrimMaterials;
import net.minecraft.item.trim.ArmorTrimPattern;
import net.minecraft.item.trim.ArmorTrimPatterns;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.world.World;

public class SmithingTrimRecipe
implements SmithingRecipe {
    private final Identifier id;
    final Ingredient template;
    final Ingredient base;
    final Ingredient addition;

    public SmithingTrimRecipe(Identifier id, Ingredient template, Ingredient base, Ingredient addition) {
        this.id = id;
        this.template = template;
        this.base = base;
        this.addition = addition;
    }

    @Override
    public boolean matches(Inventory inventory, World world) {
        return this.template.test(inventory.getStack(0)) && this.base.test(inventory.getStack(1)) && this.addition.test(inventory.getStack(2));
    }

    @Override
    public ItemStack craft(Inventory inventory, DynamicRegistryManager registryManager) {
        ItemStack lv = inventory.getStack(1);
        if (this.base.test(lv)) {
            Optional<RegistryEntry.Reference<ArmorTrimMaterial>> optional = ArmorTrimMaterials.get(registryManager, inventory.getStack(2));
            Optional<RegistryEntry.Reference<ArmorTrimPattern>> optional2 = ArmorTrimPatterns.get(registryManager, inventory.getStack(0));
            if (optional.isPresent() && optional2.isPresent()) {
                Optional<ArmorTrim> optional3 = ArmorTrim.getTrim(registryManager, lv);
                if (optional3.isPresent() && optional3.get().equals((RegistryEntry<ArmorTrimPattern>)optional2.get(), (RegistryEntry<ArmorTrimMaterial>)optional.get())) {
                    return ItemStack.EMPTY;
                }
                ItemStack lv2 = lv.copy();
                lv2.setCount(1);
                ArmorTrim lv3 = new ArmorTrim((RegistryEntry<ArmorTrimMaterial>)optional.get(), (RegistryEntry<ArmorTrimPattern>)optional2.get());
                if (ArmorTrim.apply(registryManager, lv2, lv3)) {
                    return lv2;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack getOutput(DynamicRegistryManager registryManager) {
        Optional<RegistryEntry.Reference<ArmorTrimMaterial>> optional2;
        ItemStack lv = new ItemStack(Items.IRON_CHESTPLATE);
        Optional<RegistryEntry.Reference<ArmorTrimPattern>> optional = registryManager.get(RegistryKeys.TRIM_PATTERN).streamEntries().findFirst();
        if (optional.isPresent() && (optional2 = registryManager.get(RegistryKeys.TRIM_MATERIAL).getEntry(ArmorTrimMaterials.REDSTONE)).isPresent()) {
            ArmorTrim lv2 = new ArmorTrim((RegistryEntry<ArmorTrimMaterial>)optional2.get(), (RegistryEntry<ArmorTrimPattern>)optional.get());
            ArmorTrim.apply(registryManager, lv, lv2);
        }
        return lv;
    }

    @Override
    public boolean testTemplate(ItemStack stack) {
        return this.template.test(stack);
    }

    @Override
    public boolean testBase(ItemStack stack) {
        return this.base.test(stack);
    }

    @Override
    public boolean testAddition(ItemStack stack) {
        return this.addition.test(stack);
    }

    @Override
    public Identifier getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.SMITHING_TRIM;
    }

    @Override
    public boolean isEmpty() {
        return Stream.of(this.template, this.base, this.addition).anyMatch(Ingredient::isEmpty);
    }

    public static class Serializer
    implements RecipeSerializer<SmithingTrimRecipe> {
        @Override
        public SmithingTrimRecipe read(Identifier arg, JsonObject jsonObject) {
            Ingredient lv = Ingredient.fromJson(JsonHelper.getObject(jsonObject, "template"));
            Ingredient lv2 = Ingredient.fromJson(JsonHelper.getObject(jsonObject, "base"));
            Ingredient lv3 = Ingredient.fromJson(JsonHelper.getObject(jsonObject, "addition"));
            return new SmithingTrimRecipe(arg, lv, lv2, lv3);
        }

        @Override
        public SmithingTrimRecipe read(Identifier arg, PacketByteBuf arg2) {
            Ingredient lv = Ingredient.fromPacket(arg2);
            Ingredient lv2 = Ingredient.fromPacket(arg2);
            Ingredient lv3 = Ingredient.fromPacket(arg2);
            return new SmithingTrimRecipe(arg, lv, lv2, lv3);
        }

        @Override
        public void write(PacketByteBuf arg, SmithingTrimRecipe arg2) {
            arg2.template.write(arg);
            arg2.base.write(arg);
            arg2.addition.write(arg);
        }

        @Override
        public /* synthetic */ Recipe read(Identifier id, PacketByteBuf buf) {
            return this.read(id, buf);
        }

        @Override
        public /* synthetic */ Recipe read(Identifier id, JsonObject json) {
            return this.read(id, json);
        }
    }
}

