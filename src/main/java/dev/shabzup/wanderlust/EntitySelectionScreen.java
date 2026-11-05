package dev.shabzup.wanderlust;

import java.util.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

final class EntitySelectionScreen extends Screen {
    private final Screen parent;
    private final List<EntityTargets.Entry> entries = EntityTargets.entries();
    private List<EntityTargets.Entry> filtered = entries;
    private int left, top, boxWidth, boxHeight, scroll, rows;
    private String query = "";
    private boolean selectedOnly;
    private float scale;
    EntitySelectionScreen(Screen parent) { super(Component.literal("KillAura entity targets")); this.parent = parent; }
    private double ox() { return width * (1 - scale) / 2; }
    private double oy() { return height * (1 - scale) / 2; }
    private MouseButtonEvent local(MouseButtonEvent e) { return new MouseButtonEvent((e.x()-ox())/scale,(e.y()-oy())/scale,e.buttonInfo()); }
    @Override protected void init() {
        scale = (float)Settings.number("ui.scale") / 100;
        boxWidth = Math.min(570, width-24); boxHeight = Math.min(440,height-24);
        left=(width-boxWidth)/2; top=(height-boxHeight)/2; rows=Math.max(1,(boxHeight-126)/20);
        button(left+12,top+10,70,"‹ Back",this::onClose);
        button(left+boxWidth-154,top+10,64,"All",()->{EntityTargets.setAll(entries,true);filter();});
        button(left+boxWidth-82,top+10,70,"None",()->{EntityTargets.setAll(entries,false);filter();});
        var search=addRenderableWidget(new EditBox(font,left+18,top+58,boxWidth-175,20,PanelStyle.text("Search entities")));
        search.setBordered(false);search.setHint(PanelStyle.text("Search name or minecraft:id"));search.setValue(query);
        search.addFormatter((value,position)->PanelStyle.text(value).getVisualOrderText());
        search.setResponder(value->{query=value;scroll=0;filter();});
        button(left+boxWidth-144,top+53,132,"Selected only",()->{selectedOnly=!selectedOnly;filter();});
        filter();
    }
    private void button(int x,int y,int w,String label,Runnable action) {
        addRenderableWidget(new PanelButton(x,y,w,22,label,PanelButton.Kind.Small,()->label.equals("Selected only")&&selectedOnly,null,action));
    }
    private void filter() {
        String needle=query.toLowerCase(Locale.ROOT);
        filtered=entries.stream().filter(e->(!selectedOnly||EntityTargets.contains(e.id()))&&(e.name()+" "+e.id()).toLowerCase(Locale.ROOT).contains(needle)).toList();
        scroll=Math.clamp(scroll,0,Math.max(0,filtered.size()-rows));
    }
    @Override public boolean mouseClicked(MouseButtonEvent e,boolean doubled) {
        e=local(e); if(super.mouseClicked(e,doubled))return true;
        if(e.button()==0&&e.x()>=left+12&&e.x()<left+boxWidth-12&&e.y()>=top+87&&e.y()<top+87+rows*20){
            int index=scroll+(int)((e.y()-top-87)/20);
            if(index<filtered.size()){var entry=filtered.get(index);EntityTargets.set(entry.id(),!EntityTargets.contains(entry.id()));filter();return true;}
        } return false;
    }
    @Override public boolean mouseReleased(MouseButtonEvent e){return super.mouseReleased(local(e));}
    @Override public boolean mouseDragged(MouseButtonEvent e,double x,double y){return super.mouseDragged(local(e),x/scale,y/scale);}
    @Override public boolean mouseScrolled(double x,double y,double horizontal,double vertical){scroll=Math.clamp(scroll+(vertical<0?3:-3),0,Math.max(0,filtered.size()-rows));return true;}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta) {
        mx=(int)((mx-ox())/scale);my=(int)((my-oy())/scale);
        g.pose().pushMatrix();g.pose().translate((float)ox(),(float)oy());g.pose().scale(scale,scale);
        g.fill(left,top,left+boxWidth,top+boxHeight,PanelStyle.background(0x0B101A,1));
        text(g,"KillAura · "+EntityTargets.count()+" entity types selected",left+96,top+17,PanelStyle.TEXT);
        g.fill(left+12,top+53,left+boxWidth-154,top+77,PanelStyle.alpha(0x22202030,1));
        for(int i=scroll;i<Math.min(filtered.size(),scroll+rows);i++) {
            var e=filtered.get(i);int y=top+87+(i-scroll)*20;
            boolean active=EntityTargets.contains(e.id());
            if(mx>=left+12&&mx<left+boxWidth-12&&my>=y&&my<y+20)g.fill(left+12,y,left+boxWidth-12,y+20,0x22B080FF);
            g.fill(left+18,y+6,left+25,y+13,active?PanelStyle.PURPLE_LIGHT:0xFF626779);
            text(g,e.name(),left+34,y+5,active?PanelStyle.TEXT:PanelStyle.MUTED);
            var id=PanelStyle.text(e.id());g.text(font,id,left+boxWidth-20-font.width(id),y+5,PanelStyle.MUTED,true);
        }
        text(g,filtered.size()+" types · Use None, then select a single type to target only that entity.",left+16,top+boxHeight-20,PanelStyle.MUTED);
        super.extractRenderState(g,mx,my,delta);g.pose().popMatrix();
    }
    private void text(GuiGraphicsExtractor g,String s,int x,int y,int color){g.text(font,PanelStyle.text(s),x,y,color,true);}
    @Override public void onClose(){minecraft.gui.setScreen(parent);}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void extractBackground(GuiGraphicsExtractor g,int x,int y,float delta){}
}
