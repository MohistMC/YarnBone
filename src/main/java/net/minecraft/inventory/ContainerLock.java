/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Unmodifiable
 */
package net.minecraft.inventory;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import org.jetbrains.annotations.Unmodifiable;

@Unmodifiable
public class ContainerLock {
    public static final ContainerLock EMPTY = new ContainerLock("");
    public static final String LOCK_KEY = "Lock";
    private final String key;

    public ContainerLock(String key) {
        this.key = key;
    }

    public boolean canOpen(ItemStack stack) {
        return this.key.isEmpty() || !stack.isEmpty() && stack.hasCustomName() && this.key.equals(stack.getName().getString());
    }

    public void writeNbt(NbtCompound nbt) {
        if (!this.key.isEmpty()) {
            nbt.putString(LOCK_KEY, this.key);
        }
    }

    public static ContainerLock fromNbt(NbtCompound nbt) {
        if (nbt.contains(LOCK_KEY, NbtElement.STRING_TYPE)) {
            return new ContainerLock(nbt.getString(LOCK_KEY));
        }
        return EMPTY;
    }
}

