package dev.shabzup.wanderlust;

import java.util.*;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.*;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;

final class ExtraActions {
    private static final Map<String,Integer> next=new HashMap<>();
    private static String vein="";
    private static int afkTick;
    static void reset(){next.clear();vein="";afkTick=0;AutoExperience.reset();AutoArmor.reset();}
    static void options() {
        AutoExperience.options();AutoArmor.options();
        for(String id:new String[]{"anti-anvil","anti-bed","auto-anvil","auto-city","auto-trap","auto-web","self-anvil","self-trap","self-web","surround","burrow","hole-filler","scaffold","air-place","liquid-filler","nuker","excavator","vein-miner","e-chest-farmer","spawn-proofer","auto-mount","auto-dirt-path","auto-farm-land","auto-log-strip","auto-farming","auto-mud","placer","entity-interact","auto-interact","double-doors-interact"}) {
            Settings.numeric(id,id+".range","Action range (blocks)",4,1,6,.25);
            Settings.numeric(id,id+".delay","Action delay (ticks)",4,1,40,1);
        }
        Settings.numeric("exp-thrower","exp-thrower.delay","Ticks between bottles",5,1,40,1);
        Settings.numeric("auto-exp","auto-exp.delay","Ticks between bottles",5,1,40,1);
        Settings.toggle("auto-exp","auto-exp.only-damaged","Only with damaged equipment",false);
        Settings.numeric("anti-afk","anti-afk.delay","Ticks between jumps",200,20,1200,20);
        Settings.string("offhand","offhand.item","Offhand item ID","minecraft:shield");
        Settings.choice("auto-weapon","auto-weapon.kind","Preferred weapon","Sword","Sword","Axe","Mace");
        Settings.numeric("attribute-swap","attribute-swap.slot","Attack hotbar slot (1–9)",1,1,9,1);
        Settings.numeric("auto-smelter","auto-smelter.delay","Transfer delay (ticks)",6,1,40,1);
        Settings.string("auto-smelter","auto-smelter.input","Input item ID","minecraft:iron_ore");
        Settings.string("auto-smelter","auto-smelter.fuel","Fuel item ID","minecraft:coal");
        Settings.numeric("auto-brewer","auto-brewer.delay","Transfer delay (ticks)",6,1,40,1);
        Settings.string("auto-brewer","auto-brewer.input","Ingredient ID","minecraft:nether_wart");
        Settings.string("auto-brewer","auto-brewer.fuel","Fuel item ID","minecraft:blaze_powder");
        Settings.numeric("arrow-dodge","arrow-dodge.range","Projectile detection range",8,2,16,1);
    }
    static boolean due(String id,int tick){if(!on(id)||tick<next.getOrDefault(id,0))return false;next.put(id,tick+(int)Settings.number(id+".delay"));return true;}
    static void tick(Minecraft mc) {
        var p=mc.player;if(p==null||!p.isAlive()||Movement.scouting())return;
        if(p.containerMenu!=p.inventoryMenu){containers(mc);return;}
        if(mc.gui.screen()!=null||mc.isPaused())return;
        if(on("anti-afk")&&p.tickCount>=afkTick){p.jumpFromGround();p.setYRot(p.getYRot()+15);afkTick=p.tickCount+(int)Settings.number("anti-afk.delay");}
        AutoArmor.tick(mc);
        if(on("auto-tool")&&mc.options.keyAttack.isDown()&&mc.hitResult instanceof BlockHitResult hit)tool(mc,mc.level.getBlockState(hit.getBlockPos()));
        if(on("auto-weapon")&&mc.crosshairPickEntity instanceof LivingEntity)weapon(mc);
        if(on("offhand"))offhand(mc);
        if(on("arrow-dodge"))dodge(mc);
        AutoExperience.tick(mc);
        if(due("exp-thrower",p.tickCount))use(mc,s->s.is(Items.EXPERIENCE_BOTTLE),90);
        if(on("no-mining-trace")&&mc.options.keyAttack.isDown())mc.hitResult=p.pick(p.blockInteractionRange(),1,false);
        if((on("speed-mine")||on("packet-mine")||on("instant-rebreak"))&&mc.options.keyAttack.isDown()&&mc.hitResult instanceof BlockHitResult hit)mine(mc,hit.getBlockPos());
        for(String id:new String[]{"surround","self-trap","self-web","self-anvil","burrow","auto-trap","auto-web","auto-anvil","auto-city","hole-filler","scaffold","anti-bed","anti-anvil","liquid-filler","nuker","excavator","vein-miner","e-chest-farmer","spawn-proofer","auto-dirt-path","auto-farm-land","auto-log-strip","auto-farming","auto-mud","placer","air-place","auto-mount","entity-interact","auto-interact","double-doors-interact"}){
            if(on(id)&&!Settings.options.stream().anyMatch(o->o.key().equals(id+".delay")))continue;
            if(due(id,p.tickCount)&&action(mc,id))break;
        }
    }
    private static boolean action(Minecraft mc,String id) {
        var p=mc.player;double range=Settings.number(id+".range");
        Player enemy=mc.level.players().stream().filter(e->Combat.validTarget(p,e)).min(Comparator.comparingDouble(e->e.distanceToSqr(p))).orElse(null);
        BlockPos self=p.blockPosition();
        if(Set.of("surround","self-trap","self-web","self-anvil","burrow","scaffold","auto-trap","auto-web","auto-anvil","auto-city").contains(id)) {
            boolean other=id.startsWith("auto-");if(other&&enemy==null)return false;BlockPos base=other?enemy.blockPosition():self;
            if(id.equals("auto-city")){for(Direction d:Direction.Plane.HORIZONTAL){BlockPos pos=base.relative(d);if(!mc.level.getBlockState(pos).isAir())return mine(mc,pos);}return false;}
            List<BlockPos> spots=new ArrayList<>();
            if(id.endsWith("web")||id.equals("burrow"))spots.add(base);
            else if(id.endsWith("anvil"))spots.add(base.above(2));
            else if(id.equals("scaffold"))spots.add(self.below());
            else {for(Direction d:Direction.Plane.HORIZONTAL)spots.add(base.relative(d));if(id.contains("trap"))spots.add(base.above(2));}
            for(BlockPos pos:spots)if(place(mc,pos,id.endsWith("web")?s->s.is(Items.COBWEB):id.endsWith("anvil")?s->s.is(Items.ANVIL):s->s.getItem() instanceof BlockItem))return true;
            return false;
        }
        if(id.equals("auto-mount")||id.equals("entity-interact")){
            for(Entity entity:mc.level.getEntitiesOfClass(Entity.class,p.getBoundingBox().inflate(range),e->e!=p&&e.isPickable())){
                if(id.equals("auto-mount")&&!(entity instanceof net.minecraft.world.entity.vehicle.boat.AbstractBoat||entity instanceof net.minecraft.world.entity.animal.equine.AbstractHorse))continue;
                if(p.isWithinEntityInteractionRange(entity,0)&&p.hasLineOfSight(entity)){mc.gameMode.interact(p,entity,new EntityHitResult(entity),InteractionHand.MAIN_HAND);return true;}
            }return false;
        }
        if(id.equals("placer")||id.equals("air-place")||id.equals("auto-interact")){if(mc.hitResult instanceof BlockHitResult hit){mc.gameMode.useItemOn(p,InteractionHand.MAIN_HAND,hit);return true;}return false;}
        if(id.equals("double-doors-interact")&&mc.options.keyUse.isDown()&&mc.hitResult instanceof BlockHitResult hit){for(Direction d:Direction.Plane.HORIZONTAL){BlockPos pos=hit.getBlockPos().relative(d);if(mc.level.getBlockState(pos).getBlock() instanceof DoorBlock){mc.gameMode.useItemOn(p,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false));return true;}}}
        if(id.equals("vein-miner")&&mc.options.keyAttack.isDown()&&mc.hitResult instanceof BlockHitResult hit)vein=key(mc.level.getBlockState(hit.getBlockPos()).getBlock());
        int r=(int)Math.ceil(Math.min(range,p.blockInteractionRange()));
        for(BlockPos pos:BlockPos.betweenClosed(self.offset(-r,-2,-r),self.offset(r,2,r))){
            if(!p.isWithinBlockInteractionRange(pos,0))continue;BlockState state=mc.level.getBlockState(pos);Block block=state.getBlock();
            if(id.equals("anti-bed")&&block instanceof BedBlock||id.equals("anti-anvil")&&block instanceof AnvilBlock)return mine(mc,pos);
            if(id.equals("nuker")&&!state.isAir()&&state.getDestroySpeed(mc.level,pos)>=0)return mine(mc,pos);
            if(id.equals("excavator")&&pos.getY()>=self.getY()&&pos.getY()<=self.getY()+1&&!state.isAir()&&pos.distSqr(self)<=range*range)return mine(mc,pos);
            if(id.equals("vein-miner")&&!vein.isEmpty()&&key(block).equals(vein))return mine(mc,pos);
            if(id.equals("liquid-filler")&&!state.getFluidState().isEmpty()&&place(mc,pos,s->s.getItem() instanceof BlockItem))return true;
            if(id.equals("hole-filler")&&state.isAir()&&!mc.level.getBlockState(pos.below()).isAir()&&pos.getY()==self.getY()){
                boolean hole=true;for(Direction d:Direction.Plane.HORIZONTAL)if(mc.level.getBlockState(pos.relative(d)).isAir())hole=false;
                if(hole&&place(mc,pos,s->s.is(Items.OBSIDIAN)))return true;
            }
            if(id.equals("spawn-proofer")&&state.isAir()&&mc.level.getBrightness(LightLayer.BLOCK,pos)<8&&!mc.level.getBlockState(pos.below()).isAir()&&place(mc,pos,s->s.is(Items.TORCH)))return true;
            if(id.equals("e-chest-farmer")){if(block instanceof EnderChestBlock)return mine(mc,pos);if(pos.equals(self.relative(p.getDirection()))&&place(mc,pos,s->s.is(Items.ENDER_CHEST)))return true;}
            Predicate<ItemStack> tool=null;
            if(id.equals("auto-dirt-path")&&(block==Blocks.GRASS_BLOCK||block==Blocks.DIRT))tool=s->s.is(ItemTags.SHOVELS);
            if(id.equals("auto-farm-land")&&(block==Blocks.GRASS_BLOCK||block==Blocks.DIRT||block==Blocks.DIRT_PATH))tool=s->s.is(ItemTags.HOES);
            if(id.equals("auto-log-strip")&&key(block).endsWith("_log")&&!key(block).contains("stripped"))tool=s->s.is(ItemTags.AXES);
            if(id.equals("auto-mud")&&(block==Blocks.DIRT||block==Blocks.COARSE_DIRT||block==Blocks.ROOTED_DIRT))tool=s->s.is(Items.POTION);
            if(tool!=null&&interact(mc,pos,tool))return true;
            if(id.equals("auto-farming")){
                if(block instanceof CropBlock crop&&crop.isMaxAge(state))return mine(mc,pos);
                if(block==Blocks.FARMLAND&&mc.level.getBlockState(pos.above()).isAir()&&place(mc,pos.above(),s->s.is(Items.WHEAT_SEEDS)||s.is(Items.CARROT)||s.is(Items.POTATO)||s.is(Items.BEETROOT_SEEDS)))return true;
            }
        } return false;
    }
    static boolean place(Minecraft mc,BlockPos pos,Predicate<ItemStack> accept) {
        if(!mc.level.getBlockState(pos).canBeReplaced()||!mc.player.isWithinBlockInteractionRange(pos,0))return false;
        for(Direction side:Direction.values()){
            BlockPos neighbor=pos.relative(side);if(mc.level.getBlockState(neighbor).canBeReplaced())continue;
            return useOn(mc,new BlockHitResult(Vec3.atCenterOf(neighbor).relative(side.getOpposite(),.5),side.getOpposite(),neighbor,false),accept);
        }return false;
    }
    private static boolean interact(Minecraft mc,BlockPos pos,Predicate<ItemStack> accept){return useOn(mc,new BlockHitResult(Vec3.atCenterOf(pos).add(0,.49,0),Direction.UP,pos,false),accept);}
    private static boolean useOn(Minecraft mc,BlockHitResult hit,Predicate<ItemStack> accept){int item=slot(mc,accept);if(item<0)return false;int old=mc.player.getInventory().getSelectedSlot();mc.player.getInventory().setSelectedSlot(item);var result=mc.gameMode.useItemOn(mc.player,InteractionHand.MAIN_HAND,hit);mc.player.getInventory().setSelectedSlot(old);return result.consumesAction();}
    static boolean mine(Minecraft mc,BlockPos pos){if(!mc.player.isWithinBlockInteractionRange(pos,0))return false;mc.gameMode.startDestroyBlock(pos,Direction.UP);mc.gameMode.continueDestroyBlock(pos,Direction.UP);mc.player.swing(InteractionHand.MAIN_HAND);return true;}
    static int slot(Minecraft mc,Predicate<ItemStack> accept){for(int i=0;i<9;i++)if(accept.test(mc.player.getInventory().getItem(i)))return i;return -1;}
    static boolean use(Minecraft mc,Predicate<ItemStack> accept,float pitch){
        boolean offhand=accept.test(mc.player.getOffhandItem());
        int slot=offhand?-1:slot(mc,accept);if(!offhand&&slot<0)return false;
        int old=mc.player.getInventory().getSelectedSlot();float before=mc.player.getXRot();
        InteractionHand hand=offhand?InteractionHand.OFF_HAND:InteractionHand.MAIN_HAND;
        if(!offhand)mc.player.getInventory().setSelectedSlot(slot);
        try{mc.player.setXRot(pitch);return mc.gameMode.useItem(mc.player,hand).consumesAction();}
        finally{mc.player.setXRot(before);mc.player.getInventory().setSelectedSlot(old);}
    }
    private static void tool(Minecraft mc,BlockState block){int best=mc.player.getInventory().getSelectedSlot();float speed=mc.player.getMainHandItem().getDestroySpeed(block);for(int i=0;i<9;i++){float candidate=mc.player.getInventory().getItem(i).getDestroySpeed(block);if(candidate>speed){speed=candidate;best=i;}}mc.player.getInventory().setSelectedSlot(best);}
    private static void weapon(Minecraft mc){String mode=Settings.choice("auto-weapon.kind");int best=-1,score=-1;for(int i=0;i<9;i++){ItemStack s=mc.player.getInventory().getItem(i);boolean match=mode.equals("Sword")?s.is(ItemTags.SWORDS):mode.equals("Axe")?s.is(ItemTags.AXES):s.is(Items.MACE);if(match&&s.getMaxDamage()>score){best=i;score=s.getMaxDamage();}}if(best>=0)mc.player.getInventory().setSelectedSlot(best);}
    private static void offhand(Minecraft mc){if(Combat.needsTotem(mc)||!mc.player.inventoryMenu.getCarried().isEmpty())return;String id=Settings.text("offhand.item");if(key(mc.player.getOffhandItem().getItem()).equals(id))return;for(int i=0;i<36;i++)if(key(mc.player.getInventory().getItem(i).getItem()).equals(id)){mc.gameMode.handleContainerInput(mc.player.inventoryMenu.containerId,i<9?i+36:i,40,ContainerInput.SWAP,mc.player);return;}}
    private static void dodge(Minecraft mc){for(AbstractArrow arrow:mc.level.getEntitiesOfClass(AbstractArrow.class,mc.player.getBoundingBox().inflate(Settings.number("arrow-dodge.range")))){Vec3 flight=arrow.getDeltaMovement();if(flight.lengthSqr()<.01)continue;Vec3 to=mc.player.position().subtract(arrow.position());if(to.dot(flight)>0&&to.normalize().dot(flight.normalize())>.9){Vec3 side=new Vec3(-flight.z,0,flight.x).normalize().scale(.5);mc.player.setDeltaMovement(side.x,mc.player.getDeltaMovement().y,side.z);return;}}}
    private static void containers(Minecraft mc){for(String id:new String[]{"auto-smelter","auto-brewer"}){boolean valid=id.equals("auto-smelter")?mc.player.containerMenu instanceof net.minecraft.world.inventory.AbstractFurnaceMenu:mc.player.containerMenu instanceof net.minecraft.world.inventory.BrewingStandMenu;if(!valid||!due(id,mc.player.tickCount))continue;var slots=mc.player.containerMenu.slots;int inventoryStart=id.equals("auto-smelter")?3:5;for(int i=0;i<inventoryStart;i++)if(i==(id.equals("auto-smelter")?2:-1)&&!slots.get(i).getItem().isEmpty()){mc.gameMode.handleContainerInput(mc.player.containerMenu.containerId,i,0,ContainerInput.QUICK_MOVE,mc.player);return;}for(int i=inventoryStart;i<slots.size();i++){String item=key(slots.get(i).getItem().getItem());if(item.equals(Settings.text(id+".input"))||item.equals(Settings.text(id+".fuel"))){mc.gameMode.handleContainerInput(mc.player.containerMenu.containerId,i,0,ContainerInput.QUICK_MOVE,mc.player);return;}}}}
    private static String key(Item item){return BuiltInRegistries.ITEM.getKey(item).toString();}
    private static String key(Block block){return BuiltInRegistries.BLOCK.getKey(block).toString();}
    private static boolean on(String id){return Wanderlust.on(id);}
}
