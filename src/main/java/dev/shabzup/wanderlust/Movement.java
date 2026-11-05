package dev.shabzup.wanderlust;

import dev.shabzup.wanderlust.mixin.GroundFlagAccessor;
import java.util.ArrayDeque;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.phys.Vec3;

public final class Movement {
    private static final ArrayDeque<Packet<?>> delayed = new ArrayDeque<>();
    private static ClientPacketListener blinkConnection;
    private static LocalPlayer scoutBody;
    private static ClientInput savedInput;
    private static final ClientInput idleInput = new ClientInput() {
        @Override public void makeJump() { keyPresses = Input.EMPTY; }
    };
    private static Vec3 cameraPrevious = Vec3.ZERO, cameraPosition = Vec3.ZERO;
    private static boolean looking;
    private static float cameraYaw, cameraPitch;
    private static int blinkTicks;

    private Movement() {}

    static void options() {
        speedOption("flight", "Flight speed", 0.45);
        speedOption("boat-fly", "Boat speed", 0.45);
        speedOption("elytra-fly", "Glide speed", 0.8);
        speedOption("elytra-boost", "Boost speed limit", 1.6);
        speedOption("speed", "Ground / swim speed", 0.38);
        speedOption("fast-climb", "Climb speed", 0.45);
        speedOption("freecam", "Camera speed", 0.6);
        Settings.numeric("step", "step.height", "Step height (blocks)", 1.05, 0.65, 2.5, 0.05);
        Settings.numeric("blink", "blink.ticks", "Maximum hold (ticks)", 60, 5, 200, 5);
    }

    private static void speedOption(String module, String label, double defaultValue) {
        Settings.numeric(module, module + ".speed", label + " (blocks/tick)", defaultValue, 0.05, 4, 0.05);
    }

    static void reset(Minecraft mc) {
        releaseBody();
        looking = false;
        clearBlink();
    }

