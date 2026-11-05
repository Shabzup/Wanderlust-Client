package dev.shabzup.wanderlust.mixin;

import dev.shabzup.wanderlust.Movement;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
abstract class ScoutInteractionMixin {
    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void wanderlust$stopAttack(CallbackInfoReturnable<Boolean> cir) {
        if (Movement.scouting()) cir.setReturnValue(false);
    }

    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    private void wanderlust$stopMining(boolean down, CallbackInfo ci) {
        if (Movement.scouting()) ci.cancel();
    }

    @Inject(method = {"startUseItem", "pickBlockOrEntity"}, at = @At("HEAD"), cancellable = true)
    private void wanderlust$stopUse(CallbackInfo ci) {
        if (Movement.scouting()) ci.cancel();
    }
}
