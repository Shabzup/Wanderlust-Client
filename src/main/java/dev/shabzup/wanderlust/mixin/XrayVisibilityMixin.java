package dev.shabzup.wanderlust.mixin;
import dev.shabzup.wanderlust.Wanderlust;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(VisGraph.class)
abstract class XrayVisibilityMixin {
    @Inject(method="setOpaque",at=@At("HEAD"),cancellable=true)
    private void showHiddenSections(BlockPos pos,CallbackInfo ci){if(Wanderlust.on("xray"))ci.cancel();}
}
