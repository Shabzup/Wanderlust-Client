package dev.shabzup.wanderlust.mixin;

import dev.shabzup.wanderlust.MenuAccess;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
class GlobalMenuKeyMixin {
    @Inject(method="keyPress",at=@At("HEAD"),cancellable=true)
    private void wanderlust$key(long window,int action,KeyEvent event,CallbackInfo ci){dev.shabzup.wanderlust.MenuMovement.observeKey(window,action,event.scancode());if(MenuAccess.key(window,action,event)||dev.shabzup.wanderlust.MenuMovement.consumesArrow(window,event.key()))ci.cancel();}
}

@Mixin(net.minecraft.client.player.KeyboardInput.class)
class MenuMovementInputMixin {
    @Inject(method="tick",at=@At("HEAD"))
    private void wanderlust$movementInput(CallbackInfo ci){dev.shabzup.wanderlust.MenuMovement.readBindings();}
}

@Mixin(JoinMultiplayerScreen.class)
class MultiplayerHeaderMixin {
    @Shadow @Final private HeaderAndFooterLayout layout;
    @Inject(method="repositionElements",at=@At("HEAD"))
    private void wanderlust$header(CallbackInfo ci){layout.setHeaderHeight(66);}
    @Inject(method="repositionElements",at=@At("TAIL"))
    private void wanderlust$title(CallbackInfo ci){layout.visitChildren(child->{if(child instanceof net.minecraft.client.gui.components.StringWidget text)text.setY(12);});}
}
