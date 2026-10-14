package dev.kestrel.lb26.module.modules.world

import dev.kestrel.lb26.module.Category
import dev.kestrel.lb26.module.Module
import dev.kestrel.lb26.value.BoolValue
import dev.kestrel.lb26.value.IntValue
import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.BlockItem
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3
import org.lwjgl.glfw.GLFW

object Scaffold : Module("Scaffold", Category.WORLD, GLFW.GLFW_KEY_UNKNOWN) {

    private val delay = add(IntValue("Delay", 0, 0, 10))
    private val autoSwitch = add(BoolValue("AutoSwitch", true))
    private val swing = add(BoolValue("Swing", true))

    private var lastPlace = 0L

    override fun onUpdate() {
        if (!enabled) return
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return
        val level = mc.level ?: return
        val gameMode = mc.gameMode ?: return

        val now = System.currentTimeMillis()
        if (now - lastPlace < delay.value * 50L) return

        val under = BlockPos.containing(player.x, player.y - 1.0, player.z)
        if (!level.getBlockState(under).isAir) return

        if (autoSwitch.value && player.getItemInHand(InteractionHand.MAIN_HAND).item !is BlockItem) {
            var found = false
            for (slot in 0..8) {
                val stack = player.inventory.getItem(slot)
                if (stack.item is BlockItem) {
                    player.inventory.selectedSlot = slot
                    found = true
                    break
                }
            }
            if (!found) return
        }
        if (player.getItemInHand(InteractionHand.MAIN_HAND).item !is BlockItem) return

        val hit = BlockHitResult(
            Vec3(under.x + 0.5, under.y + 1.0, under.z + 0.5),
            Direction.UP,
            under,
            false
        )
        gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hit)
        if (swing.value) player.swing(InteractionHand.MAIN_HAND)
        lastPlace = now
    }
}
