/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.item;

import java.util.List;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class FireworkStarItem
extends Item {
    public FireworkStarItem(Item.Settings arg) {
        super(arg);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        NbtCompound lv = stack.getSubNbt("Explosion");
        if (lv != null) {
            FireworkStarItem.appendFireworkTooltip(lv, tooltip);
        }
    }

    public static void appendFireworkTooltip(NbtCompound nbt, List<Text> tooltip) {
        int[] js;
        FireworkRocketItem.Type lv = FireworkRocketItem.Type.byId(nbt.getByte("Type"));
        tooltip.add(Text.translatable("item.minecraft.firework_star.shape." + lv.getName()).formatted(Formatting.GRAY));
        int[] is = nbt.getIntArray("Colors");
        if (is.length > 0) {
            tooltip.add(FireworkStarItem.appendColors(Text.empty().formatted(Formatting.GRAY), is));
        }
        if ((js = nbt.getIntArray("FadeColors")).length > 0) {
            tooltip.add(FireworkStarItem.appendColors(Text.translatable("item.minecraft.firework_star.fade_to").append(ScreenTexts.SPACE).formatted(Formatting.GRAY), js));
        }
        if (nbt.getBoolean("Trail")) {
            tooltip.add(Text.translatable("item.minecraft.firework_star.trail").formatted(Formatting.GRAY));
        }
        if (nbt.getBoolean("Flicker")) {
            tooltip.add(Text.translatable("item.minecraft.firework_star.flicker").formatted(Formatting.GRAY));
        }
    }

    private static Text appendColors(MutableText line, int[] colors) {
        for (int i = 0; i < colors.length; ++i) {
            if (i > 0) {
                line.append(", ");
            }
            line.append(FireworkStarItem.getColorText(colors[i]));
        }
        return line;
    }

    private static Text getColorText(int color) {
        DyeColor lv = DyeColor.byFireworkColor(color);
        if (lv == null) {
            return Text.translatable("item.minecraft.firework_star.custom_color");
        }
        return Text.translatable("item.minecraft.firework_star." + lv.getName());
    }
}

