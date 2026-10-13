package dev.kestrel.lb26.mixin;

import dev.kestrel.lb26.module.modules.render.XRay;
import net.minecraft.client.renderer.Lightmap;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Lightmap.class)
public class XRayBrightnessMixin {

    @Inject(method = "getBrightness", at = @At("HEAD"), cancellable = true)
    private static void lb26$getBrightness(DimensionType dimensionType, int light, CallbackInfoReturnable<Float> cir) {
        if (XRay.INSTANCE.getEnabled()) {
            cir.setReturnValue(1.0f);
        }
    }
}
