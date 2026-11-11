package dev.shabzup.wanderlust;

import com.mojang.blaze3d.platform.InputConstants;
import dev.shabzup.wanderlust.mixin.KeyMappingAccessor;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.inventory.*;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import org.lwjgl.glfw.GLFW;

/** Menu movement reads current bindings and releases only keys it has managed. */
public final class MenuMovement {
    private static final Set<KeyMapping> managed = new HashSet<>();
    private static final Set<Integer> scanCodes = new HashSet<>();

    static void options() {
        Settings.choice("gui-move","gui-move.screens","Allowed screens","All","All","Inventories","Wanderlust");
        Settings.toggle("gui-move","gui-move.jump","Allow jumping",true);
        Settings.toggle("gui-move","gui-move.sneak","Allow sneaking",true);
        Settings.toggle("gui-move","gui-move.sprint","Allow sprinting",true);
        Settings.toggle("gui-move","gui-move.arrows","Rotate with arrow keys",false);
        Settings.numeric("gui-move","gui-move.rotation","Arrow rotation (degrees/tick)",4,.5,15,.5);
    }

    static InputConstants.Key key(KeyMapping mapping) { return ((KeyMappingAccessor)mapping).wanderlust$key(); }
    // ToggleKeyMapping.setDown(true) represents a press edge and flips state. Automation owns a held state.
    static void hold(KeyMapping mapping,boolean held) {
        ((KeyMappingAccessor)mapping).wanderlust$held(held);
        if(!held&&mapping instanceof net.minecraft.client.ToggleKeyMapping toggle)toggle.shouldRestoreStateOnScreenClosed();
    }

    static boolean physical(Minecraft mc, KeyMapping mapping) { return physical(mc,key(mapping)); }
    private static boolean physical(Minecraft mc, InputConstants.Key key) {
        if (key.getValue()<0) return false;
        long window=mc.getWindow().handle();
        return switch(key.getType()) {
            case MOUSE -> GLFW.glfwGetMouseButton(window,key.getValue())==GLFW.GLFW_PRESS;
            case KEYSYM -> InputConstants.isKeyDown(mc.getWindow(),key.getValue());
            case SCANCODE -> scanCodes.contains(key.getValue());
        };
    }

    static boolean allowed(Minecraft mc) {
        var screen=mc.gui.screen();
        if(!Wanderlust.on("gui-move")||mc.player==null||!mc.player.isAlive()||mc.player.isSpectator()
            ||mc.isPaused()||Movement.scouting()||screen==null||screen instanceof ChatScreen
            ||screen instanceof AbstractSignEditScreen||screen instanceof AnvilScreen
            ||screen instanceof BookEditScreen||screen instanceof AbstractCommandBlockEditScreen
            ||screen instanceof StructureBlockEditScreen||screen instanceof KeyBindsScreen||screen instanceof BindScreen)return false;
        GuiEventListener focused=screen;
        for(int depth=0;focused!=null&&depth<16;depth++) {
            if(focused instanceof EditBox edit&&edit.canConsumeInput())return false;
            focused=focused instanceof ContainerEventHandler container?container.getFocused():null;
        }
        return switch(Settings.choice("gui-move.screens")) {
            case "Inventories" -> screen instanceof AbstractContainerScreen<?>;
            case "Wanderlust" -> screen instanceof ControlPanel;
            default -> true;
        };
    }

    /** Runs immediately before vanilla builds its movement input, after screen key releases. */
    public static void readBindings() {
        var mc=Minecraft.getInstance();
        if(!mc.isWindowActive())scanCodes.clear();
        update(mc,k->physical(mc,k));
    }

    static void cleanup(Minecraft mc) { if(!allowed(mc))update(mc,k->physical(mc,k)); }

    public static void observeKey(long window,int action,int scanCode) {
        if(window!=Minecraft.getInstance().getWindow().handle()||scanCode<0)return;
        if(action==GLFW.GLFW_RELEASE)scanCodes.remove(scanCode);
        else if(action==GLFW.GLFW_PRESS)scanCodes.add(scanCode);
    }

    // Keep input sampling separate so key mapping and screen transitions can be tested without OS key injection.
    static void update(Minecraft mc, Predicate<InputConstants.Key> pressed) {
        if(!allowed(mc)) {
            for(var mapping:managed)hold(mapping,mc.gui.screen()==null&&pressed.test(key(mapping)));
            managed.clear();return;
        }
        var o=mc.options;
        for(var mapping:new KeyMapping[]{o.keyUp,o.keyDown,o.keyLeft,o.keyRight,o.keyJump,o.keyShift,o.keySprint}) {
            boolean enabled=mapping!=o.keyJump||Settings.toggle("gui-move.jump");
            enabled&=mapping!=o.keyShift||Settings.toggle("gui-move.sneak");
            enabled&=mapping!=o.keySprint||Settings.toggle("gui-move.sprint");
            hold(mapping,enabled&&pressed.test(key(mapping)));managed.add(mapping);
        }
        if(Settings.toggle("gui-move.arrows")) {
            float delta=(float)Settings.number("gui-move.rotation");
            int yaw=(pressed.test(InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_RIGHT))?1:0)
                -(pressed.test(InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_LEFT))?1:0);
            int pitch=(pressed.test(InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_DOWN))?1:0)
                -(pressed.test(InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_UP))?1:0);
            mc.player.setYRot(mc.player.getYRot()+yaw*delta);
            mc.player.setXRot(Math.clamp(mc.player.getXRot()+pitch*delta,-90,90));
        }
    }

    public static boolean consumesArrow(long window,int key) {
        var mc=Minecraft.getInstance();
        return window==mc.getWindow().handle()&&key>=GLFW.GLFW_KEY_RIGHT&&key<=GLFW.GLFW_KEY_UP
            &&Settings.toggle("gui-move.arrows")&&allowed(mc);
    }

    static void reset() { for(var mapping:managed)hold(mapping,false);managed.clear();scanCodes.clear(); }
}
