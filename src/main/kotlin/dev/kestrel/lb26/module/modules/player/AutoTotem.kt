package dev.kestrel.lb26.module.modules.player

import dev.kestrel.lb26.module.Category
import dev.kestrel.lb26.module.Module
import net.minecraft.client.Minecraft
import net.minecraft.world.InteractionHand
import net.minecraft.world.inventory.ContainerInput
import net.minecraft.world.item.Items
import org.lwjgl.glfw.GLFW

object AutoTotem : Module("AutoTotem", Category.PLAYER, GLFW.GLFW_KEY_UNKNOWN) {

    private var lastSwap = 0L

    override fun onUpdate() {
        if (!enabled) return
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return

        val now = System.currentTimeMillis()
        if (now - lastSwap < 100) return

        val offhand = player.getItemInHand(InteractionHand.OFF_HAND)
        if (offhand.item == Items.TOTEM_OF_UNDYING) return

        val inv = player.inventory
        for (slot in 0 until 36) {
            val stack = inv.getItem(slot)
            if (stack.item != Items.TOTEM_OF_UNDYING) continue

            val containerSlot = if (slot < 9) slot + 36 else slot

            mc.gameMode?.handleContainerInput(0, containerSlot, 40, ContainerInput.SWAP, player)

            lastSwap = now
            return
        }
    }
}