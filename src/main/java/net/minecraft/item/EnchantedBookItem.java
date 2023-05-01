/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.item;

import java.util.List;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class EnchantedBookItem
extends Item {
    public static final String STORED_ENCHANTMENTS_KEY = "StoredEnchantments";

    public EnchantedBookItem(Item.Settings arg) {
        super(arg);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    public static NbtList getEnchantmentNbt(ItemStack stack) {
        NbtCompound lv = stack.getNbt();
        if (lv != null) {
            return lv.getList(STORED_ENCHANTMENTS_KEY, NbtElement.COMPOUND_TYPE);
        }
        return new NbtList();
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        ItemStack.appendEnchantments(tooltip, EnchantedBookItem.getEnchantmentNbt(stack));
    }

    public static void addEnchantment(ItemStack stack, EnchantmentLevelEntry entry) {
        NbtList lv = EnchantedBookItem.getEnchantmentNbt(stack);
        boolean bl = true;
        Identifier lv2 = EnchantmentHelper.getEnchantmentId(entry.enchantment);
        for (int i = 0; i < lv.size(); ++i) {
            NbtCompound lv3 = lv.getCompound(i);
            Identifier lv4 = EnchantmentHelper.getIdFromNbt(lv3);
            if (lv4 == null || !lv4.equals(lv2)) continue;
            if (EnchantmentHelper.getLevelFromNbt(lv3) < entry.level) {
                EnchantmentHelper.writeLevelToNbt(lv3, entry.level);
            }
            bl = false;
            break;
        }
        if (bl) {
            lv.add(EnchantmentHelper.createNbt(lv2, entry.level));
        }
        stack.getOrCreateNbt().put(STORED_ENCHANTMENTS_KEY, lv);
    }

    public static ItemStack forEnchantment(EnchantmentLevelEntry info) {
        ItemStack lv = new ItemStack(Items.ENCHANTED_BOOK);
        EnchantedBookItem.addEnchantment(lv, info);
        return lv;
    }
}

