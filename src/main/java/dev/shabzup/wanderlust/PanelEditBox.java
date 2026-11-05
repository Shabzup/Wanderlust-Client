package dev.shabzup.wanderlust;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
/** Keeps native editing, selection, narration and keyboard behavior with client chrome. */
final class PanelEditBox extends EditBox {
    private final String placeholder;
    private final Font face;
    private boolean secret;
    PanelEditBox(Font font,int x,int y,int w,int h,String hint){super(font,x,y,w,h,PanelStyle.text(hint));placeholder=hint;face=font;setTextShadow(false);setTextColor(PanelStyle.TEXT);setTextColorUneditable(PanelStyle.MUTED);addFormatter((s,pos)->PanelStyle.text(secret?"•".repeat(s.length()):s).getVisualOrderText());}
    void secret(){secret=true;setValue(getValue());}
    @Override public int getInnerWidth(){return getWidth()-8;}
    @Override public void extractWidgetRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){
        PanelStyle.control(g,getX(),getY(),getWidth(),getHeight(),isFocused(),false,getAlpha());
        boolean bordered=isBordered();setBordered(false);g.pose().pushMatrix();g.pose().translate(4,(getHeight()-9)/2f);
        try{super.extractWidgetRenderState(g,mx,my,delta);}finally{g.pose().popMatrix();setBordered(bordered);}
        if(getValue().isEmpty()&&!isFocused())g.text(face,PanelStyle.text(PanelStyle.fit(face,placeholder,getInnerWidth())),getX()+4,getY()+(getHeight()-9)/2,PanelStyle.alpha(PanelStyle.MUTED,getAlpha()),false);
    }
}
