package dev.shabzup.wanderlust.mixin;

import dev.shabzup.wanderlust.Movement;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
abstract class HighJumpMixin {
    @Inject(method="getJumpPower()F", at=@At("RETURN"), cancellable=true)
    private void jump(CallbackInfoReturnable<Float> result) {
        result.setReturnValue(Movement.jumpPower((LivingEntity)(Object)this, result.getReturnValue()));
    }
}

@Mixin(Player.class)
abstract class SafeWalkMixin {
    @Inject(method="maybeBackOffFromEdge", at=@At("RETURN"), cancellable=true)
    private void edge(Vec3 travel, MoverType type, CallbackInfoReturnable<Vec3> result) {
        if (type == MoverType.SELF || type == MoverType.PLAYER)
            result.setReturnValue(Movement.safeWalk((Player)(Object)this, result.getReturnValue()));
    }
}
