package me.ggthfn.heavycore.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ClientPlayNetworking;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;

public class HeavyCorePacketHandler {
    public static final Identifier ID = new Identifier("heavycore", "vault_heavycore");

    // kirim dari server ke client
    public static void sendToClient(net.minecraft.server.network.ServerPlayerEntity player, HeavyCorePacket packet) {
        net.minecraft.network.PacketByteBuf buf = new net.minecraft.network.PacketByteBuf(io.netty.buffer.Unpooled.buffer());
        packet.write(buf);
        ServerPlayNetworking.send(player, ID, buf);
    }

    // register handler di client
    public static void registerClientReceiver() {
        ClientPlayNetworking.registerGlobalReceiver(ID, (client, handler, buf, responseSender) -> {
            HeavyCorePacket packet = HeavyCorePacket.read(buf);
            client.execute(() -> {
                // set flag HUD → vault ini heavy_core
                me.ggthfn.heavycore.client.HudRenderer.shouldTrigger = packet.getPos();
            });
        });
    }
}
