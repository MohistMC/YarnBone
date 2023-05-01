/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.block.entity;

import com.mojang.logging.LogUtils;
import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChiseledBookshelfBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;

public class ChiseledBookshelfBlockEntity
extends BlockEntity
implements Inventory {
    public static final int MAX_BOOKS = 6;
    private static final Logger LOGGER = LogUtils.getLogger();
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(6, ItemStack.EMPTY);
    private int lastInteractedSlot = -1;

    public ChiseledBookshelfBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityType.CHISELED_BOOKSHELF, pos, state);
    }

    private void updateState(int interactedSlot) {
        if (interactedSlot < 0 || interactedSlot >= 6) {
            LOGGER.error("Expected slot 0-5, got {}", (Object)interactedSlot);
            return;
        }
        this.lastInteractedSlot = interactedSlot;
        BlockState lv = this.getCachedState();
        for (int j = 0; j < ChiseledBookshelfBlock.SLOT_OCCUPIED_PROPERTIES.size(); ++j) {
            boolean bl = !this.getStack(j).isEmpty();
            BooleanProperty lv2 = ChiseledBookshelfBlock.SLOT_OCCUPIED_PROPERTIES.get(j);
            lv = (BlockState)lv.with(lv2, bl);
        }
        Objects.requireNonNull(this.world).setBlockState(this.pos, lv, Block.NOTIFY_ALL);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        this.inventory.clear();
        Inventories.readNbt(nbt, this.inventory);
        this.lastInteractedSlot = nbt.getInt("last_interacted_slot");
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        Inventories.writeNbt(nbt, this.inventory, true);
        nbt.putInt("last_interacted_slot", this.lastInteractedSlot);
    }

    public int getOpenSlotCount() {
        return (int)this.inventory.stream().filter(Predicate.not(ItemStack::isEmpty)).count();
    }

    @Override
    public void clear() {
        this.inventory.clear();
    }

    @Override
    public int size() {
        return 6;
    }

    @Override
    public boolean isEmpty() {
        return this.inventory.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getStack(int slot) {
        return this.inventory.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack lv = Objects.requireNonNullElse(this.inventory.get(slot), ItemStack.EMPTY);
        this.inventory.set(slot, ItemStack.EMPTY);
        if (!lv.isEmpty()) {
            this.updateState(slot);
        }
        return lv;
    }

    @Override
    public ItemStack removeStack(int slot) {
        return this.removeStack(slot, 1);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (stack.isIn(ItemTags.BOOKSHELF_BOOKS)) {
            this.inventory.set(slot, stack);
            this.updateState(slot);
        }
    }

    @Override
    public int getMaxCountPerStack() {
        return 1;
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return Inventory.canPlayerUse(this, player);
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return stack.isIn(ItemTags.BOOKSHELF_BOOKS) && this.getStack(slot).isEmpty();
    }

    public int getLastInteractedSlot() {
        return this.lastInteractedSlot;
    }
}

