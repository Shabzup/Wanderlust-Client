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
import org.lwjgl.glfw.GLFW;

public final class WorkspaceProbe implements ClientModInitializer {
    Screen panel; boolean loading,playing; int tick,failures; int rows70; float width70; int toolbarX; long holdUntil;
    void check(boolean ok,String label){if(!ok)failures++;System.out.println("WORKSPACE "+(ok?"PASS ":"FAIL ")+label);}
    Object field(String name)throws Exception{var f=panel.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(panel);}
    Object invoke(String name,Class<?>[] types,Object...args)throws Exception{var m=panel.getClass().getDeclaredMethod(name,types);m.setAccessible(true);return m.invoke(panel,args);}
    float scale()throws Exception{return (float)field("viewScale");}
    Class<?> groupClass()throws Exception{return Class.forName("dev.shabzup.wanderlust.Feature$Group");}
    Object group(String name)throws Exception{return Enum.valueOf((Class)groupClass(),name);}
    int[] pos(String name)throws Exception{return ((Map<?,int[]>)field("positions")).get(group(name));}
    int height(String name)throws Exception{return (int)invoke("windowHeight",new Class[]{groupClass()},group(name));}
    List<?> features(String name)throws Exception{return (List<?>)invoke("features",new Class[]{groupClass()},group(name));}
    boolean on(String id)throws Exception{return (boolean)UpdateProbe.call("Settings","on",new Class[]{String.class},id);}
    void enable(String name,boolean on)throws Exception{Class cls=Class.forName("dev.shabzup.wanderlust.Feature");UpdateProbe.call("Settings","setEnabled",new Class[]{cls,boolean.class},Enum.valueOf(cls,name),on);}
    void number(String key,double value)throws Exception{var f=Class.forName("dev.shabzup.wanderlust.Settings").getDeclaredField("options");f.setAccessible(true);for(Object o:(List)f.get(null)){var k=o.getClass().getDeclaredMethod("key");k.setAccessible(true);if(k.invoke(o).equals(key)){UpdateProbe.call("Settings","setNumber",new Class[]{o.getClass(),double.class},o,value);return;}}}
    void nativeClick(double x,double y,int button){panel.mouseClicked(new MouseButtonEvent(x,y,new MouseButtonInfo(button,0)),false);}
    void click(double x,double y,int button)throws Exception{nativeClick(x*scale(),y*scale(),button);}
    Button button(String name){return panel.children().stream().filter(w->w instanceof Button b&&b.getMessage().getString().equals(name)).map(w->(Button)w).findFirst().orElseThrow();}
    void press(String name){Button b=button(name);nativeClick(b.getX()+b.getWidth()/2,b.getY()+10,0);}
    void fold(String name)throws Exception{int[] p=pos(name);click(p[0]+(int)field("columnWidth")-15,p[1]+13,0);}
    void settled()throws Exception{var f=panel.getClass().getDeclaredField("opened");f.setAccessible(true);f.setLong(panel,System.nanoTime()-2_000_000_000L);}
    void newPanel(Minecraft mc)throws Exception{var c=Class.forName("dev.shabzup.wanderlust.ControlPanel").getDeclaredConstructor();c.setAccessible(true);panel=(Screen)c.newInstance();mc.gui.setScreen(panel);settled();}
    void size(int value)throws Exception{number("ui.scale",value);panel.resize(panel.width,panel.height);settled();}
    void escape(){panel.keyPressed(new KeyEvent(GLFW.GLFW_KEY_ESCAPE,0,0));}
    EditBox search()throws Exception{return (EditBox)field("search");}
    void layout()throws Exception{
        var map=(Map<?,int[]>)field("positions");int w=(int)field("columnWidth"),cw=(int)field("workspaceWidth"),bottom=(int)field("workspaceBottom");
        List<?> groups=List.of(groupClass().getEnumConstants());boolean ok=true;
        for(int i=0;i<groups.size();i++){int[] a=map.get(groups.get(i));int ah=(int)invoke("windowHeight",new Class[]{groupClass()},groups.get(i));
            if(a[0]<0||a[0]+w>cw||a[1]<0||a[1]+ah>bottom)ok=false;
            for(int j=i+1;j<groups.size();j++){int[] b=map.get(groups.get(j));int bh=(int)invoke("windowHeight",new Class[]{groupClass()},groups.get(j));if(a[0]<b[0]+w&&a[0]+w>b[0]&&a[1]<b[1]+bh&&a[1]+ah>b[1])ok=false;}
        }
        check(ok,"all categories fit without overlap at "+panel.width+"x"+panel.height+" / "+Math.round(scale()*100)+"%");
    }
    void shot(Minecraft mc,String name){Screenshot.grab(mc.gameDirectory,name+".png",mc.gameRenderer.mainRenderTarget(),1,c->{});}
    public void onInitializeClient(){ClientTickEvents.END_CLIENT_TICK.register(mc->{try{run(mc);}catch(Throwable e){failures++;e.printStackTrace();System.out.println("WORKSPACE FAILED");mc.stop();}});}
    void run(Minecraft mc)throws Exception{
        if(!loading&&mc.isGameLoadFinished()&&mc.gui.overlay()==null){
            loading=true;GLFW.glfwHideWindow(mc.getWindow().handle());String name="Workspace-"+System.currentTimeMillis();
            mc.createWorldOpenFlows().createFreshLevel(name,new LevelSettings(name,GameType.CREATIVE,new LevelSettings.DifficultySettings(Difficulty.PEACEFUL,false,false),true,WorldDataConfiguration.DEFAULT),new WorldOptions(123,false,false),WorldPresets::createTestWorldDimensions,new TitleScreen());
        }
        if(mc.player==null||mc.gui.overlay()!=null||System.currentTimeMillis()<holdUntil)return;
        if(!playing){if(mc.gui.screen()!=null)return;playing=true;newPanel(mc);}
        tick++;
        if(tick==5){
            rows70=(int)field("maxRows");width70=(int)field("columnWidth")*scale();toolbarX=button("UI Settings").getX();
            check(Math.abs((int)field("workspaceWidth")*scale()-panel.width)<1,"70% list scale retains fullscreen horizontal workspace");
            check(Math.abs((int)field("workspaceHeight")*scale()-panel.height)<1,"70% list scale retains fullscreen vertical workspace");layout();
            size(100);check(rows70>(int)field("maxRows"),"smaller lists show more module rows");check(width70<(int)field("columnWidth")*scale(),"actual category windows shrink with size setting");
            check(button("UI Settings").getX()==toolbarX,"toolbar position stays fixed across list sizes");layout();
            size(40);check(Math.abs((int)field("workspaceWidth")*scale()-panel.width)<1,"40% list scale also covers fullscreen workspace");layout();size(70);
        }
        if(tick==7){
            fold("Combat");check(height("Combat")==27,"title collapse button reduces category to its header");
            check((int)field("draggingGroup")==-1,"collapse button does not start a drag");
            int[] p=pos("Combat");check((int)invoke("rowIndex",new Class[]{groupClass(),double.class,double.class,long.class},group("Combat"),p[0]+30.,p[1]+35.,System.nanoTime())==-1,"collapsed category cannot toggle hidden rows");
            newPanel(mc);check(height("Combat")==27,"category collapse preference survives reopening");fold("Combat");
            check(height("Combat")>27,"title expand button restores module rows");
        }
        if(tick==9){
            int[] p=pos("Combat");int w=(int)field("columnWidth");click(p[0]+w-14,p[1]+36,0);
            check(field("detail").toString().equals("KILL_AURA"),"visible ellipsis button opens module settings with left click");
            check(!on("kill-aura"),"settings button does not enable module");escape();check(field("detail")==null&&!(boolean)field("closing"),"Escape returns from settings to module lists");
            p=pos("Combat");click(p[0]+30,p[1]+36,0);check(on("kill-aura"),"scaled row left click enables intended module");
            click(p[0]+30,p[1]+36,1);check(field("detail").toString().equals("KILL_AURA"),"scaled row right click still opens settings");escape();enable("KILL_AURA",false);
        }
        if(tick==11){
            fold("Combat");panel.keyPressed(new KeyEvent(GLFW.GLFW_KEY_F,0,GLFW.GLFW_MOD_CONTROL));
            check(search().isFocused(),"Ctrl+F focuses full-size search box");search().setValue("KillAura");
            check(height("Combat")>27&&!features("Combat").isEmpty(),"search opens matching collapsed categories");
            press("Clear");check(((String)field("query")).isEmpty(),"clear button removes search filter");search().setValue("Speed");escape();
            check(((String)field("query")).isEmpty()&&!(boolean)field("closing"),"Escape clears search without closing client panel");
            enable("SPEED",true);press("Active only");check(features("Movement").size()==1&&features("Movement").get(0).toString().equals("SPEED")&&features("Combat").isEmpty(),"active-only filter displays enabled modules");press("Active only");enable("SPEED",false);
        }
        if(tick==13){
            press("UI Settings");int x=(int)field("dialogX"),y=(int)field("optionStart"),w=(int)field("dialogW"),pitch=(int)field("optionPitch");
            nativeClick(x+w-20,y+pitch+25,0);panel.mouseReleased(new MouseButtonEvent(x+w-20,y+pitch+25,new MouseButtonInfo(0,0)));
            check(Math.abs(scale()-1)<.001,"unscaled settings slider receives native mouse coordinates");check((int)field("dialogX")==x,"settings dialog stays fixed while resizing lists");escape();size(70);
            press("Fold all");check(height("Combat")==27&&height("Movement")==27&&height("Misc")==27,"Fold all collapses every category");
            press("Expand all");check(height("Combat")>27&&height("Movement")>27&&height("Misc")>27,"Expand all restores category contents");layout();
        }
        if(tick==15){
            fold("Combat");int[] p=pos("Combat");int w=(int)field("columnWidth");click(p[0]+12,p[1]+13,0);
            panel.mouseDragged(new MouseButtonEvent(panel.width-5,panel.height-20,new MouseButtonInfo(0,0)),10000,10000);
            check((p[0]+w)*scale()>panel.width-8,"drag can reach full screen right edge at 70% size");
            check((p[1]+height("Combat"))*scale()>panel.height-28,"collapsed category can reach bottom of fullscreen workspace");
            panel.mouseReleased(new MouseButtonEvent(panel.width-5,panel.height-20,new MouseButtonInfo(0,0)));layout();fold("Combat");layout();
            panel.resize(640,360);settled();layout();panel.resize(320,240);settled();layout();
            boolean widgetsFit=panel.children().stream().filter(c->c instanceof AbstractWidget).map(c->(AbstractWidget)c).allMatch(c->c.getX()>=0&&c.getRight()<=panel.width&&c.getY()>=0&&c.getBottom()<=panel.height);
            check(widgetsFit,"toolbar and search controls fit the smallest tested screen");panel.resize(720,450);settled();
        }
        if(tick==17){int[] p=pos("Combat");panel.mouseScrolled((p[0]+30)*scale(),(p[1]+60)*scale(),0,-1);check(((Map<?,Integer>)field("scrolls")).get(group("Combat"))==3,"mouse wheel scrolls module list by three rows");}
        if(tick==19){fold("Movement");((Map)field("scrolls")).put(group("Combat"),0);enable("AUTO_TOTEM",true);enable("ACTIVE_MODULES",true);holdUntil=System.currentTimeMillis()+800;}
        if(tick==22)shot(mc,"workspace-main");
        if(tick==24){press("UI Settings");check((boolean)field("uiSettings"),"full-size toolbar opens UI settings after dragging and resizing");holdUntil=System.currentTimeMillis()+800;}
        if(tick==27)shot(mc,"workspace-settings");
        if(tick==30){System.out.println("WORKSPACE COMPLETE failures="+failures);mc.stop();}
    }
}
