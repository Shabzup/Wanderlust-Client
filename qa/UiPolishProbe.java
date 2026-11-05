package qa;

import java.util.*;
import java.lang.reflect.*;
import java.util.concurrent.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.input.*;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/** Responsive geometry plus real keyboard/menu transitions, screenshots and local SOCKS5 checks. */
public final class UiPolishProbe implements ClientModInitializer {
    int tick,failures,checks,worldTick;boolean started,done,worldLoading;Screen panel,title,multi;long hold;
    CompletableFuture<Void> proxy;List<Double> extraction=new ArrayList<>();
    synchronized void check(boolean ok,String label){checks++;if(!ok)failures++;System.out.println("POLISH "+(ok?"PASS ":"FAIL ")+label);}
    static Object field(Object o,String name)throws Exception{Class c=o.getClass();while(c!=null){try{var f=c.getDeclaredField(name);f.setAccessible(true);return f.get(o);}catch(NoSuchFieldException e){c=c.getSuperclass();}}throw new NoSuchFieldException(name);}
    static void set(Object o,String name,Object value)throws Exception{Class c=o.getClass();while(c!=null){try{var f=c.getDeclaredField(name);f.setAccessible(true);f.set(o,value);return;}catch(NoSuchFieldException e){c=c.getSuperclass();}}throw new NoSuchFieldException(name);}
    Screen create(String name,Screen parent)throws Exception{var c=Class.forName("dev.shabzup.wanderlust."+name).getDeclaredConstructor(Screen.class);c.setAccessible(true);return (Screen)c.newInstance(parent);}
    Screen modules(Screen parent,boolean settings)throws Exception{var c=Class.forName("dev.shabzup.wanderlust.ControlPanel").getDeclaredConstructor(Screen.class,boolean.class);c.setAccessible(true);return (Screen)c.newInstance(parent,settings);}
    void option(String key,Object value)throws Exception{var f=Class.forName("dev.shabzup.wanderlust.Settings").getDeclaredField("options");f.setAccessible(true);for(Object o:(List)f.get(null)){if(field(o,"key").equals(key)){UpdateProbe.call("Settings",value instanceof Number?"setNumber":value instanceof Boolean?"setToggle":"setChoice",new Class[]{o.getClass(),value instanceof Number?double.class:value instanceof Boolean?boolean.class:String.class},o,value);return;}}throw new IllegalArgumentException(key);}
    List<AbstractWidget> widgets(Screen s){return s.children().stream().filter(c->c instanceof AbstractWidget).map(c->(AbstractWidget)c).toList();}
    Button button(Screen s,String label){return widgets(s).stream().filter(w->w instanceof Button&&w.getMessage().getString().equals(label)).map(w->(Button)w).findFirst().orElseThrow();}
    void bounds(Screen s,String context){var ws=widgets(s);boolean fit=true,overlap=false;for(int i=0;i<ws.size();i++){var a=ws.get(i);fit&=a.getWidth()>0&&a.getX()>=0&&a.getRight()<=s.width&&a.getY()>=0&&a.getBottom()<=s.height;for(int j=i+1;j<ws.size();j++){var b=ws.get(j);overlap|=a.getX()<b.getRight()&&a.getRight()>b.getX()&&a.getY()<b.getBottom()&&a.getBottom()>b.getY();}}check(fit&&!overlap,context+" widgets fit without overlap");}
    void layout(Screen s,String context)throws Exception{
        var positions=(Map<?,int[]>)field(s,"positions");int w=(int)field(s,"columnWidth"),maxX=(int)field(s,"workspaceWidth"),bottom=(int)field(s,"workspaceBottom"),top=(int)field(s,"homeTop");
        var m=s.getClass().getDeclaredMethod("windowHeight",Class.forName("dev.shabzup.wanderlust.Feature$Group"));m.setAccessible(true);var list=new ArrayList<>(positions.entrySet());boolean good=true;
        for(int i=0;i<list.size();i++){int[] a=list.get(i).getValue();int h=(int)m.invoke(s,list.get(i).getKey());good&=a[0]>=0&&a[0]+w<=maxX&&a[1]>=top&&a[1]+h<=bottom;for(int j=i+1;j<list.size();j++){int[] b=list.get(j).getValue();int bh=(int)m.invoke(s,list.get(j).getKey());good&=!(a[0]<b[0]+w&&a[0]+w>b[0]&&a[1]<b[1]+bh&&a[1]+h>b[1]);}}
        check(good,context+" category windows fit without overlap");bounds(s,context);
    }
    void key(Minecraft mc)throws Exception{var m=KeyboardHandler.class.getDeclaredMethod("keyPress",long.class,int.class,KeyEvent.class);m.setAccessible(true);m.invoke(mc.keyboardHandler,mc.getWindow().handle(),GLFW.GLFW_PRESS,new KeyEvent(GLFW.GLFW_KEY_RIGHT_SHIFT,0,0));m.invoke(mc.keyboardHandler,mc.getWindow().handle(),GLFW.GLFW_RELEASE,new KeyEvent(GLFW.GLFW_KEY_RIGHT_SHIFT,0,0));}
    void close(Minecraft mc,Screen original)throws Exception{Screen s=mc.gui.screen();key(mc);if(mc.gui.screen()==s&&s.getClass().getSimpleName().equals("ControlPanel")){set(s,"closingAt",System.nanoTime()-1_000_000_000L);s.tick();}check(mc.gui.screen()==original,"Right Shift restores "+original.getClass().getSimpleName());}
    void shot(Minecraft mc,String name){Screenshot.grab(mc.gameDirectory,"polish-"+name+".png",mc.gameRenderer.mainRenderTarget(),1,c->{});}
    public void onInitializeClient(){ClientTickEvents.END_CLIENT_TICK.register(mc->{if(done)return;try{run(mc);}catch(Throwable e){e.printStackTrace();System.out.println("POLISH FAILED");done=true;mc.stop();}});}
    void run(Minecraft mc)throws Exception{
        if(!started){if(!mc.isGameLoadFinished()||mc.gui.overlay()!=null)return;started=true;GLFW.glfwHideWindow(mc.getWindow().handle());GLFW.glfwSetWindowSize(mc.getWindow().handle(),1920,1080);hold=System.currentTimeMillis()+500;title=new TitleScreen();mc.gui.setScreen(title);}
        GLFW.glfwSetCursorPos(mc.getWindow().handle(),0,0);
        if(System.currentTimeMillis()<hold)return;
        if(worldLoading){
            if(mc.player==null||mc.gui.overlay()!=null)return;worldTick++;
            if(worldTick==20){mc.gui.setScreen(null);KeyMapping.click(com.mojang.blaze3d.platform.InputConstants.getKey("key.keyboard.right.shift"));}
            if(worldTick==22){panel=mc.gui.screen();check(panel!=null&&panel.getClass().getSimpleName().equals("ControlPanel"),"Right Shift opens module workspace in an integrated world");check(!panel.isPauseScreen(),"in-game popup does not pause simulation");set(panel,"opened",System.nanoTime()-1_000_000_000L);layout(panel,"In-game popup");}
            if(worldTick==35){shot(mc,"ingame-translucency");option("ui.motion",false);key(mc);}
            if(worldTick==38){check(mc.gui.screen()==null,"reduced-motion close returns to gameplay");option("ui.motion",true);System.out.println("POLISH COMPLETE checks="+checks+" failures="+failures);done=true;mc.stop();}
            return;
        }
        tick++;
        if(tick==3){
            check(widgets(title).stream().filter(w->w.getMessage().getString().equals("UI Settings")).count()==1,"title screen replaces Wanderlust with styled UI Settings");
            check(widgets(title).stream().noneMatch(w->Set.of("Wanderlust","Accounts","Proxies").contains(w.getMessage().getString())),"account and proxy entry points are absent from title screen");
            key(mc);panel=mc.gui.screen();check(panel.getClass().getSimpleName().equals("ControlPanel"),"real keyboard handler opens modules from title screen");close(mc,title);
            Screen worlds=new net.minecraft.client.gui.screens.worldselection.SelectWorldScreen(title);mc.gui.setScreen(worlds);key(mc);check(mc.gui.screen().getClass().getSimpleName().equals("ControlPanel"),"Right Shift opens modules from world list");close(mc,worlds);
            Screen arbitrary=new Screen(Component.literal("Other out-of-game screen")){};mc.gui.setScreen(arbitrary);key(mc);check(mc.gui.screen().getClass().getSimpleName().equals("ControlPanel"),"Right Shift opens modules from an arbitrary screen");close(mc,arbitrary);
            multi=new net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen(title);mc.gui.setScreen(multi);for(String name:List.of("Accounts","UI Settings","Proxies"))check(button(multi,name).getClass().getSimpleName().equals("PanelButton"),"multiplayer has styled "+name+" button");
            bounds(multi,"Multiplayer toolbar, title, list and footer");check(((net.minecraft.client.gui.layouts.HeaderAndFooterLayout)field(multi,"layout")).getHeaderHeight()>=62,"multiplayer list reserves room for the toolbar");
            key(mc);check(widgets(mc.gui.screen()).stream().noneMatch(w->Set.of("Accounts","Proxies").contains(w.getMessage().getString())),"module workspace does not duplicate multiplayer account/proxy controls");close(mc,multi);
            button(multi,"UI Settings").onPress(null);Screen settings=mc.gui.screen();check((boolean)field(settings,"uiSettings"),"menu UI Settings opens the settings dialog directly");key(mc);check(mc.gui.screen()!=settings&&!(boolean)field(mc.gui.screen(),"uiSettings"),"Right Shift opens an independent module workspace over UI Settings");close(mc,settings);
            proxy=CompletableFuture.runAsync(()->{try{ProxyProtocolProbe.run(this);}catch(Exception e){throw new CompletionException(e);}});
        }
        if(tick==5){
            int[][] sizes={{320,240},{400,300},{512,384},{640,360},{640,400},{683,384},{720,450},{800,450},{960,540},{960,600},{1280,540},{1280,720},{1280,800},{1720,720},{1920,1080}};
            for(boolean fitting:new boolean[]{false,true})for(int[] size:sizes)for(double scale:new double[]{40,70,100})for(String density:List.of("Compact","Comfortable","Spacious")){
                option("ui.auto-fit",fitting);option("ui.scale",scale);option("ui.density",density);Screen s=modules(title,false);s.init(size[0],size[1]);layout(s,size[0]+"x"+size[1]+" "+scale+"% "+density+" auto-fit="+fitting);
            }
            option("ui.auto-fit",true);option("ui.scale",70d);option("ui.density","Comfortable");
            for(int[] size:sizes)for(String name:List.of("AccountScreen","ProxyScreen","BaritoneScreen","EntitySelectionScreen")){
                Screen s=create(name,title);s.init(size[0],size[1]);bounds(s,name+" "+size[0]+"x"+size[1]);
            }
            for(int[] size:sizes){Screen s=modules(title,true);s.init(size[0],size[1]);bounds(s,"UI Settings "+size[0]+"x"+size[1]);}
            Screen s=modules(title,false);mc.gui.setScreen(s);panel=s;set(s,"opened",System.nanoTime()-1_000_000_000L);
            var positions=(Map<?,int[]>)field(s,"positions");int[] first=positions.values().iterator().next();float scale=(float)field(s,"viewScale");
            s.mouseClicked(new MouseButtonEvent((first[0]+30)*scale,(first[1]+40)*scale,new MouseButtonInfo(0,0)),false);
            check((boolean)UpdateProbe.call("Settings","on",new Class[]{String.class},"kill-aura"),"left click on a scaled module row toggles it");
            s.mouseClicked(new MouseButtonEvent((first[0]+30)*scale,(first[1]+40)*scale,new MouseButtonInfo(1,0)),false);
            check(field(s,"detail")!=null,"right click on scaled module row opens settings");
            button(s,"Entity list").onPress(null);set(s,"closingAt",System.nanoTime()-1_000_000_000L);s.tick();Screen entities=mc.gui.screen();button(entities,"None").onPress(null);
            check(((Number)UpdateProbe.call("EntityTargets","count",new Class[0])).intValue()==0,"entity picker None clears selections");
            var search=widgets(entities).stream().filter(c->c instanceof EditBox).map(c->(EditBox)c).findFirst().orElseThrow();search.setValue("zombie");
            int xx=(int)field(entities,"left"),yy=(int)field(entities,"top");entities.mouseClicked(new MouseButtonEvent(xx+60,yy+130,new MouseButtonInfo(0,0)),false);
            check(((Number)UpdateProbe.call("EntityTargets","count",new Class[0])).intValue()==1,"entity picker filtered row selects one type");
            entities.onClose();button(s,"‹  Back").onPress(null);button(s,"UI Settings").onPress(null);button(s,"85%").onPress(null);
            check((double)UpdateProbe.call("Settings","number",new Class[]{String.class},"ui.scale")==85,"size preset applies requested scale");button(s,"70%").onPress(null);button(s,"‹  Back").onPress(null);
            for(int sample=0;sample<70;sample++){
                long begin=System.nanoTime();s.extractRenderState(new net.minecraft.client.gui.GuiGraphicsExtractor(mc,new net.minecraft.client.renderer.state.gui.GuiRenderState(),-1,-1),-1,-1,0);
                if(sample>=10)extraction.add((System.nanoTime()-begin)/1_000_000d);
            }
            extraction.sort(Double::compare);System.out.println("POLISH PERF extraction median_ms="+extraction.get(30)+" p95_ms="+extraction.get(57)+" samples=60; CPU only, excludes GPU presentation");
            check(extraction.get(57)<16.7,"local UI CPU extraction p95 stays within one 60 Hz frame budget");
            hold=System.currentTimeMillis()+650;
        }
        if(tick==7){shot(mc,"workspace-1080p");button(panel,"UI Settings").onPress(null);hold=System.currentTimeMillis()+400;}
        if(tick==9){shot(mc,"settings-1080p");mc.gui.setScreen(multi);hold=System.currentTimeMillis()+350;}
        if(tick==11){shot(mc,"multiplayer-1080p");button(multi,"Accounts").onPress(null);hold=System.currentTimeMillis()+400;}
        if(tick==13){shot(mc,"accounts-1080p");mc.gui.setScreen(create("ProxyScreen",multi));hold=System.currentTimeMillis()+400;}
        if(tick==15){shot(mc,"proxies-1080p");mc.gui.setScreen(create("EntitySelectionScreen",multi));hold=System.currentTimeMillis()+400;}
        if(tick==17){shot(mc,"entities-1080p");GLFW.glfwSetWindowSize(mc.getWindow().handle(),1280,720);mc.gui.setScreen(modules(title,false));hold=System.currentTimeMillis()+700;}
        if(tick==20){shot(mc,"workspace-720p");GLFW.glfwSetWindowSize(mc.getWindow().handle(),640,480);mc.gui.setScreen(modules(title,false));hold=System.currentTimeMillis()+700;}
        if(tick==23){shot(mc,"workspace-minimum");GLFW.glfwSetWindowSize(mc.getWindow().handle(),1920,1080);mc.gui.setScreen(modules(title,false));hold=System.currentTimeMillis()+700;}
        if(tick==26){GLFW.glfwSetWindowSize(mc.getWindow().handle(),3840,2160);mc.gui.setScreen(modules(title,false));hold=System.currentTimeMillis()+1000;}
        if(tick==29){shot(mc,"workspace-4k");GLFW.glfwSetWindowSize(mc.getWindow().handle(),1920,1080);mc.gui.setScreen(create("ProxyScreen",multi));button(mc.gui.screen(),"New SOCKS5 profile").onPress(null);hold=System.currentTimeMillis()+400;}
        if(tick==31){
            Screen editor=mc.gui.screen();for(var w:widgets(editor))if(w instanceof EditBox edit){switch(edit.getMessage().getString()){case "Profile name"->edit.setValue("QA profile");case "Proxy hostname / IP"->edit.setValue("127.0.0.1");case "Port"->edit.setValue("99999");case "Username (optional)"->edit.setValue("qa-user");case "Password (optional)"->edit.setValue("qa-password");}}
            button(editor,"Save profile").onPress(null);check(mc.gui.screen()==editor&&!((String)field(editor,"error")).isBlank(),"proxy editor validates an invalid port without closing");bounds(editor,"Proxy editor");
            var password=widgets(editor).stream().filter(w->w instanceof EditBox&&w.getMessage().getString().equals("Password (optional)")).map(w->(EditBox)w).findFirst().orElseThrow();
            var format=EditBox.class.getDeclaredMethod("applyFormat",String.class,int.class);format.setAccessible(true);var glyphs=(net.minecraft.util.FormattedCharSequence)format.invoke(password,password.getValue(),0);StringBuilder rendered=new StringBuilder();glyphs.accept((i,style,codepoint)->{rendered.appendCodePoint(codepoint);return true;});
            check(rendered.toString().equals("•".repeat(password.getValue().length())),"password field formats every character as a masked bullet");
            hold=System.currentTimeMillis()+400;}
        if(tick==33){shot(mc,"proxy-editor-1080p");mc.gui.setScreen(modules(title,false));hold=System.currentTimeMillis()+400;}
        if(tick>=35&&proxy.isDone()){
            proxy.join();check(widgets(mc.gui.screen()).stream().noneMatch(w->Set.of("Accounts","Proxies").contains(w.getMessage().getString())),"final workspace has only applicable navigation");
            Class feature=Class.forName("dev.shabzup.wanderlust.Feature");UpdateProbe.call("Settings","setEnabled",new Class[]{feature,boolean.class},Enum.valueOf(feature,"KILL_AURA"),false);
            worldLoading=true;String name="Polish-"+System.currentTimeMillis();
            mc.createWorldOpenFlows().createFreshLevel(name,new net.minecraft.world.level.LevelSettings(name,net.minecraft.world.level.GameType.CREATIVE,new net.minecraft.world.level.LevelSettings.DifficultySettings(net.minecraft.world.Difficulty.PEACEFUL,false,false),true,net.minecraft.world.level.WorldDataConfiguration.DEFAULT),new net.minecraft.world.level.levelgen.WorldOptions(123,false,false),net.minecraft.world.level.levelgen.presets.WorldPresets::createTestWorldDimensions,new TitleScreen());
        }
    }
}
