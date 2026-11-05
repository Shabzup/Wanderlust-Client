package dev.shabzup.wanderlust.mixin;
import dev.shabzup.wanderlust.Wanderlust;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Minecraft.class)
abstract class NavigationShutdownMixin {
    @Inject(method="close",at=@At("TAIL"))
    private void stopOptionalWorkers(CallbackInfo ci){Wanderlust.shutdownNavigation();}
}
