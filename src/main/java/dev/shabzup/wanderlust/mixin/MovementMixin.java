package dev.shabzup.wanderlust.mixin;

import dev.shabzup.wanderlust.Movement;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Entity.class)
abstract class MovementMixin {
    @ModifyVariable(method = "move", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private Vec3 wanderlust$travel(Vec3 original, MoverType type, Vec3 requested) {
        return Movement.motion((Entity) (Object) this, type, original);
    }
}
