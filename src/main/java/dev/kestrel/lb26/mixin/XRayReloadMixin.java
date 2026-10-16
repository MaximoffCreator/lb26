package dev.kestrel.lb26.mixin;

import dev.kestrel.lb26.module.modules.render.XRay;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class XRayReloadMixin {

    @Inject(method = "compileSections", at = @At("HEAD"))
    private void lb26$beforeCompile(CameraRenderState cameraState, CallbackInfo ci) {
        try {
            if (!XRay.reloadRequested) return;
            XRay.reloadRequested = false;
            LevelRenderer self = (LevelRenderer) (Object) this;
            var sections = self.visibleSections();
            if (sections == null) return;
            for (int i = 0; i < sections.size(); i++) {
                var section = sections.get(i);
                if (section == null) continue;
                section.setSectionNode(i + 1L);
            }
        } catch (Throwable ignored) { }
    }
}
