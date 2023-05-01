/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.network;

public enum NetworkSide {
    SERVERBOUND,
    CLIENTBOUND;


    public NetworkSide getOpposite() {
        return this == CLIENTBOUND ? SERVERBOUND : CLIENTBOUND;
    }
}

