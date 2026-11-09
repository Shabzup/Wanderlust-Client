package dev.shabzup.wanderlust;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;

/** Keeps hotbar and offhand contents stocked using normal inventory clicks. */
final class AutoReplenish {
    private static final ItemStack[] previous = new ItemStack[10];
    private static boolean active, screenWasOpen;
    private static int nextMove;

    private AutoReplenish() {}

    static void reset() {
        Arrays.fill(previous, null);
        active = screenWasOpen = false;
        nextMove = 0;
    }

    static void screenOpened() { screenWasOpen = true; }

    static void tick(Minecraft mc, LocalPlayer player) {
        if (!Wanderlust.on("auto-replenish")) {
            reset();
            return;
        }
        if (!active || screenWasOpen) {
            snapshot(player);
            active = true;
            screenWasOpen = false;
            nextMove = player.tickCount + (int) Settings.number("replenish-delay");
            return;
        }
        if (player.tickCount < nextMove || !player.inventoryMenu.getCarried().isEmpty()) return;

        int threshold = (int) Settings.number("replenish-count");
        Set<String> excluded = Arrays.stream(Settings.text("replenish-excluded").split(","))
            .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toSet());
        int count = Settings.toggle("replenish-offhand") ? 10 : 9;
        for (int slot = 0; slot < count; slot++) {
            ItemStack now = current(player, slot);
            ItemStack before = previous[slot];
            previous[slot] = now.copy();
            if (!now.isEmpty() && excluded.contains(id(now))) continue;
            if (before == null || before.isEmpty() || excluded.contains(id(before))) continue;
            if (slot == 9 && now.is(net.minecraft.world.item.Items.TOTEM_OF_UNDYING)
                && Wanderlust.on("auto-totem")) continue;

            boolean empty = now.isEmpty();
            boolean stackable = !empty ? now.isStackable() : before.isStackable();
            if (!empty && (!stackable || now.getCount() > threshold)) continue;
            if (empty && !stackable && !Settings.toggle("replenish-unstackable")) continue;
            if (!empty && !same(before, now, false)) continue;

            int source = findSource(player, empty ? before : now, slot, empty && !stackable);
            if (source < 0) continue;
            int sourceMenu = menuSlot(source);
            int targetMenu = menuSlot(slot);
            if (empty) {
                mc.gameMode.handleContainerInput(player.inventoryMenu.containerId, sourceMenu,
                    slot == 9 ? 40 : slot, ContainerInput.SWAP, player);
            } else {
                // Pick up the spare, merge into the low stack, then return any remainder.
                for (int click : new int[]{sourceMenu, targetMenu, sourceMenu})
                    mc.gameMode.handleContainerInput(player.inventoryMenu.containerId, click, 0, ContainerInput.PICKUP, player);
            }
            previous[slot] = current(player, slot).copy();
            if (source < 9) previous[source] = current(player, source).copy();
            nextMove = player.tickCount + Math.max(1, (int) Settings.number("replenish-delay"));
            return;
        }
    }

    private static void snapshot(LocalPlayer player) {
        for (int i = 0; i < previous.length; i++) previous[i] = current(player, i).copy();
    }

    private static ItemStack current(LocalPlayer player, int slot) {
        return slot == 9 ? player.getOffhandItem() : player.getInventory().getItem(slot);
    }

    private static int menuSlot(int inventorySlot) {
        return inventorySlot == 9 ? 45 : inventorySlot < 9 ? inventorySlot + 36 : inventorySlot;
    }

    private static int findSource(LocalPlayer player, ItemStack wanted, int target, boolean usedTool) {
        int begin = Settings.toggle("replenish-hotbar") ? 0 : 9;
        for (int i = 35; i >= begin; i--) {
            if (target < 9 && i == target) continue;
            ItemStack candidate = player.getInventory().getItem(i);
            if (candidate.isEmpty() || !same(wanted, candidate, usedTool)) continue;
            if (usedTool && !candidate.isStackable()) return i;
            if (!usedTool && candidate.isStackable()) return i;
        }
        return -1;
    }

    private static boolean same(ItemStack a, ItemStack b, boolean usedTool) {
        if (!a.is(b.getItem())) return false;
        if (usedTool) {
            if (Settings.toggle("replenish-same-enchants") && !a.getEnchantments().equals(b.getEnchantments())) return false;
            boolean sameEnchants = Settings.toggle("replenish-same-enchants");
            return ItemStack.matchesIgnoringComponents(a, b, component -> component == DataComponents.DAMAGE
                || (!sameEnchants && component == DataComponents.ENCHANTMENTS));
        }
        return ItemStack.isSameItemSameComponents(a, b);
    }

    private static String id(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }
}
