package dev.shabzup.wanderlust;
import net.minecraft.client.gui.GuiGraphicsExtractor;
final class BaritoneScreen extends ClientScreen {
    private String result="",command="";
    BaritoneScreen(net.minecraft.client.gui.screens.Screen parent){super("Baritone navigation",parent);}
    @Override protected void init(){
        layout(440,232);int x=left+16,w=cardWidth-32;
        input(x,top+61,w,"goto 100 64 100 / mine diamond_ore",command,240).setResponder(s->command=s);
        button(x,top+90,w,"Run command",()->{String c=command.trim();if(c.startsWith("#"))c=c.substring(1);result=BaritoneBridge.userCommand(c)?"Command submitted":"Check the Baritone installation and chat.";});
        String[] names={"pause","resume","cancel"};int bw=(w-12)/3;
        for(int i=0;i<3;i++){String name=names[i];button(x+i*(bw+6),top+119,bw,name,()->BaritoneBridge.userCommand(name));}
        button(x,top+cardHeight-39,w,"Back",this::onClose);
    }
    @Override void drawContent(GuiGraphicsExtractor g,int mx,int my,float delta){subtitle(g,BaritoneBridge.status());text(g,result,left+16,top+150,cardWidth-32,PanelStyle.MUTED);}
}
