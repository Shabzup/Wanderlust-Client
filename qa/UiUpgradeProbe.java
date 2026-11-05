package qa;
import java.util.*;
import java.lang.reflect.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.input.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.Difficulty;

public final class UiUpgradeProbe implements ClientModInitializer {
 boolean loading;int tick,failures;Screen panel;long holdUntil;
 void check(boolean ok,String text){if(!ok)failures++;System.out.println("UPGRADE "+(ok?"PASS ":"FAIL ")+text);}
 Object field(String name)throws Exception{var f=panel.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(panel);}
 void enable(String name,boolean enabled)throws Exception{Class cls=Class.forName("dev.shabzup.wanderlust.Feature");Object f=Enum.valueOf(cls,name);UpdateProbe.call("Settings","setEnabled",new Class[]{cls,boolean.class},f,enabled);}
 void number(String key,double value)throws Exception{var f=Class.forName("dev.shabzup.wanderlust.Settings").getDeclaredField("options");f.setAccessible(true);for(Object o:(List)f.get(null)){var get=o.getClass().getDeclaredMethod("key");get.setAccessible(true);if(get.invoke(o).equals(key)){UpdateProbe.call("Settings","setNumber",new Class[]{o.getClass(),double.class},o,value);return;}}throw new IllegalArgumentException(key);}
 void layout()throws Exception{var positions=(Map<?,int[]>)field("positions");int w=(int)field("columnWidth");int canvasW=(int)field("workspaceWidth"),canvasH=(int)field("workspaceBottom");var method=panel.getClass().getDeclaredMethod("windowHeight",Class.forName("dev.shabzup.wanderlust.Feature$Group"));method.setAccessible(true);List<Map.Entry<?,int[]>> entries=new ArrayList(positions.entrySet());boolean ok=true;for(int i=0;i<entries.size();i++){int[] a=entries.get(i).getValue();int h=(int)method.invoke(panel,entries.get(i).getKey());if(a[0]<0||a[0]+w>canvasW||a[1]+h>canvasH)ok=false;for(int j=i+1;j<entries.size();j++){int[] b=entries.get(j).getValue();int bh=(int)method.invoke(panel,entries.get(j).getKey());if(a[0]<b[0]+w&&a[0]+w>b[0]&&a[1]<b[1]+bh&&a[1]+h>b[1])ok=false;}}check(ok,"category bounds do not overlap at "+panel.width+"x"+panel.height);}
 void click(double x,double y,int button)throws Exception{float scale=(float)field("viewScale");panel.mouseClicked(new MouseButtonEvent(x*scale,y*scale,new MouseButtonInfo(button,0)),false);}
 void button(String name)throws Exception{Button b=panel.children().stream().filter(w->w instanceof Button q&&q.getMessage().getString().equals(name)).map(w->(Button)w).findFirst().orElseThrow();panel.mouseClicked(new MouseButtonEvent(b.getX()+b.getWidth()/2,b.getY()+10,new MouseButtonInfo(0,0)),false);}
 void shot(Minecraft mc,String name){Screenshot.grab(mc.gameDirectory,name+".png",mc.gameRenderer.mainRenderTarget(),1,c->{});}
 public void onInitializeClient(){ClientTickEvents.END_CLIENT_TICK.register(mc->{try{run(mc);}catch(Throwable e){e.printStackTrace();System.out.println("UPGRADE FAILED");mc.stop();}});}
 void run(Minecraft mc)throws Exception{
  if(!loading&&mc.isGameLoadFinished()&&mc.gui.overlay()==null){loading=true;org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().handle());String name="Upgrade-"+System.currentTimeMillis();mc.createWorldOpenFlows().createFreshLevel(name,new LevelSettings(name,GameType.CREATIVE,new LevelSettings.DifficultySettings(Difficulty.PEACEFUL,false,false),true,WorldDataConfiguration.DEFAULT),new WorldOptions(123,false,false),WorldPresets::createTestWorldDimensions,new TitleScreen());}
  if(mc.player==null||mc.gui.overlay()!=null||System.currentTimeMillis()<holdUntil)return;tick++;
  if(tick==20){mc.getWindow().setGuiScale(2);var c=Class.forName("dev.shabzup.wanderlust.ControlPanel").getDeclaredConstructor();c.setAccessible(true);panel=(Screen)c.newInstance();mc.gui.setScreen(panel);}
  if(tick==38){
   check((double)UpdateProbe.call("Settings","number",new Class[]{String.class},"ui.opacity")==20,"default opacity 20 percent");
   check(Math.abs((float)field("viewScale")-.7f)<.001,"default scale 70 percent");
   shot(mc,"upgrade-main");
   layout();
   var positions=(Map<?,int[]>)field("positions");int[] combat=positions.entrySet().stream().filter(e->e.getKey().toString().equals("Combat")).map(Map.Entry::getValue).findFirst().orElseThrow();
   click(combat[0]+20,combat[1]+36,0);
   check((boolean)UpdateProbe.call("Settings","on",new Class[]{String.class},"kill-aura"),"scaled left-click toggles module");
   click(combat[0]+20,combat[1]+36,1);
   check(panel.children().stream().anyMatch(w->w instanceof Button b&&b.getMessage().getString().equals("Entity list")),"right click exposes entity selector");
   holdUntil=System.currentTimeMillis()+500;
  }
  if(tick==40){shot(mc,"upgrade-aura");button("Entity list");holdUntil=System.currentTimeMillis()+500;} if(tick==42)shot(mc,"upgrade-entities"); if(tick==60)shot(mc,"upgrade-settings"); if(tick==49){check(mc.gui.screen().getClass().getSimpleName().equals("EntitySelectionScreen"),"entity selector opens");
   List<?> entries=(List<?>)UpdateProbe.call("EntityTargets","entries");check(entries.size()>70,"runtime attackable registry list");check(entries.stream().anyMatch(e->e.toString().contains("minecraft:ender_dragon")),"multipart dragon included in target registry");
   ((Button)mc.gui.screen().children().stream().filter(w->w instanceof Button b&&b.getMessage().getString().equals("None")).findFirst().orElseThrow()).onPress(null);
   check((int)UpdateProbe.call("EntityTargets","count")==0,"None button clears entity selection");
   UpdateProbe.call("EntityTargets","set",new Class[]{String.class,boolean.class},"minecraft:zombie",true);
   check((boolean)UpdateProbe.call("EntityTargets","contains",new Class[]{String.class},"minecraft:zombie")&&!(boolean)UpdateProbe.call("EntityTargets","contains",new Class[]{String.class},"minecraft:cow"),"single entity type selection excludes other types");
   mc.gui.screen().onClose();
  }
  if(tick==58){button("‹  Modules");button("UI Settings");check((boolean)field("uiSettings"),"UI settings tab opens");holdUntil=System.currentTimeMillis()+500;}
  if(tick==61){int x=(int)field("dialogX"),w=(int)field("dialogW"),y=(int)field("optionStart");panel.mouseClicked(new MouseButtonEvent(x+w-20,y+25,new MouseButtonInfo(0,0)),false);float scale=(float)field("viewScale");panel.mouseReleased(new MouseButtonEvent(x+w-20,y+25,new MouseButtonInfo(0,0)));check((double)UpdateProbe.call("Settings","number",new Class[]{String.class},"ui.opacity")==100,"opacity slider updates live setting");number("ui.opacity",20);panel.mouseClicked(new MouseButtonEvent(x+20+(w-40)/6.0,y+(int)field("optionPitch")+25,new MouseButtonInfo(0,0)),false);panel.mouseReleased(new MouseButtonEvent(0,0,new MouseButtonInfo(0,0)));check(Math.abs((float)field("viewScale")-.5f)<.01,"size slider changes scale on release");number("ui.scale",70);panel.resize(panel.width,panel.height);}
  if(tick==65){mc.gui.setScreen(null);enable("KILL_AURA",false);Class cls=Class.forName("dev.shabzup.wanderlust.Feature");check(cls.getEnumConstants().length==225,"225 module entries registered");var binds=Class.forName("dev.shabzup.wanderlust.Wanderlust").getDeclaredField("binds");binds.setAccessible(true);check(((Map)binds.get(null)).size()==225,"every module has a key mapping");enable("FAKE_PLAYER",true);}
  if(tick==70){check(mc.level.getEntity(-78432)!=null,"FakePlayer creates local dummy");enable("FAKE_PLAYER",false);enable("FULLBRIGHT",true);}
  if(tick==75){check(mc.player.hasEffect(net.minecraft.world.effect.MobEffects.NIGHT_VISION),"Fullbright applies local night vision");check(mc.level.getEntity(-78432)==null||mc.level.getEntity(-78432).isRemoved(),"FakePlayer removed when disabled");enable("FULLBRIGHT",false);enable("HIGH_JUMP",true);mc.options.keyJump.setDown(true);mc.player.setOnGround(true);var v=(net.minecraft.world.phys.Vec3)UpdateProbe.call("ExtraMovement","motion",new Class[]{net.minecraft.world.entity.Entity.class,net.minecraft.world.phys.Vec3.class},mc.player,new net.minecraft.world.phys.Vec3(0,.42,0));check(v.y>.9,"HighJump uses configured jump power");enable("HIGH_JUMP",false);mc.options.keyJump.setDown(false);}
  if(tick==80){check(!mc.player.hasEffect(net.minecraft.world.effect.MobEffects.NIGHT_VISION),"Fullbright restores prior visual state");mc.player.getInventory().setItem(8,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_PICKAXE));mc.player.getInventory().setSelectedSlot(0);UpdateProbe.call("ExtraActions","tool",new Class[]{Minecraft.class,net.minecraft.world.level.block.state.BlockState.class},mc,net.minecraft.world.level.block.Blocks.OBSIDIAN.defaultBlockState());check(mc.player.getInventory().getSelectedSlot()==8,"AutoTool chooses faster hotbar tool");var server=mc.getSingleplayerServer();var id=mc.player.getUUID();server.submit(()->{var p=server.getPlayerList().getPlayer(id);p.setGameMode(GameType.CREATIVE);p.getAbilities().flying=true;p.onUpdateAbilities();p.connection.teleport(.5,81,.5,0,0);p.getInventory().setItem(0,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.COBBLESTONE,64));p.getInventory().setSelectedSlot(0);server.overworld().setBlockAndUpdate(new net.minecraft.core.BlockPos(0,79,0),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());server.overworld().setBlockAndUpdate(new net.minecraft.core.BlockPos(0,80,0),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());}).join();}
  if(tick==93){mc.player.getInventory().setSelectedSlot(0);mc.player.getAbilities().flying=true;enable("SCAFFOLD",true);}
  if(tick==115){boolean placed=mc.getSingleplayerServer().submit(()->mc.getSingleplayerServer().overworld().getBlockState(new net.minecraft.core.BlockPos(0,80,0)).is(net.minecraft.world.level.block.Blocks.COBBLESTONE)).join();check(placed,"Scaffold places block confirmed by integrated server");enable("SCAFFOLD",false);UpdateProbe.call("Settings","put",new Class[]{String.class,String.class},"packet-canceller.classes","ServerboundSwingPacket");enable("PACKET_CANCELLER",true);check((boolean)UpdateProbe.call("ExtraMisc","outgoing",new Class[]{net.minecraft.network.protocol.Packet.class},new net.minecraft.network.protocol.game.ServerboundSwingPacket(net.minecraft.world.InteractionHand.MAIN_HAND)),"packet filter matches configured type");enable("PACKET_CANCELLER",false);UpdateProbe.call("Settings","put",new Class[]{String.class,String.class},"auto-login.command","/login dummy_fixture");UpdateProbe.call("Settings","save");String saved=java.nio.file.Files.readString(mc.gameDirectory.toPath().resolve("config/wanderlust.properties"));check(!saved.contains("dummy_fixture"),"AutoLogin secret is not written to config");UpdateProbe.call("Settings","put",new Class[]{String.class,String.class},"auto-login.command","");}
  if(tick==122){var c=Class.forName("dev.shabzup.wanderlust.ControlPanel").getDeclaredConstructor();c.setAccessible(true);panel=(Screen)c.newInstance();mc.gui.setScreen(panel);panel.resize(640,360);layout();var map=(Map<?,int[]>)field("positions");var groups=new ArrayList(map.keySet());int[] a=map.get(groups.get(0)).clone(),b=map.get(groups.get(1)).clone();float scale=(float)field("viewScale");click(a[0]+10,a[1]+10,0);double px=(b[0]+10)*scale,py=(b[1]+10)*scale;var event=new MouseButtonEvent(px,py,new MouseButtonInfo(0,0));panel.mouseDragged(event,(b[0]-a[0])*scale,(b[1]-a[1])*scale);panel.mouseReleased(event);check(map.get(groups.get(0))[0]==b[0]&&map.get(groups.get(1))[0]==a[0],"dragging onto another category swaps windows");layout();panel.resize(320,240);layout();}
  if(tick==130){System.out.println("UPGRADE COMPLETE failures="+failures);mc.stop();}
 }
}
