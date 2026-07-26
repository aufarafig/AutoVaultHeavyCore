package me.ggthfn.heavycore.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;

public class HeavyCorePacket {
    private final BlockPos pos;

    public HeavyCorePacket(BlockPos pos) {
        this.pos = pos;
    }

    public void write(PacketByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    public static HeavyCorePacket read(PacketByteBuf buf) {
        return new HeavyCorePacket(buf.readBlockPos());
    }

    public BlockPos getPos() {
        return pos;
    }
}
