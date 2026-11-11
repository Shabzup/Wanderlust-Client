package dev.shabzup.wanderlust;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;

final class PanelStyle {
    static final int TEXT = 0xFFF2F3F8, MUTED = 0xFF9DA6BB, BLUE = 0xFF8AAFFF;
    static final int PURPLE = 0xFF7561C9, PURPLE_LIGHT = 0xFFBAACF4;
    static int accent() {
        return switch (Settings.choice("ui.accent")) {
            case "Ocean" -> 0xFF419DE8;
            case "Mint" -> 0xFF38BFA0;
            case "Rose" -> 0xFFE16E99;
            case "Amber" -> 0xFFE6AE4E;
            default -> PURPLE;
        };
    }
    static int accentLight() { return mix(accent(), 0xFFFFFFFF, .38f); }
    static int category(Feature.Group group) {
        if (!Settings.toggle("ui.category-colours")) return accentLight();
        return switch (group) {
            case Combat -> 0xFFF28C9A;
            case Movement -> 0xFF72C8EE;
            case Visuals -> 0xFFBCA1F3;
            case Player -> 0xFF73D9B2;
            case World -> 0xFFF1C075;
            case Misc -> 0xFF92A9EA;
        };
    }
    static int mix(int a, int b, float t) {
        t = Math.clamp(t, 0, 1);
        int result = 0;
        for (int shift = 0; shift <= 24; shift += 8)
            result |= Math.round(((a >>> shift) & 255) * (1-t) + ((b >>> shift) & 255) * t) << shift;
        return result;
    }
    private static final FontDescription FONT = new FontDescription.Resource(Identifier.fromNamespaceAndPath("wanderlust", "ui"));
    private static final java.util.Map<String,Component> LABELS=new java.util.LinkedHashMap<>(512,.75f,true){
        @Override protected boolean removeEldestEntry(java.util.Map.Entry<String,Component> entry){return size()>512;}
    };
    private record Measure(net.minecraft.client.gui.Font font,String value){}
    private record Fit(net.minecraft.client.gui.Font font,String value,int width){}
    private static final java.util.Map<Measure,Integer> WIDTHS=boundedCache();
    private static final java.util.Map<Fit,String> FITS=boundedCache();
    private static <K,V> java.util.Map<K,V> boundedCache(){
        return new java.util.LinkedHashMap<>(512,.75f,true){
            @Override protected boolean removeEldestEntry(java.util.Map.Entry<K,V> entry){return size()>1024;}
        };
    }
    static void clearTextMetrics(){LABELS.clear();WIDTHS.clear();FITS.clear();}
    static void init(){
        var loader=net.fabricmc.fabric.api.resource.v1.ResourceLoader.get(net.minecraft.server.packs.PackType.CLIENT_RESOURCES);
        var id=Identifier.fromNamespaceAndPath("wanderlust","ui_text_metrics");
        loader.registerReloadListener(id,new net.fabricmc.fabric.api.resource.v1.reloader.SimpleReloadListener<Void>(){
            @Override protected Void prepare(net.minecraft.server.packs.resources.PreparableReloadListener.SharedState state){return null;}
            @Override protected void apply(Void ignored,net.minecraft.server.packs.resources.PreparableReloadListener.SharedState state){clearTextMetrics();}
        });
        loader.addListenerOrdering(net.fabricmc.fabric.api.resource.v1.reloader.ResourceReloaderKeys.Client.FONTS,id);
        loader.addListenerOrdering(net.fabricmc.fabric.api.resource.v1.reloader.ResourceReloaderKeys.Client.LANGUAGES,id);
    }

    static Component text(String text) {
        if(text.length()>160)return Component.literal(text).withStyle(s->s.withFont(FONT));
        return LABELS.computeIfAbsent(text,key->Component.literal(key).withStyle(s->s.withFont(FONT)));
    }
    static String fit(net.minecraft.client.gui.Font font,String value,int width) {
        if(value.length()>160)return fitUncached(font,value,width);
        return FITS.computeIfAbsent(new Fit(font,value,width),key->fitUncached(font,value,width));
    }
    static int width(net.minecraft.client.gui.Font font,String value){
        if(value.length()>160)return font.width(text(value));
        return WIDTHS.computeIfAbsent(new Measure(font,value),key->font.width(text(value)));
    }
    private static String fitUncached(net.minecraft.client.gui.Font font,String value,int width){
        if(width<=0)return "";
        if(width(font,value)<=width)return value;
        int low=0,high=value.codePointCount(0,value.length());
        while(low<high){
            int middle=(low+high+1)/2;
            String candidate=value.substring(0,value.offsetByCodePoints(0,middle))+"…";
            if(width(font,candidate)<=width)low=middle;else high=middle-1;
        }
        return low==0?"":value.substring(0,value.offsetByCodePoints(0,low))+"…";
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
    private static final float[][] GEM_VERTICES={{0,-1,0},{1,0,0},{0,0,1},{-1,0,0},{0,0,-1},{0,1,0}};
    private static final float[][] GEM_PROJECTED=new float[6][3];
    static void gem(GuiGraphicsExtractor g,int x,int y,float size,long now,float opacity) {
        if(!Settings.toggle("ui.depth"))return;
        double angle=Settings.toggle("ui.motion")?now/1_000_000_000d*.18:Math.PI/5;
        float[][] p=GEM_PROJECTED;
        double sin=Math.sin(angle),cos=Math.cos(angle);
        for(int i=0;i<6;i++){float[] v=GEM_VERTICES[i];double xx=v[0]*cos-v[2]*sin,zz=v[0]*sin+v[2]*cos;p[i][0]=x+(float)xx*size;p[i][1]=y+(v[1]*.85f+(float)zz*.32f)*size;p[i][2]=(float)zz;}
        for(int i=1;i<=4;i++){int next=i==4?1:i+1;int c=alpha(accentLight(),opacity*(p[i][2]<0?.28f:.75f));line(g,p[0][0],p[0][1],p[i][0],p[i][1],c);line(g,p[5][0],p[5][1],p[i][0],p[i][1],c);line(g,p[i][0],p[i][1],p[next][0],p[next][1],c);}
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
        control(g,x,y,w,h,hover?1f:0f,selected,alpha);
    }
    static void control(GuiGraphicsExtractor g,int x,int y,int w,int h,float hover,boolean selected,float alpha) {
        int edge=selected?accentLight():mix(0xFF46516B,accentLight(),hover*.7f);
        int fill=selected?mix(0xEE1B2333,accent(),.25f):mix(0xE51B2333,0xEE35425C,hover);
        g.fill(x,y,x+w,y+h,alpha(edge,alpha));
        g.fill(x+1,y+1,x+w-1,y+h-1,alpha(fill,alpha));
        g.fill(x+1,y+1,x+w-1,y+2,alpha(0x228F9DBD,alpha));
    }
}
