/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.screen;

import com.google.common.base.Suppliers;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.Property;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.screen.ScreenHandlerSyncHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ClickType;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public abstract class ScreenHandler {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int EMPTY_SPACE_SLOT_INDEX = -999;
    public static final int field_30731 = 0;
    public static final int field_30732 = 1;
    public static final int field_30733 = 2;
    public static final int field_30734 = 0;
    public static final int field_30735 = 1;
    public static final int field_30736 = 2;
    public static final int field_30737 = Integer.MAX_VALUE;
    private final DefaultedList<ItemStack> trackedStacks = DefaultedList.of();
    public final DefaultedList<Slot> slots = DefaultedList.of();
    private final List<Property> properties = Lists.newArrayList();
    private ItemStack cursorStack = ItemStack.EMPTY;
    private final DefaultedList<ItemStack> previousTrackedStacks = DefaultedList.of();
    private final IntList trackedPropertyValues = new IntArrayList();
    private ItemStack previousCursorStack = ItemStack.EMPTY;
    private int revision;
    @Nullable
    private final ScreenHandlerType<?> type;
    public final int syncId;
    private int quickCraftButton = -1;
    private int quickCraftStage;
    private final Set<Slot> quickCraftSlots = Sets.newHashSet();
    private final List<ScreenHandlerListener> listeners = Lists.newArrayList();
    @Nullable
    private ScreenHandlerSyncHandler syncHandler;
    private boolean disableSync;

    protected ScreenHandler(@Nullable ScreenHandlerType<?> type, int syncId) {
        this.type = type;
        this.syncId = syncId;
    }

    protected static boolean canUse(ScreenHandlerContext context, PlayerEntity player, Block block) {
        return context.get((world, pos) -> {
            if (!world.getBlockState((BlockPos)pos).isOf(block)) {
                return false;
            }
            return player.squaredDistanceTo((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5) <= 64.0;
        }, true);
    }

    public ScreenHandlerType<?> getType() {
        if (this.type == null) {
            throw new UnsupportedOperationException("Unable to construct this menu by type");
        }
        return this.type;
    }

    protected static void checkSize(Inventory inventory, int expectedSize) {
        int j = inventory.size();
        if (j < expectedSize) {
            throw new IllegalArgumentException("Container size " + j + " is smaller than expected " + expectedSize);
        }
    }

    protected static void checkDataCount(PropertyDelegate data, int expectedCount) {
        int j = data.size();
        if (j < expectedCount) {
            throw new IllegalArgumentException("Container data count " + j + " is smaller than expected " + expectedCount);
        }
    }

    public boolean isValid(int slot) {
        return slot == -1 || slot == -999 || slot < this.slots.size();
    }

    protected Slot addSlot(Slot slot) {
        slot.id = this.slots.size();
        this.slots.add(slot);
        this.trackedStacks.add(ItemStack.EMPTY);
        this.previousTrackedStacks.add(ItemStack.EMPTY);
        return slot;
    }

    protected Property addProperty(Property property) {
        this.properties.add(property);
        this.trackedPropertyValues.add(0);
        return property;
    }

    protected void addProperties(PropertyDelegate propertyDelegate) {
        for (int i = 0; i < propertyDelegate.size(); ++i) {
            this.addProperty(Property.create(propertyDelegate, i));
        }
    }

    public void addListener(ScreenHandlerListener listener) {
        if (this.listeners.contains(listener)) {
            return;
        }
        this.listeners.add(listener);
        this.sendContentUpdates();
    }

    public void updateSyncHandler(ScreenHandlerSyncHandler handler) {
        this.syncHandler = handler;
        this.syncState();
    }

    public void syncState() {
        int i;
        int j = this.slots.size();
        for (i = 0; i < j; ++i) {
            this.previousTrackedStacks.set(i, this.slots.get(i).getStack().copy());
        }
        this.previousCursorStack = this.getCursorStack().copy();
        j = this.properties.size();
        for (i = 0; i < j; ++i) {
            this.trackedPropertyValues.set(i, this.properties.get(i).get());
        }
        if (this.syncHandler != null) {
            this.syncHandler.updateState(this, this.previousTrackedStacks, this.previousCursorStack, this.trackedPropertyValues.toIntArray());
        }
    }

    public void removeListener(ScreenHandlerListener listener) {
        this.listeners.remove(listener);
    }

    public DefaultedList<ItemStack> getStacks() {
        DefaultedList<ItemStack> lv = DefaultedList.of();
        for (Slot lv2 : this.slots) {
            lv.add(lv2.getStack());
        }
        return lv;
    }

    public void sendContentUpdates() {
        int i;
        for (i = 0; i < this.slots.size(); ++i) {
            ItemStack lv = this.slots.get(i).getStack();
            com.google.common.base.Supplier<ItemStack> supplier = Suppliers.memoize(lv::copy);
            this.updateTrackedSlot(i, lv, supplier);
            this.checkSlotUpdates(i, lv, supplier);
        }
        this.checkCursorStackUpdates();
        for (i = 0; i < this.properties.size(); ++i) {
            Property lv2 = this.properties.get(i);
            int j = lv2.get();
            if (lv2.hasChanged()) {
                this.notifyPropertyUpdate(i, j);
            }
            this.checkPropertyUpdates(i, j);
        }
    }

    public void updateToClient() {
        int i;
        for (i = 0; i < this.slots.size(); ++i) {
            ItemStack lv = this.slots.get(i).getStack();
            this.updateTrackedSlot(i, lv, lv::copy);
        }
        for (i = 0; i < this.properties.size(); ++i) {
            Property lv2 = this.properties.get(i);
            if (!lv2.hasChanged()) continue;
            this.notifyPropertyUpdate(i, lv2.get());
        }
        this.syncState();
    }

    private void notifyPropertyUpdate(int index, int value) {
        for (ScreenHandlerListener lv : this.listeners) {
            lv.onPropertyUpdate(this, index, value);
        }
    }

    private void updateTrackedSlot(int slot, ItemStack stack, Supplier<ItemStack> copySupplier) {
        ItemStack lv = this.trackedStacks.get(slot);
        if (!ItemStack.areEqual(lv, stack)) {
            ItemStack lv2 = copySupplier.get();
            this.trackedStacks.set(slot, lv2);
            for (ScreenHandlerListener lv3 : this.listeners) {
                lv3.onSlotUpdate(this, slot, lv2);
            }
        }
    }

    private void checkSlotUpdates(int slot, ItemStack stack, Supplier<ItemStack> copySupplier) {
        if (this.disableSync) {
            return;
        }
        ItemStack lv = this.previousTrackedStacks.get(slot);
        if (!ItemStack.areEqual(lv, stack)) {
            ItemStack lv2 = copySupplier.get();
            this.previousTrackedStacks.set(slot, lv2);
            if (this.syncHandler != null) {
                this.syncHandler.updateSlot(this, slot, lv2);
            }
        }
    }

    private void checkPropertyUpdates(int id, int value) {
        if (this.disableSync) {
            return;
        }
        int k = this.trackedPropertyValues.getInt(id);
        if (k != value) {
            this.trackedPropertyValues.set(id, value);
            if (this.syncHandler != null) {
                this.syncHandler.updateProperty(this, id, value);
            }
        }
    }

    private void checkCursorStackUpdates() {
        if (this.disableSync) {
            return;
        }
        if (!ItemStack.areEqual(this.getCursorStack(), this.previousCursorStack)) {
            this.previousCursorStack = this.getCursorStack().copy();
            if (this.syncHandler != null) {
                this.syncHandler.updateCursorStack(this, this.previousCursorStack);
            }
        }
    }

    public void setPreviousTrackedSlot(int slot, ItemStack stack) {
        this.previousTrackedStacks.set(slot, stack.copy());
    }

    public void setPreviousTrackedSlotMutable(int slot, ItemStack stack) {
        if (slot < 0 || slot >= this.previousTrackedStacks.size()) {
            LOGGER.debug("Incorrect slot index: {} available slots: {}", (Object)slot, (Object)this.previousTrackedStacks.size());
            return;
        }
        this.previousTrackedStacks.set(slot, stack);
    }

    public void setPreviousCursorStack(ItemStack stack) {
        this.previousCursorStack = stack.copy();
    }

    public boolean onButtonClick(PlayerEntity player, int id) {
        return false;
    }

    public Slot getSlot(int index) {
        return this.slots.get(index);
    }

    public abstract ItemStack quickMove(PlayerEntity var1, int var2);

    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        try {
            this.internalOnSlotClick(slotIndex, button, actionType, player);
        }
        catch (Exception exception) {
            CrashReport lv = CrashReport.create(exception, "Container click");
            CrashReportSection lv2 = lv.addElement("Click info");
            lv2.add("Menu Type", () -> this.type != null ? Registries.SCREEN_HANDLER.getId(this.type).toString() : "<no type>");
            lv2.add("Menu Class", () -> this.getClass().getCanonicalName());
            lv2.add("Slot Count", this.slots.size());
            lv2.add("Slot", slotIndex);
            lv2.add("Button", button);
            lv2.add("Type", (Object)actionType);
            throw new CrashException(lv);
        }
    }

    private void internalOnSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        block39: {
            block50: {
                block46: {
                    ItemStack lv3;
                    ItemStack lv4;
                    Slot lv10;
                    PlayerInventory lv;
                    block49: {
                        block48: {
                            block47: {
                                block44: {
                                    ClickType lv8;
                                    block45: {
                                        block43: {
                                            block37: {
                                                block42: {
                                                    ItemStack lv32;
                                                    block41: {
                                                        block40: {
                                                            block38: {
                                                                lv = player.getInventory();
                                                                if (actionType != SlotActionType.QUICK_CRAFT) break block37;
                                                                int k = this.quickCraftStage;
                                                                this.quickCraftStage = ScreenHandler.unpackQuickCraftStage(button);
                                                                if (k == 1 && this.quickCraftStage == 2 || k == this.quickCraftStage) break block38;
                                                                this.endQuickCraft();
                                                                break block39;
                                                            }
                                                            if (!this.getCursorStack().isEmpty()) break block40;
                                                            this.endQuickCraft();
                                                            break block39;
                                                        }
                                                        if (this.quickCraftStage != 0) break block41;
                                                        this.quickCraftButton = ScreenHandler.unpackQuickCraftButton(button);
                                                        if (ScreenHandler.shouldQuickCraftContinue(this.quickCraftButton, player)) {
                                                            this.quickCraftStage = 1;
                                                            this.quickCraftSlots.clear();
                                                        } else {
                                                            this.endQuickCraft();
                                                        }
                                                        break block39;
                                                    }
                                                    if (this.quickCraftStage != 1) break block42;
                                                    Slot lv2 = this.slots.get(slotIndex);
                                                    if (!ScreenHandler.canInsertItemIntoSlot(lv2, lv32 = this.getCursorStack(), true) || !lv2.canInsert(lv32) || this.quickCraftButton != 2 && lv32.getCount() <= this.quickCraftSlots.size() || !this.canInsertIntoSlot(lv2)) break block39;
                                                    this.quickCraftSlots.add(lv2);
                                                    break block39;
                                                }
                                                if (this.quickCraftStage == 2) {
                                                    if (!this.quickCraftSlots.isEmpty()) {
                                                        if (this.quickCraftSlots.size() == 1) {
                                                            int l = this.quickCraftSlots.iterator().next().id;
                                                            this.endQuickCraft();
                                                            this.internalOnSlotClick(l, this.quickCraftButton, SlotActionType.PICKUP, player);
                                                            return;
                                                        }
                                                        ItemStack lv42 = this.getCursorStack().copy();
                                                        int m = this.getCursorStack().getCount();
                                                        for (Slot lv5 : this.quickCraftSlots) {
                                                            ItemStack lv6 = this.getCursorStack();
                                                            if (lv5 == null || !ScreenHandler.canInsertItemIntoSlot(lv5, lv6, true) || !lv5.canInsert(lv6) || this.quickCraftButton != 2 && lv6.getCount() < this.quickCraftSlots.size() || !this.canInsertIntoSlot(lv5)) continue;
                                                            ItemStack lv7 = lv42.copy();
                                                            int n = lv5.hasStack() ? lv5.getStack().getCount() : 0;
                                                            ScreenHandler.calculateStackSize(this.quickCraftSlots, this.quickCraftButton, lv7, n);
                                                            int o = Math.min(lv7.getMaxCount(), lv5.getMaxItemCount(lv7));
                                                            if (lv7.getCount() > o) {
                                                                lv7.setCount(o);
                                                            }
                                                            m -= lv7.getCount() - n;
                                                            lv5.setStack(lv7);
                                                        }
                                                        lv42.setCount(m);
                                                        this.setCursorStack(lv42);
                                                    }
                                                    this.endQuickCraft();
                                                } else {
                                                    this.endQuickCraft();
                                                }
                                                break block39;
                                            }
                                            if (this.quickCraftStage == 0) break block43;
                                            this.endQuickCraft();
                                            break block39;
                                        }
                                        if (actionType != SlotActionType.PICKUP && actionType != SlotActionType.QUICK_MOVE || button != 0 && button != 1) break block44;
                                        ClickType clickType = lv8 = button == 0 ? ClickType.LEFT : ClickType.RIGHT;
                                        if (slotIndex != EMPTY_SPACE_SLOT_INDEX) break block45;
                                        if (this.getCursorStack().isEmpty()) break block39;
                                        if (lv8 == ClickType.LEFT) {
                                            player.dropItem(this.getCursorStack(), true);
                                            this.setCursorStack(ItemStack.EMPTY);
                                        } else {
                                            player.dropItem(this.getCursorStack().split(1), true);
                                        }
                                        break block39;
                                    }
                                    if (actionType == SlotActionType.QUICK_MOVE) {
                                        if (slotIndex < 0) {
                                            return;
                                        }
                                        Slot lv2 = this.slots.get(slotIndex);
                                        if (!lv2.canTakeItems(player)) {
                                            return;
                                        }
                                        ItemStack lv33 = this.quickMove(player, slotIndex);
                                        while (!lv33.isEmpty() && ItemStack.areItemsEqual(lv2.getStack(), lv33)) {
                                            lv33 = this.quickMove(player, slotIndex);
                                        }
                                    } else {
                                        if (slotIndex < 0) {
                                            return;
                                        }
                                        Slot lv2 = this.slots.get(slotIndex);
                                        ItemStack lv34 = lv2.getStack();
                                        ItemStack lv9 = this.getCursorStack();
                                        player.onPickupSlotClick(lv9, lv2.getStack(), lv8);
                                        if (!this.handleSlotClick(player, lv8, lv2, lv34, lv9)) {
                                            if (lv34.isEmpty()) {
                                                if (!lv9.isEmpty()) {
                                                    int p = lv8 == ClickType.LEFT ? lv9.getCount() : 1;
                                                    this.setCursorStack(lv2.insertStack(lv9, p));
                                                }
                                            } else if (lv2.canTakeItems(player)) {
                                                if (lv9.isEmpty()) {
                                                    int p = lv8 == ClickType.LEFT ? lv34.getCount() : (lv34.getCount() + 1) / 2;
                                                    Optional<ItemStack> optional = lv2.tryTakeStackRange(p, Integer.MAX_VALUE, player);
                                                    optional.ifPresent(stack -> {
                                                        this.setCursorStack((ItemStack)stack);
                                                        lv2.onTakeItem(player, (ItemStack)stack);
                                                    });
                                                } else if (lv2.canInsert(lv9)) {
                                                    if (ItemStack.canCombine(lv34, lv9)) {
                                                        int p = lv8 == ClickType.LEFT ? lv9.getCount() : 1;
                                                        this.setCursorStack(lv2.insertStack(lv9, p));
                                                    } else if (lv9.getCount() <= lv2.getMaxItemCount(lv9)) {
                                                        this.setCursorStack(lv34);
                                                        lv2.setStack(lv9);
                                                    }
                                                } else if (ItemStack.canCombine(lv34, lv9)) {
                                                    Optional<ItemStack> optional2 = lv2.tryTakeStackRange(lv34.getCount(), lv9.getMaxCount() - lv9.getCount(), player);
                                                    optional2.ifPresent(stack -> {
                                                        lv9.increment(stack.getCount());
                                                        lv2.onTakeItem(player, (ItemStack)stack);
                                                    });
                                                }
                                            }
                                        }
                                        lv2.markDirty();
                                    }
                                    break block39;
                                }
                                if (actionType != SlotActionType.SWAP) break block46;
                                lv10 = this.slots.get(slotIndex);
                                lv4 = lv.getStack(button);
                                lv3 = lv10.getStack();
                                if (lv4.isEmpty() && lv3.isEmpty()) break block39;
                                if (!lv4.isEmpty()) break block47;
                                if (!lv10.canTakeItems(player)) break block39;
                                lv.setStack(button, lv3);
                                lv10.onTake(lv3.getCount());
                                lv10.setStack(ItemStack.EMPTY);
                                lv10.onTakeItem(player, lv3);
                                break block39;
                            }
                            if (!lv3.isEmpty()) break block48;
                            if (!lv10.canInsert(lv4)) break block39;
                            int q = lv10.getMaxItemCount(lv4);
                            if (lv4.getCount() > q) {
                                lv10.setStack(lv4.split(q));
                            } else {
                                lv.setStack(button, ItemStack.EMPTY);
                                lv10.setStack(lv4);
                            }
                            break block39;
                        }
                        if (!lv10.canTakeItems(player) || !lv10.canInsert(lv4)) break block39;
                        int q = lv10.getMaxItemCount(lv4);
                        if (lv4.getCount() <= q) break block49;
                        lv10.setStack(lv4.split(q));
                        lv10.onTakeItem(player, lv3);
                        if (lv.insertStack(lv3)) break block39;
                        player.dropItem(lv3, true);
                        break block39;
                    }
                    lv.setStack(button, lv3);
                    lv10.setStack(lv4);
                    lv10.onTakeItem(player, lv3);
                    break block39;
                }
                if (actionType != SlotActionType.CLONE || !player.getAbilities().creativeMode || !this.getCursorStack().isEmpty() || slotIndex < 0) break block50;
                Slot lv10 = this.slots.get(slotIndex);
                if (!lv10.hasStack()) break block39;
                ItemStack lv4 = lv10.getStack().copy();
                lv4.setCount(lv4.getMaxCount());
                this.setCursorStack(lv4);
                break block39;
            }
            if (actionType == SlotActionType.THROW && this.getCursorStack().isEmpty() && slotIndex >= 0) {
                Slot lv10 = this.slots.get(slotIndex);
                int l = button == 0 ? 1 : lv10.getStack().getCount();
                ItemStack lv3 = lv10.takeStackRange(l, Integer.MAX_VALUE, player);
                player.dropItem(lv3, true);
            } else if (actionType == SlotActionType.PICKUP_ALL && slotIndex >= 0) {
                Slot lv10 = this.slots.get(slotIndex);
                ItemStack lv4 = this.getCursorStack();
                if (!(lv4.isEmpty() || lv10.hasStack() && lv10.canTakeItems(player))) {
                    int m = button == 0 ? 0 : this.slots.size() - 1;
                    int q = button == 0 ? 1 : -1;
                    for (int p = 0; p < 2; ++p) {
                        for (int r = m; r >= 0 && r < this.slots.size() && lv4.getCount() < lv4.getMaxCount(); r += q) {
                            Slot lv11 = this.slots.get(r);
                            if (!lv11.hasStack() || !ScreenHandler.canInsertItemIntoSlot(lv11, lv4, true) || !lv11.canTakeItems(player) || !this.canInsertIntoSlot(lv4, lv11)) continue;
                            ItemStack lv12 = lv11.getStack();
                            if (p == 0 && lv12.getCount() == lv12.getMaxCount()) continue;
                            ItemStack lv13 = lv11.takeStackRange(lv12.getCount(), lv4.getMaxCount() - lv4.getCount(), player);
                            lv4.increment(lv13.getCount());
                        }
                    }
                }
            }
        }
    }

    private boolean handleSlotClick(PlayerEntity player, ClickType clickType, Slot slot, ItemStack stack, ItemStack cursorStack) {
        FeatureSet lv = player.getWorld().getEnabledFeatures();
        if (cursorStack.isItemEnabled(lv) && cursorStack.onStackClicked(slot, clickType, player)) {
            return true;
        }
        return stack.isItemEnabled(lv) && stack.onClicked(cursorStack, slot, clickType, player, this.getCursorStackReference());
    }

    private StackReference getCursorStackReference() {
        return new StackReference(){

            @Override
            public ItemStack get() {
                return ScreenHandler.this.getCursorStack();
            }

            @Override
            public boolean set(ItemStack stack) {
                ScreenHandler.this.setCursorStack(stack);
                return true;
            }
        };
    }

    public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
        return true;
    }

    public void onClosed(PlayerEntity player) {
        ItemStack lv;
        if (player instanceof ServerPlayerEntity && !(lv = this.getCursorStack()).isEmpty()) {
            if (!player.isAlive() || ((ServerPlayerEntity)player).isDisconnected()) {
                player.dropItem(lv, false);
            } else {
                player.getInventory().offerOrDrop(lv);
            }
            this.setCursorStack(ItemStack.EMPTY);
        }
    }

    protected void dropInventory(PlayerEntity player, Inventory inventory) {
        if (!player.isAlive() || player instanceof ServerPlayerEntity && ((ServerPlayerEntity)player).isDisconnected()) {
            for (int i = 0; i < inventory.size(); ++i) {
                player.dropItem(inventory.removeStack(i), false);
            }
            return;
        }
        for (int i = 0; i < inventory.size(); ++i) {
            PlayerInventory lv = player.getInventory();
            if (!(lv.player instanceof ServerPlayerEntity)) continue;
            lv.offerOrDrop(inventory.removeStack(i));
        }
    }

    public void onContentChanged(Inventory inventory) {
        this.sendContentUpdates();
    }

    public void setStackInSlot(int slot, int revision, ItemStack stack) {
        this.getSlot(slot).setStackNoCallbacks(stack);
        this.revision = revision;
    }

    public void updateSlotStacks(int revision, List<ItemStack> stacks, ItemStack cursorStack) {
        for (int j = 0; j < stacks.size(); ++j) {
            this.getSlot(j).setStackNoCallbacks(stacks.get(j));
        }
        this.cursorStack = cursorStack;
        this.revision = revision;
    }

    public void setProperty(int id, int value) {
        this.properties.get(id).set(value);
    }

    public abstract boolean canUse(PlayerEntity var1);

    protected boolean insertItem(ItemStack stack, int startIndex, int endIndex, boolean fromLast) {
        ItemStack lv2;
        Slot lv;
        boolean bl2 = false;
        int k = startIndex;
        if (fromLast) {
            k = endIndex - 1;
        }
        if (stack.isStackable()) {
            while (!stack.isEmpty() && (fromLast ? k >= startIndex : k < endIndex)) {
                lv = this.slots.get(k);
                lv2 = lv.getStack();
                if (!lv2.isEmpty() && ItemStack.canCombine(stack, lv2)) {
                    int l = lv2.getCount() + stack.getCount();
                    if (l <= stack.getMaxCount()) {
                        stack.setCount(0);
                        lv2.setCount(l);
                        lv.markDirty();
                        bl2 = true;
                    } else if (lv2.getCount() < stack.getMaxCount()) {
                        stack.decrement(stack.getMaxCount() - lv2.getCount());
                        lv2.setCount(stack.getMaxCount());
                        lv.markDirty();
                        bl2 = true;
                    }
                }
                if (fromLast) {
                    --k;
                    continue;
                }
                ++k;
            }
        }
        if (!stack.isEmpty()) {
            k = fromLast ? endIndex - 1 : startIndex;
            while (fromLast ? k >= startIndex : k < endIndex) {
                lv = this.slots.get(k);
                lv2 = lv.getStack();
                if (lv2.isEmpty() && lv.canInsert(stack)) {
                    if (stack.getCount() > lv.getMaxItemCount()) {
                        lv.setStack(stack.split(lv.getMaxItemCount()));
                    } else {
                        lv.setStack(stack.split(stack.getCount()));
                    }
                    lv.markDirty();
                    bl2 = true;
                    break;
                }
                if (fromLast) {
                    --k;
                    continue;
                }
                ++k;
            }
        }
        return bl2;
    }

    public static int unpackQuickCraftButton(int quickCraftData) {
        return quickCraftData >> 2 & 3;
    }

    public static int unpackQuickCraftStage(int quickCraftData) {
        return quickCraftData & 3;
    }

    public static int packQuickCraftData(int quickCraftStage, int buttonId) {
        return quickCraftStage & 3 | (buttonId & 3) << 2;
    }

    public static boolean shouldQuickCraftContinue(int stage, PlayerEntity player) {
        if (stage == 0) {
            return true;
        }
        if (stage == 1) {
            return true;
        }
        return stage == 2 && player.getAbilities().creativeMode;
    }

    protected void endQuickCraft() {
        this.quickCraftStage = 0;
        this.quickCraftSlots.clear();
    }

    public static boolean canInsertItemIntoSlot(@Nullable Slot slot, ItemStack stack, boolean allowOverflow) {
        boolean bl2;
        boolean bl = bl2 = slot == null || !slot.hasStack();
        if (!bl2 && ItemStack.canCombine(stack, slot.getStack())) {
            return slot.getStack().getCount() + (allowOverflow ? 0 : stack.getCount()) <= stack.getMaxCount();
        }
        return bl2;
    }

    public static void calculateStackSize(Set<Slot> slots, int mode, ItemStack stack, int stackSize) {
        switch (mode) {
            case 0: {
                stack.setCount(MathHelper.floor((float)stack.getCount() / (float)slots.size()));
                break;
            }
            case 1: {
                stack.setCount(1);
                break;
            }
            case 2: {
                stack.setCount(stack.getItem().getMaxCount());
            }
        }
        stack.increment(stackSize);
    }

    public boolean canInsertIntoSlot(Slot slot) {
        return true;
    }

    public static int calculateComparatorOutput(@Nullable BlockEntity entity) {
        if (entity instanceof Inventory) {
            return ScreenHandler.calculateComparatorOutput((Inventory)((Object)entity));
        }
        return 0;
    }

    public static int calculateComparatorOutput(@Nullable Inventory inventory) {
        if (inventory == null) {
            return 0;
        }
        int i = 0;
        float f = 0.0f;
        for (int j = 0; j < inventory.size(); ++j) {
            ItemStack lv = inventory.getStack(j);
            if (lv.isEmpty()) continue;
            f += (float)lv.getCount() / (float)Math.min(inventory.getMaxCountPerStack(), lv.getMaxCount());
            ++i;
        }
        return MathHelper.floor((f /= (float)inventory.size()) * 14.0f) + (i > 0 ? 1 : 0);
    }

    public void setCursorStack(ItemStack stack) {
        this.cursorStack = stack;
    }

    public ItemStack getCursorStack() {
        return this.cursorStack;
    }

    public void disableSyncing() {
        this.disableSync = true;
    }

    public void enableSyncing() {
        this.disableSync = false;
    }

    public void copySharedSlots(ScreenHandler handler) {
        Slot lv;
        int i;
        HashBasedTable<Inventory, Integer, Integer> table = HashBasedTable.create();
        for (i = 0; i < handler.slots.size(); ++i) {
            lv = handler.slots.get(i);
            table.put(lv.inventory, lv.getIndex(), i);
        }
        for (i = 0; i < this.slots.size(); ++i) {
            lv = this.slots.get(i);
            Integer integer = (Integer)table.get(lv.inventory, lv.getIndex());
            if (integer == null) continue;
            this.trackedStacks.set(i, handler.trackedStacks.get(integer));
            this.previousTrackedStacks.set(i, handler.previousTrackedStacks.get(integer));
        }
    }

    public OptionalInt getSlotIndex(Inventory inventory, int index) {
        for (int j = 0; j < this.slots.size(); ++j) {
            Slot lv = this.slots.get(j);
            if (lv.inventory != inventory || index != lv.getIndex()) continue;
            return OptionalInt.of(j);
        }
        return OptionalInt.empty();
    }

    public int getRevision() {
        return this.revision;
    }

    public int nextRevision() {
        this.revision = this.revision + 1 & Short.MAX_VALUE;
        return this.revision;
    }
}

