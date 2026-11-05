package dev.shabzup.wanderlust;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;

final class PanelStyle {
    static final int TEXT = 0xFFF2F3F8, MUTED = 0xFF9DA6BB, BLUE = 0xFF8AAFFF;
    static final int PURPLE = 0xFF7561C9, PURPLE_LIGHT = 0xFFBAACF4;
    private static final FontDescription FONT = new FontDescription.Resource(Identifier.fromNamespaceAndPath("wanderlust", "ui"));
    private static final java.util.Map<String,Component> LABELS=new java.util.LinkedHashMap<>(512,.75f,true){
        @Override protected boolean removeEldestEntry(java.util.Map.Entry<String,Component> entry){return size()>512;}
    };

    static Component text(String text) {
        if(text.length()>160)return Component.literal(text).withStyle(s->s.withFont(FONT));
        return LABELS.computeIfAbsent(text,key->Component.literal(key).withStyle(s->s.withFont(FONT)));
    }
    static String fit(net.minecraft.client.gui.Font font,String value,int width) {
        if(width<=0)return "";
        if(font.width(text(value))<=width)return value;
        int end=value.length();while(end>0&&font.width(text(value.substring(0,end)+"…"))>width)end--;
        return end==0?"":value.substring(0,end)+"…";
    }
    static void surface(GuiGraphicsExtractor g,int x,int y,int w,int h,float opacity) {
        if(w<=2||h<=2)return;
        g.fill(x+3,y+5,x+w+3,y+h+5,alpha(0x24000000,opacity));
        g.fillGradient(x,y,x+w,y+h,background(0x19202F,opacity),background(0x0E1420,opacity));
        g.outline(x,y,w,h,alpha(0x38687999,opacity));
        g.fill(x+1,y,x+w-1,y+1,alpha(0x667F8FAE,opacity));
    }
    static void line(GuiGraphicsExtractor g,float x,float y,float toX,float toY,int color) {
        float dx=toX-x,dy=toY-y;
        g.pose().pushMatrix();g.pose().translate(x,y);g.pose().rotate((float)Math.atan2(dy,dx));
        g.fill(0,0,(int)Math.ceil(Math.hypot(dx,dy)),1,color);g.pose().popMatrix();
    }
    /** A small projected octahedron; fixed geometry, no world render or blur pass. */
    static void gem(GuiGraphicsExtractor g,int x,int y,float size,long now,float opacity) {
        if(!Settings.toggle("ui.depth"))return;
        double angle=Settings.toggle("ui.motion")?now/1_000_000_000d*.18:Math.PI/5;
        float[][] p=new float[6][3];
        float[][] vertices={{0,-1,0},{1,0,0},{0,0,1},{-1,0,0},{0,0,-1},{0,1,0}};
        for(int i=0;i<6;i++){float[] v=vertices[i];double xx=v[0]*Math.cos(angle)-v[2]*Math.sin(angle),zz=v[0]*Math.sin(angle)+v[2]*Math.cos(angle);p[i][0]=x+(float)xx*size;p[i][1]=y+(v[1]*.85f+(float)zz*.32f)*size;p[i][2]=(float)zz;}
        for(int i=1;i<=4;i++){int next=i==4?1:i+1;int c=alpha(PURPLE_LIGHT,opacity*(p[i][2]<0?.28f:.75f));line(g,p[0][0],p[0][1],p[i][0],p[i][1],c);line(g,p[5][0],p[5][1],p[i][0],p[i][1],c);line(g,p[i][0],p[i][1],p[next][0],p[next][1],c);}
    }
    static float ease(float t) { return 1 - (float)Math.pow(1 - Math.clamp(t, 0, 1), 3); }
    static float approach(float current, float target, float seconds, float speed) {
        return current + (target - current) * (1 - (float)Math.exp(-seconds * speed));
    }
    static int alpha(int color, float opacity) {
        return ((int)((color >>> 24) * Math.clamp(opacity, 0, 1)) << 24) | (color & 0xFFFFFF);
    }
    static int background(int color, float opacity) {
        int amount = (int)Math.round(255 * Settings.number("ui.opacity") / 100);
        return alpha((amount << 24) | (color & 0xFFFFFF), opacity);
    }
    static void round(GuiGraphicsExtractor g, int x, int y, int w, int h, int r, int color) {
        if (w <= 0 || h <= 0 || color >>> 24 == 0) return;
        g.fill(x, y, x + w, y + h, color);
    }
    static void control(GuiGraphicsExtractor g,int x,int y,int w,int h,boolean hover,boolean selected,float alpha) {
        int edge=selected?PURPLE_LIGHT:hover?0xFF8E96B5:0xFF46516B;
        int fill=selected?0xEE393052:hover?0xEE293248:0xE51B2333;
        g.fill(x,y,x+w,y+h,alpha(edge,alpha));
        g.fill(x+1,y+1,x+w-1,y+h-1,alpha(fill,alpha));
        g.fill(x+1,y+1,x+w-1,y+2,alpha(0x228F9DBD,alpha));
    }
}
