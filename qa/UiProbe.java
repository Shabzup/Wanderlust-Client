package qa;

import java.util.*;
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

public final class UiProbe implements ClientModInitializer {
    boolean loading; int tick, failures;
    Screen panel;
    void check(boolean condition,String what){ if(!condition)failures++; System.out.println("UI "+(condition?"PASS ":"FAIL ")+what);}
    Screen create()throws Exception{
        var c=Class.forName("dev.shabzup.wanderlust.ControlPanel").getDeclaredConstructor();c.setAccessible(true);return (Screen)c.newInstance();
    }
    Button button(String label) {
        return panel.children().stream().filter(w->w instanceof Button b && b.getMessage().getString().equals(label)).map(w->(Button)w).findFirst().orElseThrow();
    }
    void shot(Minecraft mc,String name){Screenshot.grab(mc.gameDirectory,name+".png",mc.gameRenderer.mainRenderTarget(),1,c->{});}
    public void onInitializeClient(){
        ClientTickEvents.END_CLIENT_TICK.register(mc->{
            try {run(mc);}catch(Throwable e){e.printStackTrace();System.out.println("UI FAILED");mc.stop();}
        });
    }
    void run(Minecraft mc)throws Exception{
        if(!loading && mc.isGameLoadFinished() && mc.gui.overlay()==null){
            loading=true;
            org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().handle());
            mc.options.pauseOnLostFocus=false;
            String name="UI-"+System.currentTimeMillis();
            mc.createWorldOpenFlows().createFreshLevel(name,new LevelSettings(name,GameType.CREATIVE,new LevelSettings.DifficultySettings(Difficulty.PEACEFUL,false,false),true,WorldDataConfiguration.DEFAULT),new WorldOptions(123,false,false),WorldPresets::createTestWorldDimensions,new TitleScreen());
        }
        if(mc.player==null || mc.gui.overlay()!=null)return;
        tick++;
        if(tick==20){ panel=create();mc.gui.setScreen(panel); }
        if(tick==21)shot(mc,"ui-opening");
        if(tick==35){
            shot(mc,"ui-compact");
            check(!panel.isPauseScreen(),"popup does not pause game");
            var controls=panel.children().stream().filter(w->w instanceof AbstractWidget).map(w->(AbstractWidget)w).toList();
            boolean overlap=false;
            for(int i=0;i<controls.size();i++)for(int j=i+1;j<controls.size();j++){
                var a=controls.get(i);var b=controls.get(j);
                if(a.getX()<b.getX()+b.getWidth() && a.getX()+a.getWidth()>b.getX()
                    && a.getY()<b.getY()+b.getHeight() && a.getY()+a.getHeight()>b.getY())overlap=true;
            }
            check(!overlap,"compact interactive controls have no overlaps");
            button("Movement").onPress(null);
        }
        if(tick==45){
            shot(mc,"ui-movement");
            button("Flight").onPress(null);
            check((boolean)UpdateProbe.call("Wanderlust","on",new Class[]{String.class},"flight"),"module card toggles on");
        }
        if(tick==55){
            shot(mc,"ui-enabled");
            var b=button("Flight");
            panel.mouseClicked(new MouseButtonEvent(b.getX()+8,b.getY()+8,new MouseButtonInfo(1,0)),false);
        }
        if(tick==65){
            check(panel.children().stream().anyMatch(w->w instanceof Button b && b.getMessage().getString().startsWith("Key  ")),"right click opens settings and keybind");
            shot(mc,"ui-settings");
            button("< Modules").onPress(null);
        }
        if(tick==70){
            var search=panel.children().stream().filter(w->w instanceof EditBox).map(w->(EditBox)w).findFirst().orElseThrow();
            search.setValue("totem");
        }
        if(tick==76){
            check(panel.children().stream().anyMatch(w->w instanceof Button b && b.getMessage().getString().equals("AutoTotem")),"search finds modules across categories");
            shot(mc,"ui-search");
            mc.getWindow().setGuiScale(1);
            panel.resize(mc.getWindow().getGuiScaledWidth(),mc.getWindow().getGuiScaledHeight());
            button("Combat").onPress(null);
        }
        if(tick==85){
            shot(mc,"ui-large");
            panel.keyPressed(new KeyEvent(344,0,0));
            check(mc.gui.screen()==panel,"close animation retains popup initially");
        }
        if(tick==93){
            check(mc.gui.screen()==null,"Right Shift closes popup after animation");
            panel=create();mc.gui.setScreen(panel);
        }
        if(tick==105){button("Accounts").onPress(null);}
        if(tick==113){check(mc.gui.screen().getTitle().getString().equals("Accounts"),"animated account navigation");mc.gui.screen().onClose();}
        if(tick==122){check(mc.gui.screen()==panel,"return from Accounts restores panel");System.out.println("UI COMPLETE failures="+failures);mc.stop();}
    }
}
