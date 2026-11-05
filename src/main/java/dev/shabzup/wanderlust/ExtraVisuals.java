package dev.shabzup.wanderlust;

import java.util.*;
import net.minecraft.client.Minecraft;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gizmos.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;

public final class ExtraVisuals {
    private static final String[] IDS={"block-esp","block-selection","breadcrumbs","trail","city-esp","entity-owner","hole-esp","item-highlight","light-overlay","logout-spots","marker","trajectories","trajectories-plus","tunnel-esp","void-esp","grid","deepslate-esp","better-locator","nerd-vision","break-indicators","stash-finder","sign-search","better-beacons"};
    private static final Set<String> SCANS=Set.of("block-esp","deepslate-esp","hole-esp","light-overlay","tunnel-esp","void-esp","stash-finder","sign-search","better-beacons");
    private record Mark(BlockPos pos,int color,String label){}
    private static final Map<String,List<Mark>> blocks=new HashMap<>();
    private static final Deque<Vec3> trail=new ArrayDeque<>();
    private static final Map<UUID,Vec3> previousPlayers=new HashMap<>();
    private static final Map<UUID,Vec3> logout=new HashMap<>();
    private static int scan;
    private static Integer oldFov;
    private static boolean ownVision;
    private static MobEffectInstance previousVision;
    private static final Map<Vec3,Long> pops=new LinkedHashMap<>();
    static List<BlockPos> generatedOres=List.of();
    static void reset(){blocks.clear();trail.clear();pops.clear();generatedOres=List.of();previousPlayers.clear();logout.clear();scan=0;ownVision=false;previousVision=null;}
    public static void pop(Vec3 position){pops.put(position,System.currentTimeMillis());}
    static void options(){
        for(String id:IDS)Settings.numeric(id,id+".range","Render / scan range",id.equals("block-esp")?16:24,4,SCANS.contains(id)?24:64,4);
        Settings.string("block-esp","block-esp.blocks","Block IDs (comma separated)","minecraft:diamond_ore,minecraft:deepslate_diamond_ore,minecraft:ancient_debris");
        Settings.numeric("trail","trail.length","History length (ticks)",300,20,1200,20);
        Settings.numeric("breadcrumbs","breadcrumbs.length","History length (ticks)",300,20,1200,20);
        Settings.numeric("zoom-plus","zoom-plus.fov","Zoom FOV",25,10,90,1);
        Settings.string("sign-search","sign-search.query","Sign text filter (empty matches all)","");
        Settings.numeric("marker","marker.x","X",0,-30000000,30000000,1);Settings.numeric("marker","marker.y","Y",64,-64,320,1);Settings.numeric("marker","marker.z","Z",0,-30000000,30000000,1);
    }
    static boolean active(){for(String id:IDS)if(on(id))return true;return on("chams")||on("pop-chams")||on("ore-sim");}
    static void tick(Minecraft mc){
        if(on("fullbright")){
            if(!ownVision){previousVision=mc.player.getEffect(MobEffects.NIGHT_VISION);ownVision=true;}
            mc.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION,300,0,false,false));
        }else if(ownVision){mc.player.removeEffect(MobEffects.NIGHT_VISION);if(previousVision!=null)mc.player.addEffect(previousVision);ownVision=false;previousVision=null;}
        if(on("zoom-plus")){if(oldFov==null)oldFov=mc.options.fov().get();mc.options.fov().set((int)Settings.number("zoom-plus.fov"));}
        else if(oldFov!=null){mc.options.fov().set(oldFov);oldFov=null;}
        if(on("breadcrumbs")||on("trail")){trail.addLast(mc.player.position());int limit=(int)Math.max(Settings.number("trail.length"),Settings.number("breadcrumbs.length"));while(trail.size()>limit)trail.removeFirst();}else trail.clear();
        if(on("logout-spots")){Set<UUID> seen=new HashSet<>();for(Player p:mc.level.players()){seen.add(p.getUUID());previousPlayers.put(p.getUUID(),p.position());}for(var e:previousPlayers.entrySet())if(!seen.contains(e.getKey()))logout.put(e.getKey(),e.getValue());previousPlayers.keySet().retainAll(seen);}else{previousPlayers.clear();logout.clear();}
        if(scan--<=0){scan=30;scan(mc);}
    }
    private static void scan(Minecraft mc){
        blocks.clear();boolean needed=false;for(String id:new String[]{"block-esp","deepslate-esp","hole-esp","light-overlay","tunnel-esp","void-esp","stash-finder","sign-search","better-beacons"})if(on(id))needed=true;
        if(!needed)return;
        int radius=4;for(String id:SCANS)if(on(id))radius=Math.max(radius,(int)Settings.number(id+".range"));
        BlockPos center=mc.player.blockPosition();Set<String> wanted=new HashSet<>(Arrays.asList(Settings.text("block-esp.blocks").split(",")));
        for(BlockPos mutable:BlockPos.betweenClosed(center.offset(-radius,-8,-radius),center.offset(radius,8,radius))){
            if(!mc.level.isLoaded(mutable))continue;BlockPos pos=mutable.immutable();var state=mc.level.getBlockState(pos);Block b=state.getBlock();String key=BuiltInRegistries.BLOCK.getKey(b).toString();
            if(on("block-esp")&&wanted.contains(key))mark("block-esp",pos,0xFF78ECFF,"");
            if(on("deepslate-esp")&&key.contains("deepslate"))mark("deepslate-esp",pos,0xFF9B7BE8,"");
            if(on("better-beacons")&&b==Blocks.BEACON)mark("better-beacons",pos,0xFF75FFCC,"Beacon");
            if(on("stash-finder")&&(b instanceof ChestBlock||b instanceof BarrelBlock||b instanceof ShulkerBoxBlock))mark("stash-finder",pos,0xFFFFCF65,"Storage");
            if(on("sign-search")&&b instanceof SignBlock&&mc.level.getBlockEntity(pos) instanceof net.minecraft.world.level.block.entity.SignBlockEntity sign){String query=Settings.text("sign-search.query").toLowerCase(Locale.ROOT);String text=Arrays.stream(sign.getFrontText().getMessages(false)).map(net.minecraft.network.chat.Component::getString).collect(java.util.stream.Collectors.joining(" "));if(text.toLowerCase(Locale.ROOT).contains(query))mark("sign-search",pos,0xFFFFEB98,text.isBlank()?"Sign":text);}
            if(state.isAir()&&!mc.level.getBlockState(pos.below()).isAir()){
                if(on("light-overlay")&&pos.getY()==center.getY()&&mc.level.getBrightness(LightLayer.BLOCK,pos)<8)mark("light-overlay",pos,0xFFFF6C6C,Integer.toString(mc.level.getBrightness(LightLayer.BLOCK,pos)));
                if((on("hole-esp")||on("tunnel-esp"))&&mc.level.getBlockState(pos.above()).isAir()){
                    int walls=0;for(Direction d:Direction.Plane.HORIZONTAL)if(!mc.level.getBlockState(pos.relative(d)).isAir())walls++;
                    if(walls==4&&on("hole-esp"))mark("hole-esp",pos,0xFF7AFFB0,"");
                    if(on("tunnel-esp")&&walls==2)mark("tunnel-esp",pos,0xFF8ED7FF,"");
                }
            }
            if(on("void-esp")&&pos.getY()==center.getY()-8&&state.isAir()){boolean empty=true;for(int y=pos.getY()-1;y>=mc.level.getMinY();y--)if(!mc.level.getBlockState(new BlockPos(pos.getX(),y,pos.getZ())).isAir()){empty=false;break;}if(empty)mark("void-esp",pos,0xFFED76FF,"");}
        }
    }
    private static void mark(String id,BlockPos pos,int color,String label){var list=blocks.computeIfAbsent(id,k->new ArrayList<>());if(list.size()<512)list.add(new Mark(pos,color,label));}
    static void draw(LevelExtractionContext ctx){
        var mc=Minecraft.getInstance();
        if(on("ore-sim"))for(BlockPos pos:generatedOres)Gizmos.cuboid(pos,GizmoStyle.stroke(0xFF72DFF0,1.5f)).setAlwaysOnTop();
        for(var e:blocks.entrySet())if(on(e.getKey()))for(Mark marker:e.getValue()){
            if(mc.player.distanceToSqr(Vec3.atCenterOf(marker.pos()))>Math.pow(Settings.number(e.getKey()+".range"),2))continue;
            Gizmos.cuboid(marker.pos(),GizmoStyle.stroke(marker.color(),1.2f)).setAlwaysOnTop();
            if(!marker.label().isBlank())Gizmos.billboardText(marker.label(),Vec3.atCenterOf(marker.pos()).add(0,.7,0),TextGizmo.Style.whiteAndCentered().withScale(.5f)).setAlwaysOnTop();
        }
        if(on("block-selection")&&mc.hitResult instanceof BlockHitResult hit)Gizmos.cuboid(hit.getBlockPos(),GizmoStyle.stroke(0xFFC58AFF,2)).setAlwaysOnTop();
        if(on("break-indicators")&&mc.options.keyAttack.isDown()&&mc.hitResult instanceof BlockHitResult hit){Gizmos.cuboid(hit.getBlockPos(),GizmoStyle.stroke(0xFFFFB365,3)).setAlwaysOnTop();Gizmos.billboardText("Mining",Vec3.atCenterOf(hit.getBlockPos()),TextGizmo.Style.whiteAndCentered()).setAlwaysOnTop();}
        if(on("trail")||on("breadcrumbs")){Vec3 last=null;for(Vec3 point:trail){if(last!=null&&on("trail"))Gizmos.line(last.add(0,.05,0),point.add(0,.05,0),0xFFC58AFF,1.5f);if(on("breadcrumbs"))Gizmos.cuboid(new AABB(point.add(-.025,0,-.025),point.add(.025,.05,.025)),GizmoStyle.stroke(0xFFBDA5FF,1));last=point;}}
        if(on("logout-spots"))for(Vec3 point:logout.values()){Gizmos.cuboid(new AABB(point.add(-.3,0,-.3),point.add(.3,1.8,.3)),GizmoStyle.stroke(0xFFED92CB,2)).setAlwaysOnTop();Gizmos.billboardText("Logout",point.add(0,2,0),TextGizmo.Style.whiteAndCentered()).setAlwaysOnTop();}
        if(on("marker")){Vec3 pos=new Vec3(Settings.number("marker.x"),Settings.number("marker.y"),Settings.number("marker.z"));Gizmos.cuboid(new AABB(pos,pos.add(1,1,1)),GizmoStyle.stroke(0xFFFFD36E,2)).setAlwaysOnTop();Gizmos.billboardText("Marker",pos.add(.5,1.2,.5),TextGizmo.Style.whiteAndCentered()).setAlwaysOnTop();}
        if(on("grid")){BlockPos p=mc.player.blockPosition();int range=Math.min(32,(int)Settings.number("grid.range"));for(int i=-range;i<=range;i++){Gizmos.line(new Vec3(p.getX()+i,p.getY()+.02,p.getZ()-range),new Vec3(p.getX()+i,p.getY()+.02,p.getZ()+range),0x667F9BB4,1);Gizmos.line(new Vec3(p.getX()-range,p.getY()+.02,p.getZ()+i),new Vec3(p.getX()+range,p.getY()+.02,p.getZ()+i),0x667F9BB4,1);}}
        for(Entity entity:ctx.level().entitiesForRendering()){
            if(entity==mc.player)continue;
            if(on("chams")&&entity instanceof net.minecraft.world.entity.LivingEntity)Gizmos.cuboid(entity.getBoundingBox(),GizmoStyle.fill(0x55C58AFF)).setAlwaysOnTop();
            if(on("item-highlight")&&entity instanceof ItemEntity)Gizmos.cuboid(entity.getBoundingBox(),GizmoStyle.stroke(0xFFFFCE67,2)).setAlwaysOnTop();
            if(on("nerd-vision"))Gizmos.billboardText(entity.getType().getDescription().getString()+" #"+entity.getId(),entity.position().add(0,entity.getBbHeight()+.2,0),TextGizmo.Style.whiteAndCentered().withScale(.5f)).setAlwaysOnTop();
            if(on("entity-owner")&&entity instanceof TamableAnimal pet&&pet.getOwnerReference()!=null)Gizmos.billboardText("Owner "+pet.getOwnerReference().getUUID(),entity.position().add(0,entity.getBbHeight()+.4,0),TextGizmo.Style.whiteAndCentered().withScale(.4f)).setAlwaysOnTop();
            if(on("better-locator")&&entity instanceof Player)Gizmos.line(ctx.camera().position(),entity.position(),0xFF7AF0DC,1.5f).setAlwaysOnTop();
            if(on("city-esp")&&entity instanceof Player){for(Direction d:Direction.Plane.HORIZONTAL){BlockPos pos=entity.blockPosition().relative(d);if(!ctx.level().getBlockState(pos).isAir())Gizmos.cuboid(pos,GizmoStyle.stroke(0xFFFF8877,2)).setAlwaysOnTop();}}
        }
        if(on("pop-chams")){pops.values().removeIf(time->System.currentTimeMillis()-time>3000);for(Vec3 pos:pops.keySet())Gizmos.cuboid(new AABB(pos.add(-.3,0,-.3),pos.add(.3,1.8,.3)),GizmoStyle.strokeAndFill(0xFFFFC96A,2,0x55FFC96A)).setAlwaysOnTop();}else pops.clear();
        if(on("trajectories")||on("trajectories-plus"))trajectory(mc);
    }
    private static void trajectory(Minecraft mc){
        ItemStack item=mc.player.getMainHandItem();double range=Settings.number(on("trajectories-plus")?"trajectories-plus.range":"trajectories.range");
        boolean throwable=item.is(Items.BOW)||item.is(Items.CROSSBOW)||item.is(Items.TRIDENT)||item.is(Items.ENDER_PEARL)||item.is(Items.SNOWBALL)||item.is(Items.EGG)||item.is(Items.SPLASH_POTION)||item.is(Items.EXPERIENCE_BOTTLE)||item.is(Items.WIND_CHARGE);
        if(throwable){double speed=item.is(Items.BOW)||item.is(Items.CROSSBOW)?3:item.is(Items.TRIDENT)?2.5:item.is(Items.SPLASH_POTION)||item.is(Items.EXPERIENCE_BOTTLE)?.75:1.5;path(mc,mc.player.getEyePosition(),mc.player.getLookAngle().scale(speed),item.is(Items.WIND_CHARGE)?0:.05,range);}
        if(on("trajectories-plus"))for(Entity e:mc.level.entitiesForRendering())if(e instanceof net.minecraft.world.entity.projectile.Projectile&&e.distanceToSqr(mc.player)<=range*range)path(mc,e.position(),e.getDeltaMovement(),.05,range);
    }
    private static void path(Minecraft mc,Vec3 start,Vec3 velocity,double gravity,double range){Vec3 pos=start;for(int i=0;i<160;i++){Vec3 next=pos.add(velocity);var hit=mc.level.clip(new net.minecraft.world.level.ClipContext(pos,next,net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,mc.player));Gizmos.line(pos,next,0xFF8CEBFF,1.5f).setAlwaysOnTop();if(hit.getType()!=HitResult.Type.MISS){Gizmos.cuboid(new AABB(hit.getLocation().add(-.1,-.1,-.1),hit.getLocation().add(.1,.1,.1)),GizmoStyle.stroke(0xFFFFD066,2)).setAlwaysOnTop();break;}if(next.distanceToSqr(start)>range*range)break;pos=next;velocity=velocity.scale(.99).add(0,-gravity,0);}}
    private static boolean on(String id){return Wanderlust.on(id);}
}
