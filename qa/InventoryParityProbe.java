package qa;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

/** Inventory assertions read the integrated server's authoritative slots. */
public final class InventoryParityProbe extends CoreModulesProbe {
    void value(String key, String value) throws Exception {
        UpdateProbe.call("Settings", "put", new Class[]{String.class,String.class},key,value);
    }
    @Override void run(Minecraft mc) throws Exception {
        if(!loading && mc.isGameLoadFinished() && mc.gui.overlay()==null) {
            loading=true;
            org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().handle());
            String name="InventoryParity-"+System.currentTimeMillis();
            mc.createWorldOpenFlows().createFreshLevel(name,new LevelSettings(name,GameType.SURVIVAL,
                new LevelSettings.DifficultySettings(Difficulty.PEACEFUL,false,false),true,WorldDataConfiguration.DEFAULT),
                new WorldOptions(123,false,false),WorldPresets::createTestWorldDimensions,new TitleScreen());
        }
        if(mc.player==null || mc.gui.overlay()!=null || mc.gui.screen()!=null)return;
        tick++;
        if(tick==5) {
            value("replenish-excluded","minecraft:cobblestone");
            server(mc,p->{
                p.getInventory().setItem(0,new ItemStack(Items.COBBLESTONE,4));
                p.getInventory().setItem(9,new ItemStack(Items.COBBLESTONE,20));
                p.setItemSlot(EquipmentSlot.OFFHAND,new ItemStack(Items.SHIELD));
                p.inventoryMenu.broadcastChanges();
            });
        }
        if(tick==15)enable("AUTO_REPLENISH",true);
        if(tick==35){
            check(read(mc,p->p.getInventory().getItem(0).getCount()==4 && p.getInventory().getItem(9).getCount()==20),"replenish exclusion prevents move");
            value("replenish-excluded","");
        }
        if(tick==55){
            check(read(mc,p->p.getInventory().getItem(0).getCount()==24 && p.getInventory().getItem(9).isEmpty()),"inventory slot 9 replenishes hotbar correctly");
            check(read(mc,p->p.getOffhandItem().is(Items.SHIELD)),"slot 9 operation preserves offhand");
            enable("AUTO_REPLENISH",false);
            value("mend-mode","Offhand queue");
            value("mend-excluded","minecraft:diamond_pickaxe");
            server(mc,p->{
                ItemStack pick=new ItemStack(Items.DIAMOND_PICKAXE),axe=new ItemStack(Items.DIAMOND_AXE);
                for(ItemStack item:new ItemStack[]{pick,axe}){
                    item.enchant(p.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.MENDING),1);
                    item.setDamageValue(100);
                }
                p.getInventory().setItem(10,pick);p.getInventory().setItem(11,axe);
                p.inventoryMenu.broadcastChanges();
            });
        }
        if(tick==65)enable("AUTO_MEND",true);
        if(tick==85){
            check(read(mc,p->p.getOffhandItem().is(Items.SHIELD)),"mending queue protects occupied offhand");
            value("mend-force","true");
        }
        if(tick==105){
            check(read(mc,p->p.getOffhandItem().is(Items.DIAMOND_AXE)),"force mode queues damaged Mending tool");
            check(read(mc,p->p.getInventory().getItem(10).is(Items.DIAMOND_PICKAXE)),"mending queue respects item exclusion");
            check(read(mc,p->p.getInventory().getItem(11).is(Items.SHIELD)),"displaced offhand safely stored");
            // Simulate the completed repair to isolate the queue transition from XP acquisition.
            server(mc,p->{p.getOffhandItem().setDamageValue(0);p.inventoryMenu.broadcastChanges();});
        }
        if(tick==130){
            check(!(boolean)UpdateProbe.call("Settings","on",new Class[]{String.class},"auto-mend"),"mending queue disables on completion");
            check(read(mc,p->p.getOffhandItem().isEmpty()),"completed repair returned to inventory");
            check(read(mc,p->{for(int i=0;i<36;i++)if(p.getInventory().getItem(i).is(Items.DIAMOND_AXE)&&p.getInventory().getItem(i).getDamageValue()==0)return true;return false;}),"repaired item preserved");
            System.out.println("INVENTORY COMPLETE failures="+failures);mc.stop();
        }
    }
}
