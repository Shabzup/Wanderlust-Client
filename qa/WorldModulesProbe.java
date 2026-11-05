package qa;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
public final class WorldModulesProbe extends CoreModulesProbe {
 int beforePlace;
 @Override void check(boolean ok,String text){if(!ok)failures++;System.out.println("WORLD "+(ok?"PASS ":"FAIL ")+text);}
 int count(Minecraft mc,Block block){return read(mc,p->{int n=0;for(BlockPos pos:BlockPos.betweenClosed(p.blockPosition().offset(-5,-2,-5),p.blockPosition().offset(5,2,5)))if(p.level().getBlockState(pos).is(block))n++;return n;});}
 int placed(Minecraft mc){return read(mc,p->{int n=0;for(BlockPos pos:BlockPos.betweenClosed(p.blockPosition().offset(-6,-2,-6),p.blockPosition().offset(6,7,6)))if(p.level().getBlockState(pos).is(Blocks.COBBLESTONE))n++;return n;});}
 @Override void run(Minecraft mc)throws Exception{
  if(!loading&&mc.isGameLoadFinished()&&mc.gui.overlay()==null){loading=true;org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().handle());String name="WorldModules-"+System.currentTimeMillis();mc.createWorldOpenFlows().createFreshLevel(name,new LevelSettings(name,GameType.CREATIVE,new LevelSettings.DifficultySettings(Difficulty.PEACEFUL,false,false),true,WorldDataConfiguration.DEFAULT),new WorldOptions(123,false,false),WorldPresets::createTestWorldDimensions,new TitleScreen());}
  if(mc.player==null||mc.gui.overlay()!=null)return;if(!playing){if(mc.gui.screen()!=null)return;playing=true;}tick++;
  if(tick==5)server(mc,p->{p.getInventory().setItem(0,new ItemStack(Items.DIAMOND_SHOVEL));p.getInventory().setSelectedSlot(0);for(int dx=1;dx<=2;dx++){p.level().setBlockAndUpdate(p.blockPosition().offset(dx,-1,0),Blocks.GRASS_BLOCK.defaultBlockState());p.level().setBlockAndUpdate(p.blockPosition().offset(dx,0,0),Blocks.AIR.defaultBlockState());}});
  if(tick==15)enable("AUTO_DIRT_PATH",true);
  if(tick==45){check(count(mc,Blocks.DIRT_PATH)>0,"AutoDirtPath converts grass on server");enable("AUTO_DIRT_PATH",false);server(mc,p->p.getInventory().setItem(0,new ItemStack(Items.DIAMOND_HOE)));}
  if(tick==60)enable("AUTO_FARM_LAND",true);
  if(tick==90){check(count(mc,Blocks.FARMLAND)>0,"AutoFarmLand tills ground on server");enable("AUTO_FARM_LAND",false);server(mc,p->{p.getInventory().setItem(0,new ItemStack(Items.DIAMOND_AXE));p.level().setBlockAndUpdate(p.blockPosition().offset(1,0,0),Blocks.OAK_LOG.defaultBlockState());});}
  if(tick==105)enable("AUTO_LOG_STRIP",true);
  if(tick==135){check(count(mc,Blocks.STRIPPED_OAK_LOG)>0,"AutoLogStrip strips an oak log on server");enable("AUTO_LOG_STRIP",false);server(mc,p->{p.setItemSlot(EquipmentSlot.CHEST,new ItemStack(Items.LEATHER_CHESTPLATE));p.getInventory().setItem(12,new ItemStack(Items.NETHERITE_CHESTPLATE));});}
  if(tick==150)enable("AUTO_ARMOR",true);
  if(tick==180){check(read(mc,p->p.getItemBySlot(EquipmentSlot.CHEST).is(Items.NETHERITE_CHESTPLATE)),"AutoArmor upgrades leather to netherite on server");enable("AUTO_ARMOR",false);server(mc,p->p.getInventory().setItem(13,new ItemStack(Items.SHIELD)));}
  if(tick==195)enable("OFFHAND",true);
  if(tick==225){check(read(mc,p->p.getOffhandItem().is(Items.SHIELD)),"Offhand equips configured inventory shield on server");enable("OFFHAND",false);server(mc,p->{p.setGameMode(GameType.SURVIVAL);p.getFoodData().setFoodLevel(20);p.getInventory().setItem(0,new ItemStack(Items.SHEARS));p.getInventory().setItem(1,new ItemStack(Items.WHEAT,16));p.level().setBlockAndUpdate(p.blockPosition().offset(1,0,0),Blocks.AIR.defaultBlockState());for(int i=0;i<2;i++){var sheep=(Sheep)net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getValue(net.minecraft.resources.Identifier.fromNamespaceAndPath("minecraft","sheep")).create(p.level(),EntitySpawnReason.COMMAND);sheep.setNoAi(true);sheep.setPos(p.getX()+1+i,p.getY(),p.getZ());p.level().addFreshEntity(sheep);}});}
  if(tick==245){enable("AUTO_SHEARER",true);enable("AUTO_BREED",true);}
  if(tick==320){check(read(mc,p->p.level().getEntitiesOfClass(Sheep.class,p.getBoundingBox().inflate(5)).stream().anyMatch(Sheep::isSheared)),"AutoShearer shears a sheep on server");check(read(mc,p->p.getInventory().getItem(1).getCount()<16&&p.level().getEntitiesOfClass(Sheep.class,p.getBoundingBox().inflate(5)).stream().anyMatch(Sheep::isInLove)),"AutoBreed feeds sheep into server breeding state");enable("AUTO_SHEARER",false);enable("AUTO_BREED",false);enable("STEP",true);}
  if(tick==330){check(mc.player.maxUpStep()>=1,"Step raises client step attribute");enable("STEP",false);}
  if(tick==340){check(mc.player.maxUpStep()<1,"disabling Step restores client step attribute");server(mc,p->{p.setGameMode(GameType.CREATIVE);p.getInventory().setItem(0,new ItemStack(Items.COBBLESTONE,64));p.getInventory().setSelectedSlot(0);});mc.player.setYRot(0);mc.player.setXRot(-30);}
  if(tick==355)enable("AIR_PLACE",true);
  if(tick==385){check(placed(mc)>0,"AirPlace places floating cobblestone on server from a missed ray hit");enable("AIR_PLACE",false);server(mc,p->{for(var sheep:p.level().getEntitiesOfClass(Sheep.class,p.getBoundingBox().inflate(8)))sheep.discard();for(BlockPos pos:BlockPos.betweenClosed(p.blockPosition().offset(-6,0,-6),p.blockPosition().offset(6,7,6)))if(p.level().getBlockState(pos).is(Blocks.COBBLESTONE))p.level().setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());p.level().setBlockAndUpdate(p.blockPosition().offset(0,1,3),Blocks.STONE.defaultBlockState());});beforePlace=placed(mc);mc.player.setYRot(0);mc.player.setXRot(5);}
  if(tick==400)enable("PLACER",true);
  if(tick==415)System.out.println("PLACER FIXTURE hit="+mc.hitResult+" item="+mc.player.getMainHandItem()+" pos="+mc.player.position());
  if(tick==430){check(placed(mc)>beforePlace,"Placer repeatedly places held blocks on server");enable("PLACER",false);System.out.println("WORLD COMPLETE failures="+failures);mc.stop();}
 }
}
