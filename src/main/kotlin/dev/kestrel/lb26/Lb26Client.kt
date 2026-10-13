package dev.kestrel.lb26

import dev.kestrel.lb26.config.Config
import dev.kestrel.lb26.module.Module
import dev.kestrel.lb26.module.modules.combat.AutoAnchor
import dev.kestrel.lb26.module.modules.combat.AutoCrystal
import dev.kestrel.lb26.module.modules.combat.HardKillAura
import dev.kestrel.lb26.module.modules.combat.KillAura
import dev.kestrel.lb26.module.modules.movement.Fly
import dev.kestrel.lb26.module.modules.movement.NoFall
import dev.kestrel.lb26.module.modules.player.AutoTotem
import dev.kestrel.lb26.module.modules.render.FullBright
import dev.kestrel.lb26.module.modules.render.XRay
import dev.kestrel.lb26.ui.ClickGui
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import org.lwjgl.glfw.GLFW

object Lb26Client : ClientModInitializer {

    val allModules: List<Module> = listOf(
        KillAura, HardKillAura, AutoAnchor, AutoCrystal,
        AutoTotem, Fly, NoFall, XRay, FullBright
    )

    private var lastSavedKeys = emptyMap<String, Int>()

    override fun onInitializeClient() {
        Config.load(allModules)

        val wasDown = mutableMapOf<Int, Boolean>()
        var menuWasDown = false

        ClientTickEvents.END_CLIENT_TICK.register { mc ->
            if (System.currentTimeMillis() % 5000 < 50) println("LB26 TICKING")
            for (m in allModules) m.onUpdate()

            val currentKeys = allModules.associate { it.name to it.key }
            if (currentKeys != lastSavedKeys) {
                if (lastSavedKeys.isNotEmpty()) Config.save(allModules)
                lastSavedKeys = currentKeys
            }

            if (mc.gui.screen() != null) {
                for (m in allModules) wasDown[m.key] = true
                menuWasDown = true
                return@register
            }

            val window = GLFW.glfwGetCurrentContext()
            if (window != 0L) {
                val menuDown = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_N) == GLFW.GLFW_PRESS
                if (menuDown && !menuWasDown) { println("LB26 MENU TRIGGERED"); mc.gui.setScreen(ClickGui()) }
                menuWasDown = menuDown

                for (module in allModules) {
                    if (module.key == GLFW.GLFW_KEY_UNKNOWN) continue
                    val down = GLFW.glfwGetKey(window, module.key) == GLFW.GLFW_PRESS
                    val was = wasDown[module.key] ?: false
                    if (down && !was) module.enabled = !module.enabled
                    wasDown[module.key] = down
                }
            }
        }
    }
}
