package me.ggthfn.heavycore.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketDecoder;
import net.minecraft.network.codec.ValueFirstEncoder;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record HeavyCorePacket(BlockPos pos) implements CustomPayload {
    public static final CustomPayload.Id<HeavyCorePacket> ID = new CustomPayload.Id<>(net.minecraft.util.Identifier.of("heavycore", "vault_heavycore"));

    public static final PacketCodec<PacketByteBuf, HeavyCorePacket> CODEC = CustomPayload.codecOf(
        (ValueFirstEncoder<PacketByteBuf, HeavyCorePacket>) (payload, buf) -> buf.writeBlockPos(payload.pos),
        (PacketDecoder<PacketByteBuf, HeavyCorePacket>) buf -> new HeavyCorePacket(buf.readBlockPos())
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
