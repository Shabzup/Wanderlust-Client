package dev.shabzup.wanderlust.mixin;
import dev.shabzup.wanderlust.Proxies;
import io.netty.channel.ChannelPipeline;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Connection.class)
class ProxyConnectionMixin {
    @Inject(method="configurePacketHandler",at=@At("TAIL"))
    private void wanderlust$proxy(ChannelPipeline pipeline,CallbackInfo ci){Proxies.install(pipeline);}
}
