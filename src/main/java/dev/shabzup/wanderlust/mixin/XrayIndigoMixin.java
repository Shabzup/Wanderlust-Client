package dev.shabzup.wanderlust.mixin;
import dev.shabzup.wanderlust.*;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(targets="net.fabricmc.fabric.impl.client.indigo.renderer.render.AltModelBlockRendererImpl",remap=false)
abstract class XrayIndigoMixin {
    @Shadow private BlockState blockState;
    @Inject(method="transform",at=@At("HEAD"),cancellable=true,remap=false)
    private void hideUnselected(MutableQuadView quad,CallbackInfoReturnable<Boolean> cir){
        if(Wanderlust.on("xray")&&!Xray.matches(blockState))cir.setReturnValue(false);
    }
    @Inject(method="transform",at=@At("RETURN"),remap=false)
    private void brightenOre(MutableQuadView quad,CallbackInfoReturnable<Boolean> cir){
        if(cir.getReturnValue()&&Wanderlust.on("xray")&&Xray.matches(blockState)){
            for(int i=0;i<4;i++){quad.color(i,0xFFFFFFFF);quad.lightmap(i,net.minecraft.util.LightCoordsUtil.FULL_BRIGHT);}
            quad.emissive(true);
        }
    }
}
