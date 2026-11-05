package dev.shabzup.wanderlust;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
final class ProxyScreen extends ClientScreen {
    private int page;
    ProxyScreen(Screen parent){super("Connection profiles",parent);}
    @Override protected void init(){
        layout(460,360);int x=left+16,w=cardWidth-32,y=top+58;
        button(x,y,(w-6)/2,"New SOCKS5 profile",()->minecraft.gui.setScreen(new ProxyEditScreen(this,null))).active=Accounts.canSwitch();
        button(x+(w+6)/2,y,(w-6)/2,"Use Direct",()->{Proxies.direct();rebuildWidgets();}).active=Accounts.canSwitch();
        int rows=Math.max(1,(cardHeight-175)/29),pages=Math.max(1,(Proxies.profiles.size()+rows-1)/rows);page=Math.clamp(page,0,pages-1);
        for(int i=page*rows;i<Math.min(Proxies.profiles.size(),(page+1)*rows);i++){
            var p=Proxies.profiles.get(i);int yy=y+35+(i%rows)*29;
            button(x,yy,w-91,(Proxies.activeId().equals(p.id())?"● ":"")+p.name(),()->{Proxies.activate(p);rebuildWidgets();}).active=Accounts.canSwitch();
            button(x+w-85,yy,50,"Edit",()->minecraft.gui.setScreen(new ProxyEditScreen(this,p))).active=Accounts.canSwitch();
            button(x+w-29,yy,29,"×",()->{Proxies.remove(p);rebuildWidgets();}).active=Accounts.canSwitch();
        }
        int bottom=top+cardHeight-77;
        button(x,bottom,28,"‹",()->{page--;rebuildWidgets();}).active=page>0;
        button(x+34,bottom,28,"›",()->{page++;rebuildWidgets();}).active=page+1<pages;
        button(x+w-82,bottom,82,"Back",this::onClose);
    }
    @Override void drawContent(GuiGraphicsExtractor g,int mx,int my,float delta){
        subtitle(g,Proxies.status);
        if(Proxies.profiles.isEmpty())text(g,"No profiles yet. Direct is the default.",left+16,top+105,cardWidth-32,PanelStyle.MUTED);
        text(g,"New game TCP connections only · Disconnect to change",left+16,top+cardHeight-43,cardWidth-32,PanelStyle.MUTED);
        text(g,"Authentication HTTP and DNS use your normal connection.",left+16,top+cardHeight-25,cardWidth-32,PanelStyle.MUTED);
    }
}
final class ProxyEditScreen extends ClientScreen {
    private final Proxies.Profile original;
    private String name,host,port,user,pass,error="";
    ProxyEditScreen(Screen parent,Proxies.Profile p){super(p==null?"New SOCKS5 profile":"Edit SOCKS5 profile",parent);original=p;name=p==null?"":p.name();host=p==null?"":p.host();port=p==null?"1080":Integer.toString(p.port());user=p==null?"":p.username();pass=p==null?"":p.password();}
    @Override protected void init(){
        layout(460,300);int x=left+16,w=cardWidth-32,y=top+57;
        input(x,y,w,"Profile name",name,64).setResponder(s->name=s);
        input(x,y+29,w-78,"Proxy hostname / IP",host,253).setResponder(s->host=s);
        input(x+w-72,y+29,72,"Port",port,5).setResponder(s->port=s);
        input(x,y+58,(w-6)/2,"Username (optional)",user,255).setResponder(s->user=s);
        var password=input(x+(w+6)/2,y+58,(w-6)/2,"Password (optional)",pass,255);
        ((PanelEditBox)password).secret();password.setResponder(s->pass=s);
        button(x,top+cardHeight-40,(w-6)/2,"Save profile",()->{
            try{if(!Accounts.canSwitch()){error="Disconnect before editing profiles.";return;}Proxies.saveProfile(original,name.trim(),host.trim(),Integer.parseInt(port),user,pass);onClose();}
            catch(NumberFormatException ex){error="Enter a numeric port between 1 and 65535.";}
            catch(IllegalArgumentException ex){error=ex.getMessage();}
        });
        button(x+(w+6)/2,top+cardHeight-40,(w-6)/2,"Cancel",this::onClose);
    }
    @Override void drawContent(GuiGraphicsExtractor g,int mx,int my,float delta){subtitle(g,"SOCKS5 · optional username and password");text(g,error.isBlank()?"Credentials are saved in a Windows-encrypted vault.":error,left+16,top+cardHeight-65,cardWidth-32,error.isBlank()?PanelStyle.MUTED:0xFFFFA6AF);}
}
