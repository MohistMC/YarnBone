/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft;

public class SaveVersion {
    private final int id;
    private final String series;
    public static String MAIN_SERIES = "main";

    public SaveVersion(int id) {
        this(id, MAIN_SERIES);
    }

    public SaveVersion(int id, String series) {
        this.id = id;
        this.series = series;
    }

    public boolean isNotMainSeries() {
        return !this.series.equals(MAIN_SERIES);
    }

    public String getSeries() {
        return this.series;
    }

    public int getId() {
        return this.id;
    }

    public boolean isAvailableTo(SaveVersion other) {
        return this.getSeries().equals(other.getSeries());
    }
}

