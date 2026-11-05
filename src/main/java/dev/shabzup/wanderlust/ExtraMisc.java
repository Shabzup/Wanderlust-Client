package dev.shabzup.wanderlust;

import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.io.*;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.multiplayer.*;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;

public final class ExtraMisc {
    private static final Map<String,Long> next=new HashMap<>();
    private static final Set<String> nearby=new HashSet<>(),messaged=new HashSet<>();
    private static final Deque<String> packetLog=new ArrayDeque<>();
    private static ServerData lastServer;
    private static long reconnectAt;
    private static int note;
    private static volatile boolean discord;
    static void options(){
        for(String id:new String[]{"spam","message-aura","dm-spam","auto-endorse","auto-sell"}){
            Settings.string(id,id+".message","Message / command (empty disables)","");
            Settings.numeric(id,id+".seconds","Interval (seconds)",30,1,600,1);
        }
        Settings.string("dm-spam","dm-spam.recipient","Recipient username","");
        Settings.numeric("auto-reconnect","auto-reconnect.seconds","Reconnect delay (seconds)",5,1,60,1);
        Settings.string("packet-canceller","packet-canceller.classes","Packet class names to cancel","");
        Settings.string("server-spoof","server-spoof.brand","Client brand","vanilla");
        Settings.string("sound-blocker","sound-blocker.ids","Blocked sound IDs (comma separated)","minecraft:entity.bat.ambient");
        Settings.string("chat-utility","chat-utility.prefix","Outgoing chat prefix","");
        Settings.string("book-bot","book-bot.text","Book text (use | between pages)","WanderlustClient");
        Settings.numeric("notebot","notebot.delay","Ticks per note",5,1,40,1);
        Settings.string("notebot","notebot.notes","Note pitches (0–24, comma separated)","0,4,7,12");
        Settings.string("discord-presence","discord-presence.app","Discord application ID","");
        Settings.string("discord-presence","discord-presence.details","Activity text","Playing Minecraft with Wanderlust");
        Settings.secret("auto-login","auto-login.command","Login command (memory only)","");
    }
    static void init(){
        ClientReceiveMessageEvents.GAME.register((message,overlay)->{
            if(Wanderlust.on("auto-login")&&message.getString().toLowerCase(Locale.ROOT).contains("login")&&due("login",5000))send(Minecraft.getInstance(),Settings.text("auto-login.command"));
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler,mc)->{
            reconnectAt=0;nearby.clear();messaged.clear();
            if(on("disconnect-sound"))mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP,1));
        });
    }
    static void tick(Minecraft mc){
        if(mc.getCurrentServer()!=null)lastServer=mc.getCurrentServer();
        if(on("auto-reconnect")&&lastServer!=null&&mc.gui.screen() instanceof DisconnectedScreen){
            if(reconnectAt==0)reconnectAt=System.currentTimeMillis()+(long)Settings.number("auto-reconnect.seconds")*1000;
            if(System.currentTimeMillis()>=reconnectAt){reconnectAt=0;ConnectScreen.startConnecting(new TitleScreen(),mc,ServerAddress.parseString(lastServer.ip),lastServer,false,null);}
        }else reconnectAt=0;
        if(on("discord-presence")&&!discord){discord=true;Thread.startVirtualThread(ExtraMisc::discord);}else if(!on("discord-presence"))discord=false;
        if(due("packetflush",1000))flushPackets();
        if(mc.player==null||mc.level==null)return;
        if(on("notifier")){
            Set<String> now=new HashSet<>();for(var player:mc.level.players())if(player!=mc.player){String name=player.getGameProfile().name();now.add(name);if(nearby.add(name))local(mc,name+" entered your loaded area.");}nearby.retainAll(now);
        }else nearby.clear();
        for(String id:new String[]{"spam","dm-spam","auto-endorse","auto-sell"})if(on(id)&&due(id,(long)Settings.number(id+".seconds")*1000)){
            String text=Settings.text(id+".message");if(text.isBlank())continue;if(id.equals("dm-spam")&&!Settings.text("dm-spam.recipient").isBlank())text="/msg "+Settings.text("dm-spam.recipient")+" "+text;
            send(mc,text);
        }
        if(on("message-aura")){for(var p:mc.level.players())if(p!=mc.player&&messaged.add(p.getGameProfile().name())){String text=Settings.text("message-aura.message");if(!text.isBlank())send(mc,"/msg "+p.getGameProfile().name()+" "+text);}}
        else messaged.clear();
        if(on("book-bot")&&mc.player.getMainHandItem().is(Items.WRITABLE_BOOK)){
            List<String> pages=Arrays.stream(Settings.text("book-bot.text").split("\\|",-1)).limit(100).map(s->s.substring(0,Math.min(1024,s.length()))).toList();
            mc.player.connection.send(new net.minecraft.network.protocol.game.ServerboundEditBookPacket(mc.player.getInventory().getSelectedSlot(),pages,Optional.empty()));disable("book-bot");
        }
        if(mc.gui.screen()!=null&&on("inventory-tweaks")&&due("inventory",300))merge(mc);
        if(mc.gui.screen()==null&&on("notebot")&&mc.player.tickCount%(int)Settings.number("notebot.delay")==0)note(mc);
    }
    static boolean due(String key,long millis){long now=System.currentTimeMillis();if(now<next.getOrDefault(key,0L))return false;next.put(key,now+millis);return true;}
    public static boolean outgoing(Packet<?> packet){
        String simple=packet.getClass().getSimpleName();
        if(on("packet-logger"))synchronized(packetLog){if(packetLog.size()<2048)packetLog.add(Instant.now()+" OUT "+packet.getClass().getName());}
        return on("packet-canceller")&&Arrays.stream(Settings.text("packet-canceller.classes").split(",")).map(String::trim).filter(s->!s.isBlank()).anyMatch(s->s.equals(simple)||s.equals(packet.getClass().getName()));
    }
    public static Component chat(Component text){return on("better-chat")?Component.literal("["+LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))+"] ").append(text):text;}
    public static String outgoingChat(String text){return on("chat-utility")?Settings.text("chat-utility.prefix")+text:text;}
    private static void flushPackets(){List<String> lines; synchronized(packetLog){if(packetLog.isEmpty())return;lines=new ArrayList<>(packetLog);packetLog.clear();}try{Files.write(FabricLoader.getInstance().getConfigDir().resolve("wanderlust-packets.log"),lines,StandardOpenOption.CREATE,StandardOpenOption.APPEND);}catch(IOException ignored){}}
    static void send(Minecraft mc,String text){if(mc.player==null||text.isBlank())return;if(text.startsWith("/"))mc.player.connection.sendCommand(text.substring(1));else mc.player.connection.sendChat(text);}
    static void local(Minecraft mc,String text){mc.gui.hud.getChat().addClientSystemMessage(Component.literal("[Wanderlust] "+text));}
    static void disable(String id){for(Feature f:Feature.values())if(f.id.equals(id)){Settings.setEnabled(f,false);}}
    private static void merge(Minecraft mc){var menu=mc.player.containerMenu;if(!menu.getCarried().isEmpty())return;for(int i=0;i<menu.slots.size();i++){var a=menu.slots.get(i);if(a.container!=mc.player.getInventory())continue;ItemStack item=a.getItem();if(item.isEmpty()||item.getCount()>=item.getMaxStackSize())continue;for(int j=i+1;j<menu.slots.size();j++){var b=menu.slots.get(j);if(b.container!=mc.player.getInventory()||!ItemStack.isSameItemSameComponents(item,b.getItem()))continue;for(int slot:new int[]{j,i,j})mc.gameMode.handleContainerInput(menu.containerId,slot,0,ContainerInput.PICKUP,mc.player);return;}}}
    private static void note(Minecraft mc){if(mc.player.isCreative())return;int[] pitches;try{pitches=Arrays.stream(Settings.text("notebot.notes").split(",")).map(String::trim).mapToInt(Integer::parseInt).toArray();}catch(NumberFormatException e){return;}if(pitches.length==0)return;int target=Math.clamp(pitches[note%pitches.length],0,24);for(BlockPos pos:BlockPos.betweenClosed(mc.player.blockPosition().offset(-4,-2,-4),mc.player.blockPosition().offset(4,2,4))){var state=mc.level.getBlockState(pos);if(!(state.getBlock() instanceof NoteBlock)||!mc.player.isWithinBlockInteractionRange(pos,0))continue;if(state.getValue(NoteBlock.NOTE)!=target)mc.gameMode.useItemOn(mc.player,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false));else{var action=net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK;mc.player.connection.send(new net.minecraft.network.protocol.game.ServerboundPlayerActionPacket(action,pos,Direction.UP));mc.player.connection.send(new net.minecraft.network.protocol.game.ServerboundPlayerActionPacket(net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK,pos,Direction.UP));note++;}return;}}
    private static void discord(){String id=Settings.text("discord-presence.app");if(id.isBlank())return;for(int i=0;i<10;i++){try(RandomAccessFile pipe=new RandomAccessFile("\\\\?\\pipe\\discord-ipc-"+i,"rw")){var gson=new com.google.gson.Gson();writeFrame(pipe,0,gson.toJson(Map.of("v",1,"client_id",id)));int op=Integer.reverseBytes(pipe.readInt()),size=Integer.reverseBytes(pipe.readInt());if(size<0||size>65536)return;byte[] reply=new byte[size];pipe.readFully(reply);writeFrame(pipe,1,gson.toJson(Map.of("cmd","SET_ACTIVITY","args",Map.of("pid",ProcessHandle.current().pid(),"activity",Map.of("details",Settings.text("discord-presence.details"),"state","WanderlustClient")),"nonce",UUID.randomUUID().toString())));while(discord&&Wanderlust.on("discord-presence")){try{Thread.sleep(500);}catch(InterruptedException stop){Thread.currentThread().interrupt();break;}}return;}catch(IOException ignored){}}}
    private static void writeFrame(RandomAccessFile pipe,int op,String text)throws IOException{byte[] bytes=text.getBytes(java.nio.charset.StandardCharsets.UTF_8);pipe.writeInt(Integer.reverseBytes(op));pipe.writeInt(Integer.reverseBytes(bytes.length));pipe.write(bytes);}
    private static boolean on(String id){return Wanderlust.on(id);}
}
