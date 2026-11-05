package dev.shabzup.wanderlust;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;
final class BindScreen extends ClientScreen {
    private final Feature feature;
    BindScreen(net.minecraft.client.gui.screens.Screen parent,Feature feature){super("Hotkey · "+feature.label,parent);this.feature=feature;}
    @Override protected void init(){layout(400,176);int w=(cardWidth-38)/2;button(left+16,top+cardHeight-42,w,"Clear binding",()->bind(InputConstants.UNKNOWN));button(left+22+w,top+cardHeight-42,w,"Cancel",this::onClose);}
    private void bind(InputConstants.Key key){Wanderlust.binds.get(feature).setKey(key);KeyMapping.resetMapping();minecraft.options.save();onClose();}
    @Override public boolean keyPressed(KeyEvent e){if(e.key()==GLFW.GLFW_KEY_ESCAPE){onClose();return true;}bind(InputConstants.getKey(e));return true;}
    @Override void drawContent(GuiGraphicsExtractor g,int mx,int my,float delta){subtitle(g,"Press a key to bind this module");text(g,"Escape cancels. Check conflicts in Minecraft Controls.",left+16,top+77,cardWidth-32,PanelStyle.MUTED);}
}
