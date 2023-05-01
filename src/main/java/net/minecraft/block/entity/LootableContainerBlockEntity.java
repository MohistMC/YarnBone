/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.block.entity;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

public abstract class LootableContainerBlockEntity
extends LockableContainerBlockEntity {
    public static final String LOOT_TABLE_KEY = "LootTable";
    public static final String LOOT_TABLE_SEED_KEY = "LootTableSeed";
    @Nullable
    protected Identifier lootTableId;
    protected long lootTableSeed;

    protected LootableContainerBlockEntity(BlockEntityType<?> arg, BlockPos arg2, BlockState arg3) {
        super(arg, arg2, arg3);
    }

    public static void setLootTable(BlockView world, Random random, BlockPos pos, Identifier id) {
        BlockEntity lv = world.getBlockEntity(pos);
        if (lv instanceof LootableContainerBlockEntity) {
            ((LootableContainerBlockEntity)lv).setLootTable(id, random.nextLong());
        }
    }

    protected boolean deserializeLootTable(NbtCompound nbt) {
        if (nbt.contains(LOOT_TABLE_KEY, NbtElement.STRING_TYPE)) {
            this.lootTableId = new Identifier(nbt.getString(LOOT_TABLE_KEY));
            this.lootTableSeed = nbt.getLong(LOOT_TABLE_SEED_KEY);
            return true;
        }
        return false;
    }

    protected boolean serializeLootTable(NbtCompound nbt) {
        if (this.lootTableId == null) {
            return false;
        }
        nbt.putString(LOOT_TABLE_KEY, this.lootTableId.toString());
        if (this.lootTableSeed != 0L) {
            nbt.putLong(LOOT_TABLE_SEED_KEY, this.lootTableSeed);
        }
        return true;
    }

    public void checkLootInteraction(@Nullable PlayerEntity player) {
        if (this.lootTableId != null && this.world.getServer() != null) {
            LootTable lv = this.world.getServer().getLootManager().getTable(this.lootTableId);
            if (player instanceof ServerPlayerEntity) {
                Criteria.PLAYER_GENERATES_CONTAINER_LOOT.trigger((ServerPlayerEntity)player, this.lootTableId);
            }
            this.lootTableId = null;
            LootContext.Builder lv2 = new LootContext.Builder((ServerWorld)this.world).parameter(LootContextParameters.ORIGIN, Vec3d.ofCenter(this.pos)).random(this.lootTableSeed);
            if (player != null) {
                lv2.luck(player.getLuck()).parameter(LootContextParameters.THIS_ENTITY, player);
            }
            lv.supplyInventory(this, lv2.build(LootContextTypes.CHEST));
        }
    }

    public void setLootTable(Identifier id, long seed) {
        this.lootTableId = id;
        this.lootTableSeed = seed;
    }

    @Override
    public boolean isEmpty() {
        this.checkLootInteraction(null);
        return this.getInvStackList().stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getStack(int slot) {
        this.checkLootInteraction(null);
        return this.getInvStackList().get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        this.checkLootInteraction(null);
        ItemStack lv = Inventories.splitStack(this.getInvStackList(), slot, amount);
        if (!lv.isEmpty()) {
            this.markDirty();
        }
        return lv;
    }

    @Override
    public ItemStack removeStack(int slot) {
        this.checkLootInteraction(null);
        return Inventories.removeStack(this.getInvStackList(), slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        this.checkLootInteraction(null);
        this.getInvStackList().set(slot, stack);
        if (stack.getCount() > this.getMaxCountPerStack()) {
            stack.setCount(this.getMaxCountPerStack());
        }
        this.markDirty();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return Inventory.canPlayerUse(this, player);
    }

    @Override
    public void clear() {
        this.getInvStackList().clear();
    }

    protected abstract DefaultedList<ItemStack> getInvStackList();

    protected abstract void setInvStackList(DefaultedList<ItemStack> var1);

    @Override
    public boolean checkUnlocked(PlayerEntity player) {
        return super.checkUnlocked(player) && (this.lootTableId == null || !player.isSpectator());
    }

    @Override
    @Nullable
    public ScreenHandler createMenu(int i, PlayerInventory arg, PlayerEntity arg2) {
        if (this.checkUnlocked(arg2)) {
            this.checkLootInteraction(arg.player);
            return this.createScreenHandler(i, arg);
        }
        return null;
    }
}

