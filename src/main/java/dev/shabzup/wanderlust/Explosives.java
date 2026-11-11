package dev.shabzup.wanderlust;

import java.util.Comparator;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BedItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Each action uses the current client world. Placement, charging and detonation
 * are rechecked on later ticks so a rejected action never advances a local plan. */
final class Explosives {
    private static int nextScan;
    static String lastDecision="Idle";
    private record PendingCrystal(BlockPos base,int deadline){}
    private static PendingCrystal pendingCrystal;
    private static final java.util.Map<BlockPos,Integer> rejectedCrystalBases=new java.util.HashMap<>();
    private enum Kind {
        Crystal("crystal-aura", 12), Anchor("anchor-aura", 10), Bed("bed-aura", 10);
        final String id;
        final int diameter;
        Kind(String id, int diameter) { this.id = id; this.diameter = diameter; }
    }

    private record Action(BlockHitResult hit, int slot, EndCrystal crystal, double score, boolean existing) {
        boolean betterThan(Action other) {
            return other == null || (existing != other.existing ? existing : score > other.score);
        }
    }

    static void options() {
        for (Kind kind : Kind.values()) {
            Settings.numeric(kind.id, kind.id + "-min-hit", "Minimum estimated damage (HP)", 4, 1, 30, 1);
            Settings.numeric(kind.id, kind.id + "-self-hit", "Maximum estimated self damage (HP)", 12, 0, 80, 1);
        }
    }

    static void reset() { nextScan = 0;pendingCrystal=null;rejectedCrystalBases.clear(); }

