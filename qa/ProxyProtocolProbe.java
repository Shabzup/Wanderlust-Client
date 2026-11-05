package qa;

import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.*;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.network.EventLoopGroupHolder;

/** Only loopback listeners and synthetic credentials are used. */
final class ProxyProtocolProbe {
    static Object call(String method,Class[] types,Object...args)throws Exception{return UpdateProbe.call("Proxies",method,types,args);}
    static List profiles()throws Exception{var f=Class.forName("dev.shabzup.wanderlust.Proxies").getDeclaredField("profiles");f.setAccessible(true);return (List)f.get(null);}
    static void activate(Object p)throws Exception{call("activate",new Class[]{p.getClass()},p);long until=System.nanoTime()+TimeUnit.SECONDS.toNanos(3);while(System.nanoTime()<until){var f=Class.forName("dev.shabzup.wanderlust.Proxies").getDeclaredField("route");f.setAccessible(true);Object r=f.get(null);if(r!=null&&UiPolishProbe.field(r,"address")!=null)return;Thread.sleep(5);}throw new IllegalStateException("Proxy fixture DNS timeout");}
    static final class Server implements AutoCloseable {
        final ServerSocket server;final CompletableFuture<Void> task;
        volatile boolean connected,credentials;final boolean auth,reject,echo;
        Server(boolean auth,boolean reject,boolean echo)throws Exception{this.auth=auth;this.reject=reject;this.echo=echo;server=new ServerSocket(0,1,InetAddress.getLoopbackAddress());server.setSoTimeout(5000);task=CompletableFuture.runAsync(()->{try(Socket s=server.accept()){s.setSoTimeout(5000);var in=new DataInputStream(s.getInputStream());var out=s.getOutputStream();if(in.readUnsignedByte()!=5)throw new IOException("Not SOCKS5");int count=in.readUnsignedByte();in.readNBytes(count);out.write(new byte[]{5,(byte)(auth?2:0)});out.flush();if(auth){if(in.readUnsignedByte()!=1)throw new IOException("Auth version");String user=new String(in.readNBytes(in.readUnsignedByte()),StandardCharsets.UTF_8);String pass=new String(in.readNBytes(in.readUnsignedByte()),StandardCharsets.UTF_8);credentials=user.equals("qa-user")&&pass.equals("qa-password");out.write(new byte[]{1,(byte)(reject?1:0)});out.flush();if(reject)return;}if(in.readUnsignedByte()!=5||in.readUnsignedByte()!=1)throw new IOException("CONNECT missing");in.readByte();int type=in.readUnsignedByte();in.readNBytes(type==1?4:type==4?16:in.readUnsignedByte());int port=in.readUnsignedShort();connected=port==25565;out.write(new byte[]{5,0,0,1,127,0,0,1,0,0});out.flush();if(echo){if(!new String(in.readNBytes(4),StandardCharsets.US_ASCII).equals("test"))throw new IOException("Payload missing");out.write("pong".getBytes(StandardCharsets.US_ASCII));out.flush();}else Thread.sleep(100);}catch(Exception e){throw new CompletionException(e);}});}
        public void close()throws Exception{server.close();task.get(6,TimeUnit.SECONDS);}
    }
    static Object add(Server s,boolean auth)throws Exception{Class p=Class.forName("dev.shabzup.wanderlust.Proxies$Profile");call("saveProfile",new Class[]{p,String.class,String.class,int.class,String.class,String.class},null,"QA loopback","127.0.0.1",s.server.getLocalPort(),auth?"qa-user":"",auth?"qa-password":"");Object profile=profiles().getLast();activate(profile);return profile;}
    static void raw(UiPolishProbe q,boolean auth,boolean reject)throws Exception{
        var group=new NioEventLoopGroup(1);Object profile=null;
        try(var server=new Server(auth,reject,!reject)){
            profile=add(server,auth);
            if(auth&&!reject){
                Path vault=Minecraft.getInstance().gameDirectory.toPath().resolve("config/wanderlust-proxies.dat");String file=new String(Files.readAllBytes(vault),StandardCharsets.ISO_8859_1);
                q.check(!file.contains("qa-user")&&!file.contains("qa-password"),"proxy credentials are absent from plaintext vault bytes");
                call("load",new Class[0]);Object restored=profiles().getLast();activate(restored);
                q.check(UiPolishProbe.field(restored,"password").equals("qa-password"),"encrypted proxy credentials reload before authenticated tunnel test");
            }
            CompletableFuture<String> reply=new CompletableFuture<>();
            var channel=new Bootstrap().group(group).channel(NioSocketChannel.class).handler(new ChannelInitializer<Channel>(){protected void initChannel(Channel ch)throws Exception{call("install",new Class[]{ChannelPipeline.class},ch.pipeline());ch.pipeline().addLast(new SimpleChannelInboundHandler<ByteBuf>(){protected void channelRead0(ChannelHandlerContext c,ByteBuf b){reply.complete(b.toString(StandardCharsets.US_ASCII));}public void exceptionCaught(ChannelHandlerContext c,Throwable t){reply.completeExceptionally(t);c.close();}});}}).connect(new InetSocketAddress("127.0.0.1",25565)).sync().channel();
            channel.writeAndFlush(Unpooled.copiedBuffer("test",StandardCharsets.US_ASCII));
            boolean failed=false;String response="";try{response=reply.get(5,TimeUnit.SECONDS);}catch(ExecutionException ex){failed=true;}
            q.check(reject?failed&&!server.connected:response.equals("pong")&&server.connected,(auth?"authenticated":"unauthenticated")+" SOCKS5 "+(reject?"rejects bad credentials without direct fallback":"tunnels bytes through loopback proxy"));
            if(auth)q.check(server.credentials,"SOCKS5 sends configured synthetic username/password");channel.close().sync();
        }finally{call("direct",new Class[0]);if(profile!=null)call("remove",new Class[]{profile.getClass()},profile);group.shutdownGracefully(0,1,TimeUnit.SECONDS).sync();}
    }
    static void run(UiPolishProbe q)throws Exception{
        raw(q,false,false);raw(q,true,false);raw(q,true,true);
        try(var server=new Server(false,false,false)){
            Object p=add(server,false);var connection=new Connection(PacketFlow.CLIENTBOUND);
            Channel channel=Connection.connect(new InetSocketAddress("127.0.0.1",25565),EventLoopGroupHolder.remote(false),connection).sync().channel();Object handler=channel.pipeline().get("wanderlust_proxy");
            var future=(io.netty.util.concurrent.Future<?>)handler.getClass().getMethod("connectFuture").invoke(handler);future.await(5,TimeUnit.SECONDS);
            q.check(handler!=null&&future.isSuccess()&&server.connected,"real Minecraft Connection mixin routes TCP through SOCKS5");channel.close().sync();
            Path vault=Minecraft.getInstance().gameDirectory.toPath().resolve("config/wanderlust-proxies.dat");byte[] saved=Files.readAllBytes(vault);String raw=new String(saved,StandardCharsets.ISO_8859_1);q.check(!raw.contains("QA loopback")&&!raw.contains("127.0.0.1"),"proxy vault hides saved profile data");
            String id=(String)call("activeId",new Class[0]);call("load",new Class[0]);q.check(call("activeId",new Class[0]).equals(id)&&!profiles().isEmpty(),"saved proxy profile and active selection reload");
            Files.write(vault,new byte[]{1,2,3});call("load",new Class[0]);boolean blocked=false;var channelTest=new io.netty.channel.embedded.EmbeddedChannel();try{call("install",new Class[]{ChannelPipeline.class},channelTest.pipeline());}catch(Exception ex){blocked=true;}channelTest.finishAndReleaseAll();q.check(blocked,"unreadable active proxy vault fails closed");
            Files.write(vault,saved);call("load",new Class[0]);call("direct",new Class[0]);var copy=new ArrayList<>(profiles());for(Object entry:copy)call("remove",new Class[]{entry.getClass()},entry);
        }
    }
}
