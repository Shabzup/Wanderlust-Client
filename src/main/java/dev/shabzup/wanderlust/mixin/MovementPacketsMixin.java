package dev.shabzup.wanderlust.mixin;

import dev.shabzup.wanderlust.Movement;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonPacketListenerImpl.class)
abstract class MovementPacketsMixin {
    @Inject(method = "send", at = @At("HEAD"), cancellable = true)
    private void wanderlust$filterMovementPackets(Packet<?> packet, CallbackInfo ci) {
        if (Movement.holdPacket((ClientCommonPacketListenerImpl) (Object) this, packet)) ci.cancel();
    }
}
