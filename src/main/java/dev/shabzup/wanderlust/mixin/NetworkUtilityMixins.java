package dev.shabzup.wanderlust.mixin;
import dev.shabzup.wanderlust.*;
import net.minecraft.client.multiplayer.*;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.sounds.*;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.*;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(ClientCommonPacketListenerImpl.class)
abstract class OutgoingUtilityMixin {
 @Inject(method="send",at=@At("HEAD"),cancellable=true)private void send(Packet<?> packet,CallbackInfo ci){if(ExtraMisc.outgoing(packet))ci.cancel();}
 @ModifyVariable(method="send",at=@At("HEAD"),argsOnly=true)private Packet<?> brand(Packet<?> packet){if(Wanderlust.on("server-spoof")&&packet instanceof ServerboundCustomPayloadPacket custom&&custom.payload() instanceof BrandPayload)return new ServerboundCustomPayloadPacket(new BrandPayload(Wanderlust.text("server-spoof.brand")));return packet;}
}
@Mixin(Connection.class)
abstract class PacketKickMixin {
 @Inject(method="exceptionCaught",at=@At("HEAD"),cancellable=true)private void exception(io.netty.channel.ChannelHandlerContext context,Throwable error,CallbackInfo ci){if(Wanderlust.on("anti-packet-kick")&&error instanceof io.netty.handler.codec.DecoderException)ci.cancel();}
}
@Mixin(ChatComponent.class)
abstract class ChatDecorateMixin {
 @ModifyVariable(method={"addClientSystemMessage","addServerSystemMessage","addPlayerMessage"},at=@At("HEAD"),argsOnly=true,ordinal=0)private Component chat(Component original){return ExtraMisc.chat(original);}
}
@Mixin(ClientPacketListener.class)
abstract class ChatPrefixMixin {
 @ModifyVariable(method="sendChat",at=@At("HEAD"),argsOnly=true)private String prefix(String text){return ExtraMisc.outgoingChat(text);}
}
@Mixin(SoundManager.class)
abstract class SoundBlockMixin {
 @Inject(method="play",at=@At("HEAD"),cancellable=true)private void sound(SoundInstance sound,CallbackInfoReturnable<SoundEngine.PlayResult> cir){if(Wanderlust.on("sound-blocker")&&java.util.Arrays.asList(Wanderlust.text("sound-blocker.ids").split(",")).contains(sound.getIdentifier().toString()))cir.setReturnValue(SoundEngine.PlayResult.NOT_STARTED);}
}
