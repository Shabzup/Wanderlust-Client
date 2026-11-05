package dev.shabzup.wanderlust;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Shared chrome for the small account, navigation and connection dialogs. */
abstract class ClientScreen extends Screen {
    final Screen parent;
    int left,top,cardWidth,cardHeight;
    private final long opened=System.nanoTime();
    ClientScreen(String title,Screen parent){super(Component.literal(title));this.parent=parent;}
    void layout(int idealWidth,int idealHeight){cardWidth=Math.min(idealWidth,width-24);cardHeight=Math.min(idealHeight,height-24);left=(width-cardWidth)/2;top=(height-cardHeight)/2;}
    PanelButton button(int x,int y,int w,String text,Runnable action){var b=addRenderableWidget(new PanelButton(x,y,w,24,text,PanelButton.Kind.Navigation,()->false,null,action));b.setTooltip(net.minecraft.client.gui.components.Tooltip.create(PanelStyle.text(switch(text){case "×"->"Remove this saved entry";case "‹"->"Previous page";case "›"->"Next page";default->text;})));b.setTooltipDelay(java.time.Duration.ofMillis(400));return b;}
    EditBox input(int x,int y,int w,String hint,String value,int max){var edit=addRenderableWidget(new PanelEditBox(font,x,y,w,22,hint));edit.setMaxLength(max);edit.setValue(value);return edit;}
    void text(GuiGraphicsExtractor g,String value,int x,int y,int available,int color){g.text(font,PanelStyle.text(PanelStyle.fit(font,value,available)),x,y,color,false);}
    void subtitle(GuiGraphicsExtractor g,String value){text(g,value,left+16,top+38,cardWidth-32,PanelStyle.MUTED);}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){g.fillGradient(0,0,width,height,0xED111725,0xF0080C14);float a=Settings.toggle("ui.motion")?PanelStyle.ease((System.nanoTime()-opened)/220_000_000f):1;PanelStyle.surface(g,left,top,cardWidth,cardHeight,a);PanelStyle.gem(g,left+cardWidth-29,top+24,11,System.nanoTime(),a);text(g,title.getString(),left+16,top+17,cardWidth-65,PanelStyle.TEXT);drawContent(g,mx,my,delta);super.extractRenderState(g,mx,my,delta);}
    abstract void drawContent(GuiGraphicsExtractor g,int mx,int my,float delta);
    @Override public void extractBackground(GuiGraphicsExtractor g,int x,int y,float delta){}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void onClose(){minecraft.gui.setScreen(parent);}
}
