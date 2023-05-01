/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.item;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.client.item.BundleTooltipData;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.item.TooltipData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ClickType;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class BundleItem
extends Item {
    private static final String ITEMS_KEY = "Items";
    public static final int MAX_STORAGE = 64;
    private static final int BUNDLE_ITEM_OCCUPANCY = 4;
    private static final int ITEM_BAR_COLOR = MathHelper.packRgb(0.4f, 0.4f, 1.0f);

    public BundleItem(Item.Settings arg) {
        super(arg);
    }

    public static float getAmountFilled(ItemStack stack) {
        return (float)BundleItem.getBundleOccupancy(stack) / 64.0f;
    }

    @Override
    public boolean onStackClicked(ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player) {
        if (clickType != ClickType.RIGHT) {
            return false;
        }
        ItemStack lv = slot.getStack();
        if (lv.isEmpty()) {
            this.playRemoveOneSound(player);
            BundleItem.removeFirstStack(stack).ifPresent(removedStack -> BundleItem.addToBundle(stack, slot.insertStack((ItemStack)removedStack)));
        } else if (lv.getItem().canBeNested()) {
            int i = (64 - BundleItem.getBundleOccupancy(stack)) / BundleItem.getItemOccupancy(lv);
            int j = BundleItem.addToBundle(stack, slot.takeStackRange(lv.getCount(), i, player));
            if (j > 0) {
                this.playInsertSound(player);
            }
        }
        return true;
    }

    @Override
    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        if (clickType != ClickType.RIGHT || !slot.canTakePartial(player)) {
            return false;
        }
        if (otherStack.isEmpty()) {
            BundleItem.removeFirstStack(stack).ifPresent(arg3 -> {
                this.playRemoveOneSound(player);
                cursorStackReference.set((ItemStack)arg3);
            });
        } else {
            int i = BundleItem.addToBundle(stack, otherStack);
            if (i > 0) {
                this.playInsertSound(player);
                otherStack.decrement(i);
            }
        }
        return true;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack lv = user.getStackInHand(hand);
        if (BundleItem.dropAllBundledItems(lv, user)) {
            this.playDropContentsSound(user);
            user.incrementStat(Stats.USED.getOrCreateStat(this));
            return TypedActionResult.success(lv, world.isClient());
        }
        return TypedActionResult.fail(lv);
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return BundleItem.getBundleOccupancy(stack) > 0;
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        return Math.min(1 + 12 * BundleItem.getBundleOccupancy(stack) / 64, 13);
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        return ITEM_BAR_COLOR;
    }

    private static int addToBundle(ItemStack bundle, ItemStack stack) {
        if (stack.isEmpty() || !stack.getItem().canBeNested()) {
            return 0;
        }
        NbtCompound lv = bundle.getOrCreateNbt();
        if (!lv.contains(ITEMS_KEY)) {
            lv.put(ITEMS_KEY, new NbtList());
        }
        int i = BundleItem.getBundleOccupancy(bundle);
        int j = BundleItem.getItemOccupancy(stack);
        int k = Math.min(stack.getCount(), (64 - i) / j);
        if (k == 0) {
            return 0;
        }
        NbtList lv2 = lv.getList(ITEMS_KEY, NbtElement.COMPOUND_TYPE);
        Optional<NbtCompound> optional = BundleItem.canMergeStack(stack, lv2);
        if (optional.isPresent()) {
            NbtCompound lv3 = optional.get();
            ItemStack lv4 = ItemStack.fromNbt(lv3);
            lv4.increment(k);
            lv4.writeNbt(lv3);
            lv2.remove(lv3);
            lv2.add(0, lv3);
        } else {
            ItemStack lv5 = stack.copy();
            lv5.setCount(k);
            NbtCompound lv6 = new NbtCompound();
            lv5.writeNbt(lv6);
            lv2.add(0, lv6);
        }
        return k;
    }

    private static Optional<NbtCompound> canMergeStack(ItemStack stack, NbtList items) {
        if (stack.isOf(Items.BUNDLE)) {
            return Optional.empty();
        }
        return items.stream().filter(NbtCompound.class::isInstance).map(NbtCompound.class::cast).filter(item -> ItemStack.canCombine(ItemStack.fromNbt(item), stack)).findFirst();
    }

    private static int getItemOccupancy(ItemStack stack) {
        NbtCompound lv;
        if (stack.isOf(Items.BUNDLE)) {
            return 4 + BundleItem.getBundleOccupancy(stack);
        }
        if ((stack.isOf(Items.BEEHIVE) || stack.isOf(Items.BEE_NEST)) && stack.hasNbt() && (lv = BlockItem.getBlockEntityNbt(stack)) != null && !lv.getList("Bees", NbtElement.COMPOUND_TYPE).isEmpty()) {
            return 64;
        }
        return 64 / stack.getMaxCount();
    }

    private static int getBundleOccupancy(ItemStack stack) {
        return BundleItem.getBundledStacks(stack).mapToInt(arg -> BundleItem.getItemOccupancy(arg) * arg.getCount()).sum();
    }

    private static Optional<ItemStack> removeFirstStack(ItemStack stack) {
        NbtCompound lv = stack.getOrCreateNbt();
        if (!lv.contains(ITEMS_KEY)) {
            return Optional.empty();
        }
        NbtList lv2 = lv.getList(ITEMS_KEY, NbtElement.COMPOUND_TYPE);
        if (lv2.isEmpty()) {
            return Optional.empty();
        }
        boolean i = false;
        NbtCompound lv3 = lv2.getCompound(0);
        ItemStack lv4 = ItemStack.fromNbt(lv3);
        lv2.remove(0);
        if (lv2.isEmpty()) {
            stack.removeSubNbt(ITEMS_KEY);
        }
        return Optional.of(lv4);
    }

    private static boolean dropAllBundledItems(ItemStack stack, PlayerEntity player) {
        NbtCompound lv = stack.getOrCreateNbt();
        if (!lv.contains(ITEMS_KEY)) {
            return false;
        }
        if (player instanceof ServerPlayerEntity) {
            NbtList lv2 = lv.getList(ITEMS_KEY, NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < lv2.size(); ++i) {
                NbtCompound lv3 = lv2.getCompound(i);
                ItemStack lv4 = ItemStack.fromNbt(lv3);
                player.dropItem(lv4, true);
            }
        }
        stack.removeSubNbt(ITEMS_KEY);
        return true;
    }

    private static Stream<ItemStack> getBundledStacks(ItemStack stack) {
        NbtCompound lv = stack.getNbt();
        if (lv == null) {
            return Stream.empty();
        }
        NbtList lv2 = lv.getList(ITEMS_KEY, NbtElement.COMPOUND_TYPE);
        return lv2.stream().map(NbtCompound.class::cast).map(ItemStack::fromNbt);
    }

    @Override
    public Optional<TooltipData> getTooltipData(ItemStack stack) {
        DefaultedList<ItemStack> lv = DefaultedList.of();
        BundleItem.getBundledStacks(stack).forEach(lv::add);
        return Optional.of(new BundleTooltipData(lv, BundleItem.getBundleOccupancy(stack)));
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("item.minecraft.bundle.fullness", BundleItem.getBundleOccupancy(stack), 64).formatted(Formatting.GRAY));
    }

    @Override
    public void onItemEntityDestroyed(ItemEntity entity) {
        ItemUsage.spawnItemContents(entity, BundleItem.getBundledStacks(entity.getStack()));
    }

    private void playRemoveOneSound(Entity entity) {
        entity.playSound(SoundEvents.ITEM_BUNDLE_REMOVE_ONE, 0.8f, 0.8f + entity.getWorld().getRandom().nextFloat() * 0.4f);
    }

    private void playInsertSound(Entity entity) {
        entity.playSound(SoundEvents.ITEM_BUNDLE_INSERT, 0.8f, 0.8f + entity.getWorld().getRandom().nextFloat() * 0.4f);
    }

    private void playDropContentsSound(Entity entity) {
        entity.playSound(SoundEvents.ITEM_BUNDLE_DROP_CONTENTS, 0.8f, 0.8f + entity.getWorld().getRandom().nextFloat() * 0.4f);
    }
}

