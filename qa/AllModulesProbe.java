package qa;
import java.util.*;
import java.nio.file.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.world.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

public final class AllModulesProbe implements ClientModInitializer {
 boolean loading,playing;int index=-1,age,failures;Object[] modules;Class cls;String current;long began;float oldFly,oldStep;net.minecraft.client.player.ClientInput oldInput;
 List<String> results=new ArrayList<>(List.of("module,runtime_status,enabled_ticks,world_present,player_alive,details"));
 void enable(Object value,boolean enabled)throws Exception{UpdateProbe.call("Settings","setEnabled",new Class[]{cls,boolean.class},value,enabled);}
 String id(Object value)throws Exception{var f=cls.getDeclaredField("id");f.setAccessible(true);return (String)f.get(value);}
 public void onInitializeClient(){ClientTickEvents.END_CLIENT_TICK.register(mc->{try{run(mc);}catch(Throwable e){failures++;e.printStackTrace();System.out.println("ALL FAILED "+current);mc.stop();}});}
 void run(Minecraft mc)throws Exception{
  if(!loading&&mc.isGameLoadFinished()&&mc.gui.overlay()==null){loading=true;org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().handle());String name="AllModules-"+System.currentTimeMillis();mc.createWorldOpenFlows().createFreshLevel(name,new LevelSettings(name,GameType.CREATIVE,new LevelSettings.DifficultySettings(Difficulty.PEACEFUL,false,false),true,WorldDataConfiguration.DEFAULT),new WorldOptions(123,false,false),WorldPresets::createTestWorldDimensions,new TitleScreen());}
  if(!playing){if(mc.player==null||mc.gui.overlay()!=null||mc.gui.screen()!=null)return;playing=true;cls=Class.forName("dev.shabzup.wanderlust.Feature");modules=cls.getEnumConstants();began=System.currentTimeMillis();}
  if(index>=0){age++;if(age<8)return;if(age==8){enable(modules[index],false);return;}if(age<11)return;}

  if(index>=0){
   boolean healthy=mc.level!=null&&mc.player!=null&&mc.player.isAlive();
   boolean cleanup=true;
   if(healthy)cleanup=switch(current){
    case "auto-walk" -> !mc.options.keyUp.isDown();
    case "sneak","auto-sneak" -> !mc.options.keyShift.isDown();
    case "fly-speed" -> Math.abs(mc.player.getAbilities().getFlyingSpeed()-oldFly)<.0001;
    case "step" -> Math.abs(mc.player.maxUpStep()-oldStep)<.001;
    case "freecam" -> mc.player.input==oldInput&&!(boolean)UpdateProbe.call("Movement","scouting");
    case "free-look" -> !(boolean)UpdateProbe.call("Movement","looking");
    case "fake-player" -> mc.level.getEntity(-78432)==null;
    case "fullbright" -> !mc.player.hasEffect(net.minecraft.world.effect.MobEffects.NIGHT_VISION);
    default -> true;
   };
   if(!cleanup){System.out.println("ALL CLEANUP FAIL "+current);healthy=false;}

   if(!healthy)failures++;
   String details="Runtime activation and available cleanup checks only; functional scenario still required";
   results.add(current+","+(healthy?"RUNTIME_PASS":"RUNTIME_FAIL")+",7,"+(mc.level!=null)+","+(mc.player!=null&&mc.player.isAlive())+","+details);
   System.out.println("ALL RESULT "+current+" "+(healthy?"RUNTIME_PASS":"RUNTIME_FAIL"));
  }
  index++;age=0;
  if(index>=modules.length){Files.write(mc.gameDirectory.toPath().resolve("all-modules-runtime.csv"),results);System.out.println("ALL COMPLETE tested="+modules.length+" failures="+failures+" elapsed_ms="+(System.currentTimeMillis()-began));mc.stop();return;}
  mc.gui.setScreen(null);
  if(mc.player!=null){mc.player.getAbilities().flying=true;mc.player.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);mc.options.keyAttack.setDown(false);mc.options.keyUse.setDown(false);mc.options.keyJump.setDown(false);mc.options.keyUp.setDown(false);mc.options.keyDown.setDown(false);mc.options.keyLeft.setDown(false);mc.options.keyRight.setDown(false);mc.options.keyShift.setDown(false);oldFly=mc.player.getAbilities().getFlyingSpeed();oldStep=mc.player.maxUpStep();oldInput=mc.player.input;}
  current=id(modules[index]);System.out.println("ALL START "+current);enable(modules[index],true);
 }
}
