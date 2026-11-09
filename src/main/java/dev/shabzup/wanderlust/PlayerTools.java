package dev.shabzup.wanderlust;

import dev.shabzup.wanderlust.mixin.FishingHookAccessor;
import dev.shabzup.wanderlust.mixin.SignEditorAccessor;
import java.util.*;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.EntityHitResult;

public final class PlayerTools {
    private static final Map<Integer, Integer> fedAt = new HashMap<>();
    private static int foodSlot = -1, originalSlot, nextMeal, nextUtility, nextCast, nextRespawn;
    private static String foodModule;
    private static LocalPlayer eater;

    static void options() {
        Settings.numeric("auto-eat", "eat-hunger", "Hunger (0-20)", 14, 1, 19, 1);
        Settings.numeric("auto-gap", "gap-health", "Health (HP)", 10, 1, 20, 1);
        Settings.numeric("auto-log", "log-health", "Health (HP)", 5, 1, 20, 1);
        Settings.numeric("auto-mend", "mend-interval", "Ticks per XP bottle", 8, 3, 60, 1);
        for (int i = 1; i <= 4; i++) {
            Settings.string("auto-sign", "sign-line-" + i, "Line " + i, i == 1 ? "Shabzup" : "");
        }
    }

    static void reset(Minecraft mc) {
        finishFood(mc);
        AutoReplenish.reset();
        fedAt.clear();
        nextMeal = nextUtility = nextCast = nextRespawn = 0;
    }

    // Vanilla releases held-use items each tick when the physical use key is up.
    public static boolean holdingFood(Minecraft mc) {
        return eater != null && mc.player == eater && mc.gui.screen() == null && !Movement.scouting() && Wanderlust.on(foodModule)
            && eater.getInventory().getSelectedSlot() == foodSlot && eater.getUseItem().has(DataComponents.FOOD)
            && eater.getUseItemRemainingTicks() > -10;
    }

    private static void finishFood(Minecraft mc) {
        if (eater != null && eater == mc.player && eater.getInventory().getSelectedSlot() == foodSlot) {
            var player = eater;
            eater = null;
            if (player.isUsingItem() && player.getUseItem().has(DataComponents.FOOD) && mc.gameMode != null) {
                mc.gameMode.releaseUsingItem(player);
            }
            player.getInventory().setSelectedSlot(originalSlot);
        }
        eater = null;
        foodSlot = -1;
    }

    static boolean tick(Minecraft mc) {
        LocalPlayer p = mc.player;
        var screen = mc.gui.screen();
        if (!p.isAlive()) {
            finishFood(mc);
            if (Wanderlust.on("auto-respawn") && screen instanceof DeathScreen && !mc.level.getLevelData().isHardcore()) {
                if (nextRespawn == 0) nextRespawn = p.tickCount + (int)Wanderlust.number("respawn-delay");
                if (p.tickCount >= nextRespawn) {
                    nextRespawn = 0;
                    p.respawn();
                    mc.gui.setScreen(null);
                }
            }
            return true;
        }
        nextRespawn = 0;
        float safeHealth = p.getHealth() + (Settings.toggle("log-absorption") ? p.getAbsorptionAmount() : 0);
        if (Wanderlust.on("auto-log") && !mc.hasSingleplayerServer() && safeHealth <= Wanderlust.number("log-health")) {
            p.connection.getConnection().disconnect(Component.literal("WanderlustClient: low health (" + (int)safeHealth + " HP)"));
            return true;
        }
        if (eater != null) {
            if (holdingFood(mc)) return true;
            finishFood(mc);
        }
        if (screen != null) {
            AutoReplenish.screenOpened();
            if (Wanderlust.on("auto-sign") && screen instanceof AbstractSignEditScreen) {
                String[] lines = ((SignEditorAccessor) screen).wanderlust$messages();
                for (int i = 0; i < 4; i++) lines[i] = Wanderlust.text("sign-line-" + (i + 1));
                screen.onClose();
            }
            return true;
        }
        if (p.isSpectator() || mc.isPaused() || p.containerMenu != p.inventoryMenu || Wanderlust.on("freecam")) return true;
        AutoReplenish.tick(mc, p);
        if (p.isUsingItem()) return false; // Combat can still update its own bow draw.
        if (p.tickCount >= nextMeal && startFood(mc, p)) return true;
        if (p.tickCount >= nextUtility) {
            if (Wanderlust.on("auto-mend") && mend(mc, p)) return true;
            if ((Wanderlust.on("auto-breed") || Wanderlust.on("auto-shearer")) && p.tickCount % 4 == 0 && tendAnimals(mc, p)) return true;
        }
        return Wanderlust.on("auto-fish") && fish(mc, p);
    }

