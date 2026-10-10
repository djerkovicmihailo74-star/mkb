package com.example.multikeybinds;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class ExtraKeysScreen extends Screen {
    private final Screen parent;
    private int page = 0;
    private KeyBinding listeningFor = null;
    private boolean armed = false;

    public ExtraKeysScreen(Screen parent) {
        super(Text.literal("Multi Keybinds"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        KeyBinding[] all = client.options.allKeys;
        int rowH = 24;
        int perPage = Math.max(1, (height - 90) / rowH);
        int pages = Math.max(1, (all.length + perPage - 1) / perPage);
        if (page >= pages) page = pages - 1;

        addDrawableChild(new TextWidget(width / 2 - 100, 12, 200, 12, title, textRenderer).alignCenter());
        addDrawableChild(new TextWidget(width / 2 - 150, 26, 300, 12,
                Text.literal("Click + then press a key or mouse button. Click a key to remove it.")
                        .formatted(Formatting.GRAY), textRenderer).alignCenter());

        int left = width / 2 - 200;
        int y = 46;
        for (int i = page * perPage; i < Math.min(all.length, (page + 1) * perPage); i++) {
            KeyBinding kb = all[i];
            String id = kb.getTranslationKey();

            addDrawableChild(new TextWidget(left, y + 6, 150, 12,
                    Text.translatable(id), textRenderer));

            boolean listening = kb == listeningFor;
            Text plus = listening
                    ? Text.literal("...").formatted(Formatting.YELLOW)
                    : Text.literal("+").formatted(Formatting.GREEN, Formatting.BOLD);
            ButtonWidget plusBtn = ButtonWidget.builder(plus, b -> {
                listeningFor = kb;
                armed = false;
                clearAndInit();
            }).dimensions(left + 155, y, 22, 20).build();
            plusBtn.active = ExtraBinds.get(id).size() < ExtraBinds.MAX_PER_ACTION;
            addDrawableChild(plusBtn);

            int x = left + 182;
            for (Bind bind : List.copyOf(ExtraBinds.get(id))) {
                addDrawableChild(ButtonWidget.builder(
                        Text.literal(bind.displayName() + " x"),
                        b -> { ExtraBinds.remove(id, bind); clearAndInit(); })
                        .dimensions(x, y, 68, 20).build());
                x += 72;
            }
            y += rowH;
        }

        int by = height - 28;
        ButtonWidget prev = ButtonWidget.builder(Text.literal("< Prev"), b -> { page--; clearAndInit(); })
                .dimensions(width / 2 - 154, by, 70, 20).build();
        prev.active = page > 0;
        addDrawableChild(prev);
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
                .dimensions(width / 2 - 50, by, 100, 20).build());
        ButtonWidget next = ButtonWidget.builder(Text.literal("Next >"), b -> { page++; clearAndInit(); })
                .dimensions(width / 2 + 84, by, 70, 20).build();
        next.active = page < pages - 1;
        addDrawableChild(next);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return listeningFor == null;
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }

    private boolean anythingDown(long w) {
        for (int c = 32; c <= 348; c++) if (GLFW.glfwGetKey(w, c) == GLFW.GLFW_PRESS) return true;
        for (int b = 0; b <= 7; b++) if (GLFW.glfwGetMouseButton(w, b) == GLFW.GLFW_PRESS) return true;
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (listeningFor == null) return;
        long w = client.getWindow().getHandle();

        if (!armed) { // wait until the click on "+" is released
            if (!anythingDown(w)) armed = true;
            return;
        }
        if (GLFW.glfwGetKey(w, GLFW.GLFW_KEY_ESCAPE) == GLFW.GLFW_PRESS) {
            listeningFor = null;
            clearAndInit();
            return;
        }
        for (int c = 32; c <= 348; c++) {
            if (GLFW.glfwGetKey(w, c) == GLFW.GLFW_PRESS) { finish(new Bind("key", c)); return; }
        }
        for (int b = 0; b <= 7; b++) {
            if (GLFW.glfwGetMouseButton(w, b) == GLFW.GLFW_PRESS) { finish(new Bind("mouse", b)); return; }
        }
    }

    private void finish(Bind bind) {
        ExtraBinds.add(listeningFor.getTranslationKey(), bind);
        listeningFor = null;
        clearAndInit();
    }
}
