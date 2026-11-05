package qa;

import java.util.*;
import java.lang.reflect.*;
import java.nio.file.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.world.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

public final class MemoryProbe implements ClientModInitializer {
    boolean started; int tick,failures;
    final boolean write=System.getProperty("wanderlust.qa.phase", "read").equals("write");
    void check(boolean result,String label){if(!result)failures++;System.out.println("MEMORY "+(result?"PASS ":"FAIL ")+label);}
    Object call(Object target,String name,Object...args)throws Exception{
        Class<?> cls=target instanceof Class<?> c?c:target.getClass();
        for(Method method:cls.getMethods())if(method.getName().equals(name)&&method.getParameterCount()==args.length){
            boolean fit=true;for(int i=0;i<args.length;i++)if(args[i]!=null&&!method.getParameterTypes()[i].isInstance(args[i]))fit=false;
            if(fit)return method.invoke(target instanceof Class<?>?null:target,args);
        }throw new NoSuchMethodException(name);
    }
    List<?> accounts()throws Exception{var f=Class.forName("dev.shabzup.wanderlust.Accounts").getDeclaredField("entries");f.setAccessible(true);return (List<?>)f.get(null);}
    void enable(String name,boolean on)throws Exception{Class cls=Class.forName("dev.shabzup.wanderlust.Feature");UpdateProbe.call("Settings","setEnabled",new Class[]{cls,boolean.class},Enum.valueOf(cls,name),on);}
    Object entryField(Object entry,String field)throws Exception{var m=entry.getClass().getDeclaredMethod(field);m.setAccessible(true);return m.invoke(entry);}
    List<GuiMessage> chat(Minecraft mc)throws Exception{var f=mc.gui.hud.getChat().getClass().getDeclaredField("allMessages");f.setAccessible(true);return new ArrayList<>((List<GuiMessage>)f.get(mc.gui.hud.getChat()));}
    void finish(Minecraft mc){System.out.println("MEMORY COMPLETE phase="+(write?"write":"read")+" failures="+failures);mc.stop();}
    public void onInitializeClient(){ClientTickEvents.END_CLIENT_TICK.register(mc->{try{run(mc);}catch(Throwable error){error.printStackTrace();System.out.println("MEMORY FAILED");mc.stop();}});}
    void run(Minecraft mc)throws Exception{
        if(!started&&mc.isGameLoadFinished()&&mc.gui.overlay()==null){
            started=true;org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().handle());
            if(write){
                UpdateProbe.call("Accounts","addOffline",new Class[]{String.class},"QA_Persist");
                Class<?> authClass=Class.forName("net.raphimc.minecraftauth.java.JavaAuthManager");
                Object http=call(Class.forName("net.raphimc.minecraftauth.MinecraftAuth"),"createHttpClient");
                Object builder=call(authClass,"create",http);
                Class<?> tokenClass=Class.forName("net.raphimc.minecraftauth.msa.model.MsaToken");
                Object token=tokenClass.getConstructor(long.class,String.class,String.class).newInstance(System.currentTimeMillis()+86400000,"fixture-access-v1","fixture-refresh-v1");
                Object auth=call(builder,"login",token);
                Object profile=Class.forName("net.raphimc.minecraftauth.java.model.MinecraftProfile").getConstructor(UUID.class,String.class).newInstance(UUID.fromString("00000000-0000-4000-8000-000000002602"),"QA_Microsoft");
                call(call(auth,"getMinecraftProfile"),"set",profile);
                UpdateProbe.call("Accounts","remember",new Class[]{String.class,authClass},"QA_Microsoft",auth);
                UpdateProbe.call("Accounts","saveNames");
                enable("ACTIVE_MODULES",true);enable("ESP",true);
                byte[] vault=Files.readAllBytes(mc.gameDirectory.toPath().resolve("config/wanderlust-accounts.dat"));
                String raw=new String(vault,java.nio.charset.StandardCharsets.ISO_8859_1);
                check(!raw.contains("fixture-refresh-v1")&&!raw.contains("QA_Microsoft"),"account vault excludes plaintext identity and refresh data");
                String settings=Files.readString(mc.gameDirectory.toPath().resolve("config/wanderlust.properties"));
                check(!settings.contains("fixture-access-v1")&&!settings.contains("fixture-refresh-v1"),"credentials are absent from general settings");
                check(accounts().size()==2,"offline and synthetic Microsoft account saved");finish(mc);return;
            }
            check(accounts().size()==2,"both accounts restored in a new Minecraft process");
            Object ms=accounts().stream().filter(e->{try{return entryField(e,"auth")!=null;}catch(Exception failure){throw new RuntimeException(failure);}}).findFirst().orElseThrow();
            Object auth=entryField(ms,"auth");
            Object msa=call(call(auth,"getMsaToken"),"getCached");
            check(call(msa,"getRefreshToken").equals("fixture-refresh-v1"),"Microsoft refresh data survives encrypted round trip");
            check(call(call(call(auth,"getMinecraftProfile"),"getCached"),"getName").equals("QA_Microsoft"),"cached Microsoft profile restored");
            check((boolean)UpdateProbe.call("Settings","on",new Class[]{String.class},"active-modules")&&(boolean)UpdateProbe.call("Settings","on",new Class[]{String.class},"esp"),"HUD toggle and enabled modules restored across processes");
            String name="Memory-"+System.currentTimeMillis();
            mc.createWorldOpenFlows().createFreshLevel(name,new LevelSettings(name,GameType.CREATIVE,new LevelSettings.DifficultySettings(Difficulty.PEACEFUL,false,false),true,WorldDataConfiguration.DEFAULT),new WorldOptions(123,false,false),WorldPresets::createTestWorldDimensions,new TitleScreen());
        }
        if(write||mc.player==null||mc.gui.overlay()!=null||mc.gui.screen()!=null)return;
        tick++;
        if(tick==25){
            int before=chat(mc).size();enable("HIGH_JUMP",true);enable("HIGH_JUMP",true);enable("HIGH_JUMP",false);
            List<GuiMessage> messages=chat(mc);check(messages.size()==before+2,"one local message per actual toggle, no duplicate on unchanged state");
            var notices=messages.stream().filter(m->m.content().getString().contains("HighJump")).toList();
            check(notices.stream().anyMatch(m->m.content().getString().contains("enabled"))&&notices.stream().anyMatch(m->m.content().getString().contains("disabled")),"ON and OFF notices appear in chat");
            check(notices.stream().allMatch(m->m.source().toString().contains("CLIENT")),"toggle notices are client system messages");
            check((boolean)UpdateProbe.call("ClientFeedback","visible"),"active list visible during gameplay");
            List<?> active=(List<?>)UpdateProbe.call("ClientFeedback","enabled");check(active.stream().anyMatch(f->f.toString().equals("ESP"))&&active.stream().noneMatch(f->f.toString().equals("HIGH_JUMP")),"active list reflects current enabled modules");
        }
        if(tick==30)Screenshot.grab(mc.gameDirectory,"memory-hud.png",mc.gameRenderer.mainRenderTarget(),1,c->{});
        if(tick==35){enable("ACTIVE_MODULES",false);check(!(boolean)UpdateProbe.call("ClientFeedback","visible"),"HUD toggle hides the list");UpdateProbe.call("Settings","load");check(!(boolean)UpdateProbe.call("Settings","on",new Class[]{String.class},"active-modules"),"disabled list preference saved immediately");enable("ACTIVE_MODULES",true);}
        if(tick==40){accounts().clear();UpdateProbe.call("Accounts","saveNames");UpdateProbe.call("Accounts","loadSaved");check(accounts().isEmpty(),"removed accounts stay removed after reloading the vault");finish(mc);}
    }
}
