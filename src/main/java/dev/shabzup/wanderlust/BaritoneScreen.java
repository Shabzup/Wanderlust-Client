package dev.shabzup.wanderlust;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

final class BaritoneScreen extends Screen {
    private final Screen parent;
    private String result = "";
    BaritoneScreen(Screen parent) { super(Component.literal("Baritone")); this.parent = parent; }

    @Override protected void init() {
        int x = width / 2 - 140, y = height / 2 - 35;
        var command = addRenderableWidget(new EditBox(font, x, y, 280, 20, Component.literal("Baritone command")));
        command.setMaxLength(240);
        command.setHint(Component.literal("goto 100 64 100, mine diamond_ore, follow player"));
        addRenderableWidget(Button.builder(Component.literal("Run"), b -> {
            String text = command.getValue().trim();
            if (text.startsWith("#")) text = text.substring(1);
            result = BaritoneBridge.userCommand(text) ? "Command submitted" : "Command failed. Check chat / Baritone installation.";
        }).bounds(x, y + 25, 280, 20).build());
        String[] names = {"pause", "resume", "cancel"};
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            addRenderableWidget(Button.builder(Component.literal(name), b -> BaritoneBridge.userCommand(name))
                .bounds(x + i * 94, y + 50, 92, 20).build());
        }
        addRenderableWidget(Button.builder(Component.literal("Back"), b -> onClose()).bounds(x, y + 100, 280, 20).build());
    }

    @Override public void extractRenderState(GuiGraphicsExtractor g, int x, int y, float delta) {
        g.fill(0, 0, width, height, 0xF0192422);
        g.centeredText(font, "Baritone", width / 2, height / 2 - 70, 0xFF85E7B6);
        g.centeredText(font, BaritoneBridge.status(), width / 2, height / 2 - 55, 0xFFFFFFFF);
        g.centeredText(font, result, width / 2, height / 2 + 45, 0xFFFFFFFF);
        super.extractRenderState(g, x, y, delta);
    }
    @Override public boolean isPauseScreen() { return false; }
    @Override public void onClose() { minecraft.gui.setScreen(parent); }
}
