package dev.shabzup.wanderlust;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.gizmos.SimpleGizmoCollector;
import net.minecraft.gizmos.TextGizmo;
import net.minecraft.util.debug.DebugSubscriptions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

final class Visuals {
    private record Marker(BlockPos pos, int color) {}
    private static final RenderStateDataKey<List<SimpleGizmoCollector.GizmoInstance>> OVERLAYS =
        RenderStateDataKey.create(() -> "Wanderlust overlays");
    private static final int PLAYER = 0xff53ddfa, ITEM = 0xfffccc6e, STRUCTURE = 0xffec88ff;
    private static final List<Marker> storage = new ArrayList<>(), landmarks = new ArrayList<>();
    private static List<AABB> structures = List.of();
    private static CompletableFuture<List<AABB>> structureScan;
    private static BlockPos origin;
    private static Direction forward;
    private static int scanTimer;

    static void options() {
        Settings.numeric("esp", "esp-range", "Entity range (blocks)", 96, 8, 192, 8);
        Settings.numeric("tracers", "tracer-range", "Tracer range (blocks)", 64, 8, 160, 8);
        Settings.numeric("nametags", "nametag-range", "Name range (blocks)", 96, 8, 192, 8);
        Settings.numeric("storage-esp", "storage-range", "Storage range (chunks)", 4, 1, 8, 1);
        Settings.numeric("structure-esp", "structure-range", "Structure range (chunks)", 5, 1, 8, 1);
        Settings.choice("build-pos", "build-shape", "Layout shape", "frame", "line", "frame", "floor");
        Settings.numeric("build-pos", "build-length", "Length (blocks)", 5, 1, 16, 1);
        Settings.numeric("build-pos", "build-width", "Width (blocks)", 5, 1, 16, 1);
    }

    static void init() {
        LevelExtractionEvents.END_EXTRACTION.register(Visuals::extract);
        LevelRenderEvents.BEFORE_GIZMOS.register(context -> {
            var state = (FabricRenderState) context.levelState();
            var overlays = state.getData(OVERLAYS);
            if (overlays != null) context.levelRenderer().addMainThreadGizmos(overlays);
        });
    }

    static void reset(Minecraft mc) {
        origin = null;
        storage.clear();
        landmarks.clear();
        structures = List.of();
        structureScan = null;
        scanTimer = 0;
    }

