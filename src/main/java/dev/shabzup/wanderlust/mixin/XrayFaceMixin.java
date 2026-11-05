package dev.shabzup.wanderlust.mixin;

import dev.shabzup.wanderlust.Wanderlust;
import dev.shabzup.wanderlust.Xray;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
abstract class XrayFaceMixin {
    @Inject(method="shouldRenderFace",at=@At("HEAD"),cancellable=true)
    private static void showOreFaces(BlockState state,BlockState adjacent,Direction direction,CallbackInfoReturnable<Boolean> cir) {
        if(Wanderlust.on("xray"))cir.setReturnValue(Xray.matches(state));
    }
}
