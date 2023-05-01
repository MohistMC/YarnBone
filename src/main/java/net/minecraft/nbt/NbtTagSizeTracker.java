/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.nbt;

import com.google.common.annotations.VisibleForTesting;

public class NbtTagSizeTracker {
    public static final NbtTagSizeTracker EMPTY = new NbtTagSizeTracker(0L){

        @Override
        public void add(long bytes) {
        }
    };
    private final long maxBytes;
    private long allocatedBytes;

    public NbtTagSizeTracker(long maxBytes) {
        this.maxBytes = maxBytes;
    }

    public void add(long bytes) {
        this.allocatedBytes += bytes;
        if (this.allocatedBytes > this.maxBytes) {
            throw new RuntimeException("Tried to read NBT tag that was too big; tried to allocate: " + this.allocatedBytes + "bytes where max allowed: " + this.maxBytes);
        }
    }

    @VisibleForTesting
    public long getAllocatedBytes() {
        return this.allocatedBytes;
    }
}

