package dev.kestrel.lb26.module

import dev.kestrel.lb26.value.Value
import org.lwjgl.glfw.GLFW

abstract class Module(
    val name: String,
    val category: Category,
    var key: Int = GLFW.GLFW_KEY_UNKNOWN
) {
    var enabled = false
        set(v) {
            if (field == v) return
            field = v
            println("$name: ${if (v) "ON" else "OFF"}")
        }

    val values = mutableListOf<Value<*>>()

    protected fun <T : Value<*>> add(value: T): T {
        values += value
        return value
    }

    fun toggle() { enabled = !enabled }

    open fun onUpdate() {}
}