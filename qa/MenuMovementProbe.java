package qa;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

/** Uses controlled input samples; never injects keyboard/mouse events into the operating system. */
public final class MenuMovementProbe extends CoreModulesProbe {
    public static Predicate<InputConstants.Key> input=key->false;
    double forwardZ,menuZ;float flySpeed;
    boolean heldEveryTick;
    InputConstants.Key originalForward;
    final InputConstants.Key remapped=InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_R);
    void value(String key,String value)throws Exception{UpdateProbe.call("Settings","put",new Class[]{String.class,String.class},key,value);}
    void sample(Minecraft mc,Predicate<InputConstants.Key> input)throws Exception{
        MenuMovementProbe.input=input;
        UpdateProbe.call("MenuMovement","update",new Class[]{Minecraft.class,Predicate.class},mc,input);
    }
    void inventory(Minecraft mc){mc.gui.setScreen(new InventoryScreen(mc.player));}
    @Override void check(boolean ok,String text){if(!ok)failures++;System.out.println("MENU MOVEMENT "+(ok?"PASS ":"FAIL ")+text);}
    static final class TextScreen extends Screen {
        TextScreen(){super(Component.literal("Synthetic text input"));}
        @Override protected void init(){var edit=new EditBox(font,10,10,160,20,Component.literal("Text"));addRenderableWidget(edit);setFocused(edit);edit.setFocused(true);}
        @Override public boolean isPauseScreen(){return false;}
    }
    @Override void run(Minecraft mc)throws Exception{
        if(!loading&&mc.isGameLoadFinished()&&mc.gui.overlay()==null){
            loading=true;GLFW.glfwHideWindow(mc.getWindow().handle());String name="MenuMovement-"+System.currentTimeMillis();
            mc.createWorldOpenFlows().createFreshLevel(name,new LevelSettings(name,GameType.SURVIVAL,
                new LevelSettings.DifficultySettings(Difficulty.PEACEFUL,false,false),true,WorldDataConfiguration.DEFAULT),
                new WorldOptions(123,false,false),WorldPresets::createTestWorldDimensions,new TitleScreen());
        }
        if(mc.player==null||mc.gui.overlay()!=null)return;
        if(!playing){if(mc.gui.screen()!=null)return;playing=true;}tick++;
        var p=mc.player;var o=mc.options;
        if(tick==5)server(mc,s->{
            for(int x=115;x<=125;x++)for(int z=110;z<=145;z++)s.level().setBlockAndUpdate(new BlockPos(x,150,z),Blocks.STONE.defaultBlockState());
            s.teleportTo(s.level(),120.5,151,120.5,Set.of(),0,0,true);s.setDeltaMovement(Vec3.ZERO);
        });
        if(tick==30)enable("AUTO_WALK",true);
        if(tick==50){check(p.getZ()>122,"AutoWalk advances without physical input");forwardZ=p.getZ();value("auto-walk.direction","Backward");}
        if(tick==70){check(p.getZ()<forwardZ-1,"AutoWalk switches direction and releases previous key");inventory(mc);enable("AUTO_WALK",false);}
        if(tick==75)check(!o.keyUp.isDown()&&!o.keyDown.isDown(),"AutoWalk releases held keys when disabled in inventory");
        if(tick==80){mc.gui.setScreen(null);enable("SNEAK",true);}
        if(tick==90)check(read(mc,s->s.isShiftKeyDown()),"Sneak reaches server input state");
        if(tick==95){inventory(mc);enable("SNEAK",false);}
        if(tick==100)check(!o.keyShift.isDown()&&!read(mc,s->s.isShiftKeyDown()),"Sneak releases key and server state inside inventory");
        if(tick==105){mc.gui.setScreen(null);enable("AUTO_SNEAK",true);}
        if(tick==115)check(read(mc,s->s.isShiftKeyDown()),"AutoSneak reaches server input state");
        if(tick==120){inventory(mc);enable("AUTO_SNEAK",false);}
        if(tick==125)check(!o.keyShift.isDown()&&!read(mc,s->s.isShiftKeyDown()),"AutoSneak releases key and server state inside inventory");
        if(tick==130){mc.gui.setScreen(null);flySpeed=p.getAbilities().getFlyingSpeed();enable("FLY_SPEED",true);}
        if(tick==135){check(p.getAbilities().getFlyingSpeed()>flySpeed,"FlySpeed applies configured flight attribute");inventory(mc);enable("FLY_SPEED",false);}
        if(tick==140)check(p.getAbilities().getFlyingSpeed()==flySpeed,"FlySpeed restores original attribute while inventory is open");
        if(tick==145){
            originalForward=(InputConstants.Key)UpdateProbe.call("MenuMovement","key",new Class[]{KeyMapping.class},o.keyUp);
            o.keyUp.setKey(remapped);KeyMapping.resetMapping();enable("GUI_MOVE",true);menuZ=p.getZ();
        }
        if(tick>=145&&tick<175)sample(mc,key->key.equals(remapped));
        if(tick==175){
            check(p.getZ()>menuZ+1,"GuiMove moves player in inventory using remapped forward binding");sample(mc,key->false);
            check(!o.keyUp.isDown(),"GuiMove releases sampled key when input ends");
        }
        if(tick==180){value("gui-move.screens","Wanderlust");sample(mc,key->key.equals(remapped));check(!o.keyUp.isDown(),"GuiMove screen restriction excludes ordinary inventory");}
        if(tick==185){
            value("gui-move.screens","All");mc.gui.setScreen(new ChatScreen("",false));sample(mc,key->true);
            check(!o.keyUp.isDown()&&!o.keyJump.isDown(),"GuiMove suspends all managed inputs in chat");
        }
        if(tick==190){mc.gui.setScreen(new TextScreen());sample(mc,key->true);check(!o.keyUp.isDown(),"GuiMove suspends input for a focused text field");}
        if(tick==195){
            inventory(mc);value("gui-move.jump","false");value("gui-move.sneak","false");value("gui-move.sprint","false");sample(mc,key->true);
            check(o.keyUp.isDown()&&!o.keyJump.isDown()&&!o.keyShift.isDown()&&!o.keySprint.isDown(),"GuiMove independently restricts jump sneak and sprint");sample(mc,key->false);
        }
        if(tick==200){
            value("gui-move.jump","true");value("gui-move.sneak","true");value("gui-move.sprint","true");sample(mc,key->true);
            check(o.keyJump.isDown()&&o.keyShift.isDown()&&o.keySprint.isDown(),"GuiMove optional actions follow sampled bindings");sample(mc,key->false);
        }
        if(tick==205){
            o.keyUp.setKey(InputConstants.Type.MOUSE.getOrCreate(4));sample(mc,key->key.getType()==InputConstants.Type.MOUSE&&key.getValue()==4);
            check(o.keyUp.isDown(),"GuiMove supports a mouse-bound movement action");sample(mc,key->false);o.keyUp.setKey(remapped);
        }
        if(tick==210){
            value("gui-move.arrows","true");float yaw=p.getYRot(),pitch=p.getXRot();sample(mc,key->key.getType()==InputConstants.Type.KEYSYM&&(key.getValue()==GLFW.GLFW_KEY_RIGHT||key.getValue()==GLFW.GLFW_KEY_UP));
            check(p.getYRot()==yaw+4&&p.getXRot()==Math.clamp(pitch-4,-90,90),"GuiMove arrow rotation applies configured rate and pitch limit");
            check((boolean)UpdateProbe.call("MenuMovement","consumesArrow",new Class[]{long.class,int.class},mc.getWindow().handle(),GLFW.GLFW_KEY_RIGHT),"rotation arrows are reserved from menu navigation");
            enable("GUI_MOVE",false);sample(mc,key->true);
            check(!o.keyUp.isDown()&&!o.keyShift.isDown()&&!o.keyJump.isDown(),"disabling GuiMove releases managed inputs in the menu");
            o.keyUp.setKey(originalForward);KeyMapping.resetMapping();mc.gui.setScreen(null);value("auto-walk.direction","Forward");enable("AUTO_WALK",true);
        }
        if(tick==220){
            check(o.keyUp.isDown(),"AutoWalk resumes from menu transition");
            UpdateProbe.call("ExtraMovement","reset");check(!o.keyUp.isDown(),"world-change reset clears module-owned keys");enable("AUTO_WALK",false);
        }
        if(tick==230){o.toggleCrouch().set(true);enable("SNEAK",true);heldEveryTick=true;}
        if(tick>=235&&tick<=250)heldEveryTick&=o.keyShift.isDown();
        if(tick==250)check(heldEveryTick&&read(mc,s->s.isShiftKeyDown()),"Sneak remains held with Minecraft Toggle Sneak enabled");
        if(tick==255){inventory(mc);enable("SNEAK",false);}
        if(tick==260)check(!o.keyShift.isDown()&&!read(mc,s->s.isShiftKeyDown()),"Toggle Sneak mode does not prevent module cleanup");
        if(tick==265){
            o.toggleSprint().set(true);enable("GUI_MOVE",true);heldEveryTick=true;
            var shift=(InputConstants.Key)UpdateProbe.call("MenuMovement","key",new Class[]{KeyMapping.class},o.keyShift);
            var sprint=(InputConstants.Key)UpdateProbe.call("MenuMovement","key",new Class[]{KeyMapping.class},o.keySprint);
            sample(mc,key->key.equals(shift)||key.equals(sprint));
        }
        if(tick>=266&&tick<=280)heldEveryTick&=o.keyShift.isDown()&&o.keySprint.isDown();
        if(tick==280){
            check(heldEveryTick,"GuiMove holds sneak and sprint without flipping toggle-key states");
            enable("GUI_MOVE",false);sample(mc,key->true);
            check(!o.keyShift.isDown()&&!o.keySprint.isDown(),"GuiMove releases toggle-key states on disable");
        }
        if(tick==285)mc.gui.setScreen(null);
        if(tick==290){
            check(!o.keyShift.isDown()&&!o.keySprint.isDown()&&!read(mc,s->s.isShiftKeyDown()),"closing inventory does not resurrect disabled toggle-key state");
            o.toggleCrouch().set(false);o.toggleSprint().set(false);
            System.out.println("MENU MOVEMENT COMPLETE failures="+failures);mc.stop();
        }
    }
}
