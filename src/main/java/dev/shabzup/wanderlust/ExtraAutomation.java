package dev.shabzup.wanderlust;

import java.nio.file.*;
import java.util.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;

final class ExtraAutomation {
    private static boolean wasDead,rangeInside,jumpPressed,sucking,oreScanning;
    private static int instruction,waitUntil;
    private static final Set<Integer> pearls=new HashSet<>();
    static void options(){
        Settings.string("auto-crafter","auto-crafter.output","Recipe output item ID","minecraft:oak_planks");
        Settings.numeric("auto-crafter","auto-crafter.delay","Craft delay (ticks)",10,2,100,1);
        for(String id:new String[]{"auto-feed","auto-heal","auto-repair"}){Settings.string(id,id+".command","Server command",id.equals("auto-feed")?"/feed":id.equals("auto-heal")?"/heal":"/repair");Settings.numeric(id,id+".threshold","Health / hunger / durability threshold",id.equals("auto-repair")?30:10,1,100,1);}
        Settings.numeric("auto-leave","auto-leave.health","Disconnect below health (HP)",4,1,20,1);
        Settings.numeric("auto-trade","auto-trade.offer","Trade index (1-based)",1,1,64,1);
        Settings.numeric("auto-trade","auto-trade.delay","Trade interval (ticks)",10,2,100,1);
        Settings.string("death-commands","death-commands.commands","Respawn commands (; separated)","");
        Settings.numeric("slot-click","slot-click.slot","Container slot index",0,0,200,1);
        Settings.choice("slot-click","slot-click.action","Click action","Quick move","Quick move","Pickup","Throw");
        Settings.numeric("slot-click","slot-click.delay","Click interval (ticks)",10,1,100,1);
        Settings.string("instructions","instructions.steps","Steps (; separated): wait, jump, use, chat, baritone","wait 20; jump");
        Settings.string("minescript-integration","minescript-integration.command","Minescript command (without backslash)","");
        for(String axis:new String[]{"x","y","z"})Settings.numeric("range-actions","range-actions."+axis,axis.toUpperCase(Locale.ROOT),axis.equals("y")?64:0,-30000000,30000000,1);
        Settings.numeric("range-actions","range-actions.radius","Trigger radius",5,1,100,1);
        Settings.string("range-actions","range-actions.command","Command on entering region","");
        Settings.string("category-manager","category-manager.module","Module ID to move","kill-aura");
        Settings.choice("category-manager","category-manager.group","Destination category","Combat",Arrays.stream(Feature.Group.values()).map(Enum::name).toArray(String[]::new));
        Settings.numeric("item-sucker","item-sucker.range","Item search range",16,2,64,1);
        Settings.numeric("smite-aura","smite-aura.delay","Command interval (seconds)",10,1,120,1);
    }
    static void tick(Minecraft mc){
        var p=mc.player;if(p==null)return;
        if(on("hotkey-utility")){ExtraMisc.disable("hotkey-utility");mc.gui.setScreen(new KeyBindsScreen(mc.gui.screen(),mc.options));}
        if(on("category-manager")){for(Feature f:Feature.values())if(f.id.equals(Settings.text("category-manager.module"))){try{f.group=Feature.Group.valueOf(Settings.choice("category-manager.group"));Settings.put("category."+f.id,f.group.name());}catch(IllegalArgumentException ignored){}}ExtraMisc.disable("category-manager");if(mc.gui.screen() instanceof ControlPanel panel)panel.resize(panel.width,panel.height);}
        if(on("minescript-integration")){String command=Settings.text("minescript-integration.command");if(!command.isBlank()&&FabricLoader.getInstance().isModLoaded("minescript"))mc.gui.setScreen(new ChatScreen("\\"+command,false));else ExtraMisc.local(mc,"Install Minescript and configure a script command first.");ExtraMisc.disable("minescript-integration");}
        if(on("bedrock-printer")){exportBedrock(mc);ExtraMisc.disable("bedrock-printer");}
        boolean dead=!p.isAlive();if(wasDead&&!dead&&on("death-commands"))for(String command:Settings.text("death-commands.commands").split(";"))ExtraMisc.send(mc,command.trim());wasDead=dead;
        if(dead)return;
        if(on("auto-leave")&&p.getHealth()<=Settings.number("auto-leave.health")){p.connection.getConnection().disconnect(net.minecraft.network.chat.Component.literal("Wanderlust: AutoLeave health threshold"));return;}
        if(on("auto-feed")&&p.getFoodData().getFoodLevel()<=Settings.number("auto-feed.threshold")&&ExtraMisc.due("feed",30000))ExtraMisc.send(mc,Settings.text("auto-feed.command"));
        if(on("auto-heal")&&p.getHealth()<=Settings.number("auto-heal.threshold")&&ExtraMisc.due("heal",30000))ExtraMisc.send(mc,Settings.text("auto-heal.command"));
        if(on("auto-repair")&&EquipmentSlot.VALUES.stream().map(p::getItemBySlot).anyMatch(s->s.isDamaged()&&(s.getMaxDamage()-s.getDamageValue())*100.0/s.getMaxDamage()<=Settings.number("auto-repair.threshold"))&&ExtraMisc.due("repair",30000))ExtraMisc.send(mc,Settings.text("auto-repair.command"));
        if(on("ore-sim")&&ExtraMisc.due("orescan",6000))ores(mc);
        if(on("pearl-checker")){for(Entity e:mc.level.entitiesForRendering())if(BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).toString().equals("minecraft:ender_pearl")&&pearls.add(e.getId()))ExtraMisc.local(mc,"Ender pearl at "+e.blockPosition().toShortString());}else pearls.clear();
        if(p.containerMenu!=p.inventoryMenu||mc.gui.screen()!=null){containers(mc);return;}
        if(on("z-aimbot")){LivingEntity target=target(mc,e->true);if(target!=null)ExtraWorld.look(p,target.getEyePosition());}
        boolean jump=mc.options.keyJump.isDown();if((on("wind-charge-jump")||on("mace-combo"))&&jump&&!jumpPressed&&p.onGround())ExtraActions.use(mc,s->s.is(Items.WIND_CHARGE),90);jumpPressed=jump;
        if(on("legit-mace-kill")||on("mace-combo")||on("armor-buster")||on("blaze-form")){
            LivingEntity target=target(mc,e->on("blaze-form")?e instanceof Blaze:on("armor-buster")?e instanceof net.minecraft.world.entity.player.Player&&e.getArmorValue()>0:true);
            boolean mace=on("legit-mace-kill")||on("mace-combo");
            if(target!=null&&(!mace||p.getMainHandItem().is(Items.MACE)&&p.fallDistance>1)&&p.getAttackStrengthScale(.5f)>.95&&p.isWithinAttackRange(p.getMainHandItem(),target.getBoundingBox(),0)){ExtraWorld.look(p,target.getEyePosition());mc.gameMode.attack(p,target);p.swing(InteractionHand.MAIN_HAND);}
        }
        if(on("auto-sleep")&&mc.level.dimension().equals(Level.OVERWORLD)&&ExtraMisc.due("sleep",3000))for(BlockPos pos:BlockPos.betweenClosed(p.blockPosition().offset(-4,-2,-4),p.blockPosition().offset(4,2,4)))if(mc.level.getBlockState(pos).getBlock() instanceof BedBlock&&p.isWithinBlockInteractionRange(pos,0)){mc.gameMode.useItemOn(p,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false));break;}
        if(on("smite-aura")&&ExtraMisc.due("smite",(long)Settings.number("smite-aura.delay")*1000))for(var player:mc.level.players())if(player!=p&&player.distanceToSqr(p)<256)ExtraMisc.send(mc,"/smite "+player.getGameProfile().name());
        boolean itemMove=false;if(on("item-sucker")){ItemEntity item=mc.level.getEntitiesOfClass(ItemEntity.class,p.getBoundingBox().inflate(Settings.number("item-sucker.range"))).stream().min(Comparator.comparingDouble(e->e.distanceToSqr(p))).orElse(null);if(item!=null&&item.distanceToSqr(p)>1){if(BaritoneBridge.available()&&ExtraMisc.due("itempath",1500))BaritoneBridge.command("goto "+item.blockPosition().getX()+" "+item.blockPosition().getY()+" "+item.blockPosition().getZ());else if(!BaritoneBridge.available()){ExtraWorld.look(p,item.position());mc.options.keyUp.setDown(true);itemMove=true;}}}if(sucking&&!itemMove)mc.options.keyUp.setDown(false);sucking=itemMove;
        boolean inside=p.position().distanceToSqr(new Vec3(Settings.number("range-actions.x"),Settings.number("range-actions.y"),Settings.number("range-actions.z")))<=Math.pow(Settings.number("range-actions.radius"),2);
        if(on("range-actions")&&inside&&!rangeInside)ExtraMisc.send(mc,Settings.text("range-actions.command"));rangeInside=inside;
        if(on("instructions"))instructions(mc);else{instruction=0;waitUntil=0;}
    }
    private static LivingEntity target(Minecraft mc,java.util.function.Predicate<LivingEntity> filter){var p=mc.player;return mc.level.getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(16),e->Combat.validTarget(p,e)&&p.hasLineOfSight(e)&&filter.test(e)).stream().min(Comparator.comparingDouble(e->e.distanceToSqr(p))).orElse(null);}
    private static void containers(Minecraft mc){
        var menu=mc.player.containerMenu;
        if(on("slot-click")&&mc.player.tickCount%(int)Settings.number("slot-click.delay")==0){int slot=(int)Settings.number("slot-click.slot");if(slot<menu.slots.size()){ContainerInput action=switch(Settings.choice("slot-click.action")){case "Pickup"->ContainerInput.PICKUP;case "Throw"->ContainerInput.THROW;default->ContainerInput.QUICK_MOVE;};mc.gameMode.handleContainerInput(menu.containerId,slot,0,action,mc.player);}}
        if(on("auto-trade")&&menu instanceof MerchantMenu merchant&&mc.player.tickCount%(int)Settings.number("auto-trade.delay")==0){int index=(int)Settings.number("auto-trade.offer")-1;if(index<merchant.getOffers().size()){merchant.setSelectionHint(index);merchant.tryMoveItems(index);mc.player.connection.send(new net.minecraft.network.protocol.game.ServerboundSelectTradePacket(index));mc.gameMode.handleContainerInput(menu.containerId,2,0,ContainerInput.QUICK_MOVE,mc.player);}}
        if(on("auto-crafter")&&(menu instanceof CraftingMenu||menu==mc.player.inventoryMenu)&&mc.player.tickCount%(int)Settings.number("auto-crafter.delay")==0){String output=Settings.text("auto-crafter.output");var context=SlotDisplayContext.fromLevel(mc.level);for(var collection:mc.player.getRecipeBook().getCollections())for(var recipe:collection.getRecipes())if(recipe.display().result().resolveForStacks(context).stream().anyMatch(s->BuiltInRegistries.ITEM.getKey(s.getItem()).toString().equals(output))){mc.gameMode.handlePlaceRecipe(menu.containerId,recipe.id(),true);mc.gameMode.handleContainerInput(menu.containerId,0,0,ContainerInput.QUICK_MOVE,mc.player);return;}}
    }
    private static void instructions(Minecraft mc){if(mc.player.tickCount<waitUntil)return;String[] steps=Settings.text("instructions.steps").split(";");if(instruction>=steps.length){ExtraMisc.disable("instructions");instruction=0;return;}String line=steps[instruction++].trim();if(line.startsWith("wait ")){try{waitUntil=mc.player.tickCount+Math.clamp(Integer.parseInt(line.substring(5).trim()),1,12000);}catch(NumberFormatException ignored){}}else if(line.equals("jump"))mc.player.jumpFromGround();else if(line.equals("use"))mc.gameMode.useItem(mc.player,InteractionHand.MAIN_HAND);else if(line.startsWith("chat "))ExtraMisc.send(mc,line.substring(5));else if(line.startsWith("baritone "))BaritoneBridge.userCommand(line.substring(9));}
    private static void exportBedrock(Minecraft mc){List<String> data=new ArrayList<>();data.add("x,y,z");BlockPos base=mc.player.blockPosition();for(int x=-16;x<=16;x++)for(int z=-16;z<=16;z++)for(int y=mc.level.getMinY();y<=mc.level.getMaxY();y++){BlockPos pos=new BlockPos(base.getX()+x,y,base.getZ()+z);if(mc.level.isLoaded(pos)&&mc.level.getBlockState(pos).is(Blocks.BEDROCK))data.add(pos.getX()+","+y+","+pos.getZ());}try{Path path=FabricLoader.getInstance().getConfigDir().resolve("wanderlust-bedrock-"+System.currentTimeMillis()+".csv");Files.write(path,data);ExtraMisc.local(mc,"Exported "+(data.size()-1)+" bedrock blocks to "+path.getFileName());}catch(java.io.IOException e){ExtraMisc.local(mc,"Bedrock export failed: "+e.getMessage());}}
    private static void ores(Minecraft mc){if(oreScanning)return;var server=mc.getSingleplayerServer();if(server==null){ExtraMisc.local(mc,"OreSim needs an integrated world; remote ore seed simulation is unavailable.");return;}oreScanning=true;var clientLevel=mc.level;var dimension=mc.level.dimension();BlockPos origin=mc.player.blockPosition();server.submit(()->{List<BlockPos> ores=new ArrayList<>();var level=server.getLevel(dimension);if(level!=null)for(BlockPos pos:BlockPos.betweenClosed(origin.offset(-16,-32,-16),origin.offset(16,32,16))){if(!level.hasChunkAt(pos))continue;String id=BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock()).toString();if((id.endsWith("_ore")||id.endsWith("ancient_debris"))&&ores.size()<1024)ores.add(pos.immutable());}return List.copyOf(ores);}).whenComplete((positions,error)->mc.execute(()->{oreScanning=false;if(error==null&&mc.level==clientLevel)ExtraVisuals.generatedOres=positions;}));}
    private static boolean on(String id){return Wanderlust.on(id);}
}
