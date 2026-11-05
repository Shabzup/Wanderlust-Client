package dev.shabzup.wanderlust;

import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.*;
import org.lwjgl.glfw.GLFW;

public final class ExtraPlayer {
    public static boolean picking;
    private static RemotePlayer dummy;
    private static Double blockReach,entityReach;
    private static Boolean oldTooltips,oldBob;
    private static boolean middle,quiver;
    private static int attackSlot=-1;
    static void options(){
        Settings.numeric("reach","reach.blocks","Block reach",4.5,1,8,.25);Settings.numeric("reach","reach.entities","Entity reach",3,1,6,.25);
        Settings.numeric("hitboxes","hitboxes.expand","Target box expansion",.2,0,1,.05);
        Settings.numeric("rotation","rotation.yaw","Yaw",0,-180,180,1);Settings.numeric("rotation","rotation.pitch","Pitch",0,-90,90,1);
        Settings.string("ghost-hand","ghost-hand.blocks","Ignored block IDs (comma separated)","minecraft:chest,minecraft:ender_chest");
        Settings.string("name-protect","name-protect.alias","Displayed local name","Shabzup");
        Settings.numeric("hand-view","hand-view.x","Hand X offset",0,-1,1,.05);Settings.numeric("hand-view","hand-view.y","Hand Y offset",0,-1,1,.05);Settings.numeric("hand-view","hand-view.z","Hand Z offset",0,-1,1,.05);Settings.numeric("hand-view","hand-view.scale","Hand scale",1,.2,2,.05);
        Settings.string("no-render","no-render.entities","Hidden entity IDs","minecraft:item,minecraft:armor_stand");
    }
    static void tick(Minecraft mc){
        var p=mc.player;if(p==null)return;
        if(on("reach")){if(blockReach==null){blockReach=p.getAttributeBaseValue(Attributes.BLOCK_INTERACTION_RANGE);entityReach=p.getAttributeBaseValue(Attributes.ENTITY_INTERACTION_RANGE);}p.getAttribute(Attributes.BLOCK_INTERACTION_RANGE).setBaseValue(Settings.number("reach.blocks"));p.getAttribute(Attributes.ENTITY_INTERACTION_RANGE).setBaseValue(Settings.number("reach.entities"));}
        else if(blockReach!=null){p.getAttribute(Attributes.BLOCK_INTERACTION_RANGE).setBaseValue(blockReach);p.getAttribute(Attributes.ENTITY_INTERACTION_RANGE).setBaseValue(entityReach);blockReach=entityReach=null;}
        if(on("better-tooltips")){if(oldTooltips==null)oldTooltips=mc.options.advancedItemTooltips;mc.options.advancedItemTooltips=true;}else if(oldTooltips!=null){mc.options.advancedItemTooltips=oldTooltips;oldTooltips=null;}
        if(on("camera-tweaks")){if(oldBob==null)oldBob=mc.options.bobView().get();mc.options.bobView().set(false);}else if(oldBob!=null){mc.options.bobView().set(oldBob);oldBob=null;}
        if(on("fake-player")&&dummy==null){dummy=new RemotePlayer(mc.level,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"WanderlustDummy"));dummy.setId(-78432);dummy.setPos(p.position().add(2,0,0));dummy.setYRot(p.getYRot());for(EquipmentSlot slot:EquipmentSlot.VALUES)dummy.setItemSlot(slot,p.getItemBySlot(slot).copy());mc.level.addEntity(dummy);}
        else if(!on("fake-player")&&dummy!=null){dummy.discard();dummy=null;}
        if(mc.gui.screen()!=null)return;
        if(on("rotation")){p.setYRot((float)Settings.number("rotation.yaw"));p.setXRot((float)Settings.number("rotation.pitch"));}
        if(on("no-status-effects")){p.removeEffect(MobEffects.NAUSEA);p.removeEffect(MobEffects.BLINDNESS);p.removeEffect(MobEffects.DARKNESS);}
        if(on("liquid-interact"))mc.hitResult=p.pick(p.blockInteractionRange(),1,true);
        if(on("ghost-hand")){picking=true;try{mc.hitResult=p.pick(p.blockInteractionRange(),1,on("liquid-interact"));}finally{picking=false;}}
        boolean pressed=GLFW.glfwGetMouseButton(mc.getWindow().handle(),GLFW.GLFW_MOUSE_BUTTON_MIDDLE)==GLFW.GLFW_PRESS;
        if(on("middle-click-extra")&&pressed&&!middle)ExtraActions.use(mc,s->s.is(Items.ENDER_PEARL),p.getXRot());middle=pressed;
        if(on("chest-swap")){swapChest(mc);disable("chest-swap");}
        if(on("quiver")&&p.getMainHandItem().is(Items.BOW)){p.setXRot(-90);mc.options.keyUse.setDown(!p.isUsingItem()||p.getTicksUsingItem()<20);quiver=true;}else if(quiver){mc.options.keyUse.setDown(false);quiver=false;}
        if(on("hand-derp")&&p.tickCount%5==0)p.swing(p.tickCount%10==0?InteractionHand.MAIN_HAND:InteractionHand.OFF_HAND);
    }
    private static void swapChest(Minecraft mc){boolean glider=mc.player.getItemBySlot(EquipmentSlot.CHEST).has(DataComponents.GLIDER);for(int i=0;i<36;i++){var s=mc.player.getInventory().getItem(i);var equip=s.get(DataComponents.EQUIPPABLE);if(equip==null||equip.slot()!=EquipmentSlot.CHEST||s.has(DataComponents.GLIDER)==glider)continue;if(!mc.player.inventoryMenu.getCarried().isEmpty())return;int from=i<9?i+36:i;for(int slot:new int[]{from,6,from})mc.gameMode.handleContainerInput(mc.player.inventoryMenu.containerId,slot,0,ContainerInput.PICKUP,mc.player);return;}}
    public static boolean ignoreBlock(net.minecraft.world.level.block.state.BlockState state){return picking&&Arrays.asList(Settings.text("ghost-hand.blocks").split(",")).contains(BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());}
    public static boolean hide(Entity entity){return on("no-render")&&Arrays.asList(Settings.text("no-render.entities").split(",")).contains(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString());}
    public static void beforeAttack(){var mc=Minecraft.getInstance();if(on("attribute-swap")&&mc.player!=null){attackSlot=mc.player.getInventory().getSelectedSlot();mc.player.getInventory().setSelectedSlot((int)Settings.number("attribute-swap.slot")-1);}}
    public static void afterAttack(){var p=Minecraft.getInstance().player;if(attackSlot>=0&&p!=null)p.getInventory().setSelectedSlot(attackSlot);attackSlot=-1;}
    private static void disable(String id){for(Feature f:Feature.values())if(f.id.equals(id)){Settings.setEnabled(f,false);}}
    static void reset(){dummy=null;blockReach=entityReach=null;}
    private static boolean on(String id){return Wanderlust.on(id);}
}
