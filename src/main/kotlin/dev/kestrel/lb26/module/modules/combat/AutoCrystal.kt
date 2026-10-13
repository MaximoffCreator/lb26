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
import net.minecraft.world.entity.boss.enderdragon.EndCrystal
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3
import org.lwjgl.glfw.GLFW

object AutoCrystal : Module("AutoCrystal", Category.COMBAT, GLFW.GLFW_KEY_UNKNOWN) {

    private val targetRange = add(FloatValue("TargetRange", 8.0f, 3.0f, 12.0f))
    private val cycleDelay = add(IntValue("CycleDelay", 17, 5, 500))
    private val autoSwitch = add(BoolValue("AutoSwitch", true))
    private val playersOnly = add(BoolValue("PlayersOnly", true))
    private val placeObsidian = add(BoolValue("PlaceObsidian", true))

    private var lastAction = 0L
    private var stage = 0
    private var basePos: BlockPos? = null
    private var obsidianNeeded = false

    override fun onUpdate() {
        if (!enabled) return
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return
        val gameMode = mc.gameMode ?: return
        val level = mc.level ?: return

        val now = System.currentTimeMillis()
        if (now - lastAction < cycleDelay.value) return

        when (stage) {
            0 -> {
                // find target
                val box = player.boundingBox.inflate(targetRange.value.toDouble())
                val target = level.getEntitiesOfClass(LivingEntity::class.java, box)
                    .filter { it != player && it.isAlive }
                    .filter { !playersOnly.value || it is Player }
                    .sortedBy { e -> player.distanceTo(e) }
                    .firstOrNull() ?: return

                // block under target's feet
                val feet = BlockPos(target.x.toInt(), (target.y - 0.5).toInt(), target.z.toInt())
                basePos = feet

                val stateBelow = level.getBlockState(feet)
                obsidianNeeded = placeObsidian.value &&
                    stateBelow.block != Blocks.OBSIDIAN &&
                    stateBelow.block != Blocks.BEDROCK

                stage = if (obsidianNeeded) 1 else 2
                lastAction = now
            }
            1 -> {
                // place obsidian at basePos
                val pos = basePos ?: run { stage = 0; return }
                if (autoSwitch.value) selectItem(mc, Items.OBSIDIAN)
                if (player.getItemInHand(InteractionHand.MAIN_HAND).item != Items.OBSIDIAN) {
                    stage = 0
                    return
                }
                val above = pos.above()
                val bhr = BlockHitResult(
                    Vec3(above.x + 0.5, above.y + 1.0, above.z + 0.5),
                    Direction.UP,
                    above,
                    false
                )
                gameMode.useItemOn(player, InteractionHand.MAIN_HAND, bhr)
                stage = 2
                lastAction = now
            }
            2 -> {
                // place crystal on the obsidian
                val pos = basePos ?: run { stage = 0; return }
                if (autoSwitch.value) selectItem(mc, Items.END_CRYSTAL)
                if (player.getItemInHand(InteractionHand.MAIN_HAND).item != Items.END_CRYSTAL) {
                    stage = 0
                    return
                }
                val bhr = BlockHitResult(
                    Vec3(pos.x + 0.5, pos.y + 1.0, pos.z + 0.5),
                    Direction.UP,
                    pos,
                    false
                )
                gameMode.useItemOn(player, InteractionHand.MAIN_HAND, bhr)
                stage = 3
                lastAction = now
            }
            3 -> {
                // detonate
                val crystal = level.getEntitiesOfClass(EndCrystal::class.java, player.boundingBox.inflate(8.0))
                    .firstOrNull() ?: run { stage = 0; return }
                gameMode.attack(player, crystal)
                player.swing(InteractionHand.MAIN_HAND)
                stage = 0
                basePos = null
                lastAction = System.currentTimeMillis()
            }
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
}
