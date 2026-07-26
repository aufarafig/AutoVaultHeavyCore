package me.ggthfn.heavycore;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import me.ggthfn.heavycore.network.HeavyCorePacket;

public class TukangKunci implements ModInitializer {
    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playS2C().register(HeavyCorePacket.ID, HeavyCorePacket.CODEC);
        System.out.println("Tukangkunci mod initialized!");
    }
}
