package me.ggthfn.heavycore.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class HeavyCorePacketHandler {
    public static void sendToClient(net.minecraft.server.network.ServerPlayerEntity player, HeavyCorePacket packet) {
        ServerPlayNetworking.send(player, packet);
    }
}
