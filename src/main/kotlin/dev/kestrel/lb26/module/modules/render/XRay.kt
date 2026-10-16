package dev.kestrel.lb26.module.modules.render

import dev.kestrel.lb26.module.Category
import dev.kestrel.lb26.module.Module
import dev.kestrel.lb26.value.BoolValue
import net.minecraft.client.Minecraft
import net.minecraft.client.Options
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.level.block.Block
import org.lwjgl.glfw.GLFW

object XRay : Module("XRay", Category.RENDER, GLFW.GLFW_KEY_X) {

    private val showChests = add(BoolValue("ShowChests", true))
    private val showSpawners = add(BoolValue("ShowSpawners", true))
    private val showLava = add(BoolValue("ShowLava", true))
    private val brightOres = add(BoolValue("BrightOres", true))

    private val oreNames: Set<String> = setOf(
        "diamond_ore", "deepslate_diamond_ore",
        "iron_ore", "deepslate_iron_ore",
        "gold_ore", "deepslate_gold_ore",
        "emerald_ore", "deepslate_emerald_ore",
        "redstone_ore", "deepslate_redstone_ore",
        "lapis_ore", "deepslate_lapis_ore",
        "copper_ore", "deepslate_copper_ore",
        "coal_ore", "deepslate_coal_ore",
        "nether_gold_ore", "nether_quartz_ore",
        "ancient_debris"
    )

    private val chestNames: Set<String> = setOf(
        "chest", "trapped_chest", "ender_chest",
        "barrel", "shulker_box",
        "hopper", "furnace", "blast_furnace", "smoker",
        "dispenser", "dropper"
    )

    private var originalGamma: Double = 0.5
    private var gammaSaved = false
    private var wasEnabled = false
    @JvmField var reloadRequested: Boolean = false

    @JvmStatic

    fun isActive(): Boolean = enabled

    @JvmStatic
    fun isOreVisible(block: Block): Boolean = isVisible(block)

    fun isVisible(block: Block): Boolean {
        if (!enabled) return true
        val name = BuiltInRegistries.BLOCK.getKey(block).path
        if (name in oreNames) return true
        if (showChests.value && name in chestNames) return true
        if (showSpawners.value && name == "spawner") return true
        if (showLava.value && (name == "lava" || name == "water")) return true
        if (name == "air" || name == "cave_air" || name == "void_air") return true
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

        // on toggle: request a reload that will fire inside the compileSections hook
        if (wasEnabled != enabled) {
            wasEnabled = enabled
            reloadRequested = true
        }
    }
}
