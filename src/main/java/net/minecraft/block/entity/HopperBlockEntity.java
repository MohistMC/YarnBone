/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.block.entity;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.InventoryProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.Hopper;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.screen.HopperScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class HopperBlockEntity
extends LootableContainerBlockEntity
implements Hopper {
    public static final int TRANSFER_COOLDOWN = 8;
    public static final int INVENTORY_SIZE = 5;
    private DefaultedList<ItemStack> inventory = DefaultedList.ofSize(5, ItemStack.EMPTY);
    private int transferCooldown = -1;
    private long lastTickTime;

    public HopperBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityType.HOPPER, pos, state);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        if (!this.deserializeLootTable(nbt)) {
            Inventories.readNbt(nbt, this.inventory);
        }
        this.transferCooldown = nbt.getInt("TransferCooldown");
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        if (!this.serializeLootTable(nbt)) {
            Inventories.writeNbt(nbt, this.inventory);
        }
        nbt.putInt("TransferCooldown", this.transferCooldown);
    }

    @Override
    public int size() {
        return this.inventory.size();
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        this.checkLootInteraction(null);
        return Inventories.splitStack(this.getInvStackList(), slot, amount);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        this.checkLootInteraction(null);
        this.getInvStackList().set(slot, stack);
        if (stack.getCount() > this.getMaxCountPerStack()) {
            stack.setCount(this.getMaxCountPerStack());
        }
    }

    @Override
    protected Text getContainerName() {
        return Text.translatable("container.hopper");
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, HopperBlockEntity blockEntity) {
        --blockEntity.transferCooldown;
        blockEntity.lastTickTime = world.getTime();
        if (!blockEntity.needsCooldown()) {
            blockEntity.setTransferCooldown(0);
            HopperBlockEntity.insertAndExtract(world, pos, state, blockEntity, () -> HopperBlockEntity.extract(world, blockEntity));
        }
    }

    private static boolean insertAndExtract(World world, BlockPos pos, BlockState state, HopperBlockEntity blockEntity, BooleanSupplier booleanSupplier) {
        if (world.isClient) {
            return false;
        }
        if (!blockEntity.needsCooldown() && state.get(HopperBlock.ENABLED).booleanValue()) {
            boolean bl = false;
            if (!blockEntity.isEmpty()) {
                bl = HopperBlockEntity.insert(world, pos, state, blockEntity);
            }
            if (!blockEntity.isFull()) {
                bl |= booleanSupplier.getAsBoolean();
            }
            if (bl) {
                blockEntity.setTransferCooldown(8);
                HopperBlockEntity.markDirty(world, pos, state);
                return true;
            }
        }
        return false;
    }

    private boolean isFull() {
        for (ItemStack lv : this.inventory) {
            if (!lv.isEmpty() && lv.getCount() == lv.getMaxCount()) continue;
            return false;
        }
        return true;
    }

    private static boolean insert(World world, BlockPos pos, BlockState state, Inventory inventory) {
        Inventory lv = HopperBlockEntity.getOutputInventory(world, pos, state);
        if (lv == null) {
            return false;
        }
        Direction lv2 = state.get(HopperBlock.FACING).getOpposite();
        if (HopperBlockEntity.isInventoryFull(lv, lv2)) {
            return false;
        }
        for (int i = 0; i < inventory.size(); ++i) {
            if (inventory.getStack(i).isEmpty()) continue;
            ItemStack lv3 = inventory.getStack(i).copy();
            ItemStack lv4 = HopperBlockEntity.transfer(inventory, lv, inventory.removeStack(i, 1), lv2);
            if (lv4.isEmpty()) {
                lv.markDirty();
                return true;
            }
            inventory.setStack(i, lv3);
        }
        return false;
    }

    private static IntStream getAvailableSlots(Inventory inventory, Direction side) {
        if (inventory instanceof SidedInventory) {
            return IntStream.of(((SidedInventory)inventory).getAvailableSlots(side));
        }
        return IntStream.range(0, inventory.size());
    }

    private static boolean isInventoryFull(Inventory inventory, Direction direction) {
        return HopperBlockEntity.getAvailableSlots(inventory, direction).allMatch(slot -> {
            ItemStack lv = inventory.getStack(slot);
            return lv.getCount() >= lv.getMaxCount();
        });
    }

    private static boolean isInventoryEmpty(Inventory inv, Direction facing) {
        return HopperBlockEntity.getAvailableSlots(inv, facing).allMatch(slot -> inv.getStack(slot).isEmpty());
    }

    public static boolean extract(World world, Hopper hopper) {
        Inventory lv = HopperBlockEntity.getInputInventory(world, hopper);
        if (lv != null) {
            Direction lv2 = Direction.DOWN;
            if (HopperBlockEntity.isInventoryEmpty(lv, lv2)) {
                return false;
            }
            return HopperBlockEntity.getAvailableSlots(lv, lv2).anyMatch(slot -> HopperBlockEntity.extract(hopper, lv, slot, lv2));
        }
        for (ItemEntity lv3 : HopperBlockEntity.getInputItemEntities(world, hopper)) {
            if (!HopperBlockEntity.extract(hopper, lv3)) continue;
            return true;
        }
        return false;
    }

    private static boolean extract(Hopper hopper, Inventory inventory, int slot, Direction side) {
        ItemStack lv = inventory.getStack(slot);
        if (!lv.isEmpty() && HopperBlockEntity.canExtract(hopper, inventory, lv, slot, side)) {
            ItemStack lv2 = lv.copy();
            ItemStack lv3 = HopperBlockEntity.transfer(inventory, hopper, inventory.removeStack(slot, 1), null);
            if (lv3.isEmpty()) {
                inventory.markDirty();
                return true;
            }
            inventory.setStack(slot, lv2);
        }
        return false;
    }

    public static boolean extract(Inventory inventory, ItemEntity itemEntity) {
        boolean bl = false;
        ItemStack lv = itemEntity.getStack().copy();
        ItemStack lv2 = HopperBlockEntity.transfer(null, inventory, lv, null);
        if (lv2.isEmpty()) {
            bl = true;
            itemEntity.discard();
        } else {
            itemEntity.setStack(lv2);
        }
        return bl;
    }

    /*
     * Enabled aggressive block sorting
     * Lifted jumps to return sites
     */
    public static ItemStack transfer(@Nullable Inventory from, Inventory to, ItemStack stack, @Nullable Direction side) {
        if (to instanceof SidedInventory) {
            SidedInventory lv = (SidedInventory)to;
            if (side != null) {
                int[] is = lv.getAvailableSlots(side);
                int i = 0;
                while (i < is.length) {
                    if (stack.isEmpty()) return stack;
                    stack = HopperBlockEntity.transfer(from, to, stack, is[i], side);
                    ++i;
                }
                return stack;
            }
        }
        int j = to.size();
        int i = 0;
        while (i < j) {
            if (stack.isEmpty()) return stack;
            stack = HopperBlockEntity.transfer(from, to, stack, i, side);
            ++i;
        }
        return stack;
    }

    private static boolean canInsert(Inventory inventory, ItemStack stack, int slot, @Nullable Direction side) {
        SidedInventory lv;
        if (!inventory.isValid(slot, stack)) {
            return false;
        }
        return !(inventory instanceof SidedInventory) || (lv = (SidedInventory)inventory).canInsert(slot, stack, side);
    }

    private static boolean canExtract(Inventory hopperInventory, Inventory fromInventory, ItemStack stack, int slot, Direction facing) {
        SidedInventory lv;
        if (!fromInventory.canTransferTo(hopperInventory, slot, stack)) {
            return false;
        }
        return !(fromInventory instanceof SidedInventory) || (lv = (SidedInventory)fromInventory).canExtract(slot, stack, facing);
    }

    private static ItemStack transfer(@Nullable Inventory from, Inventory to, ItemStack stack, int slot, @Nullable Direction side) {
        ItemStack lv = to.getStack(slot);
        if (HopperBlockEntity.canInsert(to, stack, slot, side)) {
            int k;
            boolean bl = false;
            boolean bl2 = to.isEmpty();
            if (lv.isEmpty()) {
                to.setStack(slot, stack);
                stack = ItemStack.EMPTY;
                bl = true;
            } else if (HopperBlockEntity.canMergeItems(lv, stack)) {
                int j = stack.getMaxCount() - lv.getCount();
                k = Math.min(stack.getCount(), j);
                stack.decrement(k);
                lv.increment(k);
                boolean bl3 = bl = k > 0;
            }
            if (bl) {
                HopperBlockEntity lv2;
                if (bl2 && to instanceof HopperBlockEntity && !(lv2 = (HopperBlockEntity)to).isDisabled()) {
                    k = 0;
                    if (from instanceof HopperBlockEntity) {
                        HopperBlockEntity lv3 = (HopperBlockEntity)from;
                        if (lv2.lastTickTime >= lv3.lastTickTime) {
                            k = 1;
                        }
                    }
                    lv2.setTransferCooldown(8 - k);
                }
                to.markDirty();
            }
        }
        return stack;
    }

    @Nullable
    private static Inventory getOutputInventory(World world, BlockPos pos, BlockState state) {
        Direction lv = state.get(HopperBlock.FACING);
        return HopperBlockEntity.getInventoryAt(world, pos.offset(lv));
    }

    @Nullable
    private static Inventory getInputInventory(World world, Hopper hopper) {
        return HopperBlockEntity.getInventoryAt(world, hopper.getHopperX(), hopper.getHopperY() + 1.0, hopper.getHopperZ());
    }

    public static List<ItemEntity> getInputItemEntities(World world, Hopper hopper) {
        return hopper.getInputAreaShape().getBoundingBoxes().stream().flatMap(box -> world.getEntitiesByClass(ItemEntity.class, box.offset(hopper.getHopperX() - 0.5, hopper.getHopperY() - 0.5, hopper.getHopperZ() - 0.5), EntityPredicates.VALID_ENTITY).stream()).collect(Collectors.toList());
    }

    @Nullable
    public static Inventory getInventoryAt(World world, BlockPos pos) {
        return HopperBlockEntity.getInventoryAt(world, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5);
    }

    @Nullable
    private static Inventory getInventoryAt(World world, double x, double y, double z) {
        List<Entity> list;
        BlockEntity lv5;
        Inventory lv = null;
        BlockPos lv2 = BlockPos.ofFloored(x, y, z);
        BlockState lv3 = world.getBlockState(lv2);
        Block lv4 = lv3.getBlock();
        if (lv4 instanceof InventoryProvider) {
            lv = ((InventoryProvider)((Object)lv4)).getInventory(lv3, world, lv2);
        } else if (lv3.hasBlockEntity() && (lv5 = world.getBlockEntity(lv2)) instanceof Inventory && (lv = (Inventory)((Object)lv5)) instanceof ChestBlockEntity && lv4 instanceof ChestBlock) {
            lv = ChestBlock.getInventory((ChestBlock)lv4, lv3, world, lv2, true);
        }
        if (lv == null && !(list = world.getOtherEntities(null, new Box(x - 0.5, y - 0.5, z - 0.5, x + 0.5, y + 0.5, z + 0.5), EntityPredicates.VALID_INVENTORIES)).isEmpty()) {
            lv = (Inventory)((Object)list.get(world.random.nextInt(list.size())));
        }
        return lv;
    }

    private static boolean canMergeItems(ItemStack first, ItemStack second) {
        if (!first.isOf(second.getItem())) {
            return false;
        }
        if (first.getDamage() != second.getDamage()) {
            return false;
        }
        if (first.getCount() > first.getMaxCount()) {
            return false;
        }
        return ItemStack.areNbtEqual(first, second);
    }

    @Override
    public double getHopperX() {
        return (double)this.pos.getX() + 0.5;
    }

    @Override
    public double getHopperY() {
        return (double)this.pos.getY() + 0.5;
    }

    @Override
    public double getHopperZ() {
        return (double)this.pos.getZ() + 0.5;
    }

    private void setTransferCooldown(int transferCooldown) {
        this.transferCooldown = transferCooldown;
    }

    private boolean needsCooldown() {
        return this.transferCooldown > 0;
    }

    private boolean isDisabled() {
        return this.transferCooldown > 8;
    }

    @Override
    protected DefaultedList<ItemStack> getInvStackList() {
        return this.inventory;
    }

    @Override
    protected void setInvStackList(DefaultedList<ItemStack> list) {
        this.inventory = list;
    }

    public static void onEntityCollided(World world, BlockPos pos, BlockState state, Entity entity, HopperBlockEntity blockEntity) {
        if (entity instanceof ItemEntity && VoxelShapes.matchesAnywhere(VoxelShapes.cuboid(entity.getBoundingBox().offset(-pos.getX(), -pos.getY(), -pos.getZ())), blockEntity.getInputAreaShape(), BooleanBiFunction.AND)) {
            HopperBlockEntity.insertAndExtract(world, pos, state, blockEntity, () -> HopperBlockEntity.extract(blockEntity, (ItemEntity)entity));
        }
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return new HopperScreenHandler(syncId, playerInventory, this);
    }
}

