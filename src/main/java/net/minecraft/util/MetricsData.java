/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.util;

public class MetricsData {
    public static final int SIZE = 240;
    private final long[] samples = new long[240];
    private int startIndex;
    private int sampleCount;
    private int writeIndex;

    public void pushSample(long time) {
        this.samples[this.writeIndex] = time;
        ++this.writeIndex;
        if (this.writeIndex == 240) {
            this.writeIndex = 0;
        }
        if (this.sampleCount < 240) {
            this.startIndex = 0;
            ++this.sampleCount;
        } else {
            this.startIndex = this.wrapIndex(this.writeIndex + 1);
        }
    }

    public long average(int offset) {
        int j = (this.startIndex + offset) % 240;
        long l = 0L;
        for (int k = this.startIndex; k != j; ++k) {
            l += this.samples[k];
        }
        return l / (long)offset;
    }

    public int method_34913(int offset, int j) {
        return this.scaleSample(this.average(offset), j, 60);
    }

    public int scaleSample(long sample, int destScale, int srcScale) {
        double d = (double)sample / (double)(1000000000L / (long)srcScale);
        return (int)(d * (double)destScale);
    }

    public int getStartIndex() {
        return this.startIndex;
    }

    public int getCurrentIndex() {
        return this.writeIndex;
    }

    public int wrapIndex(int index) {
        return index % 240;
    }

    public long[] getSamples() {
        return this.samples;
    }
}