    static void tick(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null || !player.isAlive()) {
            reset(mc);
            return;
        }
        if (Wanderlust.on("freecam")) {
            if (scoutBody != player) {
                releaseBody();
                scoutBody = player;
                savedInput = player.input;
                player.input = idleInput;
                cameraPosition = player.getEyePosition();
                if (mc.gameMode != null) mc.gameMode.stopDestroyBlock();
            }
        } else releaseBody();
        boolean wantsLook = scouting() || Wanderlust.on("free-look");
        if (wantsLook && !looking) {
            cameraYaw = player.getYRot();
            cameraPitch = player.getXRot();
        }
        looking = wantsLook;
        cameraPrevious = cameraPosition;
        if (scouting() && mc.gui.screen() == null && !mc.isPaused()) {
            Vec3 travel = controls(mc.options, cameraYaw, cameraPitch, false);
            Vec3 next = cameraPosition.add(travel.scale(Wanderlust.number("freecam.speed")));
            double range = Wanderlust.number("freecam.range");
            Vec3 origin = scoutBody.getEyePosition();
            if (next.distanceToSqr(origin) > range * range) next = origin.add(next.subtract(origin).normalize().scale(range));
            cameraPosition = next;
        }
        if (blinkConnection != null && (!Wanderlust.on("blink") || ++blinkTicks >= Wanderlust.number("blink.ticks"))) {
            flushBlink(mc);
        }
    }

    private static void releaseBody() {
        if (scoutBody != null && scoutBody.input == idleInput) scoutBody.input = savedInput;
        scoutBody = null;
        savedInput = null;
    }

    public static boolean scouting() { return scoutBody != null; }
    public static boolean looking() { return looking; }
    public static float yaw() { return cameraYaw; }
    public static float pitch() { return cameraPitch; }
    public static Vec3 position(float partialTick) { return cameraPrevious.lerp(cameraPosition, partialTick); }

    public static boolean turn(double horizontal, double vertical) {
        if (!looking) return false;
        double sensitivity = Settings.number("freelook.sensitivity");
        cameraYaw += (float) (horizontal * 0.15 * sensitivity);
        cameraPitch = Math.clamp(cameraPitch + (float) (vertical * 0.15 * sensitivity), -90, 90);
        return true;
    }

    private static int axis(boolean positive, boolean negative) {
        return (positive ? 1 : 0) - (negative ? 1 : 0);
    }

    private static Vec3 controls(Options keys, float yaw, float pitch, boolean boat) {
        int forward = axis(keys.keyUp.isDown(), keys.keyDown.isDown());
        int right = axis(keys.keyRight.isDown(), keys.keyLeft.isDown());
        int vertical = axis(keys.keyJump.isDown(), (boat ? keys.keySprint : keys.keyShift).isDown());
        double angle = Math.toRadians(yaw), tilt = Math.toRadians(pitch);
        double along = forward * Math.cos(tilt);
        Vec3 vector = new Vec3(-along * Math.sin(angle) - right * Math.cos(angle),
            vertical - forward * Math.sin(tilt), along * Math.cos(angle) - right * Math.sin(angle));
        return vector.lengthSqr() > 1 ? vector.normalize() : vector;
    }

    /** Override only self-propelled travel, leaving collisions and external displacements to vanilla. */
    public static Vec3 motion(Entity entity, MoverType type, Vec3 original) {
        if ((type != MoverType.SELF && type != MoverType.PLAYER) || !entity.level().isClientSide()) return original;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (p == null || !p.isAlive() || scouting() || mc.gui.screen() != null || mc.isPaused()) return original;
        boolean boat = entity instanceof AbstractBoat && entity.getControllingPassenger() == p;
        original = ExtraMovement.motion(entity, original);
        if (!boat && (entity != p || p.isPassenger())) return original;
        Vec3 adjusted;
        if (boat) {
            if (!Wanderlust.on("boat-fly")) return original;
            Vec3 input = controls(mc.options, p.getYRot(), 0, true);
            adjusted = new Vec3(input.x * Wanderlust.number("boat-fly.speed"),
                input.y * Wanderlust.number("boat-fly.vertical"), input.z * Wanderlust.number("boat-fly.speed"));
        } else if (Wanderlust.on("flight")) {
            Vec3 input = controls(mc.options, p.getYRot(), 0, false);
            adjusted = new Vec3(input.x * Wanderlust.number("flight.speed"),
                input.y * Wanderlust.number("flight.vertical"), input.z * Wanderlust.number("flight.speed"));
        } else if (p.isFallFlying() && Wanderlust.on("elytra-fly")) {
            double speed = Wanderlust.number("elytra-fly.speed");
            if (Wanderlust.on("elytra-boost") && mc.options.keyUp.isDown()) speed = Math.max(speed, Wanderlust.number("elytra-boost.speed"));
            adjusted = controls(mc.options, p.getYRot(), p.getXRot(), false).scale(speed);
        } else if (p.isFallFlying() && Wanderlust.on("elytra-boost") && mc.options.keyUp.isDown()) {
            double limit = Wanderlust.number("elytra-boost.speed");
            adjusted = original.add(p.getLookAngle().scale(0.12));
            if (adjusted.length() > limit) adjusted = adjusted.normalize().scale(limit);
        } else if (Wanderlust.on("fast-climb") && p.onClimbable()
            && (mc.options.keyJump.isDown() || mc.options.keyUp.isDown()) && !mc.options.keyShift.isDown()) {
            adjusted = new Vec3(original.x, Math.max(original.y, Wanderlust.number("fast-climb.speed")), original.z);
        } else if (Wanderlust.on("speed") && (p.onGround() || Settings.toggle("speed-water") && p.isInWater())
            && (mc.options.keyUp.isDown() || mc.options.keyDown.isDown() || mc.options.keyLeft.isDown() || mc.options.keyRight.isDown())) {
            Vec3 direction = controls(mc.options, p.getYRot(), 0, false).multiply(1, 0, 1).normalize();
            double speed = Math.max(original.horizontalDistance(), Wanderlust.number("speed.speed"));
            adjusted = new Vec3(direction.x * speed, original.y, direction.z * speed);
        } else if (Wanderlust.on("jesus") && !mc.options.keyShift.isDown()
            && (p.isInWater() && Settings.toggle("jesus-water") || p.isInLava() && Settings.toggle("jesus-lava"))
            && p.level().getFluidState(p.blockPosition().above()).isEmpty()) {
            adjusted = new Vec3(original.x, Math.max(original.y, 0.12), original.z);
        } else if (Wanderlust.on("no-fall") && Settings.choice("no-fall.mode").equals("Velocity")
            && original.y < -1.8 && !p.isFallFlying()) {
            // Vanilla accumulates a packet's descent before processing its ground flag.
            // Keep even a terminal-velocity fall below the damage threshold per packet.
            adjusted = new Vec3(original.x, -1.8, original.z);
        } else return original;
        entity.setDeltaMovement(adjusted);
        return adjusted;
    }

    /** Protocol acknowledgements and keepalives always pass. Server teleports invalidate old positions. */
    public static boolean holdPacket(ClientCommonPacketListenerImpl sender, Packet<?> packet) {
        Minecraft mc = Minecraft.getInstance();
        if (!mc.isSameThread() || sender != mc.getConnection() || mc.player == null) return false;
        if (packet instanceof ServerboundAcceptTeleportationPacket) {
            clearBlink();
            return false;
        }
        if (packet instanceof ServerboundMovePlayerPacket move) {
            if (Wanderlust.on("potion-saver") && mc.player.getDeltaMovement().lengthSqr()<.00001 && !mc.player.isUsingItem()) return true;
            if (Wanderlust.on("anti-hunger") && mc.player.onGround()) ((GroundFlagAccessor)move).wanderlust$setGround(false);
            if (Wanderlust.on("no-fall") && Settings.choice("no-fall.mode").equals("Packet")
                && mc.player.fallDistance > 1 && !mc.player.isFallFlying()
                && !mc.player.isPassenger() && mc.player.getDeltaMovement().y < 0) {
                ((GroundFlagAccessor) move).wanderlust$setGround(true);
            }
        } else if (!(packet instanceof ServerboundMoveVehiclePacket)) return false;
        if (blinkConnection != null && blinkConnection != sender) clearBlink();
        if (!Wanderlust.on("blink") || !mc.player.isAlive()) {
            if (blinkConnection != null) flushBlink(mc);
            return false;
        }
        if (delayed.size() >= (int)Wanderlust.number("blink.max-packets")) flushBlink(mc);
        blinkConnection = mc.getConnection();
        delayed.addLast(packet);
        return true;
    }

    private static void flushBlink(Minecraft mc) {
        if (blinkConnection == mc.getConnection() && blinkConnection != null && blinkConnection.getConnection().isConnected()) {
            // Use the transport directly so replay cannot re-enter the listener's queue hook.
            for (Packet<?> packet : delayed) blinkConnection.getConnection().send(packet);
        }
        clearBlink();
    }

    private static void clearBlink() {
        delayed.clear();
        blinkTicks = 0;
        blinkConnection = null;
    }
}
