package qa;
import java.util.*;
import java.util.function.Consumer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.server.level.ServerPlayer;

public class CoreModulesProbe implements ClientModInitializer{
 boolean loading,playing;int tick,failures;UUID cow;
 void check(boolean ok,String text){if(!ok)failures++;System.out.println("CORE "+(ok?"PASS ":"FAIL ")+text);}
 void enable(String name,boolean on)throws Exception{Class cls=Class.forName("dev.shabzup.wanderlust.Feature");UpdateProbe.call("Settings","setEnabled",new Class[]{cls,boolean.class},Enum.valueOf(cls,name),on);}
 void server(Minecraft mc,Consumer<ServerPlayer> action){var id=mc.player.getUUID();mc.getSingleplayerServer().submit(()->action.accept(mc.getSingleplayerServer().getPlayerList().getPlayer(id))).join();}
 <T>T read(Minecraft mc,java.util.function.Function<ServerPlayer,T> action){var id=mc.player.getUUID();return mc.getSingleplayerServer().submit(()->action.apply(mc.getSingleplayerServer().getPlayerList().getPlayer(id))).join();}
 public void onInitializeClient(){ClientTickEvents.END_CLIENT_TICK.register(mc->{try{run(mc);}catch(Throwable e){e.printStackTrace();System.out.println("CORE FAILED");mc.stop();}});}
 void run(Minecraft mc)throws Exception{
  if(!loading&&mc.isGameLoadFinished()&&mc.gui.overlay()==null){loading=true;org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().handle());String name="Core-"+System.currentTimeMillis();mc.createWorldOpenFlows().createFreshLevel(name,new LevelSettings(name,GameType.SURVIVAL,new LevelSettings.DifficultySettings(Difficulty.PEACEFUL,false,false),true,WorldDataConfiguration.DEFAULT),new WorldOptions(123,false,false),WorldPresets::createTestWorldDimensions,new TitleScreen());}
  if(mc.player==null||mc.gui.overlay()!=null)return;if(!playing){if(mc.gui.screen()!=null)return;playing=true;}tick++;
  if(tick==5)server(mc,p->p.getInventory().setItem(12,new ItemStack(Items.TOTEM_OF_UNDYING)));
  if(tick==15)enable("AUTO_TOTEM",true);
  if(tick==30){check(read(mc,p->p.getOffhandItem().is(Items.TOTEM_OF_UNDYING)),"AutoTotem equips inventory totem on server");enable("AUTO_TOTEM",false);server(mc,p->{p.setItemSlot(EquipmentSlot.OFFHAND,ItemStack.EMPTY);p.getInventory().setItem(12,new ItemStack(Items.TOTEM_OF_UNDYING));});}
  if(tick==45){check(read(mc,p->p.getOffhandItem().isEmpty()),"disabled AutoTotem leaves offhand unchanged");server(mc,p->{p.getInventory().setItem(0,new ItemStack(Items.COBBLESTONE,16));p.getInventory().setItem(9,new ItemStack(Items.COBBLESTONE,32));p.getInventory().setSelectedSlot(0);});enable("AUTO_REPLENISH",true);}
  if(tick==65)server(mc,p->p.getInventory().setItem(0,ItemStack.EMPTY));
  if(tick==85){check(read(mc,p->p.getInventory().getItem(0).is(Items.COBBLESTONE)&&p.getInventory().getItem(0).getCount()==32),"AutoReplenish replaces depleted hotbar stack on server");enable("AUTO_REPLENISH",false);server(mc,p->{p.getInventory().setItem(0,ItemStack.EMPTY);p.getInventory().setItem(1,new ItemStack(Items.BREAD,20));p.getFoodData().setFoodLevel(5);});}
  if(tick==95)enable("AUTO_EAT",true);
  if(tick==150){check(read(mc,p->p.getFoodData().getFoodLevel()>5&&p.getInventory().getItem(1).getCount()<20),"AutoEat consumes bread and restores server hunger");enable("AUTO_EAT",false);server(mc,p->{p.getInventory().setItem(2,new ItemStack(Items.GOLDEN_APPLE,10));p.setHealth(4);});}
  if(tick==165)enable("AUTO_GAP",true);
  if(tick==220){check(read(mc,p->p.getAbsorptionAmount()>0&&p.getInventory().getItem(2).getCount()<10),"AutoGap consumes golden apple and grants server absorption");enable("AUTO_GAP",false);server(mc,p->{p.setHealth(20);p.getInventory().setItem(3,new ItemStack(Items.EXPERIENCE_BOTTLE,64));ItemStack armor=new ItemStack(Items.DIAMOND_CHESTPLATE);armor.enchant(p.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.MENDING),1);armor.setDamageValue(150);p.setItemSlot(EquipmentSlot.CHEST,armor);});}
  if(tick==235)enable("AUTO_MEND",true);
  if(tick==305){check(read(mc,p->p.getItemBySlot(EquipmentSlot.CHEST).getDamageValue()<150&&p.getInventory().getItem(3).getCount()<64),"AutoMend repairs Mending armor using server XP bottles");enable("AUTO_MEND",false);server(mc,p->{p.setHealth(20);p.getInventory().setItem(0,new ItemStack(Items.DIAMOND_SWORD));p.getInventory().setSelectedSlot(0);var target=(net.minecraft.world.entity.Mob)net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getValue(net.minecraft.resources.Identifier.fromNamespaceAndPath("minecraft","cow")).create(p.level(),EntitySpawnReason.COMMAND);target.setPos(p.getX()+2,p.getY(),p.getZ());target.setNoAi(true);p.level().addFreshEntity(target);cow=target.getUUID();});UpdateProbe.call("EntityTargets","setAll",new Class[]{List.class,boolean.class},UpdateProbe.call("EntityTargets","entries"),false);UpdateProbe.call("EntityTargets","set",new Class[]{String.class,boolean.class},"minecraft:cow",true);}
  if(tick==325)enable("KILL_AURA",true);
  if(tick==370){check(read(mc,p->{var e=p.level().getEntity(cow);return e==null||((net.minecraft.world.entity.LivingEntity)e).getHealth()<10;}),"KillAura damages selected cow type on integrated server");enable("KILL_AURA",false);System.out.println("CORE COMPLETE failures="+failures);mc.stop();}
 }
}
