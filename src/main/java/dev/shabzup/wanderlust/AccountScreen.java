package dev.shabzup.wanderlust;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Util;
final class AccountScreen extends ClientScreen {
    private int page,count,rows;
    private boolean wasBusy;
    private String username="";
    AccountScreen(net.minecraft.client.gui.screens.Screen parent){super("Accounts",parent);}
    @Override protected void init(){
        layout(440,360);wasBusy=Accounts.busy;count=Accounts.entries.size();
        int x=left+16,w=cardWidth-32,y=top+57;
        var name=input(x,y,w-106,"Offline username",username,16);name.setResponder(s->username=s);
        button(x+w-100,y,100,"Add offline",()->{Accounts.addOffline(username.trim());rebuildWidgets();});
        button(x,y+29,(w-6)/2,"Microsoft sign-in",Accounts::login).active=Accounts.canSwitch()&&!Accounts.busy;
        button(x+(w+6)/2,y+29,(w-6)/2,"Restore launcher",Accounts::restore).active=Accounts.canSwitch()&&!Accounts.busy;
        rows=Math.max(1,(cardHeight-205)/28);int pages=Math.max(1,(count+rows-1)/rows);page=Math.clamp(page,0,pages-1);
        for(int i=page*rows;i<Math.min(count,(page+1)*rows);i++){
            var entry=Accounts.entries.get(i);int yy=y+59+(i%rows)*28;
            button(x,yy,w-34,entry.name()+(entry.auth()==null?" · Offline":" · Microsoft"),()->Accounts.use(entry)).active=Accounts.canSwitch()&&!Accounts.busy;
            button(x+w-28,yy,28,"×",()->{Accounts.entries.remove(entry);Accounts.saveNames();rebuildWidgets();});
        }
        int bottom=top+cardHeight-70;
        button(x,bottom,28,"‹",()->{page--;rebuildWidgets();}).active=page>0;
        button(x+34,bottom,28,"›",()->{page++;rebuildWidgets();}).active=page+1<pages;
        button(x+68,bottom,w-150,"Sign-in page",()->{if(Accounts.verificationUrl.startsWith("https://"))Util.getPlatform().openUri(Accounts.verificationUrl);}).active=Accounts.busy&&!Accounts.verificationUrl.isBlank();
        button(x+w-76,bottom,76,Accounts.busy?"Cancel":"Back",()->{if(Accounts.busy){Accounts.cancel();rebuildWidgets();}else onClose();});
    }
    @Override public void tick(){if(wasBusy!=Accounts.busy||count!=Accounts.entries.size())rebuildWidgets();}
    @Override void drawContent(GuiGraphicsExtractor g,int mx,int my,float delta){
        subtitle(g,Accounts.canSwitch()?"Current session · "+minecraft.getUser().getName():"Disconnect before switching accounts");
        if(count==0)text(g,"No saved accounts. Add one above.",left+16,top+128,cardWidth-32,PanelStyle.MUTED);
        text(g,Accounts.status,left+16,top+cardHeight-43,cardWidth-32,PanelStyle.TEXT);
        text(g,Accounts.busy?"Microsoft code · "+Accounts.verificationCode:"Encrypted for your Windows login · Page "+(page+1),left+16,top+cardHeight-24,cardWidth-32,PanelStyle.MUTED);
    }
    @Override public void onClose(){Accounts.cancel();super.onClose();}
}