    static boolean tick(Minecraft mc) {
        boolean crystal = Wanderlust.on("crystal-aura");
        boolean anchor = Wanderlust.on("anchor-aura") && mc.level.dimension().equals(Level.OVERWORLD);
        boolean bed = Wanderlust.on("bed-aura") && (mc.level.dimension().equals(Level.NETHER) || mc.level.dimension().equals(Level.END));
        if ((!crystal && !anchor && !bed) || mc.player.tickCount < nextScan) return false;
        nextScan = mc.player.tickCount + 3;
        var p = mc.player;
        rejectedCrystalBases.entrySet().removeIf(entry->entry.getValue()<=p.tickCount);
        if(!crystal)pendingCrystal=null;
        if(pendingCrystal!=null){
            boolean confirmed=!mc.level.getEntitiesOfClass(EndCrystal.class,new AABB(pendingCrystal.base.above()).inflate(.25)).isEmpty();
            if(confirmed)pendingCrystal=null;
            else if(p.tickCount>=pendingCrystal.deadline){
                rejectedCrystalBases.put(pendingCrystal.base,p.tickCount+40);
                pendingCrystal=null;
            }
        }
        double targetRange = Math.max(crystal ? Wanderlust.number("crystal-aura-range") : 0,
            Math.max(anchor ? Wanderlust.number("anchor-aura-range") : 0, bed ? Wanderlust.number("bed-aura-range") : 0));
        Player enemy = mc.level.players().stream().filter(e -> Combat.validTarget(p, e) && e.distanceToSqr(p) < targetRange * targetRange)
            .min(Comparator.comparingDouble(e -> e.distanceToSqr(p))).orElse(null);
        if (enemy == null) {lastDecision="No valid player target";return false;}
        Action best = null;
        if (crystal) {
            for (var e : mc.level.getEntitiesOfClass(EndCrystal.class, p.getBoundingBox().inflate(p.entityInteractionRange() + 1))) {
                if (!e.isAlive() || !p.isWithinAttackRange(p.getMainHandItem(), e.getBoundingBox(), 0) || !p.hasLineOfSight(e)) continue;
                double score = score(mc, Kind.Crystal, e.position(), enemy);
                var a = new Action(null, -1, e, score, true);
                if (score > 0 && a.betterThan(best)) best = a;
            }
        }
        int crystals = crystal ? slot(p, s -> s.is(Items.END_CRYSTAL)) : -1;
        int anchors = anchor ? slot(p, s -> s.is(Items.RESPAWN_ANCHOR)) : -1;
        int glowstone = anchor ? slot(p, s -> s.is(Items.GLOWSTONE)) : -1;
        int detonate = anchor ? slot(p, s -> !s.is(Items.GLOWSTONE)) : -1;
        int beds = bed ? slot(p, s -> s.getItem() instanceof BedItem) : -1;
        // Existing crystals can still be broken with no spare crystal in the hotbar.
        if (anchor || bed || crystals >= 0) {
            int r = (int)Math.ceil(p.blockInteractionRange());
            BlockPos origin = p.blockPosition();
            for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-r, -r, -r), origin.offset(r, r + 1, r))) {
                if (!p.isWithinBlockInteractionRange(pos, 0)) continue;
                var state = mc.level.getBlockState(pos);
                Action a = null;
                if (anchor && state.is(Blocks.RESPAWN_ANCHOR)) {
                    int required = state.getValue(RespawnAnchorBlock.CHARGE) == 0 ? glowstone : detonate;
                    if (required >= 0) a = blockAction(mc, Kind.Anchor, enemy, pos, Vec3.atCenterOf(pos), required, true);
                } else if (bed && state.getBlock() instanceof BedBlock) {
                    BlockPos head = state.getValue(BedBlock.PART) == BedPart.HEAD ? pos : pos.relative(state.getValue(BedBlock.FACING));
                    a = blockAction(mc, Kind.Bed, enemy, pos, Vec3.atCenterOf(head), p.getInventory().getSelectedSlot(), true);
                } else if ((best == null || !best.existing) && !state.canBeReplaced()) {
                    BlockPos air = pos.above();
                    if (!mc.level.getBlockState(air).isAir()) continue;
                    Vec3 crystalBase = Vec3.atBottomCenterOf(air);
                    if (crystals >= 0 && pendingCrystal==null && !rejectedCrystalBases.containsKey(pos) && (state.is(Blocks.OBSIDIAN) || state.is(Blocks.BEDROCK))
                        && p.isWithinAttackRange(p.getMainHandItem(), new AABB(crystalBase.add(-1, 0, -1), crystalBase.add(1, 2, 1)), 0)
                        && mc.level.getEntities(null, new AABB(air).expandTowards(0, 1, 0)).isEmpty()) {
                        a = blockAction(mc, Kind.Crystal, enemy, pos, crystalBase, crystals, false);
                    }
                    if (!state.isFaceSturdy(mc.level, pos, Direction.UP) || !mc.level.isUnobstructed(null,
                        net.minecraft.world.phys.shapes.Shapes.create(new AABB(air)))) {
                        if (a != null && a.betterThan(best)) best = a;
                        continue;
                    }
                    if (anchors >= 0 && glowstone >= 0 && detonate >= 0) {
                        Action b = blockAction(mc, Kind.Anchor, enemy, pos, Vec3.atCenterOf(air), anchors, false);
                        if (b != null && b.betterThan(a)) a = b;
                    }
                    if (beds >= 0) {
                        BlockPos head = air.relative(p.getDirection());
                        if (mc.level.getBlockState(head).isAir() && mc.level.isUnobstructed(null, net.minecraft.world.phys.shapes.Shapes.create(new AABB(head)))) {
                            Action b = blockAction(mc, Kind.Bed, enemy, pos, Vec3.atCenterOf(head), beds, false);
                            if (b != null && b.betterThan(a)) a = b;
                        }
                    }
                }
                if (a != null && a.betterThan(best)) best = a;
            }
        }
        if (best == null) {lastDecision="No usable placement or break candidate";return false;}
        if (best.crystal != null) {
            mc.gameMode.attack(p, best.crystal);
            p.swing(InteractionHand.MAIN_HAND);
        } else {
            int previous = p.getInventory().getSelectedSlot();
            p.getInventory().setSelectedSlot(best.slot);
            boolean placingCrystal=p.getMainHandItem().is(Items.END_CRYSTAL);
            var result = mc.gameMode.useItemOn(p, InteractionHand.MAIN_HAND, best.hit);
            lastDecision="Use slot "+best.slot+" at "+best.hit.getBlockPos()+" result "+result+" score "+best.score;
            if(placingCrystal){
                BlockPos base=best.hit.getBlockPos().immutable();
                if(result.consumesAction())pendingCrystal=new PendingCrystal(base,p.tickCount+8);
                else rejectedCrystalBases.put(base,p.tickCount+40);
            }
            p.getInventory().setSelectedSlot(previous);
            if (result.consumesAction()) p.swing(InteractionHand.MAIN_HAND);
        }
        return true;
    }

    private static Action blockAction(Minecraft mc, Kind kind, Player enemy, BlockPos pos, Vec3 blast, int slot, boolean existing) {
        Vec3 eye = mc.player.getEyePosition();
        Vec3 end = Vec3.atCenterOf(pos).add(0, existing ? -.1 : .499, 0);
        var hit = ray(mc, eye, end);
        if (hit.getType() != HitResult.Type.BLOCK || !hit.getBlockPos().equals(pos) || !existing && hit.getDirection() != Direction.UP) return null;
        double score = score(mc, kind, blast, enemy);
        return score > 0 ? new Action(hit, slot, null, score, existing) : null;
    }

    private static BlockHitResult ray(Minecraft mc, Vec3 from, Vec3 to) {
        return mc.level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player));
    }

    private static double score(Minecraft mc, Kind kind, Vec3 blast, Player enemy) {
        double target = damage(mc, kind, blast, enemy);
        if (target < Wanderlust.number(kind.id + "-min-hit")) return 0;
        var p = mc.player;
        double self = p.isCreative() ? 0 : damage(mc, kind, blast, p);
        if (self > Wanderlust.number(kind.id + "-self-hit") || self >= p.getHealth() + p.getAbsorptionAmount()) return 0;
        return target - self * .15;
    }

    static double crystalDamage(Minecraft mc,Vec3 blast,Player player) { return damage(mc,Kind.Crystal,blast,player); }

    private static double damage(Minecraft mc, Kind kind, Vec3 blast, Player player) {
        double distance = player.position().distanceTo(blast);
        if (distance >= kind.diameter) return 0;
        // Sample feet, torso and head. Rays hitting an exploding bed/anchor
        // itself still count since vanilla removes that block before the blast.
        double exposure = 0;
        BlockPos source = BlockPos.containing(blast);
        for (double h : new double[]{.1, .5, .9}) {
            var hit = ray(mc, player.position().add(0, h * player.getBbHeight(), 0), blast);
            if (hit.getType() == HitResult.Type.MISS || hit.getBlockPos().equals(source)) exposure += 1.0 / 3;
        }
        double impact = (1 - distance / kind.diameter) * exposure;
        float raw = (float)((impact * impact + impact) * 3.5 * kind.diameter + 1);
        return CombatRules.getDamageAfterAbsorb(player, raw, mc.level.damageSources().explosion(null, null),
            player.getArmorValue(), (float)player.getAttributeValue(Attributes.ARMOR_TOUGHNESS));
    }

    private static int slot(Player player, Predicate<ItemStack> accept) {
        var inventory = player.getInventory();
        if (accept.test(inventory.getSelectedItem())) return inventory.getSelectedSlot();
        for (int i = 0; i < 9; i++) if (accept.test(inventory.getItem(i))) return i;
        return -1;
    }
}
