package dev.shabzup.wanderlust.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.shabzup.wanderlust.Movement;
import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
abstract class CameraMixin {
    @Shadow private boolean detached;
    @Shadow protected abstract void setPosition(Vec3 position);
    @Shadow protected abstract void setRotation(float yaw, float pitch);

    @ModifyExpressionValue(method = "alignWithEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getViewYRot(F)F"))
    private float wanderlust$viewYaw(float original) {
        return Movement.looking() ? Movement.yaw() : original;
    }

    @ModifyExpressionValue(method = "alignWithEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getViewXRot(F)F"))
    private float wanderlust$viewPitch(float original) {
        return Movement.looking() ? Movement.pitch() : original;
    }

    @Inject(method = "alignWithEntity", at = @At("TAIL"))
    private void wanderlust$detach(float partialTick, CallbackInfo ci) {
        if (Movement.scouting()) {
            detached = true;
            setRotation(Movement.yaw(), Movement.pitch());
            setPosition(Movement.position(partialTick));
        }
    }
}
