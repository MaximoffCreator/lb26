package dev.kestrel.lb26.mixin;

import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SectionRenderDispatcher.class)
public interface SectionDispatcherInvoker {

    @Invoker("schedule")
    void lb26$schedule(Object task);
}
