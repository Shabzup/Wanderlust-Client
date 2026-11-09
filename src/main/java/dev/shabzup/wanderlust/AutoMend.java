package dev.shabzup.wanderlust;

import java.util.Arrays;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

/** Independently implemented offhand repair queue; XP comes from the world. */
final class AutoMend {
    private static boolean moved;
    private static int nextSwap;

    static void reset() { moved = false; nextSwap = 0; }

    static boolean tick(Minecraft mc, LocalPlayer p) {
        if (mc.gameMode == null || p.containerMenu != p.inventoryMenu
            || !p.inventoryMenu.getCarried().isEmpty() || p.tickCount < nextSwap
            || Wanderlust.on("auto-totem") || Wanderlust.on("offhand")) return false;
        ItemStack held = p.getOffhandItem();
        boolean mending = repairable(held);
        if (mending && held.isDamaged()) return false;
        if (!held.isEmpty() && !mending && !Settings.toggle("mend-force")) return false;

        var excluded = Arrays.stream(Settings.text("mend-excluded").split(",")).map(String::trim).toList();
        for (int slot = 0; slot < 36; slot++) {
            ItemStack item = p.getInventory().getItem(slot);
            if (!repairable(item) || !item.isDamaged()
                || excluded.contains(BuiltInRegistries.ITEM.getKey(item.getItem()).toString())) continue;
            swap(mc, p, slot);
            moved = true;
            return true;
        }
        if (Settings.toggle("mend-auto-disable")) {
            if (moved && !held.isEmpty()) {
                // Never use a negative slot or drop the repaired item when inventory is full.
                for (int slot = 0; slot < 36; slot++) {
                    if (p.getInventory().getItem(slot).isEmpty()) { swap(mc, p, slot); break; }
                }
            }
            Settings.setEnabled(Feature.AUTO_MEND, false);
            reset();
        }
        return false;
    }

    private static boolean repairable(ItemStack item) {
        return item.isDamageableItem() && EnchantmentHelper.has(item, EnchantmentEffectComponents.REPAIR_WITH_XP);
    }

    private static void swap(Minecraft mc, LocalPlayer p, int slot) {
        mc.gameMode.handleContainerInput(p.inventoryMenu.containerId, slot < 9 ? slot + 36 : slot,
            40, ContainerInput.SWAP, p);
        nextSwap = p.tickCount + (int) Settings.number("mend-queue-delay");
    }
}
