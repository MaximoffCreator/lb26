package dev.kestrel.lb26.module.modules.render

import dev.kestrel.lb26.module.Category
import dev.kestrel.lb26.module.Module
import dev.kestrel.lb26.value.BoolValue
import net.minecraft.client.Minecraft
import net.minecraft.client.Options
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import org.lwjgl.glfw.GLFW

object XRay : Module("XRay", Category.RENDER, GLFW.GLFW_KEY_X) {

    private val showChests = add(BoolValue("ShowChests", true))
    private val showSpawners = add(BoolValue("ShowSpawners", true))
    private val showLava = add(BoolValue("ShowLava", true))
    private val brightOres = add(BoolValue("BrightOres", true))

    private val ores: Set<Block> = setOf(
        Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE,
        Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE,
        Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE,
        Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE,
        Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE,
        Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE,
        Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE,
        Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE,
        Blocks.NETHER_GOLD_ORE, Blocks.NETHER_QUARTZ_ORE,
        Blocks.ANCIENT_DEBRIS
    )

    private val chests: Set<Block> = setOf(
        Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.ENDER_CHEST,
        Blocks.BARREL, Blocks.SHULKER_BOX,
        Blocks.HOPPER, Blocks.FURNACE, Blocks.BLAST_FURNACE, Blocks.SMOKER,
        Blocks.DISPENSER, Blocks.DROPPER
    )

    private var originalGamma: Double = 0.5
    private var gammaSaved = false

    fun isVisible(block: Block): Boolean {
        if (!enabled) return true
        if (block in ores) return true
        if (showChests.value && block in chests) return true
        if (showSpawners.value && block == Blocks.SPAWNER) return true
        if (showLava.value && (block == Blocks.LAVA || block == Blocks.WATER)) return true
        if (block == Blocks.AIR) return true
        return false
    }

    private fun readGamma(options: Options): Double {
        return try {
            val field = options.javaClass.getDeclaredField("gamma")
            field.isAccessible = true
            val opt = field.get(options)
            val valueField = opt.javaClass.getDeclaredField("value")
            valueField.isAccessible = true
            (valueField.get(opt) as Double)
        } catch (_: Throwable) {
            try { options.gamma().get() } catch (_: Throwable) { 0.5 }
        }
    }

    private fun writeGamma(options: Options, value: Double) {
        try {
            val field = options.javaClass.getDeclaredField("gamma")
            field.isAccessible = true
            val opt = field.get(options)
            val valueField = opt.javaClass.getDeclaredField("value")
            valueField.isAccessible = true
            valueField.set(opt, value)
        } catch (_: Throwable) {
            try { options.gamma().set(value) } catch (_: Throwable) { }
        }
    }

    override fun onUpdate() {
        val mc = Minecraft.getInstance()
        val options = mc.options ?: return

        if (enabled && brightOres.value) {
            if (!gammaSaved) {
                originalGamma = readGamma(options)
                gammaSaved = true
            }
            writeGamma(options, 10.0)
        } else {
            if (gammaSaved) {
                writeGamma(options, originalGamma)
                gammaSaved = false
            }
        }
    }
}
