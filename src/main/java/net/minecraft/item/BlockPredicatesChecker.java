/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.item;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Objects;
import net.minecraft.block.Block;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.command.argument.BlockPredicateArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registry;
import org.jetbrains.annotations.Nullable;

public class BlockPredicatesChecker {
    private final String key;
    @Nullable
    private CachedBlockPosition cachedPos;
    private boolean lastResult;
    private boolean nbtAware;

    public BlockPredicatesChecker(String key) {
        this.key = key;
    }

    private static boolean canUseCache(CachedBlockPosition pos, @Nullable CachedBlockPosition cachedPos, boolean nbtAware) {
        if (cachedPos == null || pos.getBlockState() != cachedPos.getBlockState()) {
            return false;
        }
        if (!nbtAware) {
            return true;
        }
        if (pos.getBlockEntity() == null && cachedPos.getBlockEntity() == null) {
            return true;
        }
        if (pos.getBlockEntity() == null || cachedPos.getBlockEntity() == null) {
            return false;
        }
        return Objects.equals(pos.getBlockEntity().createNbtWithId(), cachedPos.getBlockEntity().createNbtWithId());
    }

    public boolean check(ItemStack stack, Registry<Block> blockRegistry, CachedBlockPosition pos) {
        if (BlockPredicatesChecker.canUseCache(pos, this.cachedPos, this.nbtAware)) {
            return this.lastResult;
        }
        this.cachedPos = pos;
        this.nbtAware = false;
        NbtCompound lv = stack.getNbt();
        if (lv != null && lv.contains(this.key, NbtElement.LIST_TYPE)) {
            NbtList lv2 = lv.getList(this.key, NbtElement.STRING_TYPE);
            for (int i = 0; i < lv2.size(); ++i) {
                String string = lv2.getString(i);
                try {
                    BlockPredicateArgumentType.BlockPredicate lv3 = BlockPredicateArgumentType.parse(blockRegistry.getReadOnlyWrapper(), new StringReader(string));
                    this.nbtAware |= lv3.hasNbt();
                    if (lv3.test(pos)) {
                        this.lastResult = true;
                        return true;
                    }
                    continue;
                }
                catch (CommandSyntaxException commandSyntaxException) {
                    // empty catch block
                }
            }
        }
        this.lastResult = false;
        return false;
    }
}

