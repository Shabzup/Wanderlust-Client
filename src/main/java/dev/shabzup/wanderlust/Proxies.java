package dev.shabzup.wanderlust;

import com.google.gson.*;
import com.sun.jna.Platform;
import com.sun.jna.platform.win32.Crypt32Util;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.local.LocalChannel;
import io.netty.handler.proxy.Socks5ProxyHandler;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import net.fabricmc.loader.api.FabricLoader;

/** SOCKS5 applies only to new Minecraft TCP channels, never to authentication HTTP. */
public final class Proxies {
    record Profile(String id,String name,String host,int port,String username,String password){
        @Override public String toString(){return name+" (SOCKS5)";}
    }
    private record Route(String id,InetSocketAddress address,Profile profile){}
    static final List<Profile> profiles=new ArrayList<>();
    private static volatile Route route;
    private static volatile int generation;
    static volatile String status="Direct connection";
    private static boolean readFailed;
    private static final Path path=FabricLoader.getInstance().getConfigDir().resolve("wanderlust-proxies.dat");
    private static final byte[] MAGIC="WLPR1".getBytes(StandardCharsets.US_ASCII),ENTROPY="WanderlustClient/proxies/v1".getBytes(StandardCharsets.UTF_8);

    static String activeId(){Route r=route;return r==null?"":r.id;}
    static void validate(String name,String host,int port,String user,String pass){
        if(name.isBlank()||name.length()>64)throw new IllegalArgumentException("Enter a profile name (1–64 characters).");
        if(host.isBlank()||host.length()>253||host.contains("://")||host.chars().anyMatch(Character::isWhitespace))throw new IllegalArgumentException("Enter a hostname or IP, without a protocol or port.");
        if(port<1||port>65535)throw new IllegalArgumentException("Port must be between 1 and 65535.");
        if(user.getBytes(StandardCharsets.UTF_8).length>255||pass.getBytes(StandardCharsets.UTF_8).length>255)throw new IllegalArgumentException("SOCKS5 credentials must be at most 255 UTF-8 bytes.");
        if(user.isEmpty()&&!pass.isEmpty())throw new IllegalArgumentException("A password requires a username.");
    }
    static void saveProfile(Profile old,String name,String host,int port,String user,String pass){
        if(readFailed)throw new IllegalArgumentException("Back up and remove the unreadable proxy vault before saving profiles.");
        validate(name,host,port,user,pass);
        Profile p=new Profile(old==null?UUID.randomUUID().toString():old.id,name,host,port,user,pass);
        if(old!=null)profiles.remove(old);profiles.add(p);if(activeId().equals(p.id))activate(p);else save();
    }
    static void direct(){generation++;route=null;status="Direct connection";save();}
    static void remove(Profile p){if(activeId().equals(p.id))direct();profiles.remove(p);save();}
    static void activate(Profile p){
        int request=++generation;route=new Route(p.id,null,p);status="Resolving proxy…";save();
        Thread.ofVirtual().name("Wanderlust proxy DNS").start(()->{
            try{InetSocketAddress address=new InetSocketAddress(p.host,p.port);if(address.isUnresolved())throw new IllegalArgumentException();if(generation==request){route=new Route(p.id,address,p);status="SOCKS5 · "+p.name;}}
            catch(RuntimeException ex){if(generation==request)status="Proxy unavailable. Choose Direct or edit the profile.";}
        });
    }
    public static void install(ChannelPipeline pipeline){
        Route r=route;if(r==null||pipeline.channel() instanceof LocalChannel||pipeline.get("wanderlust_proxy")!=null)return;
        if(r.address==null)throw new IllegalStateException("Wanderlust proxy is not ready. Check Proxies before connecting.");
        var handler=r.profile.username.isBlank()?new Socks5ProxyHandler(r.address):new Socks5ProxyHandler(r.address,r.profile.username,r.profile.password);
        handler.setConnectTimeoutMillis(10000);pipeline.addFirst("wanderlust_proxy",handler);
    }
    static void load(){
        profiles.clear();readFailed=false;generation++;route=null;status="Direct connection";
        if(!Files.isRegularFile(path))return;byte[] plain=null;
        try{
            if(!Platform.isWindows()||Files.size(path)>1024*1024)throw new IllegalStateException();
            byte[] bytes=Files.readAllBytes(path);if(bytes.length<=MAGIC.length||!Arrays.equals(MAGIC,Arrays.copyOf(bytes,MAGIC.length)))throw new IllegalArgumentException();
            plain=Crypt32Util.cryptUnprotectData(Arrays.copyOfRange(bytes,MAGIC.length,bytes.length),ENTROPY,1,null);
            JsonObject root=JsonParser.parseString(new String(plain,StandardCharsets.UTF_8)).getAsJsonObject();
            if(root.get("version").getAsInt()!=1)throw new IllegalArgumentException();
            List<Profile> loaded=new ArrayList<>();
            for(JsonElement e:root.getAsJsonArray("profiles")){JsonObject o=e.getAsJsonObject();Profile p=new Profile(o.get("id").getAsString(),o.get("name").getAsString(),o.get("host").getAsString(),o.get("port").getAsInt(),o.get("username").getAsString(),o.get("password").getAsString());validate(p.name,p.host,p.port,p.username,p.password);loaded.add(p);}
            profiles.addAll(loaded);String id=root.get("active").getAsString();if(!id.isEmpty()){
                Profile p=profiles.stream().filter(v->v.id.equals(id)).findFirst().orElseThrow();activate(p);
            }
        }catch(Exception|LinkageError ex){readFailed=true;route=new Route("unavailable",null,null);status="Cannot read proxy vault. Direct is available; the file is preserved.";}
        finally{if(plain!=null)Arrays.fill(plain,(byte)0);}
    }
    private static void save(){
        if(readFailed)return;byte[] plain=null;
        try{
            if(!Platform.isWindows()){status="Profiles are session-only here; encrypted saving requires Windows.";return;}
            JsonObject root=new JsonObject();root.addProperty("version",1);root.addProperty("active",activeId());root.add("profiles",new Gson().toJsonTree(profiles));plain=root.toString().getBytes(StandardCharsets.UTF_8);
            byte[] encrypted=Crypt32Util.cryptProtectData(plain,ENTROPY,1,"Wanderlust proxy vault",null);byte[] bytes=Arrays.copyOf(MAGIC,MAGIC.length+encrypted.length);System.arraycopy(encrypted,0,bytes,MAGIC.length,encrypted.length);
            Files.createDirectories(path.getParent());Path temp=path.resolveSibling(path.getFileName()+".tmp");Files.write(temp,bytes);try{Files.move(temp,path,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}catch(AtomicMoveNotSupportedException ex){Files.move(temp,path,StandardCopyOption.REPLACE_EXISTING);}
        }catch(Exception|LinkageError ex){status="Proxy profile is active for this session; encrypted saving failed.";}
        finally{if(plain!=null)Arrays.fill(plain,(byte)0);}
    }
}
