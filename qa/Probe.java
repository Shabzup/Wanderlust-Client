package qa;
import java.util.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.Difficulty;
import net.minecraft.world.item.*;
public class Probe implements ClientModInitializer {
    boolean loading; int tick, idleTicks, failures;
    Class<?> feature,settings;
    public void onInitializeClient(){
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            try { run(mc); } catch(Throwable e){System.out.println("QA FAILED"); e.printStackTrace();mc.stop();}
        });
    }
    void check(boolean ok,String message) {if(!ok)failures++;System.out.println((ok?"QA PASS ":"QA FAIL ")+message);}
    void enable(String name, boolean enabled)throws Exception{
        var m=settings.getDeclaredMethod("setEnabled",feature,boolean.class);m.setAccessible(true);
        m.invoke(null,Enum.valueOf((Class)feature,name),enabled);
    }
    void run(Minecraft mc)throws Exception{
        if(!loading && mc.isGameLoadFinished() && mc.gui.overlay()==null){
            loading=true; org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().handle()); mc.options.pauseOnLostFocus=false;
            check(!FabricLoader.getInstance().isModLoaded("meteor-client"),"Meteor absent");
            UpdateProbe.menu(this,mc);
            String name = "QAWanderlust-"+System.currentTimeMillis();
            mc.createWorldOpenFlows().createFreshLevel(name,new LevelSettings(name,GameType.SURVIVAL,new LevelSettings.DifficultySettings(Difficulty.NORMAL,false,false),true,WorldDataConfiguration.DEFAULT),new WorldOptions(3221,false,false),WorldPresets::createTestWorldDimensions,new TitleScreen());
        }
        if(mc.player==null || mc.level==null || mc.gui.overlay()!=null || !mc.player.isAlive()) {
            if(++idleTicks%100==0) System.out.println("QA waiting on "+mc.gui.screen()+" player="+(mc.player!=null));
            return;
        }
        tick++; UpdateProbe.tick(this,mc,tick);
        if(tick==30){
            if(!FabricLoader.getInstance().isModLoaded("wanderlust")){System.out.println("QA BASELINE WORLD READY");mc.stop();return;}
            check(mc.getSingleplayerServer()!=null,"world joined with WanderlustClient");
            feature=Class.forName("dev.shabzup.wanderlust.Feature");
            settings=Class.forName("dev.shabzup.wanderlust.Settings");
            mc.gui.setScreen(null);
            KeyMapping.click(InputConstants.getKey("key.keyboard.right.shift"));
        }
        if(tick==35){
            check(mc.gui.screen()!=null && mc.gui.screen().getTitle().getString().equals("WanderlustClient"),"Right Shift opens panel");
            check(mc.gui.screen().children().stream().filter(w->w instanceof Button).count()>5,"panel has interactive controls");
            for(String tab:List.of("Movement","Visuals","Player","World","Combat")){
                var item=mc.gui.screen().children().stream().filter(w->w instanceof Button b && b.getMessage().getString().equals(tab)).findFirst().orElseThrow();
                ((Button)item).onPress(null);
                check(mc.gui.screen().children().stream().anyMatch(w->w instanceof Button b && b.getMessage().getString().contains("OFF")),"open category "+tab);
            }
            for(String id:new String[]{"ESP","TRACERS","STORAGE_ESP","STRUCTURE_ESP","NAMETAGS","BUILD_POS"}) enable(id,true);
        }
        if(tick==50) {
            mc.gui.setScreen(null);
            var server=mc.getSingleplayerServer();var id=mc.player.getUUID();
            server.execute(()->{
                var p=server.getPlayerList().getPlayer(id);
                p.setItemSlot(EquipmentSlot.OFFHAND,ItemStack.EMPTY);
                p.getInventory().setItem(9,new ItemStack(Items.TOTEM_OF_UNDYING));
                p.getInventory().setItem(0,new ItemStack(Items.BREAD,4));
                
                p.getFoodData().setFoodLevel(6);
            });
            enable("AUTO_TOTEM",true);
        }
        if(tick==45) net.minecraft.client.Screenshot.grab(mc,true);
        if(tick==78) mc.player.getInventory().setSelectedSlot(4);
        if(tick==85){
            check(mc.player.getOffhandItem().is(Items.TOTEM_OF_UNDYING),"AutoTotem equipped from inventory");
            enable("AUTO_EAT",true);
        }
        if(tick>=85 && tick<=145 && tick%5==0)System.out.println("FOOD "+tick+" hunger="+mc.player.getFoodData().getFoodLevel()+" selected="+mc.player.getInventory().getSelectedSlot()+" bread="+mc.player.getInventory().getItem(0)+" use="+mc.player.getUseItem()+" left="+mc.player.getUseItemRemainingTicks()+" effects="+mc.player.getInventory().getItem(0).get(net.minecraft.core.component.DataComponents.CONSUMABLE)+" ui="+mc.gui.screen());
        if(tick==145){
            check(mc.player.getInventory().getItem(0).getCount()<4,"AutoEat consumed bread");
            enable("AUTO_EAT",false);
            var server=mc.getSingleplayerServer();var id=mc.player.getUUID();
            server.execute(()->{
                var p=server.getPlayerList().getPlayer(id);
                p.getInventory().setItem(2,new ItemStack(Items.COBBLESTONE,1));
                p.getInventory().setItem(11,new ItemStack(Items.COBBLESTONE,26));
            });
            enable("AUTO_REPLENISH",true);
        }
        if(tick==160){
            var server=mc.getSingleplayerServer();var id=mc.player.getUUID();
            server.execute(()->server.getPlayerList().getPlayer(id).getInventory().setItem(2,ItemStack.EMPTY));
        }
        if(tick==180){
            check(mc.player.getInventory().getItem(2).getCount()==26,"AutoReplenish restored exhausted slot");
            enable("AUTO_REPLENISH",false);
            var server=mc.getSingleplayerServer();var id=mc.player.getUUID();
            server.execute(()->{
                var p=server.getPlayerList().getPlayer(id);
                server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),"execute at "+p.getScoreboardName()+" run summon minecraft:sheep ~1 ~ ~ {NoAI:1b}");
                p.getInventory().setItem(3,new ItemStack(Items.SHEARS));
                p.getInventory().setItem(5,new ItemStack(Items.WHEAT,16));
            });
            enable("AUTO_SHEARER",true);enable("AUTO_BREED",true);
        }
        if(tick==230){
            var sheep=mc.level.getEntitiesOfClass(net.minecraft.world.entity.animal.sheep.Sheep.class,mc.player.getBoundingBox().inflate(4));
            check(sheep.stream().anyMatch(s->s.isSheared()),"AutoShearer sheared a sheep");
            check(mc.player.getInventory().getItem(5).getCount()<16,"AutoBreed fed an adult sheep");
            enable("AUTO_BREED",false);enable("AUTO_SHEARER",false);
        }
        if(tick==231) enable("KILL_AURA",true);
        if(tick==260){
            var sheep=mc.level.getEntitiesOfClass(net.minecraft.world.entity.animal.sheep.Sheep.class,mc.player.getBoundingBox().inflate(6));
            check(sheep.isEmpty() || sheep.stream().anyMatch(s->s.getHealth()<s.getMaxHealth()),"KillAura damaged a target");
            enable("KILL_AURA",false);
            enable("STEP",true);
        }
        if(tick==265){
            check(mc.player.maxUpStep() >= 1.0,"Step raises step height");
            enable("STEP",false);
        }
        if(tick==270){
            for(Object f:feature.getEnumConstants()) enable(((Enum<?>)f).name(),true);
        }
        if(tick==290) {
            for(Object f:feature.getEnumConstants()) enable(((Enum<?>)f).name(),false);
        }
        if(tick==300){System.out.println("QA UPDATES COMPLETE failures="+failures);mc.stop();return;}
        if(tick>=300 && tick<501) CombatProbe.tick(this,mc,tick-300);
        MotionProbe.run(this,mc,tick);
        if(tick==730){System.out.println("QA COMPLETE "+feature.getEnumConstants().length+" modules, failures="+failures);mc.stop();}
    }
}





