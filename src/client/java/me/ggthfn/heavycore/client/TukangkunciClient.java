package me.ggthfn.heavycore.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import me.ggthfn.heavycore.network.HeavyCoreClientPacketHandler;

public class TukangkunciClient implements ClientModInitializer {
    private static int interactCooldown = 0;

    @Override
    public void onInitializeClient() {
        HeavyCoreClientPacketHandler.register();

        HudRenderCallback.EVENT.register((context, tickDelta) -> {
            HudRenderer.render(context);
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (interactCooldown > 0) {
                interactCooldown--;
            }

            if (HudRenderer.shouldTrigger != null && interactCooldown == 0) {
                BlockPos pos = HudRenderer.shouldTrigger;
                HudRenderer.shouldTrigger = null;

                if (client.interactionManager != null && client.player != null && client.crosshairTarget instanceof BlockHitResult bhr) {
                    if (bhr.getBlockPos().equals(pos)) {
                        client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, bhr);
                        interactCooldown = 10;
                    }
                }
            }
        });
    }
}
