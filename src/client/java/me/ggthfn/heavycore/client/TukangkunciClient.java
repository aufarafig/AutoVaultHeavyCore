package me.ggthfn.heavycore.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import me.ggthfn.heavycore.network.HeavyCoreClientPacketHandler;

public class TukangkunciClient implements ClientModInitializer {
    public static final String MOD_ID = "tukangkunci";

    public static KeyBinding toggleKey;
    public static KeyBinding openGuiKey;

    @Override
    public void onInitializeClient() {
        HeavyCoreClientPacketHandler.register();
        HeavyCoreConfig.load();

        HudRenderCallback.EVENT.register((context, tickDelta) -> {
            HudRenderer.render(context);
        });

        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.tukangkunci.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                KeyBinding.MISC_CATEGORY
        ));

        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.tukangkunci.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                KeyBinding.MISC_CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(VaultAutoOpener::onClientTick);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) {
                HeavyCoreConfig.get().enabled = !HeavyCoreConfig.get().enabled;
                HeavyCoreConfig.save();
                if (client.player != null) {
                    String stateStr = HeavyCoreConfig.get().enabled ? "\u00a7aON\u00a7r" : "\u00a7cOFF\u00a7r";
                    client.player.sendMessage(
                            Text.translatable("tukangkunci.chat.toggle", stateStr),
                            true);
                }
            }
            while (openGuiKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new HeavyCoreConfigScreen(null));
                }
            }
        });
    }
}
