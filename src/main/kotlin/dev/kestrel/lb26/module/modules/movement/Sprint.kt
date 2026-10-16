package dev.kestrel.lb26.module.modules.movement

import dev.kestrel.lb26.module.Category
import dev.kestrel.lb26.module.Module
import dev.kestrel.lb26.value.BoolValue
import net.minecraft.client.Minecraft
import org.lwjgl.glfw.GLFW

object Sprint : Module("Sprint", Category.MOVEMENT, GLFW.GLFW_KEY_UNKNOWN) {

    private val onlyWhileForward = add(BoolValue("OnlyWhileForward", true))
    private val stopOnItemUse = add(BoolValue("StopOnItemUse", true))

    override fun onUpdate() {
        if (!enabled) return
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return

        if (onlyWhileForward.value && !mc.options.keyUp.isDown) return
        if (stopOnItemUse.value && player.isUsingItem) return
        if (player.isShiftKeyDown) return
        if (!player.canSprint()) return

        player.isSprinting = true
    }
}
