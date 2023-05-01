/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.potion.PotionUtil;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class SuspiciousStewItem
extends Item {
    public static final String EFFECTS_KEY = "Effects";
    public static final String EFFECT_ID_KEY = "EffectId";
    public static final String EFFECT_DURATION_KEY = "EffectDuration";
    public static final int DEFAULT_DURATION = 160;

    public SuspiciousStewItem(Item.Settings arg) {
        super(arg);
    }

    public static void addEffectToStew(ItemStack stew, StatusEffect effect, int duration) {
        NbtCompound lv = stew.getOrCreateNbt();
        NbtList lv2 = lv.getList(EFFECTS_KEY, NbtElement.LIST_TYPE);
        NbtCompound lv3 = new NbtCompound();
        lv3.putInt(EFFECT_ID_KEY, StatusEffect.getRawId(effect));
        lv3.putInt(EFFECT_DURATION_KEY, duration);
        lv2.add(lv3);
        lv.put(EFFECTS_KEY, lv2);
    }

    private static void forEachEffect(ItemStack stew, Consumer<StatusEffectInstance> effectConsumer) {
        NbtCompound lv = stew.getNbt();
        if (lv != null && lv.contains(EFFECTS_KEY, NbtElement.LIST_TYPE)) {
            NbtList lv2 = lv.getList(EFFECTS_KEY, NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < lv2.size(); ++i) {
                NbtCompound lv3 = lv2.getCompound(i);
                int j = lv3.contains(EFFECT_DURATION_KEY, NbtElement.INT_TYPE) ? lv3.getInt(EFFECT_DURATION_KEY) : 160;
                StatusEffect lv4 = StatusEffect.byRawId(lv3.getInt(EFFECT_ID_KEY));
                if (lv4 == null) continue;
                effectConsumer.accept(new StatusEffectInstance(lv4, j));
            }
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        if (context.isCreative()) {
            ArrayList<StatusEffectInstance> list2 = new ArrayList<StatusEffectInstance>();
            SuspiciousStewItem.forEachEffect(stack, list2::add);
            PotionUtil.buildTooltip(list2, tooltip, 1.0f);
        }
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        ItemStack lv = super.finishUsing(stack, world, user);
        SuspiciousStewItem.forEachEffect(lv, user::addStatusEffect);
        if (user instanceof PlayerEntity && ((PlayerEntity)user).getAbilities().creativeMode) {
            return lv;
        }
        return new ItemStack(Items.BOWL);
    }
}

