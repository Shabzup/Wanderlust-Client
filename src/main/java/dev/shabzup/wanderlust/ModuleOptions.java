package dev.shabzup.wanderlust;

/** Registers the per-module controls used by the client and its settings panel. */
final class ModuleOptions {
    private ModuleOptions() {}

    static void register() {
        Settings.numeric("ui", "ui.opacity", "Background opacity (%)", 20, 0, 100, 1);
        Settings.numeric("ui", "ui.scale", "Module list size (%)", 70, 40, 100, 1);
        Settings.choice("ui", "ui.density", "Row spacing", "Comfortable", "Compact", "Comfortable", "Spacious");
        Settings.toggle("ui", "ui.auto-fit", "Fit lists to available space", true);
        Settings.toggle("ui", "ui.motion", "Smooth animations", true);
        Settings.toggle("ui", "ui.depth", "Decorative 3D motion", true);
        Settings.choice("ui", "ui.accent", "Accent colour", "Violet", "Violet", "Ocean", "Mint", "Rose", "Amber");
        Settings.toggle("ui", "ui.category-colours", "Colour each category", true);
        Settings.string("xray", "xray.blocks", "Block IDs (comma separated)", "minecraft:coal_ore,minecraft:deepslate_coal_ore,minecraft:iron_ore,minecraft:deepslate_iron_ore,minecraft:copper_ore,minecraft:deepslate_copper_ore,minecraft:gold_ore,minecraft:deepslate_gold_ore,minecraft:redstone_ore,minecraft:deepslate_redstone_ore,minecraft:lapis_ore,minecraft:deepslate_lapis_ore,minecraft:emerald_ore,minecraft:deepslate_emerald_ore,minecraft:diamond_ore,minecraft:deepslate_diamond_ore,minecraft:nether_gold_ore,minecraft:nether_quartz_ore,minecraft:ancient_debris");
        Settings.numeric("kill-aura", "aura-delay", "Attack delay (ticks)", 3, 1, 20, 1);
        Settings.toggle("kill-aura", "aura-through-walls", "Allow targets through walls", false);
        Settings.choice("kill-aura", "aura-priority", "Target priority", "Closest", "Closest", "Lowest health");

        for (String id : new String[]{"crystal-aura", "anchor-aura", "bed-aura"}) {
            Settings.numeric(id, id + "-range", "Target range (blocks)", 12, 4, 32, 1);
        }
        Settings.numeric("auto-totem", "totem-health", "Equip below health (HP)", 20, 1, 20, 1);
        Settings.toggle("criticals", "criticals-players-only", "Only critical player targets", false);
        for (String id : new String[]{"bow-aimbot", "bow-spam"}) {
            String key = id.equals("bow-aimbot") ? "bow" : "bowspam";
            Settings.toggle(id, key + "-players", "Target players", true);
            Settings.toggle(id, key + "-hostiles", "Target hostile mobs", false);
            Settings.toggle(id, key + "-neutrals", "Target neutral mobs", false);
            Settings.toggle(id, key + "-passives", "Target passive mobs", false);
        }
        Settings.toggle("bow-aimbot", "bow-lead-target", "Predict target movement", true);
        Settings.toggle("bow-aimbot", "bow-aim-head", "Aim at upper body", false);
        Settings.toggle("bow-spam", "bow-require-target", "Only fire with a valid target", false);
        Settings.numeric("bow-spam", "bowspam-range", "Target range (blocks)", 36, 4, 96, 4);
        Settings.toggle("auto-clicker", "click-hold-only", "Only while attack is held", true);

        Settings.numeric("flight", "flight.vertical", "Vertical speed", .45, .05, 2, .05);
        Settings.numeric("boat-fly", "boat-fly.vertical", "Vertical speed", .45, .05, 2, .05);
        Settings.toggle("speed", "speed-water", "Apply while swimming", true);
        Settings.choice("no-fall", "no-fall.mode", "Protection mode", "Packet", "Packet", "Velocity");
        Settings.toggle("jesus", "jesus-water", "Walk on water", true);
        Settings.toggle("jesus", "jesus-lava", "Walk on lava", true);
        Settings.numeric("blink", "blink.max-packets", "Maximum queued packets", 256, 16, 512, 16);
        Settings.numeric("freecam", "freecam.range", "Maximum distance (blocks)", 64, 8, 256, 8);
        Settings.numeric("free-look", "freelook.sensitivity", "Camera sensitivity", 1, .25, 3, .05);

        Settings.toggle("esp", "esp-players", "Players", true);
        Settings.toggle("esp", "esp-hostiles", "Hostile mobs", false);
        Settings.toggle("esp", "esp-passives", "Passive mobs", false);
        Settings.toggle("esp", "esp-items", "Dropped items", true);
        Settings.toggle("tracers", "tracers-players", "Players", true);
        Settings.toggle("tracers", "tracers-mobs", "Living mobs", false);
        Settings.toggle("nametags", "nametags-health", "Show health", true);
        Settings.toggle("nametags", "nametags-distance", "Show distance", false);
        Settings.toggle("structure-esp", "structure-spawners", "Spawners and vaults", true);
        Settings.toggle("structure-esp", "structure-portals", "Portals and gateways", true);
        Settings.toggle("structure-esp", "structure-bells", "Bells", true);
        for (String key : new String[]{"chests", "shulkers", "furnaces", "hoppers", "ender"}) {
            Settings.toggle("storage-esp", "storage-" + key, "Show " + key, true);
        }

        Settings.toggle("auto-mend", "mend-armor", "Repair armor", true);
        Settings.choice("auto-mend", "mend-mode", "Repair mode", "XP bottles", "XP bottles", "Offhand queue");
        Settings.toggle("auto-mend", "mend-force", "Replace occupied offhand", false);
        Settings.toggle("auto-mend", "mend-auto-disable", "Disable when queue finishes", true);
        Settings.string("auto-mend", "mend-excluded", "Excluded item IDs (comma separated)", "");
        Settings.numeric("auto-mend", "mend-queue-delay", "Ticks between item swaps", 2, 1, 40, 1);
        Settings.toggle("auto-mend", "mend-tools", "Repair held tools", false);
        Settings.numeric("auto-mend", "mend-durability", "Repair below durability (%)", 95, 5, 99, 5);
        Settings.numeric("auto-replenish", "replenish-count", "Minimum stack count", 8, 1, 63, 1);
        Settings.numeric("auto-replenish", "replenish-delay", "Ticks between moves", 1, 0, 40, 1);
        Settings.toggle("auto-replenish", "replenish-offhand", "Refill offhand", true);
        Settings.toggle("auto-replenish", "replenish-unstackable", "Replace used tools and items", true);
        Settings.toggle("auto-replenish", "replenish-same-enchants", "Match enchantments", true);
        Settings.toggle("auto-replenish", "replenish-hotbar", "Search other hotbar slots", false);
        Settings.string("auto-replenish", "replenish-excluded", "Excluded item IDs (comma separated)", "");
        Settings.toggle("auto-log", "log-absorption", "Include absorption health", true);
        Settings.numeric("auto-respawn", "respawn-delay", "Respawn delay (ticks)", 1, 1, 100, 1);

        Settings.numeric("auto-breed", "breed-range", "Animal range (blocks)", 4, 2, 8, .5);
        Settings.numeric("auto-shearer", "shearer-range", "Sheep range (blocks)", 4, 2, 8, .5);
        Settings.numeric("auto-fish", "fish-delay", "Recast delay (ticks)", 15, 1, 60, 1);
    }
}
