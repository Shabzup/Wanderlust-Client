package qa;

import java.lang.reflect.*;
import java.util.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import com.mojang.blaze3d.platform.InputConstants;

final class UpdateProbe {
    static Object call(String cls, String name, Class<?>[] types, Object... args) throws Exception {
        var m=Class.forName("dev.shabzup.wanderlust."+cls).getDeclaredMethod(name,types);
        m.setAccessible(true);return m.invoke(null,args);
    }
    static Object call(String cls,String name)throws Exception{return call(cls,name,new Class<?>[0]);}
    static void menu(Probe probe, Minecraft mc)throws Exception{
        String original=mc.getUser().getName();
        call("Accounts","addOffline",new Class[]{String.class},"QA_Offline");
        var f=Class.forName("dev.shabzup.wanderlust.Accounts").getDeclaredField("entries");f.setAccessible(true);
        var entry=((List<?>)f.get(null)).getFirst();
        call("Accounts","use",new Class[]{entry.getClass()},entry);
        probe.check(mc.getUser().getName().equals("QA_Offline"),"offline username switches actual session");
        probe.check(mc.getGameProfile().name().equals("QA_Offline"),"offline profile cache updated");
        call("Accounts","restore");
        probe.check(mc.getUser().getName().equals(original)&&mc.getGameProfile().name().equals(original),"launcher account and profile restored");
        var c=Class.forName("dev.shabzup.wanderlust.AccountScreen").getDeclaredConstructor(Screen.class);c.setAccessible(true);
        mc.gui.setScreen((Screen)c.newInstance(mc.gui.screen()));
        probe.check(mc.gui.screen().children().size()>4,"account switcher UI initialized");
        mc.gui.setScreen(null);
    }
    static KeyMapping key;
    static void tick(Probe probe,Minecraft mc,int t)throws Exception{
        if(t==240){
            String original=mc.getUser().getName();
            var f=Class.forName("dev.shabzup.wanderlust.Accounts").getDeclaredField("entries");f.setAccessible(true);
            var e=((List<?>)f.get(null)).getFirst();
            call("Accounts","use",new Class[]{e.getClass()},e);
            probe.check(mc.getUser().getName().equals(original),"account switching blocked inside world");
            var feature=Class.forName("dev.shabzup.wanderlust.Feature");
            Object speed=Enum.valueOf((Class)feature,"SPEED");
            var c=Class.forName("dev.shabzup.wanderlust.BindScreen").getDeclaredConstructor(Screen.class,feature);c.setAccessible(true);
            var screen=(Screen)c.newInstance(null,speed);
            mc.gui.setScreen(screen);
            screen.keyPressed(new KeyEvent(71,0,0));
            var binds=Class.forName("dev.shabzup.wanderlust.Wanderlust").getDeclaredField("binds");binds.setAccessible(true);
            key=(KeyMapping)((Map<?,?>)binds.get(null)).get(speed);
            probe.check(key.getTranslatedKeyMessage().getString().equalsIgnoreCase("G"),"per-module bind screen stores chosen key");
            mc.gui.setScreen(null);
            KeyMapping.click(InputConstants.getKey("key.keyboard.g"));
        }
        if(t==244){
            probe.check((boolean)call("Wanderlust","on",new Class[]{String.class},"speed"),"custom key toggles module on");
            KeyMapping.click(InputConstants.getKey("key.keyboard.g"));
        }
        if(t==248){
            probe.check(!(boolean)call("Wanderlust","on",new Class[]{String.class},"speed"),"custom key toggles module off");
            key.setKey(InputConstants.UNKNOWN);KeyMapping.resetMapping();mc.options.save();
        }
        if(t==295){
            probe.enable("FLIGHT",true);
            if((boolean)call("BaritoneBridge","available")){
                probe.check((boolean)call("BaritoneBridge","command",new Class[]{String.class},"goto 100 100"),"Baritone command submitted through optional bridge");
            } else probe.check((boolean)call("Wanderlust","on",new Class[]{String.class},"flight"),"standalone modules work without Baritone");
        }
        if(t==299){
            if((boolean)call("BaritoneBridge","available")){
                probe.check((boolean)call("BaritoneBridge","yields",new Class[]{String.class},"flight"),"Baritone navigation owns motion while enabled flight yields");
                call("BaritoneBridge","command",new Class[]{String.class},"cancel");
            }
            probe.enable("FLIGHT",false);
        }
    }
}
