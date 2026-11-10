package dev.shabzup.wanderlust;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

/** Repair thresholds use hysteresis: start low, then finish the chosen item. */
final class AutoExperience {
    private static EquipmentSlot repairing;
    private static Item repairItem;
    private static int nextUse;

    static void options() {
        Settings.choice("auto-exp","auto-exp.behavior","XP use mode","Continuous","Continuous","Repair");
        Settings.choice("auto-exp","auto-exp.equipment","Equipment to repair","Both","Both","Armor","Hands");
        Settings.numeric("auto-exp","auto-exp.start","Start below durability (%)",30,1,99,1);
        Settings.numeric("auto-exp","auto-exp.stop","Stop at durability (%)",80,1,100,1);
        Settings.toggle("auto-exp","auto-exp.replenish","Move inventory bottles to hotbar",true);
        Settings.numeric("auto-exp","auto-exp.slot","Bottle hotbar slot (1–9)",6,1,9,1);
        Settings.toggle("auto-exp","auto-exp.ground","Only while on ground",false);
        Settings.toggle("auto-exp","auto-exp.pause-use","Pause while using another item",true);
    }

    static void reset() { repairing=null;repairItem=null;nextUse=0; }

    static void tick(Minecraft mc) {
        if (!Wanderlust.on("auto-exp")) { reset();return; }
        var p=mc.player;
        if(p.tickCount<nextUse || !p.inventoryMenu.getCarried().isEmpty()
            || Settings.toggle("auto-exp.ground")&&!p.onGround()
            || Settings.toggle("auto-exp.pause-use")&&p.isUsingItem())return;
        boolean repair=Settings.choice("auto-exp.behavior").equals("Repair");
        if(repair) {
            double stop=Math.max(Settings.number("auto-exp.start"),Settings.number("auto-exp.stop"));
            if(repairing!=null && (!allowed(repairing) || !p.getItemBySlot(repairing).is(repairItem)
                || !needs(p.getItemBySlot(repairing),stop))) { repairing=null;repairItem=null; }
            if(repairing==null) {
                for(EquipmentSlot slot:EquipmentSlot.VALUES) {
                    ItemStack item=p.getItemBySlot(slot);
                    if(allowed(slot)&&needs(item,Settings.number("auto-exp.start"))) {repairing=slot;repairItem=item.getItem();break;}
                }
            }
            if(repairing==null)return;
        } else {
            repairing=null;repairItem=null;
            if(Settings.toggle("auto-exp.only-damaged") && EquipmentSlot.VALUES.stream().noneMatch(s->p.getItemBySlot(s).isDamaged()))return;
        }
        boolean available=p.getOffhandItem().is(Items.EXPERIENCE_BOTTLE)
            || ExtraActions.slot(mc,s->s.is(Items.EXPERIENCE_BOTTLE))>=0;
        if(!available && Settings.toggle("auto-exp.replenish")) {
            int target=(int)Settings.number("auto-exp.slot")-1;
            // Keep the hand being repaired available for XP pickup after the throw.
            if(repairing==EquipmentSlot.MAINHAND && target==p.getInventory().getSelectedSlot())target=(target+1)%9;
            for(int i=9;i<36;i++)if(p.getInventory().getItem(i).is(Items.EXPERIENCE_BOTTLE)) {
                mc.gameMode.handleContainerInput(p.inventoryMenu.containerId,i,target,ContainerInput.SWAP,p);
                nextUse=p.tickCount+1;return;
            }
        }
        if(available) {
            ExtraActions.use(mc,s->s.is(Items.EXPERIENCE_BOTTLE),90);
            nextUse=p.tickCount+(int)Settings.number("auto-exp.delay");
        }
    }

    private static boolean allowed(EquipmentSlot slot) {
        String mode=Settings.choice("auto-exp.equipment");
        return slot.isArmor()? !mode.equals("Hands")
            : (slot==EquipmentSlot.MAINHAND||slot==EquipmentSlot.OFFHAND)&&!mode.equals("Armor");
    }
    private static boolean needs(ItemStack item,double threshold) {
        return item.isDamaged() && EnchantmentHelper.has(item,EnchantmentEffectComponents.REPAIR_WITH_XP)
            && 100d*(item.getMaxDamage()-item.getDamageValue())/item.getMaxDamage()<threshold;
    }
}
