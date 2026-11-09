package qa;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ReplenishProbe implements ClientModInitializer {
    private boolean loading, playing;
    private int tick, failures;

    private void check(boolean ok, String label) {
        if (!ok) failures++;
        System.out.println("REPLENISH " + (ok ? "PASS " : "FAIL ") + label);
    }

    private void enable(boolean on) throws Exception {
        Class<?> feature = Class.forName("dev.shabzup.wanderlust.Feature");
        Object value = Enum.valueOf((Class) feature, "AUTO_REPLENISH");
        UpdateProbe.call("Settings", "setEnabled", new Class[]{feature, boolean.class}, value, on);
    }

    private void setup(Minecraft mc) {
        mc.getSingleplayerServer().submit(() -> {
            var player = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
            player.getInventory().setItem(0, new ItemStack(Items.COBBLESTONE, 4));
            player.getInventory().setItem(10, new ItemStack(Items.COBBLESTONE, 20));
            player.getInventory().setItem(1, new ItemStack(Items.DIRT, 1));
            player.getInventory().setItem(11, new ItemStack(Items.DIRT, 16));
            player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.OFFHAND, new ItemStack(Items.GOLDEN_APPLE, 2));
            player.getInventory().setItem(12, new ItemStack(Items.GOLDEN_APPLE, 12));
            player.inventoryMenu.broadcastChanges();
        }).join();
    }

    private int count(Minecraft mc, int slot) {
        return mc.getSingleplayerServer().submit(() -> {
            var p = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
            return slot == 40 ? p.getOffhandItem().getCount() : p.getInventory().getItem(slot).getCount();
        }).join();
    }

    @Override public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            try { run(mc); }
            catch (Throwable t) { failures++; t.printStackTrace(); System.out.println("REPLENISH FAIL exception"); mc.stop(); }
        });
    }

    private void run(Minecraft mc) throws Exception {
        if (!loading && mc.isGameLoadFinished() && mc.gui.overlay() == null) {
            loading = true;
            org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().handle());
            String name = "Replenish-" + System.currentTimeMillis();
            mc.createWorldOpenFlows().createFreshLevel(name,
                new LevelSettings(name, GameType.SURVIVAL,
                    new LevelSettings.DifficultySettings(Difficulty.PEACEFUL, false, false),
                    true, WorldDataConfiguration.DEFAULT),
                new WorldOptions(123, false, false), WorldPresets::createTestWorldDimensions, new TitleScreen());
        }
        if (mc.player == null || mc.gui.overlay() != null || mc.gui.screen() != null) return;
        if (!playing) { playing = true; setup(mc); }
        tick++;
        if (tick == 12) enable(true);
        if (tick == 45) {
            check(count(mc, 0) == 24 && count(mc, 10) == 0, "low stack merged from main inventory");
            check(count(mc, 40) == 14 && count(mc, 12) == 0, "offhand stack refilled");
            mc.getSingleplayerServer().submit(() -> {
                var p = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                p.getInventory().setItem(1, new ItemStack(Items.DIRT, 16));
                p.getInventory().setItem(11, new ItemStack(Items.DIRT, 16));
                p.inventoryMenu.broadcastChanges();
            }).join();
        }
        if (tick == 55) {
            mc.getSingleplayerServer().submit(() -> {
                var p = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                p.getInventory().setItem(1, ItemStack.EMPTY);
                p.inventoryMenu.broadcastChanges();
            }).join();
        }
        if (tick == 75) {
            check(count(mc, 1) == 16 && count(mc, 11) == 0, "depleted slot restored");
            enable(false);
            mc.getSingleplayerServer().submit(() -> {
                var p = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                p.getInventory().setItem(0, new ItemStack(Items.COBBLESTONE, 2));
                p.getInventory().setItem(10, new ItemStack(Items.COBBLESTONE, 20));
                p.inventoryMenu.broadcastChanges();
            }).join();
        }
        if (tick == 95) {
            check(count(mc, 0) == 2 && count(mc, 10) == 20, "disabled module leaves inventory untouched");
            System.out.println("REPLENISH COMPLETE failures=" + failures);
            mc.stop();
        }
    }
}
