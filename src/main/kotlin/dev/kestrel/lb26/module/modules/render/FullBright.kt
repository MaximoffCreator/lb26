package dev.kestrel.lb26.module.modules.render

import dev.kestrel.lb26.module.Category
import dev.kestrel.lb26.module.Module
import dev.kestrel.lb26.value.FloatValue
import net.minecraft.client.Minecraft
import org.lwjgl.glfw.GLFW

object FullBright : Module("FullBright", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN) {

    private val gamma = add(FloatValue("Gamma", 10.0f, 1.0f, 100.0f))

    private var originalGamma: Double = 0.5
    private var saved = false

    override fun onUpdate() {
        val mc = Minecraft.getInstance()
        val options = mc.options ?: return

        if (enabled) {
            if (!saved) {
                originalGamma = readGamma(options)
                saved = true
            }
            writeGamma(options, gamma.value.toDouble())
        } else {
            if (saved) {
                writeGamma(options, originalGamma)
                saved = false
            }
        }
    }

    private fun readGamma(options: net.minecraft.client.Options): Double {
        return try {
            val field = options.javaClass.getDeclaredField("gamma")
            field.isAccessible = true
            val optionInstance = field.get(options)
            val valueField = optionInstance.javaClass.getDeclaredField("value")
            valueField.isAccessible = true
            (valueField.get(optionInstance) as Double)
        } catch (_: Throwable) {
            try { options.gamma().get() } catch (_: Throwable) { 0.5 }
        }
    }

    private fun writeGamma(options: net.minecraft.client.Options, value: Double) {
        try {
            val field = options.javaClass.getDeclaredField("gamma")
            field.isAccessible = true
            val optionInstance = field.get(options)
            val valueField = optionInstance.javaClass.getDeclaredField("value")
            valueField.isAccessible = true
            valueField.set(optionInstance, value)
        } catch (_: Throwable) {
            try { options.gamma().set(value) } catch (_: Throwable) { }
        }
    }
}
