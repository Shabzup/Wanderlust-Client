package dev.shabzup.wanderlust;

import dev.shabzup.wanderlust.mixin.AttackInvoker;
import java.util.Comparator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public final class Combat {
    private static int nextClick;
    private static int nextAura;
    private static LocalPlayer archer;

    static void options() {
        Settings.numeric("kill-aura", "aura-range", "Reach (blocks)", 3, 1, 6, .25);
        Settings.numeric("auto-clicker", "click-interval", "Ticks per click", 3, 1, 40, 1);
        Settings.numeric("bow-aimbot", "bow-range", "Target range (blocks)", 36, 4, 96, 4);
        Settings.numeric("bow-spam", "bow-draw", "Draw time (ticks)", 6, 3, 20, 1);
        Explosives.options();
    }

    static void reset(Minecraft mc) {
        archer = null;
        nextClick = 0;
        nextAura = 0;
        Explosives.reset();
    }

    public static void equipTotem(Minecraft mc) {
        LocalPlayer p = mc.player;
        if (!Wanderlust.on("auto-totem") || p == null || mc.gameMode == null || !p.isAlive()
            || p.isSpectator() || p.containerMenu != p.inventoryMenu || !p.inventoryMenu.getCarried().isEmpty()
            || p.getHealth() + p.getAbsorptionAmount() > Wanderlust.number("totem-health")
            || p.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) return;
        for (int i = 0; i < 36; i++) {
            if (!p.getInventory().getItem(i).is(Items.TOTEM_OF_UNDYING)) continue;
            int menuSlot = i < 9 ? i + 36 : i;
            mc.gameMode.handleContainerInput(p.inventoryMenu.containerId, menuSlot, 40, ContainerInput.SWAP, p);
            return;
        }
    }

    public static boolean holdingBow(Minecraft mc) {
        return archer != null && mc.player == archer && archer.isAlive() && archer.getUseItem().is(Items.BOW)
            && Wanderlust.on("bow-spam") && !Wanderlust.on("freecam") && mc.gui.screen() == null;
    }

    public static void critical(Player player, Entity target) {
        if (!Wanderlust.on("criticals") || !(player instanceof LocalPlayer p) || !(target instanceof LivingEntity)
            || Settings.toggle("criticals-players-only") && !(target instanceof Player)
            || !p.onGround() || p.isPassenger() || p.isInWater() || p.onClimbable() || p.isSprinting()
            || p.hasEffect(MobEffects.BLINDNESS) || p.getAttackStrengthScale(.5f) <= .9f) return;
        // Supply a small rise and descent before vanilla transmits the attack.
        p.connection.send(new ServerboundMovePlayerPacket.Pos(p.getX(), p.getY() + .0625, p.getZ(), false, p.horizontalCollision));
        p.connection.send(new ServerboundMovePlayerPacket.Pos(p.position(), false, p.horizontalCollision));
    }

    static boolean validTarget(LocalPlayer p, LivingEntity e) {
        return e != p && e.isAlive() && !e.isSpectator() && !e.isInvulnerable() && !p.isAlliedTo(e)
            && !(e instanceof Player other && (other.isCreative() || !p.canHarmPlayer(other))) && e.isPickable();
    }

    private static LivingEntity target(Minecraft mc, double range, boolean melee, String module) {
        var p = mc.player;
        return mc.level.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(range), e ->
            validTarget(p, e) && e.getBoundingBox().distanceToSqr(p.getEyePosition()) <= range * range
            && (!melee || p.isWithinAttackRange(p.getMainHandItem(), e.getBoundingBox(), 0))
            && (Settings.toggle(module + "-through-walls") || p.hasLineOfSight(e))
            && acceptedTarget(e, module)).stream().min(targetOrder(module, p)).orElse(null);
    }

    private static Entity meleeTarget(Minecraft mc) {
        var p = mc.player;
        double range = Wanderlust.number("aura-range");
        Comparator<Entity> priority = Comparator.comparingDouble(e -> e.distanceToSqr(p));
        if (Settings.choice("aura-priority").equals("Lowest health")) priority = Comparator
            .comparingDouble((Entity e) -> e instanceof LivingEntity living ? living.getHealth() : Double.MAX_VALUE)
            .thenComparingDouble(e -> e.distanceToSqr(p));
        var candidates=java.util.stream.Stream.concat(mc.level.getEntitiesOfClass(Entity.class,p.getBoundingBox().inflate(range)).stream(),mc.level.dragonParts().stream().map(e->(Entity)e));
        return candidates.filter(e -> e != p && e.isAlive()
            && e.isPickable() && e.isAttackable() && EntityTargets.includes(e) && !p.isAlliedTo(e)
            && (!(e instanceof LivingEntity living) || validTarget(p,living))
            && e.getBoundingBox().distanceToSqr(p.getEyePosition()) <= range*range
            && p.isWithinAttackRange(p.getMainHandItem(),e.getBoundingBox(),0)
            && (Settings.toggle("aura-through-walls") || p.hasLineOfSight(e)))
            .min(priority).orElse(null);
    }

    private static boolean acceptedTarget(LivingEntity entity, String module) {
        boolean player = entity instanceof Player && Settings.toggle(module + "-players");
        boolean hostile = entity instanceof Monster && Settings.toggle(module + "-hostiles");
        boolean passive = entity instanceof Animal && Settings.toggle(module + "-passives");
        boolean neutral = entity instanceof NeutralMob && Settings.toggle(module + "-neutrals");
        return player || hostile || passive || neutral;
    }

    private static Comparator<LivingEntity> targetOrder(String module, LocalPlayer player) {
        if (module.equals("aura") && Settings.choice("aura-priority").equals("Lowest health")) {
            return Comparator.comparingDouble(LivingEntity::getHealth).thenComparingDouble(e -> e.distanceToSqr(player));
        }
        return Comparator.comparingDouble(e -> e.distanceToSqr(player));
    }

    static void tick(Minecraft mc) {
        var p = mc.player;
        if (mc.gui.screen() != null || !p.isAlive() || p.isSpectator() || mc.isPaused() || Wanderlust.on("freecam")) return;
        boolean drawing = p.getUseItem().is(Items.BOW);
        if(drawing&&archer==p&&!Wanderlust.on("bow-spam")){
            archer=null;
            mc.gameMode.releaseUsingItem(p);
            drawing=false;
        }
        if (!drawing) archer = null;
        if (Wanderlust.on("bow-spam") && !p.isUsingItem()) {
            InteractionHand hand = p.getMainHandItem().is(Items.BOW) ? InteractionHand.MAIN_HAND
                : p.getOffhandItem().is(Items.BOW) ? InteractionHand.OFF_HAND : null;
            boolean hasTarget = !Settings.toggle("bow-require-target")
                || target(mc, Wanderlust.number("bowspam-range"), false, "bowspam") != null;
            if (hand != null && hasTarget) {
                mc.gameMode.useItem(p, hand);
                drawing = p.getUseItem().is(Items.BOW);
                if (drawing) archer = p;
            }
        }
        if (drawing) {
            if (Wanderlust.on("bow-aimbot")) aim(mc);
            if (Wanderlust.on("bow-spam") && p.getTicksUsingItem() >= Wanderlust.number("bow-draw")) {
                archer = null;
                p.connection.send(new ServerboundMovePlayerPacket.Rot(p.getYRot(), p.getXRot(), p.onGround(), p.horizontalCollision));
                mc.gameMode.releaseUsingItem(p);
            }
            return;
        }
        if (p.isUsingItem() || Explosives.tick(mc)) return;
        if (Wanderlust.on("kill-aura") && p.tickCount >= nextAura && p.getAttackStrengthScale(.5f) >= .99f) {
            Entity e = meleeTarget(mc);
            if (e != null) {
                mc.gameMode.attack(p, e);
                p.swing(InteractionHand.MAIN_HAND);
                nextAura = p.tickCount + (int)Wanderlust.number("aura-delay");
                return;
            }
        }
        if (Wanderlust.on("auto-clicker") && p.tickCount >= nextClick
            && (!Settings.toggle("click-hold-only") || mc.options.keyAttack.isDown())) {
            ((AttackInvoker) mc).wanderlust$attack();
            nextClick = p.tickCount + (int)Wanderlust.number("click-interval");
        }
    }

    private static void aim(Minecraft mc) {
        LocalPlayer p = mc.player;
        LivingEntity e = target(mc, Wanderlust.number("bow-range"), false, "bow");
        if (e == null) return;
        double speed = 3 * BowItem.getPowerForTime(Math.max(1, p.getTicksUsingItem()));
        Vec3 center = e.getBoundingBox().getCenter();
        if (Settings.toggle("bow-aim-head")) center = center.add(0, e.getBbHeight() * .22, 0);
        Vec3 delta = center.subtract(p.getEyePosition());
        double flightTime = Math.min(30, delta.horizontalDistance() / speed);
        if (Settings.toggle("bow-lead-target")) delta = delta.add(e.getDeltaMovement().scale(flightTime));
        double horizontal = delta.horizontalDistance();
        if (horizontal < .01) return;
        double squared = speed * speed;
        double discriminant = squared * squared - .05 * (.05 * horizontal * horizontal + 2 * delta.y * squared);
        double angle = discriminant > 0 ? Math.atan((squared - Math.sqrt(discriminant)) / (.05 * horizontal))
            : Math.atan2(delta.y, horizontal);
        p.setYRot((float) Math.toDegrees(Math.atan2(-delta.x, delta.z)));
        p.setXRot((float) -Math.toDegrees(angle));
    }
}
