package qa;

import com.mojang.authlib.GameProfile;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.phys.AABB;

final class CombatProbe {
    static final int targetId = -20261103;
    static final AABB testBox = new AABB(-12,100,-12,20,135,12);
    static final Set<UUID> arrows = new HashSet<>();
    static volatile boolean sawCrystal, brokeCrystal, sawAnchor, sawCharge, detonated;
    static BlockPos chargedAt; static final Set<UUID> crystalsSeen = new HashSet<>();

    static void observeCrystals(){
        net.fabricmc.fabric.api.event.player.UseBlockCallback.EVENT.register((player,world,hand,hit)->{
            if(!world.isClientSide())System.out.println("SERVER USE "+player.getItemInHand(hand)+" pos "+player.position()+" hit "+hit.getBlockPos()+" block "+world.getBlockState(hit.getBlockPos())+" occupied "+world.getEntities(null,new AABB(hit.getBlockPos().above()).expandTowards(0,1,0)).size());
            if(!world.isClientSide()&&world.getBlockState(hit.getBlockPos()).is(Blocks.RESPAWN_ANCHOR)){
                sawAnchor=true;
                if(world.getBlockState(hit.getBlockPos()).getValue(RespawnAnchorBlock.CHARGE)>0){sawCharge=true;chargedAt=hit.getBlockPos().immutable();}
            }
            return net.minecraft.world.InteractionResult.PASS;
        });
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents.ENTITY_LOAD.register((entity,level)->{
            if(entity instanceof EndCrystal&&testBox.contains(entity.position())){sawCrystal=true;synchronized(crystalsSeen){crystalsSeen.add(entity.getUUID());}}
        });
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents.ENTITY_UNLOAD.register((entity,level)->{
            if(entity instanceof EndCrystal&&testBox.contains(entity.position())&&entity.getRemovalReason()==Entity.RemovalReason.KILLED)brokeCrystal=true;
        });
    }

