package dev.shabzup.wanderlust;

import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.phys.*;

final class ExtraMovement {
    private static boolean sneaking, clicked;
    private static KeyMapping walkKey;
    private static LocalPlayer flyOwner;
    private static Float oldFlySpeed;
    static void reset(){
        if(walkKey!=null)MenuMovement.hold(walkKey,false);
        if(sneaking)MenuMovement.hold(Minecraft.getInstance().options.keyShift,false);
        restoreFly();MenuMovement.reset();walkKey=null;sneaking=clicked=false;
    }
    private static void restoreFly(){
        if(flyOwner!=null&&oldFlySpeed!=null)flyOwner.getAbilities().setFlyingSpeed(oldFlySpeed);
        flyOwner=null;oldFlySpeed=null;
    }
    static void options() {
        MenuMovement.options();
        Settings.choice("auto-walk","auto-walk.direction","Walking direction","Forward","Forward","Backward","Left","Right");
        for(String id:new String[]{"air-jump","high-jump","long-jump","spider","jump-flight","jumps","wind-charge-jump"})
            Settings.numeric(id,id+".power","Jump / ascent power",id.equals("high-jump")?1:.42,.1,3,.05);
        Settings.numeric("anti-void","anti-void.height","Distance above world bottom",8,1,40,1);
        Settings.numeric("reverse-step","reverse-step.speed","Descent speed",1,.1,3,.1);
        Settings.numeric("trident-boost","trident-boost.factor","Riptide multiplier",1.5,1,5,.1);
        Settings.numeric("auto-wasp","auto-wasp.speed","Follow speed",.8,.1,3,.1);
        Settings.numeric("entity-control","entity-control.speed","Mount speed",.5,.1,3,.1);
        Settings.numeric("boat-control","boat-control.speed","Boat speed",.5,.1,3,.1);
        Settings.numeric("fly-speed","fly-speed.speed","Creative flight speed",.12,.05,1,.01);
        Settings.numeric("velocity","velocity.factor","Knockback multiplier",0,0,1,.1);
        Settings.choice("high-jump","high-jump.mode","Jump power mode","Fixed","Fixed","Multiplier");
        Settings.numeric("high-jump","high-jump.multiplier","Vanilla jump multiplier",2,1,5,.1);
        Settings.numeric("safe-walk","safe-walk.drop","Allowed drop (blocks)",.6,.1,3,.1);
        Settings.numeric("safe-walk","safe-walk.margin","Edge inset (blocks)",.05,0,.25,.01);
    }
    static void tick(Minecraft mc) {
        var p=mc.player; if(p==null)return;
        boolean menu=mc.gui.screen()!=null;
        boolean active=p.isAlive()&&!p.isSpectator()&&!Movement.scouting()&&!mc.isPaused();
        KeyMapping desired=active&&!menu&&on("auto-walk")?switch(Settings.choice("auto-walk.direction")){
            case "Backward"->mc.options.keyDown;case "Left"->mc.options.keyLeft;case "Right"->mc.options.keyRight;default->mc.options.keyUp;}:null;
        if(walkKey!=null&&walkKey!=desired)MenuMovement.hold(walkKey,!menu&&MenuMovement.physical(mc,walkKey));
        walkKey=desired;if(walkKey!=null)MenuMovement.hold(walkKey,true);
        boolean sneak=active&&!menu&&(on("sneak")||on("auto-sneak"));
        if(sneak)MenuMovement.hold(mc.options.keyShift,true);
        else if(sneaking)MenuMovement.hold(mc.options.keyShift,!menu&&MenuMovement.physical(mc,mc.options.keyShift));
        sneaking=sneak;
        if(active&&!menu&&on("fly-speed")){
            if(flyOwner!=p){restoreFly();flyOwner=p;oldFlySpeed=p.getAbilities().getFlyingSpeed();}
            p.getAbilities().setFlyingSpeed((float)Settings.number("fly-speed.speed"));
        }else restoreFly();
        MenuMovement.cleanup(mc);
        if(menu||!active)return;
        if(on("sprint")&&p.getFoodData().getFoodLevel()>6&&!p.isUsingItem())p.setSprinting(true);
        if(on("auto-jump")&&p.onGround()&&moving(mc))p.jumpFromGround();
        if(on("air-jump")&&mc.options.keyJump.isDown()&&p.tickCount%5==0)p.setDeltaMovement(p.getDeltaMovement().with(net.minecraft.core.Direction.Axis.Y,Settings.number("air-jump.power")));
        if(on("anti-void")&&p.getY()<mc.level.getMinY()+Settings.number("anti-void.height"))p.setDeltaMovement(p.getDeltaMovement().x,1,p.getDeltaMovement().z);
        if(on("click-tp")&&mc.options.keyUse.isDown()&&!clicked&&mc.hitResult instanceof BlockHitResult hit){
            Vec3 point=Vec3.atBottomCenterOf(hit.getBlockPos().relative(hit.getDirection()));p.setPos(point);p.connection.send(new net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Pos(point,false,p.horizontalCollision));
        } clicked=mc.options.keyUse.isDown();
    }
    static Vec3 motion(Entity entity,Vec3 velocity) {
        var mc=Minecraft.getInstance();var p=mc.player;if(p==null)return velocity;
        if(entity!=p){
            if(entity.getControllingPassenger()==p&&(on("entity-control")||entity instanceof AbstractBoat&&on("boat-control"))){
                double speed=Settings.number(entity instanceof AbstractBoat&&on("boat-control")?"boat-control.speed":"entity-control.speed");
                Vec3 direction=direction(mc);return new Vec3(direction.x*speed,velocity.y,direction.z*speed);
            } return velocity;
        }
        Vec3 result=velocity;
        if(on("no-slow")&&p.isUsingItem())result=new Vec3(result.x*5,result.y,result.z*5);
        if(on("spider")&&p.horizontalCollision&&moving(mc))result=new Vec3(result.x,Settings.number("spider.power"),result.z);
        if(on("long-jump")&&mc.options.keyJump.isDown()&&moving(mc)){Vec3 d=direction(mc);result=new Vec3(d.x*Settings.number("long-jump.power"),Math.max(result.y,.42),d.z*Settings.number("long-jump.power"));}
        if(on("jump-flight")&&mc.options.keyJump.isDown())result=new Vec3(result.x,Settings.number("jump-flight.power"),result.z);
        if(on("jumps")&&p.onGround()&&moving(mc))result=new Vec3(result.x,Settings.number("jumps.power"),result.z);
        if(on("reverse-step")&&!p.onGround()&&result.y<0&&!p.isInWater())result=new Vec3(result.x,-Settings.number("reverse-step.speed"),result.z);
        if(on("trident-boost")&&p.isAutoSpinAttack()){result=result.scale(Settings.number("trident-boost.factor"));if(result.length()>3)result=result.normalize().scale(3);}
        if(on("slippy")&&p.onGround())result=new Vec3(result.x*1.08,result.y,result.z*1.08);
        if(on("parkour")&&p.onGround()&&moving(mc)&&mc.level.getBlockState(BlockPos.containing(p.position().add(result.x*3,-.6,result.z*3))).isAir())result=new Vec3(result.x,.42,result.z);
        if(on("anchor")&&result.y<0&&mc.level.getBlockState(p.blockPosition().below(2)).isAir())result=new Vec3(0,result.y,0);
        if(on("auto-wasp")&&p.isFallFlying()){
            var target=mc.level.players().stream().filter(e->e!=p&&e.isAlive()).min(java.util.Comparator.comparingDouble(e->e.distanceToSqr(p))).orElse(null);
            if(target!=null){Vec3 delta=target.position().add(0,2,0).subtract(p.position());result=delta.length()>Settings.number("auto-wasp.speed")?delta.normalize().scale(Settings.number("auto-wasp.speed")):delta;}
        }
        if(!result.equals(velocity))entity.setDeltaMovement(result);return result;
    }
    private static Vec3 direction(Minecraft mc){double yaw=Math.toRadians(mc.player.getYRot());int forward=(mc.options.keyUp.isDown()?1:0)-(mc.options.keyDown.isDown()?1:0),right=(mc.options.keyRight.isDown()?1:0)-(mc.options.keyLeft.isDown()?1:0);return new Vec3(-Math.sin(yaw)*forward-Math.cos(yaw)*right,0,Math.cos(yaw)*forward-Math.sin(yaw)*right).normalize();}
    private static boolean moving(Minecraft mc){return mc.options.keyUp.isDown()||mc.options.keyDown.isDown()||mc.options.keyLeft.isDown()||mc.options.keyRight.isDown();}
    private static boolean on(String id){return Wanderlust.on(id);}
}
