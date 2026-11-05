package dev.shabzup.wanderlust;

import net.minecraft.client.Minecraft;
import net.minecraft.core.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;

final class ExtraWorld {
    private static Float rain,thunder;
    private static int next;
    private static Boolean oldNoPhysics;
    static void reset(){rain=thunder=null;oldNoPhysics=null;next=0;}
    static void options(){
        Settings.numeric("ambience","ambience.rain","Rain intensity",0,0,1,.1);
        Settings.numeric("ambience","ambience.thunder","Thunder intensity",0,0,1,.1);
        Settings.numeric("time-changer","time-changer.time","Displayed day time",6000,0,24000,100);
        Settings.numeric("timer","timer.factor","Client tick multiplier",1,.5,3,.1);
        Settings.choice("enderman-look","enderman-look.mode","Look mode","Avoid","Avoid","Stare");
        Settings.numeric("auto-nametag","auto-nametag.range","Entity range",4,1,6,.25);
        Settings.numeric("infinity-miner","infinity-miner.delay","Restart interval (ticks)",200,20,1200,20);
        Settings.string("infinity-miner","infinity-miner.block","Baritone block ID","diamond_ore");
    }
    static void tick(Minecraft mc){
        var p=mc.player;if(p==null)return;
        if(on("ambience")){if(rain==null){rain=mc.level.getRainLevel(1);thunder=mc.level.getThunderLevel(1);}mc.level.setRainLevel((float)Settings.number("ambience.rain"));mc.level.setThunderLevel((float)Settings.number("ambience.thunder"));}
        else if(rain!=null){mc.level.setRainLevel(rain);mc.level.setThunderLevel(thunder);rain=thunder=null;}
        if(on("collisions")){if(oldNoPhysics==null)oldNoPhysics=p.noPhysics;p.noPhysics=true;}else if(oldNoPhysics!=null){p.noPhysics=oldNoPhysics;oldNoPhysics=null;}
        if(mc.gui.screen()!=null||p.tickCount<next)return;next=p.tickCount+5;
        if(on("enderman-look")){var target=mc.level.getEntitiesOfClass(EnderMan.class,p.getBoundingBox().inflate(32)).stream().findFirst().orElse(null);if(target!=null){if(Settings.choice("enderman-look.mode").equals("Avoid"))p.setXRot(90);else look(p,target.getEyePosition());}}
        if(on("auto-nametag")){for(LivingEntity e:mc.level.getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(Settings.number("auto-nametag.range")),e->e!=p&&!(e instanceof net.minecraft.world.entity.player.Player)&&!e.hasCustomName())){int slot=ExtraActions.slot(mc,s->s.is(Items.NAME_TAG));if(slot>=0&&p.isWithinEntityInteractionRange(e,0)){int old=p.getInventory().getSelectedSlot();p.getInventory().setSelectedSlot(slot);mc.gameMode.interact(p,e,new EntityHitResult(e),InteractionHand.MAIN_HAND);p.getInventory().setSelectedSlot(old);break;}}}
        if(on("flamethrower")&&mc.hitResult instanceof BlockHitResult hit)ExtraActions.place(mc,hit.getBlockPos().relative(hit.getDirection()),s->s.is(Items.FLINT_AND_STEEL));
        if(on("highway-builder")){ExtraActions.place(mc,p.blockPosition().below(),s->s.getItem() instanceof net.minecraft.world.item.BlockItem);BlockPos ahead=p.blockPosition().relative(p.getDirection());if(!mc.level.getBlockState(ahead.above()).isAir())ExtraActions.mine(mc,ahead.above());}
        if(on("infinity-miner")&&p.tickCount%(int)Settings.number("infinity-miner.delay")==0&&BaritoneBridge.available())BaritoneBridge.command("mine "+Settings.text("infinity-miner.block"));
        if(on("no-ghost-blocks")&&mc.hitResult instanceof BlockHitResult hit)p.connection.send(new net.minecraft.network.protocol.game.ServerboundPlayerActionPacket(net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK,hit.getBlockPos(),hit.getDirection()));
    }
    static void look(net.minecraft.world.entity.player.Player player,Vec3 point){Vec3 delta=point.subtract(player.getEyePosition());player.setYRot((float)Math.toDegrees(Math.atan2(-delta.x,delta.z)));player.setXRot((float)-Math.toDegrees(Math.atan2(delta.y,delta.horizontalDistance())));}
    private static boolean on(String id){return Wanderlust.on(id);}
}
