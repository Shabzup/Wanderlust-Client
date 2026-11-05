package dev.shabzup.wanderlust.mixin;

import dev.shabzup.wanderlust.Wanderlust;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LiquidBlock.class)
abstract class LiquidSurfaceMixin {
    @Inject(method = "getCollisionShape", at = @At("HEAD"), cancellable = true)
    private void wanderlust$surface(BlockState state, BlockGetter level, BlockPos pos,
                                   CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        if (!(context instanceof EntityCollisionContext entityContext) || !(entityContext.getEntity() instanceof LocalPlayer player)
            || !Wanderlust.on("jesus") || player.isShiftKeyDown() || !level.getFluidState(pos.above()).isEmpty()) return;
        var fluid = state.getFluidState().getType();
        if (fluid == Fluids.WATER && !Wanderlust.setting("jesus-water")
            || fluid == Fluids.LAVA && !Wanderlust.setting("jesus-lava")) return;
        double height = state.getFluidState().getHeight(level, pos);
        VoxelShape surface = Shapes.box(0, 0, 0, 1, height, 1);
        if (context.isAbove(surface, pos, false)) cir.setReturnValue(surface);
    }
}
