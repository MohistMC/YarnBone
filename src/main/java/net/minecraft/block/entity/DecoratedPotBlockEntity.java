/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.block.entity;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.Util;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class DecoratedPotBlockEntity
extends BlockEntity {
    private static final String SHARDS_NBT_KEY = "shards";
    private static final int field_42783 = 4;
    private boolean dropNothing = false;
    private final List<Item> shards = Util.make(new ArrayList(4), arrayList -> {
        arrayList.add(Items.BRICK);
        arrayList.add(Items.BRICK);
        arrayList.add(Items.BRICK);
        arrayList.add(Items.BRICK);
    });

    public DecoratedPotBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityType.DECORATED_POT, pos, state);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        DecoratedPotBlockEntity.writeShardsToNbt(this.shards, nbt);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains(SHARDS_NBT_KEY, NbtElement.LIST_TYPE)) {
            int j;
            NbtList lv = nbt.getList(SHARDS_NBT_KEY, NbtElement.STRING_TYPE);
            this.shards.clear();
            int i = Math.min(4, lv.size());
            for (j = 0; j < i; ++j) {
                NbtElement nbtElement = lv.get(j);
                if (nbtElement instanceof NbtString) {
                    NbtString lv2 = (NbtString)nbtElement;
                    this.shards.add(Registries.ITEM.get(new Identifier(lv2.asString())));
                    continue;
                }
                this.shards.add(Items.BRICK);
            }
            j = 4 - i;
            for (int k = 0; k < j; ++k) {
                this.shards.add(Items.BRICK);
            }
        }
    }

    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return this.createNbt();
    }

    public static void writeShardsToNbt(List<Item> shards, NbtCompound nbt) {
        NbtList lv = new NbtList();
        for (Item lv2 : shards) {
            lv.add(NbtString.of(Registries.ITEM.getId(lv2).toString()));
        }
        nbt.put(SHARDS_NBT_KEY, lv);
    }

    public ItemStack asStack() {
        ItemStack lv = new ItemStack(Blocks.DECORATED_POT);
        NbtCompound lv2 = new NbtCompound();
        DecoratedPotBlockEntity.writeShardsToNbt(this.shards, lv2);
        BlockItem.setBlockEntityNbt(lv, BlockEntityType.DECORATED_POT, lv2);
        return lv;
    }

    public List<Item> getShards() {
        return this.shards;
    }

    public void onBreak(World world, BlockPos pos, ItemStack tool, PlayerEntity player) {
        if (player.isCreative()) {
            this.dropNothing = true;
            return;
        }
        if (tool.isIn(ItemTags.BREAKS_DECORATED_POTS) && !EnchantmentHelper.hasSilkTouch(tool)) {
            List<Item> list = this.getShards();
            DefaultedList<ItemStack> lv = DefaultedList.ofSize(list.size());
            lv.addAll(0, list.stream().map(Item::getDefaultStack).toList());
            ItemScatterer.spawn(world, pos, lv);
            this.dropNothing = true;
            world.playSound(null, pos, SoundEvents.BLOCK_DECORATED_POT_SHATTER, SoundCategory.PLAYERS, 1.0f, 1.0f);
        }
    }

    public boolean shouldDropNothing() {
        return this.dropNothing;
    }

    public Direction getHorizontalFacing() {
        return this.getCachedState().get(Properties.HORIZONTAL_FACING);
    }

    public void readNbtFromStack(ItemStack stack) {
        NbtCompound lv = BlockItem.getBlockEntityNbt(stack);
        if (lv != null) {
            this.readNbt(lv);
        }
    }

    public /* synthetic */ Packet toUpdatePacket() {
        return this.toUpdatePacket();
    }
}

