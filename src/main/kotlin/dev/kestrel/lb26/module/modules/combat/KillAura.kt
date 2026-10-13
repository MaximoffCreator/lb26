package dev.kestrel.lb26.module.modules.combat

import dev.kestrel.lb26.module.Category
import dev.kestrel.lb26.module.Module
import dev.kestrel.lb26.value.BoolValue
import dev.kestrel.lb26.value.FloatValue
import net.minecraft.client.Minecraft
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import org.lwjgl.glfw.GLFW
import kotlin.math.atan2
import kotlin.math.sqrt

object KillAura : Module("KillAura", Category.COMBAT, GLFW.GLFW_KEY_UNKNOWN) {
    val range = add(FloatValue("Range", 4.5f, 1.0f, 6.0f))
    val aps = add(FloatValue("APS", 12.0f, 1.0f, 20.0f))
    val playersOnly = add(BoolValue("PlayersOnly", false))
    val rotate = add(BoolValue("Rotate", true))

    private var lastAttack = 0L

    override fun onUpdate() {
        if (!enabled) return
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return
        val level = mc.level ?: return

        val box = player.boundingBox.inflate(range.value.toDouble())
        val target = level.getEntitiesOfClass(LivingEntity::class.java, box)
            .filter { it != player && it.isAlive }
            .filter { !playersOnly.value || it is Player }
            .sortedBy { e -> player.distanceTo(e) }
            .firstOrNull() ?: return

        if (rotate.value) {
            val dx = target.x - player.x
            val dy = (target.y + target.eyeHeight) - (player.y + player.eyeHeight)
            val dz = target.z - player.z
            val dist = sqrt(dx * dx + dz * dz)
            player.yRot = (Math.toDegrees(atan2(dz, dx)) - 90.0).toFloat()
            player.xRot = (-Math.toDegrees(atan2(dy, dist))).toFloat()
        }

        // Respect the vanilla attack cooldown so the server accepts every hit
        if (player.getAttackStrengthScale(0f) < 0.7f) return

        val now = System.currentTimeMillis()
        if (now - lastAttack < (1000f / aps.value)) return

        mc.gameMode?.attack(player, target)
        player.swing(InteractionHand.MAIN_HAND)
        lastAttack = now
    }
}
