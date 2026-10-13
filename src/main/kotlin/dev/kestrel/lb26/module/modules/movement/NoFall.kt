package dev.kestrel.lb26.module.modules.movement

import dev.kestrel.lb26.module.Category
import dev.kestrel.lb26.module.Module
import dev.kestrel.lb26.value.BoolValue
import net.minecraft.client.Minecraft
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
import org.lwjgl.glfw.GLFW

object NoFall : Module("NoFall", Category.MOVEMENT, GLFW.GLFW_KEY_UNKNOWN) {

    private val always = add(BoolValue("Always", true))

    private var lastSend = 0L
    private var flyWasOn = false

    override fun onUpdate() {
        if (!enabled) return
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return
        val net = mc.connection ?: return

        // Check if Fly is currently enabled so we don't spam when on the ground normally
        val onGround = player.onGround()

        if (onGround) {
            lastSend = 0
            return
        }

        if (!always.value && player.fallDistance < 1.0) return

        val now = System.currentTimeMillis()
        if (now - lastSend < 25) return

        net.send(
            ServerboundMovePlayerPacket.Pos(
                player.x,
                player.y,
                player.z,
                true,
                false
            )
        )

        lastSend = now
    }
}
