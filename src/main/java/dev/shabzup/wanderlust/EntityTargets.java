package dev.shabzup.wanderlust;

import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.*;

final class EntityTargets {
    record Entry(String id, String name) {}
    private static final Set<String> selected = new HashSet<>();
    private static boolean loaded;

    private static void load() {
        if (loaded) return;
        String saved = Settings.text("aura.entities");
        selected.addAll(Arrays.asList((saved.isBlank() ? "minecraft:player" : saved).split(",")));
        selected.remove("none");
        loaded = true;
    }

    static boolean includes(Entity entity) {
        if(entity instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragonPart)return contains("minecraft:ender_dragon");
        return contains(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString());
    }
    static boolean contains(String id) { load(); return selected.contains(id); }
    static int count() { load(); return selected.size(); }
    static void set(String id, boolean enabled) {
        load(); if (enabled) selected.add(id); else selected.remove(id); save();
    }
    static void setAll(List<Entry> entries, boolean enabled) {
        load(); selected.clear(); if (enabled) for (Entry entry : entries) selected.add(entry.id()); save();
    }
    private static void save() {
        Settings.put("aura.entities", selected.isEmpty() ? "none" : String.join(",", new TreeSet<>(selected)));
        Settings.save();
    }
    static List<Entry> entries() {
        var level = Minecraft.getInstance().level;
        List<Entry> entries = new ArrayList<>();
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            String id=BuiltInRegistries.ENTITY_TYPE.getKey(type).toString();
            boolean hit = id.equals("minecraft:player")||id.equals("minecraft:ender_dragon");
            if (!hit && level != null) {
                try {
                    Entity sample = type.create(level, EntitySpawnReason.COMMAND);
                    hit = sample != null && sample.isPickable() && sample.isAttackable();
                } catch (RuntimeException ignored) { }
            } else if (level == null) hit = true;
            if (hit) entries.add(new Entry(BuiltInRegistries.ENTITY_TYPE.getKey(type).toString(), type.getDescription().getString()));
        }
        entries.sort(Comparator.comparing(Entry::name).thenComparing(Entry::id));
        return List.copyOf(entries);
    }
}
