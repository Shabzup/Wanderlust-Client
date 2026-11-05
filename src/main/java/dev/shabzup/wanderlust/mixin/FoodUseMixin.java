package dev.shabzup.wanderlust.mixin;

import dev.shabzup.wanderlust.PlayerTools;
import dev.shabzup.wanderlust.Combat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
abstract class FoodUseMixin {
    @Inject(method = "releaseUsingItem", at = @At("HEAD"), cancellable = true)
    private void wanderlust$finishEating(Player player, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (PlayerTools.holdingFood(mc) || Combat.holdingBow(mc)) ci.cancel();
    }
}
