package dev.kestrel.lb26.mixin;

import dev.kestrel.lb26.module.modules.render.XRay;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public class XRayMixin {

    @Inject(method = "getRenderShape", at = @At("HEAD"), cancellable = true)
    private void lb26$getRenderShape(CallbackInfoReturnable<RenderShape> cir) {
        BlockState state = (BlockState) (Object) this;
        if (XRay.isOreVisible(state.getBlock())) return;
        cir.setReturnValue(RenderShape.INVISIBLE);
    }
}
