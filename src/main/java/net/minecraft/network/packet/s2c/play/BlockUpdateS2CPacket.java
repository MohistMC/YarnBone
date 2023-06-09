/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package net.minecraft.network.packet.s2c.play;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class BlockUpdateS2CPacket
implements Packet<ClientPlayPacketListener> {
    private final BlockPos pos;
    private final BlockState state;

    public BlockUpdateS2CPacket(BlockPos pos, BlockState state) {
        this.pos = pos;
        this.state = state;
    }

    public BlockUpdateS2CPacket(BlockView world, BlockPos pos) {
        this(pos, world.getBlockState(pos));
    }

    public BlockUpdateS2CPacket(PacketByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.state = buf.readRegistryValue(Block.STATE_IDS);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeRegistryValue(Block.STATE_IDS, this.state);
    }

    @Override
    public void apply(ClientPlayPacketListener arg) {
        arg.onBlockUpdate(this);
    }

    public BlockState getState() {
        return this.state;
    }

    public BlockPos getPos() {
        return this.pos;
    }
}

