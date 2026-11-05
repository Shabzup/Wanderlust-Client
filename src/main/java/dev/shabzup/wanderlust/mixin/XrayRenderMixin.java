package dev.shabzup.wanderlust.mixin;

import dev.shabzup.wanderlust.Wanderlust;
import dev.shabzup.wanderlust.Xray;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelBlockRenderer.class)
abstract class XrayRenderMixin {
    @Inject(method="tesselateBlock",at=@At("HEAD"),cancellable=true)
    private void wanderlust$filterBlocks(BlockQuadOutput output,float x,float y,float z,BlockAndTintGetter level,
        BlockPos pos,BlockState state,BlockStateModel model,long seed,CallbackInfo ci) {
        if (Wanderlust.on("xray") && !Xray.matches(state)) ci.cancel();
    }
}
