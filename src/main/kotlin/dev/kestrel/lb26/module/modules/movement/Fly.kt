package dev.kestrel.lb26.module.modules.movement

import dev.kestrel.lb26.module.Category
import dev.kestrel.lb26.module.Module
import dev.kestrel.lb26.value.BoolValue
import dev.kestrel.lb26.value.FloatValue
import net.minecraft.client.Minecraft
import org.lwjgl.glfw.GLFW

object Fly : Module("Fly", Category.MOVEMENT, GLFW.GLFW_KEY_UNKNOWN) {

    private val speed = add(FloatValue("Speed", 1.0f, 0.1f, 5.0f))
    private val verticalSpeed = add(FloatValue("Vertical", 0.5f, 0.1f, 2.0f))
    private val antiKick = add(BoolValue("AntiKick", true))

    private var tick = 0
    private var wasActive = false

    override fun onUpdate() {
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return

        // If the module was just turned off, reset flight state
        if (!enabled) {
            if (wasActive) {
                player.abilities.flying = false
                player.abilities.mayfly = false
                player.abilities.flyingSpeed = 0.05f
                wasActive = false
            }
            return
        }

        wasActive = true

        player.abilities.flying = true
        player.abilities.mayfly = true
        player.abilities.flyingSpeed = speed.value * 0.05f

        tick++

        var vy = 0.0
        if (mc.options.keyJump.isDown) vy = verticalSpeed.value.toDouble()
        if (mc.options.keyShift.isDown) vy = -verticalSpeed.value.toDouble()

        if (vy == 0.0 && antiKick.value && tick % 40 == 0) {
            vy = -0.04
        }

        val yaw = Math.toRadians(player.yRot.toDouble())
        val forward = player.input.moveVector.y.toDouble()
        val strafe = player.input.moveVector.x.toDouble()

        val vx = (-Math.sin(yaw) * forward + Math.cos(yaw) * strafe) * speed.value
        val vz = (Math.cos(yaw) * forward + Math.sin(yaw) * strafe) * speed.value

        player.setDeltaMovement(vx, vy, vz)
    }
}
