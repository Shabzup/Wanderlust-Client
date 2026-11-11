package qa;

import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.phys.Vec3;

public final class MovementParityProbe extends CoreModulesProbe {
    double peak, boostedPeak;
    int settleWait;
    void value(String key,String value)throws Exception{UpdateProbe.call("Settings","put",new Class[]{String.class,String.class},key,value);}
    void platform(Minecraft mc,boolean slab){
        server(mc,p->{
            int x=slab?200:120;
            p.level().getChunkAt(new BlockPos(x,150,120));
            if(slab)p.level().setBlockAndUpdate(new BlockPos(x,150,120),Blocks.STONE_SLAB.defaultBlockState());
            else for(int dx=-3;dx<=3;dx++)for(int dz=-3;dz<=3;dz++)
                p.level().setBlockAndUpdate(new BlockPos(x+dx,150,120+dz),Blocks.STONE.defaultBlockState());
            p.teleportTo(p.level(),x+.5,slab?150.5:151,120.5,Set.of(),0,0,true);
            p.setDeltaMovement(Vec3.ZERO);p.resetFallDistance();p.setHealth(20);
        });
    }
    @Override void check(boolean ok,String text){if(!ok)failures++;System.out.println("MOVEMENT "+(ok?"PASS ":"FAIL ")+text);}
    @Override void run(Minecraft mc)throws Exception{
        if(!loading&&mc.isGameLoadFinished()&&mc.gui.overlay()==null){
            loading=true;org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().handle());String name="MovementParity-"+System.currentTimeMillis();
            mc.createWorldOpenFlows().createFreshLevel(name,new LevelSettings(name,GameType.SURVIVAL,
                new LevelSettings.DifficultySettings(Difficulty.PEACEFUL,false,false),true,WorldDataConfiguration.DEFAULT),
                new WorldOptions(123,false,false),WorldPresets::createTestWorldDimensions,new TitleScreen());
        }
        if(mc.player==null||mc.gui.overlay()!=null||mc.gui.screen()!=null)return;tick++;
        var p=mc.player;
        if(tick==30&&(!p.onGround()||Math.abs(p.getY()-151)>.02)){
            if(++settleWait<=200){tick--;return;}
            check(false,"jump fixture failed to settle within 200 ticks");mc.stop();return;
        }
        if(tick==5)platform(mc,false);
        if(tick==30){check(p.onGround(),"jump fixture grounded");enable("HIGH_JUMP",true);peak=p.getY();mc.options.keyJump.setDown(true);}
        if(tick==32)mc.options.keyJump.setDown(false);
        if(tick>=30&&tick<=75)peak=Math.max(peak,p.getY());
        if(tick==75){
            check(peak>155&&p.onGround(),"HighJump follows raised arc and lands in survival");boostedPeak=peak;
            enable("HIGH_JUMP",false);peak=p.getY();mc.options.keyJump.setDown(true);
        }
        if(tick==77)mc.options.keyJump.setDown(false);
        if(tick>=76&&tick<=105)peak=Math.max(peak,p.getY());
        if(tick==105){
            check(peak>152&&peak<153&&boostedPeak-peak>2,"disabled HighJump restores vanilla trajectory");
            value("high-jump.mode","Multiplier");value("high-jump.multiplier","2");enable("HIGH_JUMP",true);
            peak=p.getY();mc.options.keyJump.setDown(true);
        }
        if(tick==107)mc.options.keyJump.setDown(false);
        if(tick>=106&&tick<=145)peak=Math.max(peak,p.getY());
        if(tick==145){
            check(peak>154&&peak<155.8,"HighJump multiplier changes the actual jump impulse");enable("HIGH_JUMP",false);
            platform(mc,true);
        }
        if(tick==175){
            check(p.onGround()&&Math.abs(p.getY()-150.5)<.02,"SafeWalk fixture standing on bottom slab");
            enable("SAFE_WALK",true);mc.options.keyUp.setDown(true);
        }
        if(tick==215){
            check(p.onGround()&&p.getY()>150.4&&p.getZ()>120.9&&p.getZ()<121.3,"SafeWalk reaches slab edge without falling");
            mc.options.keyUp.setDown(false);platform(mc,true);
        }
        if(tick==240){enable("SPEED",true);mc.options.keyUp.setDown(true);mc.options.keyLeft.setDown(true);}
        if(tick==280){
            check(p.onGround()&&p.getY()>150.4&&p.getX()<201.3&&p.getZ()<121.3,"SafeWalk clips diagonal Speed movement at slab corner");
            enable("SPEED",false);enable("SAFE_WALK",false);mc.options.keyLeft.setDown(false);
        }
        if(tick==300){
            check(p.getY()<149,"disabled SafeWalk allows walking off the same slab");mc.options.keyUp.setDown(false);
            platform(mc,false);
            server(mc,player->{
                for(int x=117;x<=123;x++)for(int z=120;z<=140;z++)
                    player.level().setBlockAndUpdate(new BlockPos(x,150,z),Blocks.STONE.defaultBlockState());
                for(int x=117;x<=123;x++)player.level().setBlockAndUpdate(new BlockPos(x,151,122),Blocks.STONE.defaultBlockState());
            });
        }
        if(tick==330)mc.options.keyUp.setDown(true);
        if(tick==360){
            check(p.getZ()<121.8&&p.getY()<151.1,"disabled Step stops at a full-block obstacle");
            enable("STEP",true);peak=p.getY();
        }
        if(tick>=360&&tick<=390)peak=Math.max(peak,p.getY());
        if(tick==390){
            check(p.getZ()>122.5&&peak>=151.99,"Step walks over a full block without jumping");
            enable("STEP",false);mc.options.keyUp.setDown(false);
            System.out.println("MOVEMENT COMPLETE failures="+failures);mc.stop();
        }
    }
}
