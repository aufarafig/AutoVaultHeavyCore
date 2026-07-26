package me.ggthfn.heavycore.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class HeavyCoreClientPacketHandler {
    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(HeavyCorePacket.ID, (packet, context) -> {
            // HudRenderer mendeteksi heavy_core langsung via HUD render
        });
    }
}
