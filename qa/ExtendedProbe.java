package qa;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.world.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

public final class ExtendedProbe extends Probe {
 boolean ready;
 @Override void run(Minecraft mc)throws Exception{
  if(!loading&&mc.isGameLoadFinished()&&mc.gui.overlay()==null){loading=true;CombatProbe.observeCrystals();org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().handle());String name="Extended-"+System.currentTimeMillis();mc.createWorldOpenFlows().createFreshLevel(name,new LevelSettings(name,GameType.CREATIVE,new LevelSettings.DifficultySettings(Difficulty.NORMAL,false,false),true,WorldDataConfiguration.DEFAULT),new WorldOptions(123,false,false),WorldPresets::createTestWorldDimensions,new TitleScreen());}
  if(mc.player==null||mc.gui.overlay()!=null)return;
  if(!ready){if(mc.gui.screen()!=null)return;ready=true;feature=Class.forName("dev.shabzup.wanderlust.Feature");settings=Class.forName("dev.shabzup.wanderlust.Settings");}
  tick++;
  if(tick>=5&&tick<=205)CombatProbe.tick(this,mc,tick-5);
  if(tick==205&&Boolean.getBoolean("qa.combatonly")){System.out.println("COMBAT COMPLETE failures="+failures);mc.stop();return;}
  if(tick>=210)MotionProbe.run(this,mc,tick+300);
  if(tick==425){System.out.println("EXTENDED COMPLETE failures="+failures);mc.stop();}
 }
}