    static void tick(Minecraft mc) {
        if (!Wanderlust.on("build-pos")) origin = null;
        else if (origin == null && mc.gui.screen() == null && mc.hitResult instanceof BlockHitResult hit
            && hit.getType() == HitResult.Type.BLOCK) {
            BlockPos targeted = hit.getBlockPos();
            origin = mc.level.getBlockState(targeted).canBeReplaced() ? targeted : targeted.relative(hit.getDirection());
            forward = mc.player.getDirection();
        }
        boolean findStorage = Wanderlust.on("storage-esp");
        boolean findStructures = Wanderlust.on("structure-esp");
        if (!findStorage && !findStructures) {
            scanTimer = 0;
            return;
        }
        if (structureScan != null && structureScan.isDone()) {
            structures = structureScan.join();
            structureScan = null;
        }
        if (scanTimer-- > 0) return;
        scanTimer = 14;
        int storageRadius = findStorage ? (int) Wanderlust.number("storage-range") : -1;
        int structureRadius = findStructures ? (int) Wanderlust.number("structure-range") : -1;
        int radius = Math.max(storageRadius, structureRadius);
        ChunkPos center = mc.player.chunkPosition();
        storage.clear();
        landmarks.clear();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                var chunk = mc.level.getChunkSource().getChunkNow(center.x() + dx, center.z() + dz);
                if (chunk == null) continue;
                int distance = Math.max(Math.abs(dx), Math.abs(dz));
                for (BlockEntity block : chunk.getBlockEntities().values()) {
                    if (distance <= storageRadius) {
                        int color = storageColor(block);
                        if (color != 0) storage.add(new Marker(block.getBlockPos(), color));
                    }
                    if (distance <= structureRadius && isLandmark(block)) landmarks.add(new Marker(block.getBlockPos(), STRUCTURE));
                }
            }
        }
        if (!findStructures) {
            structures = List.of();
            return;
        }
        var server = mc.getSingleplayerServer();
        if (server == null && mc.getConnection() != null) {
            List<AABB> bounds = new ArrayList<>();
            mc.getConnection().createDebugValueAccess().forEachChunk(DebugSubscriptions.STRUCTURES, (pos, starts) -> {
                if (Math.abs(pos.x() - center.x()) <= structureRadius && Math.abs(pos.z() - center.z()) <= structureRadius) {
                    for (var start : starts) bounds.add(AABB.of(start.boundingBox()));
                }
            });
            structures = bounds;
        } else if (server != null && structureScan == null) {
            var dimension = mc.level.dimension();
            // Only the integrated server owns the structure starts. Never load a chunk for this scan.
            structureScan = server.submit(() -> {
                List<AABB> bounds = new ArrayList<>();
                var level = server.getLevel(dimension);
                if (level != null) {
                    for (int x = center.x() - structureRadius; x <= center.x() + structureRadius; x++) {
                        for (int z = center.z() - structureRadius; z <= center.z() + structureRadius; z++) {
                            var chunk = level.getChunkSource().getChunkNow(x, z);
                            if (chunk == null) continue;
                            for (var start : chunk.getAllStarts().values()) {
                                if (start.isValid()) bounds.add(AABB.of(start.getBoundingBox()));
                            }
                        }
                    }
                }
                return List.copyOf(bounds);
            });
        }
    }

    private static int storageColor(BlockEntity block) {
        if (block instanceof EnderChestBlockEntity) return Wanderlust.setting("storage-ender") ? 0xffbb70ff : 0;
        if (block instanceof TrappedChestBlockEntity) return Wanderlust.setting("storage-chests") ? 0xffff6565 : 0;
        if (block instanceof ChestBlockEntity || block instanceof BarrelBlockEntity) return Wanderlust.setting("storage-chests") ? 0xffffbd54 : 0;
        if (block instanceof ShulkerBoxBlockEntity) return Wanderlust.setting("storage-shulkers") ? 0xffff8ac5 : 0;
        if (block instanceof AbstractFurnaceBlockEntity) return Wanderlust.setting("storage-furnaces") ? 0xff83d6ff : 0;
        if (block instanceof HopperBlockEntity || block instanceof DispenserBlockEntity) return Wanderlust.setting("storage-hoppers") ? 0xffacd0aa : 0;
        return 0;
    }

    private static boolean isLandmark(BlockEntity block) {
        var type = block.getType();
        if (type == BlockEntityTypes.TRIAL_SPAWNER || type == BlockEntityTypes.VAULT || type == BlockEntityTypes.MOB_SPAWNER)
            return Wanderlust.setting("structure-spawners");
        if (type == BlockEntityTypes.END_PORTAL || type == BlockEntityTypes.END_GATEWAY)
            return Wanderlust.setting("structure-portals");
        return type == BlockEntityTypes.BELL && Wanderlust.setting("structure-bells");
    }

    private static void extract(LevelExtractionContext context) {
        boolean esp = Wanderlust.on("esp"), tracers = Wanderlust.on("tracers"), names = Wanderlust.on("nametags");
        boolean containers = Wanderlust.on("storage-esp"), landmarksOn = Wanderlust.on("structure-esp");
        boolean layout = Wanderlust.on("build-pos") && origin != null;
        var state = (FabricRenderState) context.levelState();
        if (!esp && !tracers && !names && !containers && !landmarksOn && !layout && !ExtraVisuals.active()) {
            state.setData(OVERLAYS, null);
            return;
        }
        var gizmos = new SimpleGizmoCollector();
        try (var ignored = Gizmos.withCollector(gizmos)) {
            if (esp || tracers || names) drawEntities(context, esp, tracers, names);
            if (containers) drawMarkers(storage);
            if (landmarksOn) {
                drawMarkers(landmarks);
                var style = GizmoStyle.stroke(STRUCTURE, 2);
                for (AABB bounds : structures) Gizmos.cuboid(bounds, style).setAlwaysOnTop();
            }
            if (layout) drawLayout(context.level());
            ExtraVisuals.draw(context);
        }
        state.setData(OVERLAYS, gizmos.drainGizmos());
    }

    private static void drawEntities(LevelExtractionContext context, boolean esp, boolean tracers, boolean names) {
        var mc = Minecraft.getInstance();
        var camera = context.camera();
        Vec3 eye = camera.position();
        Vec3 rayStart = eye.add(new Vec3(camera.forwardVector()).scale(0.5));
        float partial = camera.getCameraEntityPartialTicks(context.deltaTracker());
        double espRange = Math.pow(Wanderlust.number("esp-range"), 2);
        double tracerRange = Math.pow(Wanderlust.number("tracer-range"), 2);
        double nameRange = Math.pow(Wanderlust.number("nametag-range"), 2);
        for (Entity entity : context.level().entitiesForRendering()) {
            if (entity == mc.player || entity == camera.entity() || !entity.isAlive()) continue;
            Vec3 pos = entity.getPosition(partial);
            double distance = pos.distanceToSqr(eye);
            boolean player = entity instanceof Player;
            boolean item = entity instanceof ItemEntity;
            boolean hostile = entity instanceof Monster;
            boolean passive = entity instanceof Animal;
            AABB box = entity.getBoundingBox().move(pos.subtract(entity.position()));
            boolean showEsp = player && Wanderlust.setting("esp-players") || item && Wanderlust.setting("esp-items")
                || hostile && Wanderlust.setting("esp-hostiles") || passive && Wanderlust.setting("esp-passives");
            boolean showTracer = player && Wanderlust.setting("tracers-players")
                || entity instanceof LivingEntity && !player && Wanderlust.setting("tracers-mobs");
            if (esp && distance <= espRange && showEsp) {
                int color = player ? PLAYER : item ? ITEM : 0xffa0dd91;
                Gizmos.cuboid(box.inflate(0.035), GizmoStyle.stroke(color, 2)).setAlwaysOnTop();
            }
            if (tracers && distance <= tracerRange && showTracer) {
                Gizmos.line(rayStart, box.getCenter(), player ? PLAYER : 0xffa0dd91, 1.3f).setAlwaysOnTop();
            }
            if (names && player && distance <= nameRange) {
                var target = (Player) entity;
                String label = target.getName().getString();
                if (Wanderlust.setting("nametags-health")) label += "  " + (int) Math.ceil(target.getHealth()) + " HP";
                if (Wanderlust.setting("nametags-distance")) label += "  " + String.format(Locale.ROOT, "%.0fm", Math.sqrt(distance));
                float scale = (float) Math.max(0.42, Math.sqrt(distance) * 0.028);
                Gizmos.billboardText(label, new Vec3(pos.x, box.maxY + 0.45, pos.z),
                    TextGizmo.Style.whiteAndCentered().withScale(scale)).setAlwaysOnTop();
            }
        }
    }

    private static void drawMarkers(List<Marker> markers) {
        for (var marker : markers) {
            Gizmos.cuboid(marker.pos(), GizmoStyle.stroke(marker.color(), 2)).setAlwaysOnTop();
        }
    }

    private static void drawLayout(ClientLevel level) {
        int length = (int) Wanderlust.number("build-length"), width = (int) Wanderlust.number("build-width");
        String shape = Wanderlust.text("build-shape").trim().toLowerCase(Locale.ROOT);
        if (shape.equals("line")) width = 1;
        boolean perimeter = !shape.equals("floor");
        var style = GizmoStyle.stroke(0xff72ffca, 1.7f);
        Direction right = forward.getClockWise();
        for (int l = 0; l < length; l++) {
            for (int w = 0; w < width; w++) {
                if (perimeter && l > 0 && l < length - 1 && w > 0 && w < width - 1) continue;
                BlockPos pos = origin.relative(forward, l).relative(right, w);
                if (level.isInsideBuildHeight(pos) && level.isLoaded(pos) && level.getBlockState(pos).canBeReplaced()) {
                    Gizmos.cuboid(pos, style).setAlwaysOnTop();
                }
            }
        }
    }
}
