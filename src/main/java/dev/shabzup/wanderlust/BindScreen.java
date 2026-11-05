package dev.shabzup.wanderlust;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

final class BindScreen extends Screen {
    private final Screen parent;
    private final Feature feature;

    BindScreen(Screen parent, Feature feature) {
        super(Component.literal("Bind " + feature.label));
        this.parent = parent;
        this.feature = feature;
    }

    @Override protected void init() {
        addRenderableWidget(Button.builder(Component.literal("Clear binding"), b -> bind(InputConstants.UNKNOWN))
            .bounds(width / 2 - 75, height / 2 + 12, 150, 20).build());
    }

    private void bind(InputConstants.Key key) {
        Wanderlust.binds.get(feature).setKey(key);
        KeyMapping.resetMapping();
        minecraft.options.save();
        onClose();
    }

    @Override public boolean keyPressed(KeyEvent event) {
        if (event.key() == GLFW.GLFW_KEY_ESCAPE) { onClose(); return true; }
        bind(InputConstants.getKey(event));
        return true;
    }

    @Override public void extractRenderState(GuiGraphicsExtractor g, int x, int y, float delta) {
        g.fill(0, 0, width, height, 0xEF192422);
        g.centeredText(font, "Press a key for " + feature.label, width / 2, height / 2 - 34, 0xFFFFFFFF);
        g.centeredText(font, "Escape cancels. Conflicts can be checked in Minecraft Controls.", width / 2, height / 2 - 18, 0xFFB0C8BD);
        super.extractRenderState(g, x, y, delta);
    }

    @Override public void onClose() { minecraft.gui.setScreen(parent); }
}
