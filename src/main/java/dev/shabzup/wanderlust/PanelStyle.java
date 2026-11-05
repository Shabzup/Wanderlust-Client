package dev.shabzup.wanderlust;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;

final class PanelStyle {
    static final int TEXT = 0xFFF1ECFA, MUTED = 0xFFA7A1B5, BLUE = 0xFF6D9FFF;
    static final int PURPLE = 0xFF813DDB, PURPLE_LIGHT = 0xFFC58AFF;
    private static final FontDescription FONT = new FontDescription.Resource(Identifier.fromNamespaceAndPath("wanderlust", "ui"));

    static Component text(String text) { return Component.literal(text).withStyle(s -> s.withFont(FONT)); }
    static float ease(float t) { return 1 - (float)Math.pow(1 - Math.clamp(t, 0, 1), 3); }
    static float approach(float current, float target, float seconds, float speed) {
        return current + (target - current) * (1 - (float)Math.exp(-seconds * speed));
    }
    static int alpha(int color, float opacity) {
        return ((int)((color >>> 24) * Math.clamp(opacity, 0, 1)) << 24) | (color & 0xFFFFFF);
    }
    static int background(int color, float opacity) {
        int amount = (int)Math.round(255 * Settings.number("ui.opacity") / 100);
        return alpha((amount << 24) | (color & 0xFFFFFF), opacity);
    }
    static void round(GuiGraphicsExtractor g, int x, int y, int w, int h, int r, int color) {
        if (w <= 0 || h <= 0 || color >>> 24 == 0) return;
        g.fill(x, y, x + w, y + h, color);
    }
    static void control(GuiGraphicsExtractor g,int x,int y,int w,int h,boolean hover,boolean selected,float alpha) {
        int edge=selected?PURPLE_LIGHT:hover?0xFFD0B5FF:0xFF76829A;
        int fill=selected?0xED58318A:hover?0xED39304D:0xD91A2130;
        g.fill(x,y,x+w,y+h,alpha(edge,alpha));
        g.fill(x+1,y+1,x+w-1,y+h-1,alpha(fill,alpha));
    }
}
