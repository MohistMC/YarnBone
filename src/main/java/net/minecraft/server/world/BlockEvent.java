/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.server.world;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;

public record BlockEvent(BlockPos pos, Block block, int type, int data) {
}