    private static boolean startFood(Minecraft mc, LocalPlayer p) {
        boolean gap = Wanderlust.on("auto-gap") && p.getHealth() + p.getAbsorptionAmount() <= Wanderlust.number("gap-health");
        int slot = gap ? hotbar(p, s -> s.is(Items.GOLDEN_APPLE)) : -1;
        if (gap && slot < 0) slot = hotbar(p, s -> s.is(Items.ENCHANTED_GOLDEN_APPLE));
        if (slot < 0) {
            if (!Wanderlust.on("auto-eat") || p.getFoodData().getFoodLevel() > Wanderlust.number("eat-hunger")) return false;
            slot = hotbar(p, s -> s.has(DataComponents.FOOD) && s.has(DataComponents.CONSUMABLE)
                && !s.is(Items.GOLDEN_APPLE) && !s.is(Items.ENCHANTED_GOLDEN_APPLE)
                && s.get(DataComponents.CONSUMABLE).onConsumeEffects().isEmpty());
            gap = false;
        }
        if (slot < 0) return false;
        originalSlot = p.getInventory().getSelectedSlot();
        p.getInventory().setSelectedSlot(slot);
        mc.gameMode.useItem(p, InteractionHand.MAIN_HAND);
        if (!p.isUsingItem()) {
            p.getInventory().setSelectedSlot(originalSlot);
            nextMeal = p.tickCount + 5;
            return false;
        }
        eater = p;
        foodSlot = slot;
        foodModule = gap ? "auto-gap" : "auto-eat";
        nextMeal = p.tickCount + (gap ? 60 : 20);
        return true;
    }

    private static int hotbar(LocalPlayer p, Predicate<ItemStack> predicate) {
        int selected = p.getInventory().getSelectedSlot();
        if (predicate.test(p.getMainHandItem())) return selected;
        for (int i = 0; i < 9; i++) {
            if (i != selected && predicate.test(p.getInventory().getItem(i))) return i;
        }
        return -1;
    }

    private static boolean mend(Minecraft mc, LocalPlayer p) {
        int bottles = hotbar(p, s -> s.is(Items.EXPERIENCE_BOTTLE));
        if (bottles < 0) return false;
        ItemStack repair = null;
        if (Settings.toggle("mend-armor")) {
            for (EquipmentSlot slot : EquipmentSlot.VALUES) {
                if (!slot.isArmor()) continue;
                ItemStack item = p.getItemBySlot(slot);
                if (needsRepair(item)) { repair = item; break; }
            }
        }
        if (repair == null && Settings.toggle("mend-tools") && needsRepair(p.getMainHandItem())) repair = p.getMainHandItem();
        if (repair != null) {
            int old = p.getInventory().getSelectedSlot();
            float pitch = p.getXRot();
            p.getInventory().setSelectedSlot(bottles);
            p.setXRot(90);
            mc.gameMode.useItem(p, InteractionHand.MAIN_HAND);
            p.setXRot(pitch);
            p.getInventory().setSelectedSlot(old);
            nextUtility = p.tickCount + (int)Wanderlust.number("mend-interval");
            return true;
        }
        return false;
    }

    private static boolean needsRepair(ItemStack item) {
        if (!item.isDamaged() || !EnchantmentHelper.has(item, EnchantmentEffectComponents.REPAIR_WITH_XP)) return false;
        int remaining = item.getMaxDamage() - item.getDamageValue();
        return remaining * 100.0 / item.getMaxDamage() <= Wanderlust.number("mend-durability");
    }

    private static boolean tendAnimals(Minecraft mc, LocalPlayer p) {
        fedAt.values().removeIf(t -> p.tickCount - t > 600);
        double radius = Math.max(Wanderlust.on("auto-breed") ? Wanderlust.number("breed-range") : 0,
            Wanderlust.on("auto-shearer") ? Wanderlust.number("shearer-range") : 0);
        for (Animal animal : mc.level.getEntitiesOfClass(Animal.class, p.getBoundingBox().inflate(radius))) {
            if (!animal.isAlive() || !p.isWithinEntityInteractionRange(animal, 0) || !p.hasLineOfSight(animal)) continue;
            int item = -1;
            if (Wanderlust.on("auto-shearer") && animal instanceof Sheep sheep && sheep.readyForShearing()) {
                item = hotbar(p, s -> s.is(Items.SHEARS));
            }
            boolean feeding = item < 0;
            if (feeding && Wanderlust.on("auto-breed") && !animal.isBaby() && animal.canFallInLove() && !fedAt.containsKey(animal.getId())) {
                item = hotbar(p, animal::isFood);
            }
            if (item < 0) continue;
            double range = feeding ? Wanderlust.number("breed-range") : Wanderlust.number("shearer-range");
            if (animal.distanceToSqr(p) > range * range) continue;
            int selected = p.getInventory().getSelectedSlot();
            p.getInventory().setSelectedSlot(item);
            var result = mc.gameMode.interact(p, animal, new EntityHitResult(animal), InteractionHand.MAIN_HAND);
            p.getInventory().setSelectedSlot(selected);
            if (result.consumesAction()) {
                if (feeding) fedAt.put(animal.getId(), p.tickCount);
                p.swing(InteractionHand.MAIN_HAND);
                nextUtility = p.tickCount + 4;
                return true;
            }
        }
        return false;
    }

    private static boolean fish(Minecraft mc, LocalPlayer p) {
        if (p.tickCount < nextCast || !p.getMainHandItem().is(Items.FISHING_ROD)) return false;
        if (p.fishing != null && !((FishingHookAccessor)p.fishing).wanderlust$biting()) return false;
        mc.gameMode.useItem(p, InteractionHand.MAIN_HAND);
        nextCast = p.tickCount + (int)Wanderlust.number("fish-delay");
        return true;
    }
}
