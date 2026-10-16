package dev.kestrel.lb26.mixin;

import dev.kestrel.lb26.module.modules.render.XRay;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LightCoordsUtil.class)
public class XRayLightMixin {

    @Inject(
        method = "getLightCoords(Lnet/minecraft/util/LightCoordsUtil$BrightnessGetter;Lnet/minecraft/world/level/BlockAndLightGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)I",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void lb26$getLightCoords(
        LightCoordsUtil.BrightnessGetter brightnessGetter,
        BlockAndLightGetter lightGetter,
        BlockState state,
        BlockPos pos,
        CallbackInfoReturnable<Integer> cir
    ) {
        if (!XRay.isActive()) return;
        if (!XRay.isOreVisible(state.getBlock())) return;
        cir.setReturnValue(LightCoordsUtil.FULL_BRIGHT);
    }
}
