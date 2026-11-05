package dev.shabzup.wanderlust.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.shabzup.wanderlust.Wanderlust;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
abstract class StepMixin {
    @ModifyReturnValue(method = "maxUpStep", at = @At("RETURN"))
    private float wanderlust$step(float original) {
        if ((Object) this instanceof LocalPlayer && Wanderlust.on("step"))
            return Math.max(original, (float) Wanderlust.number("step.height"));
        return original;
    }
}
