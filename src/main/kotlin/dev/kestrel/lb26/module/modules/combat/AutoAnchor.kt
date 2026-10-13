package dev.kestrel.lb26.module.modules.combat

import dev.kestrel.lb26.module.Category
import dev.kestrel.lb26.module.Module
import dev.kestrel.lb26.value.BoolValue
import dev.kestrel.lb26.value.FloatValue
import dev.kestrel.lb26.value.IntValue
import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Items
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3
import org.lwjgl.glfw.GLFW

object AutoAnchor : Module("AutoAnchor", Category.COMBAT, GLFW.GLFW_KEY_UNKNOWN) {

    private val targetRange = add(FloatValue("TargetRange", 8.0f, 3.0f, 12.0f))
    private val cycleDelay = add(IntValue("CycleDelay", 17, 5, 500))
    private val autoSwitch = add(BoolValue("AutoSwitch", true))
    private val playersOnly = add(BoolValue("PlayersOnly", true))

    private var lastAction = 0L
    private var stage = 0
    private var anchorPos: BlockPos? = null

    override fun onUpdate() {
        if (!enabled) return
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return
        val gameMode = mc.gameMode ?: return
        val level = mc.level ?: return

        val now = System.currentTimeMillis()
        if (now - lastAction < cycleDelay.value) return

        if (stage == 0) {
            val box = player.boundingBox.inflate(targetRange.value.toDouble())
            val target = level.getEntitiesOfClass(LivingEntity::class.java, box)
                .filter { it != player && it.isAlive }
                .filter { !playersOnly.value || it is Player }
                .sortedBy { e -> player.distanceTo(e) }
                .firstOrNull() ?: return

            val feet = BlockPos(target.x.toInt(), (target.y - 1.0).toInt(), target.z.toInt())
            anchorPos = feet

            if (autoSwitch.value) selectItem(mc, Items.RESPAWN_ANCHOR)
            if (player.getItemInHand(InteractionHand.MAIN_HAND).item != Items.RESPAWN_ANCHOR) return

            val bhr = BlockHitResult(Vec3(feet.x + 0.5, feet.y + 1.0, feet.z + 0.5), Direction.UP, feet, false)
            gameMode.useItemOn(player, InteractionHand.MAIN_HAND, bhr)
            stage = 1
            lastAction = now
        } else if (stage == 1) {
            val pos = anchorPos ?: run { stage = 0; return }
            if (autoSwitch.value) selectItem(mc, Items.GLOWSTONE)
            if (player.getItemInHand(InteractionHand.MAIN_HAND).item != Items.GLOWSTONE) return

            val bhr = BlockHitResult(Vec3(pos.x + 0.5, pos.y + 1.0, pos.z + 0.5), Direction.UP, pos, false)
            gameMode.useItemOn(player, InteractionHand.MAIN_HAND, bhr)
            stage = 2
            lastAction = now
        } else {
            val pos = anchorPos ?: run { stage = 0; return }
            if (autoSwitch.value) selectEmpty(mc)

            val bhr = BlockHitResult(Vec3(pos.x + 0.5, pos.y + 1.0, pos.z + 0.5), Direction.UP, pos, false)
            gameMode.useItemOn(player, InteractionHand.MAIN_HAND, bhr)
            stage = 0
            anchorPos = null
            lastAction = now
        }
    }

    private fun selectItem(mc: Minecraft, item: net.minecraft.world.item.Item) {
        val player = mc.player ?: return
        for (slot in 0..8) {
            if (player.inventory.getItem(slot).item == item) {
                player.inventory.selectedSlot = slot
                return
            }
        }
    }

    private fun selectEmpty(mc: Minecraft) {
        val player = mc.player ?: return
        for (slot in 0..8) {
            if (player.inventory.getItem(slot).isEmpty) {
                player.inventory.selectedSlot = slot
                return
            }
        }
    }
}
