package dev.shabzup.wanderlust;
import java.util.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
final class EntitySelectionScreen extends ClientScreen {
    private final List<EntityTargets.Entry> entries=EntityTargets.entries();
    private List<EntityTargets.Entry> filtered=entries;
    private int scroll,rows;
    private String query="";
    private boolean selectedOnly;
    EntitySelectionScreen(Screen parent){super("KillAura entity targets",parent);}
    @Override protected void init(){
        layout(620,490);int x=left+16,w=cardWidth-32,bw=(w-12)/3;rows=Math.max(1,(cardHeight-161)/24);
        button(x,top+57,bw,"All",()->{EntityTargets.setAll(entries,true);filter();});
        button(x+bw+6,top+57,bw,"None",()->{EntityTargets.setAll(entries,false);filter();});
        button(x+2*(bw+6),top+57,bw,"Selected only",()->{selectedOnly=!selectedOnly;filter();});
        input(x,top+88,w,"Search name or minecraft:id",query,128).setResponder(s->{query=s;scroll=0;filter();});
        button(left+cardWidth-92,top+cardHeight-37,76,"Back",this::onClose);filter();
    }
    private void filter(){String q=query.toLowerCase(Locale.ROOT);filtered=entries.stream().filter(e->(!selectedOnly||EntityTargets.contains(e.id()))&&(e.name()+" "+e.id()).toLowerCase(Locale.ROOT).contains(q)).toList();scroll=Math.clamp(scroll,0,Math.max(0,filtered.size()-rows));}
    @Override public boolean mouseClicked(MouseButtonEvent e,boolean doubled){
        if(super.mouseClicked(e,doubled))return true;
        if(e.button()==0&&e.x()>=left+16&&e.x()<left+cardWidth-16&&e.y()>=top+119&&e.y()<top+119+rows*24){int i=scroll+(int)((e.y()-top-119)/24);if(i<filtered.size()){var item=filtered.get(i);EntityTargets.set(item.id(),!EntityTargets.contains(item.id()));filter();return true;}}return false;
    }
    @Override public boolean mouseScrolled(double x,double y,double h,double v){if(v==0)return false;scroll=Math.clamp(scroll+(v<0?3:-3),0,Math.max(0,filtered.size()-rows));return true;}
    @Override void drawContent(GuiGraphicsExtractor g,int mx,int my,float delta){
        subtitle(g,EntityTargets.count()+" types selected · None clears every target");
        for(int i=scroll;i<Math.min(filtered.size(),scroll+rows);i++){
            var item=filtered.get(i);int y=top+119+(i-scroll)*24;boolean active=EntityTargets.contains(item.id());
            if(mx>=left+16&&mx<left+cardWidth-16&&my>=y&&my<y+24)g.fill(left+12,y,left+cardWidth-12,y+24,0x256E709E);
            g.fill(left+19,y+7,left+28,y+16,active?PanelStyle.PURPLE_LIGHT:0xFF4C5670);if(!active)g.fill(left+20,y+8,left+27,y+15,0xFF151C2B);
            text(g,item.name(),left+37,y+7,cardWidth>450?(cardWidth-65)/2:cardWidth-61,active?PanelStyle.TEXT:PanelStyle.MUTED);
            if(cardWidth>450){String id=PanelStyle.fit(font,item.id(),(cardWidth-65)/2);var label=PanelStyle.text(id);text(g,id,left+cardWidth-18-font.width(label),y+7,(cardWidth-65)/2,PanelStyle.MUTED);}
        }
        text(g,filtered.size()+" results · "+(filtered.isEmpty()?0:scroll+1)+"–"+Math.min(filtered.size(),scroll+rows),left+16,top+cardHeight-28,cardWidth-120,PanelStyle.MUTED);
        if(filtered.size()>rows){int h=rows*24,thumb=Math.max(10,h*rows/filtered.size()),yy=top+119+(h-thumb)*scroll/Math.max(1,filtered.size()-rows);g.fill(left+cardWidth-8,top+119,left+cardWidth-6,top+119+h,0x334B5874);g.fill(left+cardWidth-8,yy,left+cardWidth-6,yy+thumb,PanelStyle.PURPLE_LIGHT);}
    }
}
