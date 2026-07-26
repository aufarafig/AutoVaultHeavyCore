package me.ggthfn.heavycore.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.VaultBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

public class HudRenderer {
    // posisi vault yang harus di-trigger (diset dari HUD)
    public static BlockPos shouldTrigger = null;

    public static void render(DrawContext context) {
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
                        // simpan posisi vault untuk trigger di tick
                        shouldTrigger = bhr.getBlockPos();
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
                }
            }
        }
    }
}