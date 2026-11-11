package dev.shabzup.wanderlust;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class Wanderlust implements ClientModInitializer {
    private KeyMapping menu;
    static final java.util.Map<Feature, KeyMapping> binds = new java.util.EnumMap<>(Feature.class);
    private LocalPlayer lastPlayer;
    private ClientLevel lastLevel;

    public static boolean on(String id) {
        boolean enabled=Settings.on(id)||id.equals("kill-aura")&&Settings.on("z-kill-aura")
            ||id.equals("auto-totem")&&Settings.on("z-auto-totem")||id.equals("auto-trap")&&Settings.on("auto-trap-plus");
        return enabled&&!BaritoneBridge.yields(id);
    }
    public static double number(String key) { return Settings.number(key); }
    public static String text(String key) { return Settings.text(key); }
    public static boolean setting(String key) { return Settings.toggle(key); }
    public static void shutdownNavigation() { BaritoneBridge.shutdown(); }

    @Override public void onInitializeClient() {
        PlayerTools.options();
        Combat.options();
        Movement.options();
        Visuals.options();
        ModuleOptions.register();
        ExtraMovement.options();
        ExtraActions.options();
        ExtraVisuals.options();
        ExtraPlayer.options();
        ExtraWorld.options();
        ExtraMisc.options();
        ExtraAutomation.options();
        ExtraSwarm.options();
        ClientFeedback.options();
        Settings.load();
        for(Feature feature:Feature.values())try{String group=Settings.text("category."+feature.id);if(!group.isBlank())feature.group=Feature.Group.valueOf(group);}catch(IllegalArgumentException ignored){}
        Accounts.init();
        MenuAccess.init();
        PanelStyle.init();
        Proxies.load();
        ExtraMisc.init();
        var category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("wanderlust", "modules"));
        for (Feature feature : Feature.values()) {
            binds.put(feature, KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.wanderlust." + feature.id, GLFW.GLFW_KEY_UNKNOWN, category)));
        }
        menu = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.wanderlust.menu", GLFW.GLFW_KEY_RIGHT_SHIFT,
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath("wanderlust", "controls"))));
        Visuals.init();
        ClientFeedback.init();
        ClientLifecycleEvents.CLIENT_STOPPING.register(mc -> { Accounts.shutdown(); Settings.save(); });
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
    }

    private void tick(Minecraft mc) {
        while (menu.consumeClick()) {
            MenuAccess.toggle(mc);
        }
        for (var entry : binds.entrySet()) {
            while (entry.getValue().consumeClick()) {
                if (mc.level != null && mc.gui.screen() == null) {
                    Settings.setEnabled(entry.getKey(), !Settings.on(entry.getKey().id));
                }
            }
        }
        BaritoneBridge.tick(mc);
        ExtraMisc.tick(mc);
        ExtraSwarm.tick(mc);
        Accounts.tick();
        Xray.tick(mc);
        if (lastLevel != mc.level || lastPlayer != mc.player) {
            Movement.reset(mc);
            Combat.reset(mc);
            Visuals.reset(mc);
            PlayerTools.reset(mc);
            ExtraPlayer.reset();
            ExtraMovement.reset();ExtraActions.reset();ExtraVisuals.reset();ExtraWorld.reset();
            lastLevel = mc.level;
            lastPlayer = mc.player;
        }
        if (mc.level == null || mc.player == null || mc.gameMode == null) return;
        Combat.cleanup(mc);
        Movement.tick(mc);
        ExtraMovement.tick(mc);
        ExtraActions.tick(mc);
        ExtraVisuals.tick(mc);
        ExtraPlayer.tick(mc);
        ExtraWorld.tick(mc);
        ExtraAutomation.tick(mc);
        Visuals.tick(mc);
        if (!Movement.scouting()) Combat.equipTotem(mc);
        if (!PlayerTools.tick(mc)) Combat.tick(mc);
    }
}
