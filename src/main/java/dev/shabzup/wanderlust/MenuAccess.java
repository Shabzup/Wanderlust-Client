package dev.shabzup.wanderlust;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;

public final class MenuAccess {
    static void init(){
        ScreenEvents.AFTER_INIT.register((mc,screen,w,h)->{
            if(screen instanceof JoinMultiplayerScreen){
                int gap=6,bw=Math.min(106,(w-32)/3),x=(w-3*bw-2*gap)/2;
                add(screen,x,34,bw,"Accounts",()->mc.gui.setScreen(new AccountScreen(screen)));
                add(screen,x+bw+gap,34,bw,"UI Settings",()->mc.gui.setScreen(new ControlPanel(screen,true)));
                add(screen,x+2*(bw+gap),34,bw,"Proxies",()->mc.gui.setScreen(new ProxyScreen(screen)));
            }else if(screen instanceof TitleScreen)add(screen,8,8,100,"UI Settings",()->mc.gui.setScreen(new ControlPanel(screen,true)));
        });
    }
    private static void add(Screen screen,int x,int y,int w,String label,Runnable action){
        var b=new PanelButton(x,y,w,22,label,PanelButton.Kind.Navigation,()->false,null,action);
        b.setTooltip(net.minecraft.client.gui.components.Tooltip.create(PanelStyle.text(label.equals("Proxies")?"SOCKS5 profiles · applies to new game connections":"Wanderlust · "+label)));
        Screens.getWidgets(screen).add(b);
    }
    public static boolean key(long window,int action,KeyEvent event){
        var mc=Minecraft.getInstance();
        if(window!=mc.getWindow().handle()||mc.gui.screen()==null||mc.gui.screen() instanceof BindScreen)return false;
        KeyMapping key=KeyMapping.get("key.wanderlust.menu");
        if(key==null||!key.matches(event))return false;
        if(action==GLFW.GLFW_PRESS)toggle(mc);
        return true;
    }
    static void toggle(Minecraft mc){
        Screen screen=mc.gui.screen();
        if(screen instanceof ControlPanel panel&&!panel.settingsEntry())panel.onClose();
        else mc.gui.setScreen(new ControlPanel(screen,false));
    }
}
