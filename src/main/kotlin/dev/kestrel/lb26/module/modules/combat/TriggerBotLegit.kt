package dev.kestrel.lb26.module.modules.combat

import dev.kestrel.lb26.module.Category
import dev.kestrel.lb26.module.Module
import dev.kestrel.lb26.value.BoolValue
import dev.kestrel.lb26.value.FloatValue
import net.minecraft.client.Minecraft
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.EntityHitResult
import org.lwjgl.glfw.GLFW

object TriggerBotLegit : Module("TriggerBotLegit", Category.COMBAT, GLFW.GLFW_KEY_UNKNOWN) {

    private val range = add(FloatValue("Range", 3.0f, 1.0f, 4.5f))
    private val requireJump = add(BoolValue("RequireJump", true))
    private val cooldownThreshold = add(FloatValue("CooldownThreshold", 1.0f, 0.5f, 1.0f))
    private val minFallDistance = add(FloatValue("MinFall", 0.0f, 0.0f, 1.0f))
    private val playersOnly = add(BoolValue("PlayersOnly", true))

    private var jumping = false

    override fun onUpdate() {
        if (!enabled) return
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return
        val gameMode = mc.gameMode ?: return

        if (jumping) {
            mc.options.keyJump.isDown = false
            jumping = false
        }

        val hit = mc.hitResult as? EntityHitResult ?: return
        val target = hit.entity as? LivingEntity ?: return
        if (target == player) return
        if (!target.isAlive) return
        if (playersOnly.value && target !is Player) return
        if (player.distanceTo(target) > range.value) return

        if (player.getAttackStrengthScale(0f) < cooldownThreshold.value) return

        if (requireJump.value) {
            if (player.onGround()) {
                mc.options.keyJump.isDown = true
                jumping = true
                return
            }
            if (player.fallDistance <= minFallDistance.value) return
        }

        gameMode.attack(player, target)
        player.swing(InteractionHand.MAIN_HAND)
    }
}
