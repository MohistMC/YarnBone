/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.item;

import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LecternBlock;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.WritableBookItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.stat.Stats;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.StringHelper;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class WrittenBookItem
extends Item {
    public static final int MAX_TITLE_EDIT_LENGTH = 16;
    public static final int MAX_TITLE_VIEW_LENGTH = 32;
    public static final int MAX_PAGE_EDIT_LENGTH = 1024;
    public static final int MAX_PAGE_VIEW_LENGTH = Short.MAX_VALUE;
    public static final int MAX_PAGES = 100;
    public static final int field_30934 = 2;
    public static final String TITLE_KEY = "title";
    public static final String FILTERED_TITLE_KEY = "filtered_title";
    public static final String AUTHOR_KEY = "author";
    public static final String PAGES_KEY = "pages";
    public static final String FILTERED_PAGES_KEY = "filtered_pages";
    public static final String GENERATION_KEY = "generation";
    public static final String RESOLVED_KEY = "resolved";

    public WrittenBookItem(Item.Settings arg) {
        super(arg);
    }

    public static boolean isValid(@Nullable NbtCompound nbt) {
        if (!WritableBookItem.isValid(nbt)) {
            return false;
        }
        if (!nbt.contains(TITLE_KEY, NbtElement.STRING_TYPE)) {
            return false;
        }
        String string = nbt.getString(TITLE_KEY);
        if (string.length() > 32) {
            return false;
        }
        return nbt.contains(AUTHOR_KEY, NbtElement.STRING_TYPE);
    }

    public static int getGeneration(ItemStack stack) {
        return stack.getNbt().getInt(GENERATION_KEY);
    }

    public static int getPageCount(ItemStack stack) {
        NbtCompound lv = stack.getNbt();
        return lv != null ? lv.getList(PAGES_KEY, NbtElement.STRING_TYPE).size() : 0;
    }

    @Override
    public Text getName(ItemStack stack) {
        String string;
        NbtCompound lv = stack.getNbt();
        if (lv != null && !StringHelper.isEmpty(string = lv.getString(TITLE_KEY))) {
            return Text.literal(string);
        }
        return super.getName(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (stack.hasNbt()) {
            NbtCompound lv = stack.getNbt();
            String string = lv.getString(AUTHOR_KEY);
            if (!StringHelper.isEmpty(string)) {
                tooltip.add(Text.translatable("book.byAuthor", string).formatted(Formatting.GRAY));
            }
            tooltip.add(Text.translatable("book.generation." + lv.getInt(GENERATION_KEY)).formatted(Formatting.GRAY));
        }
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        BlockPos lv2;
        World lv = context.getWorld();
        BlockState lv3 = lv.getBlockState(lv2 = context.getBlockPos());
        if (lv3.isOf(Blocks.LECTERN)) {
            return LecternBlock.putBookIfAbsent(context.getPlayer(), lv, lv2, lv3, context.getStack()) ? ActionResult.success(lv.isClient) : ActionResult.PASS;
        }
        return ActionResult.PASS;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack lv = user.getStackInHand(hand);
        user.useBook(lv, hand);
        user.incrementStat(Stats.USED.getOrCreateStat(this));
        return TypedActionResult.success(lv, world.isClient());
    }

    public static boolean resolve(ItemStack book, @Nullable ServerCommandSource commandSource, @Nullable PlayerEntity player) {
        NbtCompound lv = book.getNbt();
        if (lv == null || lv.getBoolean(RESOLVED_KEY)) {
            return false;
        }
        lv.putBoolean(RESOLVED_KEY, true);
        if (!WrittenBookItem.isValid(lv)) {
            return false;
        }
        NbtList lv2 = lv.getList(PAGES_KEY, NbtElement.STRING_TYPE);
        NbtList lv3 = new NbtList();
        for (int i = 0; i < lv2.size(); ++i) {
            String string = WrittenBookItem.textToJson(commandSource, player, lv2.getString(i));
            if (string.length() > Short.MAX_VALUE) {
                return false;
            }
            lv3.add(i, NbtString.of(string));
        }
        if (lv.contains(FILTERED_PAGES_KEY, NbtElement.COMPOUND_TYPE)) {
            NbtCompound lv4 = lv.getCompound(FILTERED_PAGES_KEY);
            NbtCompound lv5 = new NbtCompound();
            for (String string2 : lv4.getKeys()) {
                String string3 = WrittenBookItem.textToJson(commandSource, player, lv4.getString(string2));
                if (string3.length() > Short.MAX_VALUE) {
                    return false;
                }
                lv5.putString(string2, string3);
            }
            lv.put(FILTERED_PAGES_KEY, lv5);
        }
        lv.put(PAGES_KEY, lv3);
        return true;
    }

    private static String textToJson(@Nullable ServerCommandSource commandSource, @Nullable PlayerEntity player, String text) {
        MutableText lv;
        try {
            lv = Text.Serializer.fromLenientJson(text);
            lv = Texts.parse(commandSource, lv, (Entity)player, 0);
        }
        catch (Exception exception) {
            lv = Text.literal(text);
        }
        return Text.Serializer.toJson(lv);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }
}

