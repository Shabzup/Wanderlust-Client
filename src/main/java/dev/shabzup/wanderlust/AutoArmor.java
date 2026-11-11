package dev.shabzup.wanderlust;

import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

final class AutoArmor {
    private static int nextMove;
    static void reset(){nextMove=0;}
    static void options(){
        Settings.numeric("auto-armor","armor.delay","Ticks between armor moves",2,1,40,1);
        Settings.choice("auto-armor","armor.protection","Preferred protection","Protection","Protection","Blast","Fire","Projectile");
        Settings.toggle("auto-armor","armor.blast-legs","Prefer blast protection leggings",true);
        Settings.toggle("auto-armor","armor.keep-elytra","Keep equipped elytra",true);
        Settings.toggle("auto-armor","armor.avoid-binding","Avoid curse of binding",true);
        Settings.toggle("auto-armor","armor.avoid-frost","Avoid frost walker",true);
        Settings.toggle("auto-armor","armor.prevent-break","Remove nearly broken armor",false);
        Settings.numeric("auto-armor","armor.minimum","Minimum remaining durability",10,1,100,1);
    }
    static void tick(Minecraft mc){
        var p=mc.player;
        if(!Wanderlust.on("auto-armor")){reset();return;}
        if(p.tickCount<nextMove || !p.inventoryMenu.getCarried().isEmpty() || p.isSpectator())return;
        for(EquipmentSlot slot:new EquipmentSlot[]{EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET}){
            ItemStack worn=p.getItemBySlot(slot);
            if(level(worn,Enchantments.BINDING_CURSE)>0&&!p.isCreative())continue;
            if(slot==EquipmentSlot.CHEST&&worn.has(DataComponents.GLIDER)&&Settings.toggle("armor.keep-elytra"))continue;
            boolean wornBreaking=breaking(worn);
            double best=wornBreaking?-1:score(worn,slot);
            int source=-1;
            for(int i=0;i<36;i++){
                ItemStack item=p.getInventory().getItem(i);var eq=item.get(DataComponents.EQUIPPABLE);
                if(item.isEmpty()||eq==null||eq.slot()!=slot||item.has(DataComponents.GLIDER)||breaking(item)
                    ||Settings.toggle("armor.avoid-binding")&&level(item,Enchantments.BINDING_CURSE)>0
                    ||Settings.toggle("armor.avoid-frost")&&level(item,Enchantments.FROST_WALKER)>0)continue;
                double candidate=score(item,slot);
                if(candidate>best+.0001){best=candidate;source=i;}
            }
            if(source<0&&wornBreaking)for(int i=0;i<36;i++)if(p.getInventory().getItem(i).isEmpty()){source=i;break;}
            if(source<0)continue;
            int from=source<9?source+36:source;
            int to=switch(slot){case HEAD->5;case CHEST->6;case LEGS->7;default->8;};
            for(int click:new int[]{from,to,from})mc.gameMode.handleContainerInput(p.inventoryMenu.containerId,click,0,ContainerInput.PICKUP,p);
            nextMove=p.tickCount+(int)Settings.number("armor.delay");return;
        }
    }
    private static boolean breaking(ItemStack item){return Settings.toggle("armor.prevent-break")&&item.isDamageableItem()&&item.getMaxDamage()-item.getDamageValue()<=Settings.number("armor.minimum");}
    private static int level(ItemStack item,ResourceKey<Enchantment> key){
        for(var e:item.getEnchantments().entrySet())if(e.getKey().is(key))return e.getIntValue();return 0;
    }
    private static double score(ItemStack item,EquipmentSlot slot){
        if(item.isEmpty())return -1;
        double score=0;var mods=item.get(DataComponents.ATTRIBUTE_MODIFIERS);
        if(mods!=null)for(var e:mods.modifiers()){
            if(e.attribute().equals(Attributes.ARMOR))score+=e.modifier().amount()*4;
            if(e.attribute().equals(Attributes.ARMOR_TOUGHNESS))score+=e.modifier().amount()*2;
        }
        ResourceKey<Enchantment> preferred=switch(Settings.choice("armor.protection")){
            case "Blast"->Enchantments.BLAST_PROTECTION;case "Fire"->Enchantments.FIRE_PROTECTION;
            case "Projectile"->Enchantments.PROJECTILE_PROTECTION;default->Enchantments.PROTECTION;};
        if(slot==EquipmentSlot.LEGS&&Settings.toggle("armor.blast-legs"))preferred=Enchantments.BLAST_PROTECTION;
        score+=level(item,preferred)*3+level(item,Enchantments.PROTECTION);
        score+=level(item,Enchantments.UNBREAKING)*.2+level(item,Enchantments.MENDING)*.5;
        if(item.isDamageableItem())score+=(double)(item.getMaxDamage()-item.getDamageValue())/item.getMaxDamage()*.1;
        return score;
    }
}
