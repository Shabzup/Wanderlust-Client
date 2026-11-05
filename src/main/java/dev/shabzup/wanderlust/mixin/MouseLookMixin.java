package dev.shabzup.wanderlust.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import dev.shabzup.wanderlust.Movement;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MouseHandler.class)
abstract class MouseLookMixin {
    @WrapWithCondition(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"))
    private boolean wanderlust$turnCamera(LocalPlayer player, double horizontal, double vertical) {
        return !Movement.turn(horizontal, vertical);
    }
}
