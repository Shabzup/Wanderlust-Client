package dev.shabzup.wanderlust.mixin;

import dev.shabzup.wanderlust.*;
import net.minecraft.client.*;
import net.minecraft.client.multiplayer.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.player.AbstractClientPlayer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.shapes.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(Minecraft.class)
abstract class UseDelayMixin {
 @Shadow private int rightClickDelay;
 @Inject(method="tick",at=@At("HEAD"))private void fastUse(CallbackInfo ci){if(Wanderlust.on("fast-use"))rightClickDelay=0;}
 @Inject(method="startUseItem",at=@At("HEAD"),cancellable=true)private void noInteract(CallbackInfo ci){if(Wanderlust.on("no-interact"))ci.cancel();}
}
@Mixin(MultiPlayerGameMode.class)
abstract class BreakDelayMixin {
 @Shadow private int destroyDelay;
 @Inject(method="tick",at=@At("HEAD"))private void breakDelay(CallbackInfo ci){if(Wanderlust.on("break-delay"))destroyDelay=0;}
 @Inject(method="attack",at=@At("HEAD"))private void before(Player p,Entity target,CallbackInfo ci){ExtraPlayer.beforeAttack();}
 @Inject(method="attack",at=@At("RETURN"))private void after(Player p,Entity target,CallbackInfo ci){ExtraPlayer.afterAttack();}
}
@Mixin(Entity.class)
abstract class HitboxMixin {
 @Inject(method="getPickRadius",at=@At("RETURN"),cancellable=true)private void hitbox(CallbackInfoReturnable<Float> cir){if(Wanderlust.on("hitboxes")&&(Object)this!=Minecraft.getInstance().player)cir.setReturnValue(cir.getReturnValue()+(float)Wanderlust.number("hitboxes.expand"));}
}
@Mixin(Screen.class)
abstract class PortalScreenMixin {
 @Inject(method="isAllowedInPortal",at=@At("HEAD"),cancellable=true)private void portals(CallbackInfoReturnable<Boolean> cir){if(Wanderlust.on("portals"))cir.setReturnValue(true);}
}
@Mixin(BlockBehaviour.BlockStateBase.class)
abstract class GhostShapeMixin {
 @Inject(method="getShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",at=@At("HEAD"),cancellable=true)
 private void ghost(BlockGetter level,BlockPos pos,CollisionContext context,CallbackInfoReturnable<VoxelShape> cir){if(ExtraPlayer.ignoreBlock((BlockState)(Object)this))cir.setReturnValue(Shapes.empty());}
}
@Mixin(EntityRenderer.class)
abstract class HideEntityMixin {
 @Inject(method="shouldRender",at=@At("HEAD"),cancellable=true)private void hide(Entity entity,Frustum frustum,double x,double y,double z,CallbackInfoReturnable<Boolean> cir){if(ExtraPlayer.hide(entity))cir.setReturnValue(false);}
}
@Mixin(Player.class)
abstract class NameProtectMixin {
 @Inject(method="getName",at=@At("HEAD"),cancellable=true)private void name(CallbackInfoReturnable<Component> cir){if(Wanderlust.on("name-protect")&&(Object)this==Minecraft.getInstance().player)cir.setReturnValue(Component.literal(Wanderlust.text("name-protect.alias")));}
}
@Mixin(ClientPacketListener.class)
abstract class MotionControlMixin {
 @Unique private float wanderlust$yaw,wanderlust$pitch;
 @Inject(method="handleEntityEvent",at=@At("RETURN"))private void pop(ClientboundEntityEventPacket packet,CallbackInfo ci){var mc=Minecraft.getInstance();if(mc.level!=null&&packet.getEventId()==35){Entity entity=packet.getEntity(mc.level);if(entity!=null)ExtraVisuals.pop(entity.position());}}
 @Inject(method="handleMovePlayer",at=@At("HEAD"))private void beforeMove(ClientboundPlayerPositionPacket packet,CallbackInfo ci){var p=Minecraft.getInstance().player;if(p!=null){wanderlust$yaw=p.getYRot();wanderlust$pitch=p.getXRot();}}
 @Inject(method="handleMovePlayer",at=@At("RETURN"))private void afterMove(ClientboundPlayerPositionPacket packet,CallbackInfo ci){var p=Minecraft.getInstance().player;if(p!=null&&Wanderlust.on("no-rotate")){p.setYRot(wanderlust$yaw);p.setXRot(wanderlust$pitch);}}
 @Inject(method="handleSetEntityMotion",at=@At("HEAD"),cancellable=true)private void velocity(ClientboundSetEntityMotionPacket packet,CallbackInfo ci){var mc=Minecraft.getInstance();if(mc.isSameThread()&&mc.player!=null&&packet.id()==mc.player.getId()&&Wanderlust.on("velocity")){mc.player.setDeltaMovement(packet.movement().scale(Wanderlust.number("velocity.factor")));ci.cancel();}}
}
@Mixin(ItemInHandRenderer.class)
abstract class HandViewMixin {
 @Inject(method="submitArmWithItem",at=@At("HEAD"))private void before(AbstractClientPlayer p,float partial,float pitch,InteractionHand hand,float swing,ItemStack item,float equip,PoseStack pose,SubmitNodeCollector collector,int light,CallbackInfo ci){pose.pushPose();if(Wanderlust.on("hand-view")){pose.translate(Wanderlust.number("hand-view.x"),Wanderlust.number("hand-view.y"),Wanderlust.number("hand-view.z"));float scale=(float)Wanderlust.number("hand-view.scale");pose.scale(scale,scale,scale);}}
 @Inject(method="submitArmWithItem",at=@At("RETURN"))private void after(AbstractClientPlayer p,float partial,float pitch,InteractionHand hand,float swing,ItemStack item,float equip,PoseStack pose,SubmitNodeCollector collector,int light,CallbackInfo ci){pose.popPose();}
}
@Mixin(ItemEntityRenderer.class)
abstract class ItemPhysicsMixin {
 @Inject(method="extractRenderState(Lnet/minecraft/world/entity/item/ItemEntity;Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;F)V",at=@At("RETURN"))private void state(ItemEntity entity,ItemEntityRenderState state,float partial,CallbackInfo ci){if(Wanderlust.on("item-physics")&&entity.onGround()){state.ageInTicks=0;state.bobOffset=0;}}
 @Inject(method="submit(Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",at=@At("HEAD"))private void before(ItemEntityRenderState state,PoseStack pose,SubmitNodeCollector nodes,CameraRenderState camera,CallbackInfo ci){pose.pushPose();if(Wanderlust.on("item-physics"))pose.mulPose(Axis.XP.rotationDegrees(90));}
 @Inject(method="submit(Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",at=@At("RETURN"))private void after(ItemEntityRenderState state,PoseStack pose,SubmitNodeCollector nodes,CameraRenderState camera,CallbackInfo ci){pose.popPose();}
}
