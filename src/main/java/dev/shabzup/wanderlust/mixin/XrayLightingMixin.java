package dev.shabzup.wanderlust.mixin;
import dev.shabzup.wanderlust.*;
import net.minecraft.client.renderer.block.*;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import com.mojang.blaze3d.vertex.QuadInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(BlockModelLighter.class)
abstract class XrayLightingMixin {
    private static void brighten(BlockState state,QuadInstance instance){
        if(Wanderlust.on("xray")&&Xray.matches(state)){instance.setLightCoords(net.minecraft.util.LightCoordsUtil.smoothPack(255,255));instance.setColor(0xFFFFFFFF);}
    }
    @Inject(method="prepareQuadAmbientOcclusion",at=@At("RETURN"))
    private void ambient(BlockAndTintGetter level,BlockState state,BlockPos pos,BakedQuad quad,QuadInstance instance,CallbackInfo ci){brighten(state,instance);}
    @Inject(method="prepareQuadFlat",at=@At("RETURN"))
    private void flat(BlockAndTintGetter level,BlockState state,BlockPos pos,int light,BakedQuad quad,QuadInstance instance,CallbackInfo ci){brighten(state,instance);}
}
