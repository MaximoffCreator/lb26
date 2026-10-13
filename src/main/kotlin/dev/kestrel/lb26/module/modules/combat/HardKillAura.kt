package dev.kestrel.lb26.module.modules.combat

import dev.kestrel.lb26.module.Category
import dev.kestrel.lb26.module.Module
import dev.kestrel.lb26.value.BoolValue
import dev.kestrel.lb26.value.FloatValue
import net.minecraft.client.Minecraft
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import org.lwjgl.glfw.GLFW
import kotlin.math.atan2
import kotlin.math.sqrt

object HardKillAura : Module("HardKillAura", Category.COMBAT, GLFW.GLFW_KEY_UNKNOWN) {
    val range = add(FloatValue("Range", 6.0f, 3.0f, 15.0f))
    val aps = add(FloatValue("APS", 12.0f, 1.0f, 20.0f))
    val playersOnly = add(BoolValue("PlayersOnly", false))
    val rotate = add(BoolValue("Rotate", true))
    val packetReach = add(BoolValue("PacketReach", true))
    val reachOffset = add(FloatValue("ReachOffset", 2.5f, 1.0f, 3.0f))

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

        val now = System.currentTimeMillis()
        if (now - lastAttack < (1000f / aps.value)) return

        val needsReach = packetReach.value && player.distanceTo(target) > 3.0f

        if (needsReach) {
            sendSpoofedAttack(mc, player, target)
        } else {
            mc.gameMode?.attack(player, target)
            player.swing(InteractionHand.MAIN_HAND)
        }

        lastAttack = now
    }

    private fun sendSpoofedAttack(mc: Minecraft, player: Player, target: LivingEntity) {
        val net = mc.connection ?: return
        val collision = false

        val dx = target.x - player.x
        val dy = target.y - player.y
        val dz = target.z - player.z
        val len = sqrt(dx * dx + dy * dy + dz * dz)
        if (len < 0.001) return
        val step = (len - reachOffset.value) / len

        val spoofX = player.x + dx * step
        val spoofY = player.y + dy * step
        val spoofZ = player.z + dz * step

        net.send(ServerboundMovePlayerPacket.Pos(spoofX, spoofY, spoofZ, player.onGround(), collision))

        mc.gameMode?.attack(player, target)
        player.swing(InteractionHand.MAIN_HAND)

        net.send(ServerboundMovePlayerPacket.Pos(player.x, player.y, player.z, player.onGround(), collision))
    }
}
