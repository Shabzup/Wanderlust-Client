package dev.shabzup.wanderlust;

import java.util.*;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

final class ClientFeedback {
    private static boolean dirty = true;
    private static List<Feature> enabled = List.of();

    static void options() {
        Settings.toggle("ui", "ui.toggle-notifications", "Module toggle chat messages", true);
        Settings.numeric("active-modules", "active-modules.x", "Left offset", 8, 0, 1000, 1);
        Settings.numeric("active-modules", "active-modules.y", "Top offset", 8, 0, 1000, 1);
        Settings.numeric("active-modules", "active-modules.scale", "List size (%)", 100, 75, 150, 5);
        Settings.toggle("active-modules", "active-modules.background", "Show translucent background", true);
        Settings.toggle("active-modules", "active-modules.in-menus", "Show while menus are open", false);
    }

    static void init() {
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("wanderlust", "active_modules"), ClientFeedback::render);
    }

    static void invalidate() { dirty = true; }

    static List<Feature> enabled() {
        if (dirty) {
            enabled = Arrays.stream(Feature.values()).filter(feature -> Settings.on(feature.id))
                .sorted(Comparator.comparing(feature -> feature.label, String.CASE_INSENSITIVE_ORDER)).toList();
            dirty = false;
        }
        return enabled;
    }

    static void changed(Feature feature, boolean active) {
        invalidate();
        var mc = Minecraft.getInstance();
        if (feature == Feature.XRAY) Xray.refreshGeometry();
        if (!Settings.toggle("ui.toggle-notifications")) return;
        Runnable notify = () -> {
            if (mc.player == null || mc.level == null) return;
            Component message = Component.literal("[Wanderlust] ").withStyle(ChatFormatting.LIGHT_PURPLE)
                .append(Component.literal(feature.label + " ").withStyle(ChatFormatting.WHITE))
                .append(Component.literal(active ? "enabled" : "disabled").withStyle(active ? ChatFormatting.GREEN : ChatFormatting.RED));
            // This writes to the local chat HUD and never sends a chat packet.
            mc.gui.hud.getChat().addClientSystemMessage(message);
        };
        if (mc.isSameThread()) notify.run(); else mc.execute(notify);
    }

    static boolean visible() {
        var mc = Minecraft.getInstance();
        return Settings.on("active-modules") && mc.player != null && mc.level != null && !mc.gui.hud.isHidden()
            && (mc.gui.screen() == null || mc.gui.screen() instanceof ChatScreen || Settings.toggle("active-modules.in-menus"));
    }

    private static void render(GuiGraphicsExtractor g, DeltaTracker delta) {
        if (!visible()) return;
        var mc = Minecraft.getInstance();
        float scale = (float)Settings.number("active-modules.scale") / 100;
        int canvasWidth = (int)(g.guiWidth() / scale), canvasHeight = (int)(g.guiHeight() / scale);
        int x = Math.clamp((int)Settings.number("active-modules.x"), 2, Math.max(2, canvasWidth - 100));
        int y = Math.clamp((int)Settings.number("active-modules.y"), 2, Math.max(2, canvasHeight - 28));
        int pitch = 14, rows = Math.max(1, (canvasHeight - y - 20) / pitch);
        List<Feature> modules = enabled();
        g.pose().pushMatrix();
        g.pose().scale(scale, scale);
        g.text(mc.font, PanelStyle.text("Active modules"), x + 5, y, PanelStyle.PURPLE_LIGHT, true);
        int top = y + pitch;
        for (int offset = 0; offset < modules.size(); offset += rows) {
            List<String> labels = modules.subList(offset, Math.min(modules.size(), offset + rows)).stream()
                .map(feature -> feature.label + (BaritoneBridge.yields(feature.id) ? " (paused)" : "")).toList();
            int width = labels.stream().mapToInt(label -> mc.font.width(PanelStyle.text(label))).max().orElse(40) + 12;
            if (x + width > canvasWidth - 2) {
                g.text(mc.font, PanelStyle.text("+" + (modules.size() - offset) + " more"), Math.max(2,x-width-8), canvasHeight-14, PanelStyle.MUTED, true);
                break;
            }
            for (int row = 0; row < labels.size(); row++) {
                int rowY = top + row * pitch;
                if (Settings.toggle("active-modules.background")) g.fill(x, rowY - 2, x + width, rowY + 11, PanelStyle.background(0x0B101A, 1));
                g.fill(x, rowY - 2, x + 2, rowY + 11, PanelStyle.PURPLE);
                g.text(mc.font, PanelStyle.text(labels.get(row)), x + 6, rowY, PanelStyle.TEXT, true);
            }
            x += width + 8;
        }
        g.pose().popMatrix();
    }
}
