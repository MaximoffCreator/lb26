package dev.kestrel.lb26.config

import dev.kestrel.lb26.module.Module
import java.io.File

object Config {

    private val file = File("config/lb26-keys.txt")

    fun save(modules: List<Module>) {
        try {
            file.parentFile?.mkdirs()
            file.writeText(modules.joinToString("\n") { "${it.name}=${it.key}" })
        } catch (_: Throwable) { }
    }

    fun load(modules: List<Module>) {
        try {
            if (!file.exists()) return
            for (line in file.readLines()) {
                val parts = line.split("=")
                if (parts.size != 2) continue
                val module = modules.firstOrNull { it.name == parts[0] } ?: continue
                val key = parts[1].toIntOrNull() ?: continue
                if (key != 0) module.key = key
            }
        } catch (_: Throwable) { }
    }
}
