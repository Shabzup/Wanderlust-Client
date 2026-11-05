package qa;
import java.util.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.block.*;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

public final class RepairProbe implements ClientModInitializer{
 boolean loading,playing;BlockPos fixture;long holdUntil;int tick,failures,startCount,stopCount,repairCount;
 void check(boolean ok,String message){if(!ok)failures++;System.out.println("REPAIR "+(ok?"PASS ":"FAIL ")+message);}
 void enable(String id,boolean active)throws Exception{Class cls=Class.forName("dev.shabzup.wanderlust.Feature");UpdateProbe.call("Settings","setEnabled",new Class[]{cls,boolean.class},Enum.valueOf(cls,id),active);}
 void option(String key,boolean value)throws Exception{var f=Class.forName("dev.shabzup.wanderlust.Settings").getDeclaredField("options");f.setAccessible(true);for(Object o:(List)f.get(null)){var k=o.getClass().getDeclaredMethod("key");k.setAccessible(true);if(k.invoke(o).equals(key))UpdateProbe.call("Settings","setToggle",new Class[]{o.getClass(),boolean.class},o,value);}}
 int bottles(Minecraft mc){return mc.getSingleplayerServer().submit(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());int n=0;for(int i=0;i<36;i++)if(p.getInventory().getItem(i).is(Items.EXPERIENCE_BOTTLE))n+=p.getInventory().getItem(i).getCount();if(p.getOffhandItem().is(Items.EXPERIENCE_BOTTLE))n+=p.getOffhandItem().getCount();return n;}).join();}
 int[] mesh(Minecraft mc,net.minecraft.world.level.block.state.BlockState state,BlockPos pos){int[] n={0,0};var renderer=new ModelBlockRenderer(true,true,mc.getBlockColors());renderer.tesselateBlock((x,y,z,q,instance)->{n[0]++;if(instance.getLightCoords(0)==net.minecraft.util.LightCoordsUtil.smoothPack(255,255))n[1]++;},0,0,0,mc.level,pos,state,mc.getModelManager().getBlockStateModelSet().get(state),42L);return n;}
 int[] fabricMesh(Minecraft mc,net.minecraft.world.level.block.state.BlockState state,BlockPos pos){int[] n={0,0};var api=net.fabricmc.fabric.api.client.renderer.v1.Renderer.get();var emitter=api.quadEmitter(quad->{n[0]++;if(quad.emissive()&&quad.lightmap(0)==net.minecraft.util.LightCoordsUtil.FULL_BRIGHT)n[1]++;});api.altModelBlockRenderer(true,true,mc.getBlockColors()).tesselateBlock(emitter,0,0,0,mc.level,pos,state,mc.getModelManager().getBlockStateModelSet().get(state),42L);return n;}
 int fluids(Minecraft mc,BlockPos pos){int[] output={0};var sink=(com.mojang.blaze3d.vertex.VertexConsumer)java.lang.reflect.Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{com.mojang.blaze3d.vertex.VertexConsumer.class},(proxy,method,args)->method.getReturnType()==void.class?null:proxy);var state=Blocks.WATER.defaultBlockState();new FluidRenderer(mc.getModelManager().getFluidStateModelSet()).tesselate(mc.level,pos,layer->{output[0]++;return sink;},state,state.getFluidState());return output[0];}
 void renderChecks(Minecraft mc)throws Exception{
  BlockPos pos=mc.player.blockPosition().offset(3,0,0);var ore=Blocks.DIAMOND_ORE.defaultBlockState();var stone=Blocks.STONE.defaultBlockState();
  mc.level.setBlock(pos,ore,3);for(Direction d:Direction.values())mc.level.setBlock(pos.relative(d),stone,3);
  check(mesh(mc,ore,pos)[0]==0,"normal renderer hides fully enclosed ore faces");enable("XRAY",true);
  int[] fabric=fabricMesh(mc,ore,pos);check(fabric[0]>=6&&fabric[1]==fabric[0],"Fabric renderer emits fully bright emissive ore faces");
  BlockPos grassPos=pos.above(3);mc.level.setBlock(grassPos.below(),Blocks.DIRT.defaultBlockState(),3);mc.level.setBlock(grassPos,Blocks.SHORT_GRASS.defaultBlockState(),3);check(fabricMesh(mc,Blocks.SHORT_GRASS.defaultBlockState(),grassPos)[0]==0,"Fabric renderer hides unselected uncullable plant quads");
  BlockPos waterPos=pos.offset(0,3,3);mc.level.setBlock(waterPos,Blocks.WATER.defaultBlockState(),3);check(fluids(mc,waterPos)==0,"Xray suppresses unselected fluid geometry");
  int[] rendered=mesh(mc,ore,pos);check(rendered[0]>=6,"Xray emits all enclosed ore faces using the actual model renderer");check(rendered[1]==rendered[0],"Xray emitted ore quads are fully lit");
  check(mesh(mc,stone,pos)[0]==0,"Xray emits no ordinary stone geometry");
  VisGraph graph=new VisGraph();for(int x=0;x<16;x++)for(int y=0;y<16;y++)for(int z=0;z<16;z++)graph.setOpaque(new BlockPos(x,y,z));check(graph.resolve().visibilityBetween(Direction.NORTH,Direction.SOUTH),"Xray exposes otherwise occluded chunk sections");
  enable("XRAY",false);check(fabricMesh(mc,Blocks.SHORT_GRASS.defaultBlockState(),grassPos)[0]>0,"normal plant rendering is restored after Xray");check(fluids(mc,waterPos)>0,"normal fluid rendering is restored after Xray");check(mesh(mc,ore,pos)[0]==0,"disabling Xray restores normal face culling");
  VisGraph normal=new VisGraph();for(int x=0;x<16;x++)for(int y=0;y<16;y++)for(int z=0;z<16;z++)normal.setOpaque(new BlockPos(x,y,z));check(!normal.resolve().visibilityBetween(Direction.NORTH,Direction.SOUTH),"disabling Xray restores section occlusion");
 }

 void wiringAudit()throws Exception{
  Class cls=Class.forName("dev.shabzup.wanderlust.Feature");var binds=Class.forName("dev.shabzup.wanderlust.Wanderlust").getDeclaredField("binds");binds.setAccessible(true);Map mappings=(Map)binds.get(null);
  for(Object value:cls.getEnumConstants()){
   var idField=cls.getDeclaredField("id");idField.setAccessible(true);String id=(String)idField.get(value);
   UpdateProbe.call("Settings","setEnabled",new Class[]{cls,boolean.class},value,true);
   boolean on=(boolean)UpdateProbe.call("Settings","on",new Class[]{String.class},id);
   UpdateProbe.call("Settings","setEnabled",new Class[]{cls,boolean.class},value,false);
   boolean off=!(boolean)UpdateProbe.call("Settings","on",new Class[]{String.class},id);
   if(!on||!off||!mappings.containsKey(value))failures++;
   System.out.println("AUDIT WIRING "+(on&&off&&mappings.containsKey(value)?"PASS ":"FAIL ")+id);
  }
  for(String key:new String[]{"active-modules.scale","active-modules.x","active-modules.y","air-jump.power","ambience.rain","ambience.thunder","anchor-aura-range","anti-afk.delay","anti-void.height","arrow-dodge.range","attribute-swap.slot","aura-delay","aura-range","auto-crafter.delay","auto-feed.threshold","auto-heal.threshold","auto-leave.health","auto-nametag.range","auto-reconnect.seconds","auto-repair.threshold","auto-trade.delay","auto-trade.offer","auto-wasp.speed","bed-aura-range","blink.max-packets","blink.ticks","boat-fly.speed","boat-fly.vertical","bow-draw","bow-range","bowspam-range","breadcrumbs.length","breed-range","build-length","build-width","click-interval","crystal-aura-range","eat-hunger","elytra-boost.speed","elytra-fly.speed","esp-range","fast-climb.speed","fish-delay","flight.speed","flight.vertical","fly-speed.speed","freecam.range","freecam.speed","freelook.sensitivity","gap-health","grid.range","hand-view.scale","hand-view.x","hand-view.y","hand-view.z","high-jump.power","hitboxes.expand","infinity-miner.delay","item-sucker.range","jump-flight.power","jumps.power","log-health","long-jump.power","marker.x","marker.y","marker.z","mend-durability","mend-interval","nametag-range","notebot.delay","range-actions.radius","range-actions.x","range-actions.y","range-actions.z","reach.blocks","reach.entities","replenish-count","respawn-delay","reverse-step.speed","rotation.pitch","rotation.yaw","shearer-range","slot-click.delay","slot-click.slot","smite-aura.delay","speed.speed","spider.power","step.height","storage-range","structure-range","swarm.port","time-changer.time","timer.factor","totem-health","tracer-range","trail.length","trident-boost.factor","ui.opacity","ui.scale","velocity.factor","zoom-plus.fov"})check(UpdateProbe.call("Settings","number",new Class[]{String.class},key)!=null,"numeric setting exists: "+key);
 }
 public void onInitializeClient(){ClientTickEvents.END_CLIENT_TICK.register(mc->{try{run(mc);}catch(Throwable e){failures++;e.printStackTrace();System.out.println("REPAIR FAILED");mc.stop();}});}
 void run(Minecraft mc)throws Exception{
  if(!loading&&mc.isGameLoadFinished()&&mc.gui.overlay()==null){loading=true;org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().handle());String name="Repair-"+System.currentTimeMillis();mc.createWorldOpenFlows().createFreshLevel(name,new LevelSettings(name,GameType.SURVIVAL,new LevelSettings.DifficultySettings(Difficulty.PEACEFUL,false,false),true,WorldDataConfiguration.DEFAULT),new WorldOptions(123,false,false),WorldPresets::createTestWorldDimensions,new TitleScreen());}
  if(mc.player==null||mc.gui.overlay()!=null||System.currentTimeMillis()<holdUntil)return;if(!playing){if(mc.gui.screen()!=null)return;playing=true;if(Boolean.getBoolean("qa.visualonly"))tick=205;}tick++;
  if(tick==5){renderChecks(mc);mc.getSingleplayerServer().submit(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());p.getInventory().setItem(4,new ItemStack(Items.EXPERIENCE_BOTTLE,64));p.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST,new ItemStack(Items.DIAMOND_CHESTPLATE));}).join();}
  if(tick==20){startCount=bottles(mc);check(startCount==64,"server inventory contains supplied XP bottles");enable("AUTO_EXP",true);}
  if(tick==55){check(bottles(mc)<startCount,"AutoEXP consumes XP bottles on integrated server with undamaged equipment");enable("AUTO_EXP",false);}
  if(tick==65)stopCount=bottles(mc);
  if(tick==80){check(bottles(mc)==stopCount,"AutoEXP stops consuming after disable");option("auto-exp.only-damaged",true);enable("AUTO_EXP",true);}
  if(tick==105){check(bottles(mc)==stopCount,"optional repair-only mode waits for damaged equipment");mc.getSingleplayerServer().submit(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());p.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST).setDamageValue(100);}).join();repairCount=bottles(mc);}
  if(tick==140){check(bottles(mc)<repairCount,"repair-only mode throws once equipment is damaged");enable("AUTO_EXP",false);mc.getSingleplayerServer().submit(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());p.getInventory().setItem(4,ItemStack.EMPTY);p.setItemSlot(net.minecraft.world.entity.EquipmentSlot.OFFHAND,new ItemStack(Items.EXPERIENCE_BOTTLE,32));}).join();}
  if(tick==155){option("auto-exp.only-damaged",false);enable("AUTO_EXP",true);}
  if(tick==185){check(bottles(mc)<32,"AutoEXP consumes offhand bottles on integrated server");enable("AUTO_EXP",false);repairCount=bottles(mc);enable("EXP_THROWER",true);}
  if(tick==210){if(!Boolean.getBoolean("qa.visualonly"))check(bottles(mc)<repairCount,"ExpThrower consumes XP bottles on integrated server");enable("EXP_THROWER",false);mc.getSingleplayerServer().submit(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());p.setGameMode(GameType.CREATIVE);p.getAbilities().flying=true;p.onUpdateAbilities();fixture=p.blockPosition().offset(0,1,8);var level=p.level();for(BlockPos pos:BlockPos.betweenClosed(fixture.offset(-4,-4,-3),fixture.offset(4,4,3)))level.setBlockAndUpdate(pos,Blocks.STONE.defaultBlockState());level.setBlockAndUpdate(fixture,Blocks.DIAMOND_ORE.defaultBlockState());level.setBlockAndUpdate(fixture.offset(2,0,1),Blocks.GOLD_ORE.defaultBlockState());level.setBlockAndUpdate(fixture.offset(0,0,-2),Blocks.WATER.defaultBlockState());level.setBlockAndUpdate(fixture.offset(-2,-1,-1),Blocks.DIRT.defaultBlockState());level.setBlockAndUpdate(fixture.offset(-2,1,-1),Blocks.AIR.defaultBlockState());level.setBlockAndUpdate(fixture.offset(-2,0,-1),Blocks.SHORT_GRASS.defaultBlockState());}).join();}
  if(tick==230){mc.player.setXRot(0);mc.player.setYRot(0);enable("XRAY",true);System.out.println("RENDER CAMERA "+mc.player.position()+" / "+mc.gameRenderer.mainCamera().position()+" fixture "+mc.level.getBlockState(fixture));holdUntil=System.currentTimeMillis()+2000;}
  if(tick==260){check(mc.levelRenderer.isSectionCompiledAndVisible(fixture),"Xray rebuilds complete scene geometry");System.out.println("RENDER DETAILS "+mc.gameRenderer.mainCamera().yRot()+" / "+mc.gameRenderer.mainCamera().xRot()+" compiled "+mc.levelRenderer.isSectionCompiledAndVisible(fixture)+" visible "+mc.levelRenderer.visibleSections().size()+" forward "+mc.gameRenderer.mainCamera().forwardVector());} if(tick==260)Screenshot.grab(mc.gameDirectory,"repair-xray-on.png",mc.gameRenderer.mainRenderTarget(),1,c->{});
  if(tick==270){enable("XRAY",false);holdUntil=System.currentTimeMillis()+2000;}
  if(tick==300){check(mc.levelRenderer.isSectionCompiledAndVisible(fixture),"normal scene geometry is restored after Xray");System.out.println("RESTORE DETAILS "+mc.gameRenderer.mainCamera().yRot()+" / "+mc.gameRenderer.mainCamera().xRot()+" compiled "+mc.levelRenderer.isSectionCompiledAndVisible(fixture)+" visible "+mc.levelRenderer.visibleSections().size()+" forward "+mc.gameRenderer.mainCamera().forwardVector());} if(tick==300)Screenshot.grab(mc.gameDirectory,"repair-xray-off.png",mc.gameRenderer.mainRenderTarget(),1,c->{});
  if(tick==310){wiringAudit();System.out.println("REPAIR COMPLETE failures="+failures);mc.stop();}
 }
}
