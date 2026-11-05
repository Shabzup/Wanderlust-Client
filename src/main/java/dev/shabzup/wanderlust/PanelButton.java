package dev.shabzup.wanderlust;

import java.util.function.BooleanSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/** Small, flat controls for the floating client panels. */
final class PanelButton extends Button {
    enum Kind { Navigation, Module, Small }
    private final Kind kind;
    private final BooleanSupplier selected;
    private float hover;
    private long lastFrame = System.nanoTime();

    PanelButton(int x, int y, int w, int h, String label, Kind kind, BooleanSupplier selected, Feature feature, Runnable action) {
        super(x, y, w, h, Component.literal(label), b -> action.run(), DEFAULT_NARRATION);
        this.kind = kind;
        this.selected = selected;
    }

    @Override protected void extractContents(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        long now = System.nanoTime();
        float seconds = Math.min(.05f, (now - lastFrame) / 1_000_000_000f);
        lastFrame = now;
        hover = PanelStyle.approach(hover, isHoveredOrFocused() && active ? 1 : 0, seconds, 15);
        float opacity = getAlpha() * (active ? 1 : .4f);
        if (opacity < .015f) return;
        int x = getX(), y = getY(), w = getWidth(), h = getHeight();
        boolean chosen = selected.getAsBoolean();
        PanelStyle.control(g,x,y,w,h,hover>.1f,chosen,opacity);
        var message = PanelStyle.text(getMessage().getString());
        int tx = x + (w - Minecraft.getInstance().font.width(message)) / 2;
        g.text(Minecraft.getInstance().font, message, tx, y + (h - 9) / 2,
            PanelStyle.alpha(PanelStyle.TEXT, opacity), true);
    }
}
