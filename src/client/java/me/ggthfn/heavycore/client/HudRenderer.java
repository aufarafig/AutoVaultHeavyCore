package me.ggthfn.heavycore.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.VaultBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class HudRenderer {
    public static void render(DrawContext context) {
        if (!HeavyCoreConfig.get().enabled) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;

        HitResult target = client.crosshairTarget;
        if (target instanceof BlockHitResult bhr) {
            BlockEntity be = client.world.getBlockEntity(bhr.getBlockPos());
            if (be instanceof VaultBlockEntity vault) {
                ItemStack display = vault.getSharedData().getDisplayItem();
                if (!display.isEmpty()) {
                    String itemName = display.getName().getString();

                    int color = 0xFFFFFF;
                    if (display.getItem() == Items.HEAVY_CORE && display.getCount() > 0) {
                        color = 0xFF0000;
                    } else if (display.getItem() == Items.ENCHANTED_BOOK) {
                        color = 0xAA00FF;
                    }

                    String text = "Vault Display: " + itemName;
                    int screenWidth = context.getScaledWindowWidth();
                    int screenHeight = context.getScaledWindowHeight();
                    int textWidth = client.textRenderer.getWidth(text);
                    int x = (screenWidth - textWidth) / 2;
                    int y = (screenHeight / 2) + 20;
                    context.drawText(client.textRenderer, text, x, y, color, true);

                    // Debug: cooldown
                    int cd = VaultAutoOpener.getCooldown();

                    String cdText = "cooldown: " + cd + " frames";
                    int cdColor = 0xAAAAAA; // gray
                    int cw = client.textRenderer.getWidth(cdText);
                    context.drawText(client.textRenderer, cdText, (screenWidth - cw) / 2, y + 12, cdColor, true);

                    // Warning: vault already triggered this session
                    if (VaultAutoOpener.isAlreadyTriggered(bhr.getBlockPos())) {
                        String warn = "ALREADY TRIGGERED - use new vault";
                        int warnColor = 0xFF5555; // red
                        int ww = client.textRenderer.getWidth(warn);
                        context.drawText(client.textRenderer, warn, (screenWidth - ww) / 2, y + 24, warnColor, true);
                    }
                }
            }
        }
    }
}
