package dev.shabzup.wanderlust;
import java.io.*;
import java.net.*;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.Minecraft;

final class ExtraSwarm {
 private static volatile boolean running;
 private static ServerSocket server;
 private static Socket follower;
 private static final CopyOnWriteArrayList<Socket> peers=new CopyOnWriteArrayList<>();
 private static String last="";
 static void options(){Settings.choice("swarm","swarm.mode","Local relay mode","Host","Host","Join");Settings.numeric("swarm","swarm.port","Loopback port",18081,1024,65535,1);Settings.string("swarm","swarm.token","Shared relay token","wanderlust");Settings.string("swarm","swarm.command","Baritone command to broadcast","");}
 static void tick(Minecraft mc){
  if(Wanderlust.on("swarm")&&!running){running=true;Thread.startVirtualThread(()->run(mc));}
  if(!Wanderlust.on("swarm")&&running){running=false;close();last="";}
  String command=Settings.text("swarm.command");
  if(running&&Settings.choice("swarm.mode").equals("Host")&&!command.isBlank()&&!command.equals(last)){last=command;for(Socket peer:peers){try{var out=new PrintWriter(peer.getOutputStream(),true);out.println(command);}catch(IOException e){peers.remove(peer);try{peer.close();}catch(IOException ignored){}}}}
 }
 private static void run(Minecraft mc){try{
  if(Settings.choice("swarm.mode").equals("Host")){
   server=new ServerSocket((int)Settings.number("swarm.port"),8,InetAddress.getLoopbackAddress());
   while(running){Socket peer=server.accept();peer.setSoTimeout(5000);String token=new BufferedReader(new InputStreamReader(peer.getInputStream(),java.nio.charset.StandardCharsets.UTF_8)).readLine();if(Settings.text("swarm.token").equals(token)){peer.setSoTimeout(0);peers.add(peer);String current=Settings.text("swarm.command");if(!current.isBlank())new PrintWriter(peer.getOutputStream(),true).println(current);}else peer.close();}
  }else{
   follower=new Socket(InetAddress.getLoopbackAddress(),(int)Settings.number("swarm.port"));new PrintWriter(follower.getOutputStream(),true).println(Settings.text("swarm.token"));
   try(var reader=new BufferedReader(new InputStreamReader(follower.getInputStream(),java.nio.charset.StandardCharsets.UTF_8))){String line;while(running&&(line=reader.readLine())!=null){String command=line;mc.execute(()->{if(Wanderlust.on("swarm")&&mc.player!=null)BaritoneBridge.userCommand(command);});}}
  }
 }catch(IOException error){if(running)mc.execute(()->{if(mc.player!=null)ExtraMisc.local(mc,"Local Swarm relay: "+error.getMessage());ExtraMisc.disable("swarm");});}finally{running=false;close();}}
 private static void close(){try{if(server!=null)server.close();}catch(IOException ignored){}server=null;try{if(follower!=null)follower.close();}catch(IOException ignored){}follower=null;for(Socket peer:peers)try{peer.close();}catch(IOException ignored){}peers.clear();}
}
