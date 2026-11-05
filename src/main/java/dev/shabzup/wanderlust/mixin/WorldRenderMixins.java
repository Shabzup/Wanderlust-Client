package dev.shabzup.wanderlust.mixin;
import dev.shabzup.wanderlust.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.Direction;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

@Mixin(ClientClockManager.class)
abstract class TimeDisplayMixin {
 @Inject(method="getTotalTicks",at=@At("HEAD"),cancellable=true)private void time(Holder<WorldClock> clock,CallbackInfoReturnable<Long> cir){if(Wanderlust.on("time-changer"))cir.setReturnValue((long)Wanderlust.number("time-changer.time"));}
}
@Mixin(DeltaTracker.Timer.class)
abstract class TimerSpeedMixin {
 @ModifyExpressionValue(method="advanceGameTime",at=@At(value="FIELD",target="Lnet/minecraft/client/DeltaTracker$Timer;msPerTick:F"))private float speed(float original){return Wanderlust.on("timer")?original/(float)Wanderlust.number("timer.factor"):original;}
}
@Mixin(PlayerTabOverlay.class)
abstract class TabDetailsMixin {
 @Inject(method="getNameForDisplay",at=@At("RETURN"),cancellable=true)private void tab(PlayerInfo info,CallbackInfoReturnable<Component> cir){if(Wanderlust.on("better-tab"))cir.setReturnValue(cir.getReturnValue().copy().append(Component.literal("  "+info.getLatency()+"ms")));}
}
@Mixin(BossHealthOverlay.class)
abstract class BossStackMixin {
 @Shadow @Final private java.util.Map<java.util.UUID,LerpingBossEvent> events;
 @Inject(method="extractRenderState",at=@At("HEAD"),cancellable=true)private void stack(GuiGraphicsExtractor g,CallbackInfo ci){if(!Wanderlust.on("boss-stack"))return;var counts=new java.util.LinkedHashMap<String,Integer>();var values=new java.util.HashMap<String,Float>();for(var boss:events.values()){String name=boss.getName().getString();counts.merge(name,1,Integer::sum);values.put(name,boss.getProgress());}int y=12,w=180,x=(g.guiWidth()-w)/2;for(var entry:counts.entrySet()){g.fill(x,y,x+w,y+6,0xAA22182B);g.fill(x,y,x+(int)(w*values.get(entry.getKey())),y+6,0xFFC58AFF);g.centeredText(Minecraft.getInstance().font,entry.getKey()+(entry.getValue()>1?" ×"+entry.getValue():""),g.guiWidth()/2,y-9,0xFFFFFFFF);y+=23;}ci.cancel();}
}
@Mixin(Minecraft.class)
abstract class MultitaskMixin {
 @ModifyExpressionValue(method={"startAttack","continueAttack"},at=@At(value="INVOKE",target="Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z"),require=0)private boolean use(boolean original){return original&&!Wanderlust.on("multitask");}
}
@Mixin(MultiPlayerGameMode.class)
abstract class BuildHeightMixin {
 @ModifyVariable(method="useItemOn",at=@At("HEAD"),argsOnly=true)private BlockHitResult height(BlockHitResult hit){var level=Minecraft.getInstance().level;if(Wanderlust.on("build-height")&&level!=null&&hit.getBlockPos().getY()>=level.getMaxY()-1&&hit.getDirection()==Direction.UP)return new BlockHitResult(hit.getLocation(),Direction.NORTH,hit.getBlockPos(),hit.isInside());return hit;}
}
