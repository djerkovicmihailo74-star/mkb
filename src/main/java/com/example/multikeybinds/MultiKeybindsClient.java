package com.example.multikeybinds;

import com.example.multikeybinds.mixin.KeyBindingAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.option.KeybindsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiKeybindsClient implements ClientModInitializer {
    private final Map<String, KeyBinding> byName = new HashMap<>();
    private final Map<String, Boolean> wasDown = new HashMap<>();

    @Override
    public void onInitializeClient() {
        ExtraBinds.load();

        // Add a "Multi Keys" button to the vanilla Controls (Key Binds) screen
        ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
            if (screen instanceof KeybindsScreen) {
                ButtonWidget btn = ButtonWidget.builder(
                        Text.literal("+ Multi Keys").formatted(Formatting.GREEN),
                        b -> client.setScreen(new ExtraKeysScreen(screen)))
                        .dimensions(6, 6, 90, 20).build();
                Screens.getButtons(screen).add(btn);
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
    }

    private KeyBinding find(MinecraftClient client, String key) {
        if (byName.isEmpty()) {
            for (KeyBinding kb : client.options.allKeys) byName.put(kb.getTranslationKey(), kb);
        }
        return byName.get(key);
    }

    private boolean primaryDown(KeyBinding kb, long window) {
        InputUtil.Key k = KeyBindingHelper.getBoundKeyOf(kb);
        if (k.equals(InputUtil.UNKNOWN_KEY)) return false;
        if (k.getCategory() == InputUtil.Type.MOUSE)
            return GLFW.glfwGetMouseButton(window, k.getCode()) == GLFW.GLFW_PRESS;
        return GLFW.glfwGetKey(window, k.getCode()) == GLFW.GLFW_PRESS;
    }

    private void tick(MinecraftClient client) {
        if (client.options == null || ExtraBinds.all().isEmpty()) return;
        long window = client.getWindow().getHandle();
        boolean inGame = client.currentScreen == null;

        for (Map.Entry<String, List<Bind>> e : ExtraBinds.all().entrySet()) {
            KeyBinding kb = find(client, e.getKey());
            if (kb == null) continue;

            boolean down = false;
            if (inGame) {
                for (Bind b : e.getValue()) {
                    if (b.isDown(window)) { down = true; break; }
                }
            }
            boolean was = wasDown.getOrDefault(e.getKey(), false);

            if (down && !was) {
                KeyBindingAccessor acc = (KeyBindingAccessor) kb;
                acc.mkb$setTimesPressed(acc.mkb$getTimesPressed() + 1); // tap
            }
            if (down) {
                kb.setPressed(true); // hold
            } else if (was) {
                kb.setPressed(primaryDown(kb, window));
            }
            wasDown.put(e.getKey(), down);
        }
    }
}
