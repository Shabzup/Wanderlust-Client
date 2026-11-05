package dev.shabzup.wanderlust.mixin;
import dev.shabzup.wanderlust.*;
import net.minecraft.client.renderer.block.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(FluidRenderer.class)
abstract class XrayFluidMixin {
    @Inject(method="tesselate",at=@At("HEAD"),cancellable=true)
    private void hideUnselectedFluids(BlockAndTintGetter level,BlockPos pos,FluidRenderer.Output output,BlockState state,FluidState fluid,CallbackInfo ci){
        if(Wanderlust.on("xray")&&!Xray.matches(state))ci.cancel();
    }
}
