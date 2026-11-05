package qa;

import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.ClientInput;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

final class MotionProbe {
    private static ClientInput input;
    private static Vec3 body;
    private static float yaw, health;

    private static void mouseTurn(Minecraft mc) throws Exception {
        var dx = MouseHandler.class.getDeclaredField("accumulatedDX");
        var turn = MouseHandler.class.getDeclaredMethod("turnPlayer", double.class);
        dx.setAccessible(true);
        turn.setAccessible(true);
        double previous = dx.getDouble(mc.mouseHandler);
        try {
            dx.setDouble(mc.mouseHandler, 125);
            turn.invoke(mc.mouseHandler, 0.05);
        } finally {
            dx.setDouble(mc.mouseHandler, previous);
        }
    }

    static void run(Probe probe, Minecraft mc, int tick) throws Exception {
        if (tick < 510 || tick > 710) return;
        var p = mc.player;
        var server = mc.getSingleplayerServer();
        var id = p.getUUID();
        var cam = mc.gameRenderer.mainCamera();
        switch (tick) {
            case 510 -> {
                mc.gui.setScreen(null);
                server.execute(() -> {
                    var level = server.overworld();
                    var player = server.getPlayerList().getPlayer(id);
                    level.getChunkAt(new BlockPos(120, 150, 120));
                    for (int x = 117; x <= 123; x++) for (int z = 117; z <= 123; z++)
                        level.setBlockAndUpdate(new BlockPos(x, 150, z), Blocks.STONE.defaultBlockState());
                    player.setGameMode(GameType.SURVIVAL);
                    player.getInventory().clearContent();
                    player.removeAllEffects();
                    player.getFoodData().setFoodLevel(12);
                    player.setHealth(player.getMaxHealth());
                    player.teleportTo(level, 120.5, 151, 120.5, Set.of(), 0, 0, true);
                    player.setDeltaMovement(Vec3.ZERO);
                    player.resetFallDistance();
                });
            }
            case 535 -> {
                probe.check(p.onGround() && Math.abs(p.getY() - 151) < .02, "movement fixture standing on platform");
                body = p.position();
                input = p.input;
                yaw = p.getYRot();
                probe.enable("FREECAM", true);
            }
            case 540 -> {
                probe.check(p.input != input, "Freecam suppresses body controls");
                mc.options.keyUp.setDown(true);
                mouseTurn(mc);
            }
            case 555 -> {
                cam.update(mc.getDeltaTracker());
                probe.check(p.position().distanceTo(body) < .1 && p.getYRot() == yaw,
                    "Freecam leaves body position and facing unchanged");
                probe.check(cam.position().distanceTo(p.getEyePosition()) > 2 && Math.abs(cam.yRot() - yaw) > 1,
                    "Freecam moves and rotates the rendered camera");
                mc.options.keyUp.setDown(false);
                probe.enable("FREECAM", false);
            }
            case 562 -> {
                cam.update(mc.getDeltaTracker());
                probe.check(p.input == input && cam.position().distanceTo(p.getEyePosition()) < .15,
                    "disabling Freecam restores original controls and camera");
                probe.enable("FREE_LOOK", true);
            }
            case 566 -> mouseTurn(mc);
            case 574 -> {
                cam.update(mc.getDeltaTracker());
                probe.check(p.getYRot() == yaw && Math.abs(cam.yRot() - yaw) > 1,
                    "FreeLook diverts real mouse rotation from player to camera");
                probe.enable("FREE_LOOK", false);
            }
            case 580 -> {
                cam.update(mc.getDeltaTracker());
                probe.check(Math.abs(cam.yRot() - yaw) < .01, "disabling FreeLook restores player view");
                probe.enable("FLIGHT", true);
                mc.options.keyJump.setDown(true);
            }
            case 605 -> {
                mc.options.keyJump.setDown(false);
                probe.check(p.getY() > 159, "Flight gains altitude in survival");
                health = p.getHealth();
                probe.enable("NO_FALL", true);
                probe.enable("FLIGHT", false);
            }
            case 654 -> {
                probe.check(p.onGround() && Math.abs(p.getY() - 151) < .02 && p.getHealth() >= health,
                    "NoFall prevented damage from a survival fall of more than eight blocks");
                probe.enable("NO_FALL", false);
                server.execute(() -> {
                    var player = server.getPlayerList().getPlayer(id);
                    player.teleportTo(server.overworld(), 120.5, 163, 120.5, Set.of(), 0, 0, true);
                    player.setDeltaMovement(Vec3.ZERO);
                });
            }
            case 700 -> probe.check(p.onGround() && p.getHealth() < health,
                "control fall with NoFall disabled deals damage on the same platform");
            default -> {}
        }
    }
}
