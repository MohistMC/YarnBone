/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.item;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LecternBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class WritableBookItem
extends Item {
    public WritableBookItem(Item.Settings arg) {
        super(arg);
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

    public static boolean isValid(@Nullable NbtCompound nbt) {
        if (nbt == null) {
            return false;
        }
        if (!nbt.contains("pages", NbtElement.LIST_TYPE)) {
            return false;
        }
        NbtList lv = nbt.getList("pages", NbtElement.STRING_TYPE);
        for (int i = 0; i < lv.size(); ++i) {
            String string = lv.getString(i);
            if (string.length() <= Short.MAX_VALUE) continue;
            return false;
        }
        return true;
    }
}

