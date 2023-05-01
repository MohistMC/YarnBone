/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.item;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenCustomHashSet;
import java.util.Set;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

public class ItemStackSet {
    private static final Hash.Strategy<? super ItemStack> HASH_STRATEGY = new Hash.Strategy<ItemStack>(){

        @Override
        public int hashCode(@Nullable ItemStack arg) {
            return ItemStackSet.getHashCode(arg);
        }

        @Override
        public boolean equals(@Nullable ItemStack arg, @Nullable ItemStack arg2) {
            return arg == arg2 || arg != null && arg2 != null && arg.isEmpty() == arg2.isEmpty() && ItemStack.canCombine(arg, arg2);
        }

        @Override
        public /* synthetic */ boolean equals(@Nullable Object first, @Nullable Object second) {
            return this.equals((ItemStack)first, (ItemStack)second);
        }

        @Override
        public /* synthetic */ int hashCode(@Nullable Object stack) {
            return this.hashCode((ItemStack)stack);
        }
    };

    static int getHashCode(@Nullable ItemStack stack) {
        if (stack != null) {
            NbtCompound lv = stack.getNbt();
            int i = 31 + stack.getItem().hashCode();
            return 31 * i + (lv == null ? 0 : lv.hashCode());
        }
        return 0;
    }

    public static Set<ItemStack> create() {
        return new ObjectLinkedOpenCustomHashSet<ItemStack>(HASH_STRATEGY);
    }
}