    static void tick(Probe probe, Minecraft mc, int t) throws Exception {
        var server = mc.getSingleplayerServer();
        var id = mc.player.getUUID();
        if (t == 0) {
            server.submit(() -> {
                var p = server.getPlayerList().getPlayer(id);
                var level = p.level();
                p.setGameMode(GameType.CREATIVE);
                for (BlockPos pos : BlockPos.betweenClosed(-4,109,-5,6,109,5)) {
                    level.setBlockAndUpdate(pos, Blocks.OBSIDIAN.defaultBlockState());
                }
                for (BlockPos pos : BlockPos.betweenClosed(-4,110,-5,6,114,5)) {
                    level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                }
                p.teleportTo(.5,110,.5);
                p.getAbilities().flying = true;
                p.onUpdateAbilities();
                p.getInventory().setItem(0,new ItemStack(Items.BOW));
                p.getInventory().setItem(1,new ItemStack(Items.END_CRYSTAL,16));
                p.getInventory().setItem(2,new ItemStack(Items.RESPAWN_ANCHOR,16));
                p.getInventory().setItem(3,new ItemStack(Items.GLOWSTONE,16));
                p.getInventory().setItem(10,new ItemStack(Items.ARROW,64));
                p.getInventory().setSelectedSlot(0);
            }).join();
        }
        if (t == 18) {
            var p = mc.player;
            p.getInventory().setSelectedSlot(0);
            var dummy = new RemotePlayer(mc.level,new GameProfile(UUID.fromString("00000000-0000-4000-8000-000000002603"),"TargetFixture")) {
                @Override public GameType gameMode() { return GameType.SURVIVAL; }
            };
            dummy.setId(targetId);
            dummy.snapTo(3.6,110,.5);
            dummy.setNoGravity(true);
            mc.level.addEntity(dummy);
            probe.check(mc.level.players().contains(dummy),"client target fixture added");
            probe.enable("BOW_AIMBOT",true);
            probe.enable("BOW_SPAM",true);
        }
        if (t >= 20 && t <= 180) {
            server.submit(() -> {
                var p = server.getPlayerList().getPlayer(id);
                ServerLevel level = p.level();
                if (t < 61) {
                    for (var arrow : level.getEntitiesOfClass(AbstractArrow.class, testBox)) arrows.add(arrow.getUUID());
                } else if (t < 115) {
                    var current = level.getEntitiesOfClass(EndCrystal.class,testBox); boolean any = !current.isEmpty(); for (UUID uuid : crystalsSeen) if(level.getEntity(uuid)==null) brokeCrystal=true; for(var crystal:current) crystalsSeen.add(crystal.getUUID());
                    if (!any && sawCrystal) brokeCrystal = true;
                    sawCrystal |= any;
                } else {
                    if (chargedAt != null && !level.getBlockState(chargedAt).is(Blocks.RESPAWN_ANCHOR)) detonated = true;
                    for (BlockPos pos : BlockPos.betweenClosed(-6,109,-6,6,112,6)) {
                        var state = level.getBlockState(pos);
                        if (!state.is(Blocks.RESPAWN_ANCHOR)) continue;
                        sawAnchor = true;
                        if (state.getValue(RespawnAnchorBlock.CHARGE) > 0) {
                            sawCharge = true;
                            chargedAt = pos.immutable();
                        }
                    }
                }
            }).join();
        }
        if (t == 60) {
            probe.check(arrows.size() >= 2, "BowSpam fired multiple arrows on the integrated server");
            probe.check(Math.abs(net.minecraft.util.Mth.wrapDegrees(mc.player.getYRot()+90)) < 20,
                "BowAimbot aimed toward the player fixture");
            probe.enable("BOW_SPAM",false);
            probe.enable("BOW_AIMBOT",false);
            probe.enable("CRYSTAL_AURA",true);
        }
        if(t==80){
            var crystalKind=Class.forName("dev.shabzup.wanderlust.Explosives$Kind").getEnumConstants()[0];
            double score=(double)UpdateProbe.call("Explosives","score",new Class[]{Minecraft.class,crystalKind.getClass(),net.minecraft.world.phys.Vec3.class,net.minecraft.world.entity.player.Player.class},mc,crystalKind,new net.minecraft.world.phys.Vec3(2.5,110,.5),(net.minecraft.world.entity.player.Player)mc.level.getEntity(targetId));
            Object planned=UpdateProbe.call("Explosives","blockAction",new Class[]{Minecraft.class,crystalKind.getClass(),net.minecraft.world.entity.player.Player.class,BlockPos.class,net.minecraft.world.phys.Vec3.class,int.class,boolean.class},mc,crystalKind,(net.minecraft.world.entity.player.Player)mc.level.getEntity(targetId),new BlockPos(2,109,0),new net.minecraft.world.phys.Vec3(2.5,110,.5),1,false);
            var decision=Class.forName("dev.shabzup.wanderlust.Explosives").getDeclaredField("lastDecision");decision.setAccessible(true);System.out.println("CRYSTAL DECISION "+decision.get(null));
            System.out.println("CRYSTAL PLAN "+planned+" attack-range "+mc.player.isWithinAttackRange(mc.player.getMainHandItem(),new AABB(1.5,110,-.5,3.5,112,1.5),0)+" obstacles "+mc.level.getEntities(null,new AABB(new BlockPos(2,110,0)).expandTowards(0,1,0)).size());
            System.out.println("CRYSTAL DEBUG using="+mc.player.isUsingItem()+" slot="+mc.player.getInventory().getItem(1)+" pos="+mc.player.position()+" creative="+mc.player.isCreative()+" score="+score);
        }
        if (t == 114) {
            System.out.println("CRYSTAL OBSERVED placed="+sawCrystal+" broken="+brokeCrystal);
            probe.check(sawCrystal && brokeCrystal, "CrystalAura placed and broke an end crystal on the integrated server");
            probe.enable("CRYSTAL_AURA",false);
            // The crystal scenario can knock the player beyond this platform; isolate the next module fixture.
            server.submit(()->{
                var p=server.getPlayerList().getPlayer(id);
                for(var crystal:p.level().getEntitiesOfClass(EndCrystal.class,testBox))crystal.discard();
                p.teleportTo(.5,110,.5);p.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
                p.getAbilities().flying=true;p.onUpdateAbilities();
            }).join();
        }
        if(t==125){
            probe.check(mc.player.position().distanceTo(new net.minecraft.world.phys.Vec3(.5,110,.5))<.25,
                "AnchorAura fixture resets knockback from the preceding crystal scenario");
            probe.enable("ANCHOR_AURA",true);
        }
        if (t == 180) {
            var decision=Class.forName("dev.shabzup.wanderlust.Explosives").getDeclaredField("lastDecision");decision.setAccessible(true);
            System.out.println("ANCHOR OBSERVED placed="+sawAnchor+" charged="+sawCharge+" detonated="+detonated+" player="+mc.player.position()+" target="+mc.level.getEntity(targetId)+" decision="+decision.get(null));
            probe.check(sawAnchor && sawCharge && detonated,
                "AnchorAura placed, charged and detonated an Overworld anchor on the integrated server");
            probe.enable("ANCHOR_AURA",false);
            mc.level.removeEntity(targetId, Entity.RemovalReason.DISCARDED);
        }
    }
}

