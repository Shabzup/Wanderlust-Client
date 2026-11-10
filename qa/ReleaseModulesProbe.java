package qa;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

public final class ReleaseModulesProbe extends CoreModulesProbe {
    int remaining;
    void value(String key,String value)throws Exception{UpdateProbe.call("Settings","put",new Class[]{String.class,String.class},key,value);}
    int bottles(Minecraft mc){return read(mc,p->{int n=0;for(int i=0;i<36;i++)if(p.getInventory().getItem(i).is(Items.EXPERIENCE_BOTTLE))n+=p.getInventory().getItem(i).getCount();return n;});}
    @Override void run(Minecraft mc)throws Exception{
        if(!loading&&mc.isGameLoadFinished()&&mc.gui.overlay()==null){
            loading=true;org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().handle());String name="ReleaseModules-"+System.currentTimeMillis();
            mc.createWorldOpenFlows().createFreshLevel(name,new LevelSettings(name,GameType.SURVIVAL,
                new LevelSettings.DifficultySettings(Difficulty.PEACEFUL,false,false),true,WorldDataConfiguration.DEFAULT),
                new WorldOptions(123,false,false),WorldPresets::createTestWorldDimensions,new TitleScreen());
        }
        if(mc.player==null||mc.gui.overlay()!=null||tick<325&&mc.gui.screen()!=null)return;tick++;
        if(tick==5){
            value("auto-exp.behavior","Repair");
            server(mc,p->{
                var armor=new ItemStack(Items.DIAMOND_CHESTPLATE);
                armor.enchant(p.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.MENDING),1);
                armor.setDamageValue(500);p.setItemSlot(EquipmentSlot.CHEST,armor);
                p.getInventory().setItem(9,new ItemStack(Items.EXPERIENCE_BOTTLE,64));p.inventoryMenu.broadcastChanges();
            });
        }
        if(tick==15)enable("AUTO_EXP",true);
        if(tick==85){
            check(bottles(mc)<64,"AutoEXP retrieves and consumes inventory bottles");
            check(read(mc,p->p.getItemBySlot(EquipmentSlot.CHEST).getDamageValue()<500),"AutoEXP repairs worn Mending equipment on server");
            server(mc,p->{p.getItemBySlot(EquipmentSlot.CHEST).setDamageValue(0);p.inventoryMenu.broadcastChanges();});
        }
        if(tick==100)remaining=bottles(mc);
        if(tick==125){
            check(bottles(mc)==remaining,"repair mode stops after durability target");enable("AUTO_EXP",false);
            server(mc,p->{
                p.setItemSlot(EquipmentSlot.CHEST,new ItemStack(Items.NETHERITE_CHESTPLATE));
                var diamond=new ItemStack(Items.DIAMOND_CHESTPLATE);
                diamond.enchant(p.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.PROTECTION),4);
                p.getInventory().setItem(12,diamond);p.inventoryMenu.broadcastChanges();
            });
        }
        if(tick==135)enable("AUTO_ARMOR",true);
        if(tick==160){
            check(read(mc,p->p.getItemBySlot(EquipmentSlot.CHEST).is(Items.DIAMOND_CHESTPLATE)),"AutoArmor considers protection rather than maximum durability");
            server(mc,p->{
                var cursed=new ItemStack(Items.NETHERITE_CHESTPLATE);
                cursed.enchant(p.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.BINDING_CURSE),1);
                cursed.enchant(p.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.PROTECTION),4);
                p.getInventory().setItem(13,cursed);p.inventoryMenu.broadcastChanges();
            });
        }
        if(tick==185){
            check(read(mc,p->p.getItemBySlot(EquipmentSlot.CHEST).is(Items.DIAMOND_CHESTPLATE)),"AutoArmor rejects binding-cursed replacement");
            server(mc,p->{p.setItemSlot(EquipmentSlot.CHEST,new ItemStack(Items.ELYTRA));p.inventoryMenu.broadcastChanges();});
        }
        if(tick==210){
            check(read(mc,p->p.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA)),"AutoArmor preserves equipped elytra");enable("AUTO_ARMOR",false);
            value("totem-mode","Strict");
            server(mc,p->{p.getInventory().setItem(14,new ItemStack(Items.TOTEM_OF_UNDYING));p.getInventory().setItem(15,new ItemStack(Items.SHIELD));p.inventoryMenu.broadcastChanges();});
        }
        if(tick==220){enable("AUTO_TOTEM",true);enable("OFFHAND",true);}
        if(tick==250){
            check(read(mc,p->p.getOffhandItem().is(Items.TOTEM_OF_UNDYING)),"strict AutoTotem takes priority over Offhand");enable("AUTO_TOTEM",false);enable("OFFHAND",false);
            value("aura-ignore-named","true");value("aura-weapon","Sword");value("aura-auto-switch","true");
            server(mc,p->{
                p.getInventory().setItem(0,ItemStack.EMPTY);p.getInventory().setSelectedSlot(0);p.getInventory().setItem(2,new ItemStack(Items.DIAMOND_SWORD));
                var mob=net.minecraft.world.entity.EntityTypes.COW.create(p.level(),EntitySpawnReason.COMMAND);
                mob.setPos(p.getX()+2,p.getY(),p.getZ());mob.setNoAi(true);mob.setCustomName(net.minecraft.network.chat.Component.literal("QA cow"));p.level().addFreshEntity(mob);cow=mob.getUUID();p.inventoryMenu.broadcastChanges();
            });
            UpdateProbe.call("EntityTargets","setAll",new Class[]{List.class,boolean.class},UpdateProbe.call("EntityTargets","entries"),false);
            UpdateProbe.call("EntityTargets","set",new Class[]{String.class,boolean.class},"minecraft:cow",true);
        }
        if(tick==260)enable("KILL_AURA",true);
        if(tick==285){
            check(read(mc,p->{var e=p.level().getEntity(cow);return e instanceof LivingEntity living&&living.getHealth()==10;}),"KillAura ignores named mobs when configured");
            value("aura-ignore-named","false");
        }
        if(tick==325){
            check(read(mc,p->{var e=p.level().getEntity(cow);return e==null||((LivingEntity)e).getHealth()<10;}),"KillAura selects weapon and damages allowed target");
            enable("KILL_AURA",false);
            mc.gui.setScreen(new net.minecraft.client.gui.screens.inventory.InventoryScreen(mc.player));
        }
        if(tick==335){
            check(mc.player.getInventory().getSelectedSlot()==0,"KillAura restores selected slot when disabled in an open inventory");
            System.out.println("RELEASE MODULES COMPLETE failures="+failures);mc.stop();
        }
    }
}
