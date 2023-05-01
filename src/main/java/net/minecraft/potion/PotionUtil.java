/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.potion;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class PotionUtil {
    public static final String CUSTOM_POTION_EFFECTS_KEY = "CustomPotionEffects";
    public static final String CUSTOM_POTION_COLOR_KEY = "CustomPotionColor";
    public static final String POTION_KEY = "Potion";
    private static final int DEFAULT_COLOR = 0xF800F8;
    private static final Text NONE_TEXT = Text.translatable("effect.none").formatted(Formatting.GRAY);

    public static List<StatusEffectInstance> getPotionEffects(ItemStack stack) {
        return PotionUtil.getPotionEffects(stack.getNbt());
    }

    public static List<StatusEffectInstance> getPotionEffects(Potion potion, Collection<StatusEffectInstance> custom) {
        ArrayList<StatusEffectInstance> list = Lists.newArrayList();
        list.addAll(potion.getEffects());
        list.addAll(custom);
        return list;
    }

    public static List<StatusEffectInstance> getPotionEffects(@Nullable NbtCompound nbt) {
        ArrayList<StatusEffectInstance> list = Lists.newArrayList();
        list.addAll(PotionUtil.getPotion(nbt).getEffects());
        PotionUtil.getCustomPotionEffects(nbt, list);
        return list;
    }

    public static List<StatusEffectInstance> getCustomPotionEffects(ItemStack stack) {
        return PotionUtil.getCustomPotionEffects(stack.getNbt());
    }

    public static List<StatusEffectInstance> getCustomPotionEffects(@Nullable NbtCompound nbt) {
        ArrayList<StatusEffectInstance> list = Lists.newArrayList();
        PotionUtil.getCustomPotionEffects(nbt, list);
        return list;
    }

    public static void getCustomPotionEffects(@Nullable NbtCompound nbt, List<StatusEffectInstance> list) {
        if (nbt != null && nbt.contains(CUSTOM_POTION_EFFECTS_KEY, NbtElement.LIST_TYPE)) {
            NbtList lv = nbt.getList(CUSTOM_POTION_EFFECTS_KEY, NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < lv.size(); ++i) {
                NbtCompound lv2 = lv.getCompound(i);
                StatusEffectInstance lv3 = StatusEffectInstance.fromNbt(lv2);
                if (lv3 == null) continue;
                list.add(lv3);
            }
        }
    }

    public static int getColor(ItemStack stack) {
        NbtCompound lv = stack.getNbt();
        if (lv != null && lv.contains(CUSTOM_POTION_COLOR_KEY, NbtElement.NUMBER_TYPE)) {
            return lv.getInt(CUSTOM_POTION_COLOR_KEY);
        }
        return PotionUtil.getPotion(stack) == Potions.EMPTY ? 0xF800F8 : PotionUtil.getColor(PotionUtil.getPotionEffects(stack));
    }

    public static int getColor(Potion potion) {
        return potion == Potions.EMPTY ? 0xF800F8 : PotionUtil.getColor(potion.getEffects());
    }

    public static int getColor(Collection<StatusEffectInstance> effects) {
        int i = 3694022;
        if (effects.isEmpty()) {
            return 3694022;
        }
        float f = 0.0f;
        float g = 0.0f;
        float h = 0.0f;
        int j = 0;
        for (StatusEffectInstance lv : effects) {
            if (!lv.shouldShowParticles()) continue;
            int k = lv.getEffectType().getColor();
            int l = lv.getAmplifier() + 1;
            f += (float)(l * (k >> 16 & 0xFF)) / 255.0f;
            g += (float)(l * (k >> 8 & 0xFF)) / 255.0f;
            h += (float)(l * (k >> 0 & 0xFF)) / 255.0f;
            j += l;
        }
        if (j == 0) {
            return 0;
        }
        f = f / (float)j * 255.0f;
        g = g / (float)j * 255.0f;
        h = h / (float)j * 255.0f;
        return (int)f << 16 | (int)g << 8 | (int)h;
    }

    public static Potion getPotion(ItemStack stack) {
        return PotionUtil.getPotion(stack.getNbt());
    }

    public static Potion getPotion(@Nullable NbtCompound compound) {
        if (compound == null) {
            return Potions.EMPTY;
        }
        return Potion.byId(compound.getString(POTION_KEY));
    }

    public static ItemStack setPotion(ItemStack stack, Potion potion) {
        Identifier lv = Registries.POTION.getId(potion);
        if (potion == Potions.EMPTY) {
            stack.removeSubNbt(POTION_KEY);
        } else {
            stack.getOrCreateNbt().putString(POTION_KEY, lv.toString());
        }
        return stack;
    }

    public static ItemStack setCustomPotionEffects(ItemStack stack, Collection<StatusEffectInstance> effects) {
        if (effects.isEmpty()) {
            return stack;
        }
        NbtCompound lv = stack.getOrCreateNbt();
        NbtList lv2 = lv.getList(CUSTOM_POTION_EFFECTS_KEY, NbtElement.LIST_TYPE);
        for (StatusEffectInstance lv3 : effects) {
            lv2.add(lv3.writeNbt(new NbtCompound()));
        }
        lv.put(CUSTOM_POTION_EFFECTS_KEY, lv2);
        return stack;
    }

    public static void buildTooltip(ItemStack stack, List<Text> list, float durationMultiplier) {
        PotionUtil.buildTooltip(PotionUtil.getPotionEffects(stack), list, durationMultiplier);
    }

    public static void buildTooltip(List<StatusEffectInstance> statusEffects, List<Text> list, float durationMultiplier) {
        ArrayList<Pair<EntityAttribute, EntityAttributeModifier>> list3 = Lists.newArrayList();
        if (statusEffects.isEmpty()) {
            list.add(NONE_TEXT);
        } else {
            for (StatusEffectInstance statusEffectInstance : statusEffects) {
                MutableText lv2 = Text.translatable(statusEffectInstance.getTranslationKey());
                StatusEffect lv3 = statusEffectInstance.getEffectType();
                Map<EntityAttribute, EntityAttributeModifier> map = lv3.getAttributeModifiers();
                if (!map.isEmpty()) {
                    for (Map.Entry<EntityAttribute, EntityAttributeModifier> entry : map.entrySet()) {
                        EntityAttributeModifier lv4 = entry.getValue();
                        EntityAttributeModifier lv5 = new EntityAttributeModifier(lv4.getName(), lv3.adjustModifierAmount(statusEffectInstance.getAmplifier(), lv4), lv4.getOperation());
                        list3.add(new Pair<EntityAttribute, EntityAttributeModifier>(entry.getKey(), lv5));
                    }
                }
                if (statusEffectInstance.getAmplifier() > 0) {
                    lv2 = Text.translatable("potion.withAmplifier", lv2, Text.translatable("potion.potency." + statusEffectInstance.getAmplifier()));
                }
                if (!statusEffectInstance.isDurationBelow(20)) {
                    lv2 = Text.translatable("potion.withDuration", lv2, StatusEffectUtil.durationToString(statusEffectInstance, durationMultiplier));
                }
                list.add(lv2.formatted(lv3.getCategory().getFormatting()));
            }
        }
        if (!list3.isEmpty()) {
            list.add(ScreenTexts.EMPTY);
            list.add(Text.translatable("potion.whenDrank").formatted(Formatting.DARK_PURPLE));
            for (Pair pair : list3) {
                EntityAttributeModifier lv6 = (EntityAttributeModifier)pair.getSecond();
                double d = lv6.getValue();
                double e = lv6.getOperation() == EntityAttributeModifier.Operation.MULTIPLY_BASE || lv6.getOperation() == EntityAttributeModifier.Operation.MULTIPLY_TOTAL ? lv6.getValue() * 100.0 : lv6.getValue();
                if (d > 0.0) {
                    list.add(Text.translatable("attribute.modifier.plus." + lv6.getOperation().getId(), ItemStack.MODIFIER_FORMAT.format(e), Text.translatable(((EntityAttribute)pair.getFirst()).getTranslationKey())).formatted(Formatting.BLUE));
                    continue;
                }
                if (!(d < 0.0)) continue;
                list.add(Text.translatable("attribute.modifier.take." + lv6.getOperation().getId(), ItemStack.MODIFIER_FORMAT.format(e *= -1.0), Text.translatable(((EntityAttribute)pair.getFirst()).getTranslationKey())).formatted(Formatting.RED));
            }
        }
    }
}

