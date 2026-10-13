package dev.kestrel.lb26.mixin;

import dev.kestrel.lb26.module.modules.render.XRay;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public class XRayFaceMixin {

    @Inject(method = "shouldRenderFace", at = @At("HEAD"), cancellable = true)
    private static void lb26$shouldRenderFace(BlockState state, BlockState neighbor, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (!XRay.INSTANCE.getEnabled()) return;
        if (XRay.INSTANCE.isVisible(state.getBlock())) {
            cir.setReturnValue(true);
        }
    }
}
