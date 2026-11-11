package qa.mixin;

import qa.MenuMovementProbe;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Test-JAR-only native input substitute: movement still runs through the real vanilla input tick. */
@Mixin(targets="dev.shabzup.wanderlust.MenuMovement",remap=false)
abstract class MenuInputMixinProbe {
    @Inject(method="physical(Lnet/minecraft/client/Minecraft;Lcom/mojang/blaze3d/platform/InputConstants$Key;)Z",at=@At("HEAD"),cancellable=true,remap=false)
    private static void sampled(Minecraft mc,InputConstants.Key key,CallbackInfoReturnable<Boolean> result){
        result.setReturnValue(MenuMovementProbe.input.test(key));
    }
}
