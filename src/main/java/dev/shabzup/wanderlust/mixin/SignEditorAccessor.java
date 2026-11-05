package dev.shabzup.wanderlust.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractSignEditScreen.class)
public interface SignEditorAccessor {
    @Accessor("messages") String[] wanderlust$messages();
}
