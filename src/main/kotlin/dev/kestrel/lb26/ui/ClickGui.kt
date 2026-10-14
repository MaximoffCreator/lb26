package dev.kestrel.lb26.ui

import dev.kestrel.lb26.module.Category
import dev.kestrel.lb26.module.Module
import dev.kestrel.lb26.module.modules.combat.AutoAnchor
import dev.kestrel.lb26.module.modules.combat.AutoCrystal
import dev.kestrel.lb26.module.modules.combat.KillAura
import dev.kestrel.lb26.module.modules.movement.Fly
import dev.kestrel.lb26.module.modules.movement.NoFall
import dev.kestrel.lb26.module.modules.player.AutoTotem
import dev.kestrel.lb26.module.modules.world.Scaffold
import dev.kestrel.lb26.module.modules.render.FullBright
import dev.kestrel.lb26.module.modules.render.XRay
import dev.kestrel.lb26.value.BoolValue
import dev.kestrel.lb26.value.FloatValue
import dev.kestrel.lb26.value.IntValue
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW

class ClickGui : Screen(Component.literal("lb26")) {

    private val allModules: List<Module> = listOf(
        KillAura, AutoAnchor, AutoCrystal,
        AutoTotem, Fly, NoFall, Scaffold, XRay, FullBright
    )

    private val categories = listOf(
        Category.COMBAT, Category.MOVEMENT, Category.PLAYER, Category.WORLD, Category.RENDER
    )

    private var selectedCategory = Category.COMBAT
    private var expanded: Module? = null

    // panel layout
    private val panelWidth = 420
    private val panelHeight = 260
    private val titleBarHeight = 24
    private val searchBarHeight = 18
    private val moduleHeight = 18
    private val modulePadding = 4
    private val valueIndent = 14
    private val valueRowHeight = 15

    private var panelX = 0
    private var panelY = 0

    // text input
    private var editingValue: Any? = null
    private var inputText: String = ""
    private var inputActive: Boolean = false
    private var bindingModule: Module? = null

    // search
    private var searchText: String = ""
    private var searchActive: Boolean = false

    // palette — clean dark with green accent
    private val bgPanel = 0xF0181818.toInt()
    private val bgTitle = 0xFF1F1F1F.toInt()
    private val bgSearch = 0xFF141414.toInt()
    private val bgModuleOff = 0xFF262626.toInt()
    private val bgModuleOn = 0xFF1F4F2F.toInt()
    private val bgModuleHover = 0xFF333333.toInt()
    private val bgValue = 0xFF1F1F1F.toInt()
    private val bgKeyRow = 0xFF262626.toInt()
    private val accent = 0xFF00FF88.toInt()
    private val tabActive = 0xFF2A2A2A.toInt()
    private val tabInactive = 0xFF181818.toInt()
    private val textWhite = 0xFFFFFFFF.toInt()
    private val textGrey = 0xFFB0B0B0.toInt()
    private val textDim = 0xFF707070.toInt()

    override fun init() {
        super.init()
        panelX = (width - panelWidth) / 2
        panelY = (height - panelHeight) / 2
    }

    private fun keyName(key: Int): String = when (key) {
        GLFW.GLFW_KEY_UNKNOWN -> "NONE"
        GLFW.GLFW_KEY_R -> "R"
        GLFW.GLFW_KEY_H -> "H"
        GLFW.GLFW_KEY_K -> "K"
        GLFW.GLFW_KEY_J -> "J"
        GLFW.GLFW_KEY_V -> "V"
        GLFW.GLFW_KEY_N -> "N"
        GLFW.GLFW_KEY_B -> "B"
        GLFW.GLFW_KEY_G -> "G"
        GLFW.GLFW_KEY_F -> "F"
        GLFW.GLFW_KEY_C -> "C"
        GLFW.GLFW_KEY_X -> "X"
        GLFW.GLFW_KEY_Z -> "Z"
        GLFW.GLFW_KEY_Q -> "Q"
        GLFW.GLFW_KEY_E -> "E"
        GLFW.GLFW_KEY_T -> "T"
        GLFW.GLFW_KEY_Y -> "Y"
        GLFW.GLFW_KEY_U -> "U"
        GLFW.GLFW_KEY_I -> "I"
        GLFW.GLFW_KEY_O -> "O"
        GLFW.GLFW_KEY_P -> "P"
        GLFW.GLFW_KEY_L -> "L"
        GLFW.GLFW_KEY_M -> "M"
        GLFW.GLFW_KEY_A -> "A"
        GLFW.GLFW_KEY_D -> "D"
        GLFW.GLFW_KEY_S -> "S"
        GLFW.GLFW_KEY_W -> "W"
        GLFW.GLFW_KEY_SEMICOLON -> ";"
        GLFW.GLFW_KEY_COMMA -> ","
        GLFW.GLFW_KEY_PERIOD -> "."
        GLFW.GLFW_KEY_SLASH -> "/"
        GLFW.GLFW_KEY_RIGHT_BRACKET -> "]"
        GLFW.GLFW_KEY_LEFT_BRACKET -> "["
        GLFW.GLFW_KEY_BACKSLASH -> "\\"
        GLFW.GLFW_KEY_APOSTROPHE -> "'"
        GLFW.GLFW_KEY_RIGHT_SHIFT -> "RSHIFT"
        GLFW.GLFW_KEY_LEFT_SHIFT -> "LSHIFT"
        GLFW.GLFW_KEY_RIGHT_CONTROL -> "RCTRL"
        GLFW.GLFW_KEY_LEFT_CONTROL -> "LCTRL"
        GLFW.GLFW_KEY_SPACE -> "SPACE"
        GLFW.GLFW_KEY_TAB -> "TAB"
        GLFW.GLFW_KEY_1 -> "1"
        GLFW.GLFW_KEY_2 -> "2"
        GLFW.GLFW_KEY_3 -> "3"
        GLFW.GLFW_KEY_4 -> "4"
        GLFW.GLFW_KEY_5 -> "5"
        GLFW.GLFW_KEY_6 -> "6"
        GLFW.GLFW_KEY_7 -> "7"
        GLFW.GLFW_KEY_8 -> "8"
        GLFW.GLFW_KEY_9 -> "9"
        GLFW.GLFW_KEY_0 -> "0"
        else -> "KEY$key"
    }

    override fun extractRenderState(extractor: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, delta: Float) {
        // dim the world
        extractor.fill(0, 0, width, height, 0x80000000.toInt())

        // panel body
        extractor.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, bgPanel)
        // left accent bar
        extractor.fill(panelX, panelY, panelX + 2, panelY + panelHeight, accent)
        // top accent bar

        // title bar
        extractor.fill(panelX + 2, panelY + 2, panelX + panelWidth, panelY + titleBarHeight, bgTitle)
        extractor.text(font, "lb26", panelX + 10, panelY + 9, accent, true)

        // tabs
        var tabX = panelX + 55
        for (cat in categories) {
            val tabW = font.width(cat.name) + 14
            val isSelected = cat == selectedCategory
            val tabBg = if (isSelected) tabActive else tabInactive
            val tabTextColor = if (isSelected) accent else textDim

            extractor.fill(tabX, panelY + 5, tabX + tabW, panelY + titleBarHeight - 2, tabBg)
            extractor.text(font, cat.name, tabX + 7, panelY + 9, tabTextColor, true)

            if (isSelected) {
                extractor.fill(tabX, panelY + titleBarHeight - 3, tabX + tabW, panelY + titleBarHeight - 2, accent)
            }
            tabX += tabW + 3
        }

        // search bar
        val searchY = panelY + titleBarHeight + 4
        extractor.fill(panelX + modulePadding, searchY, panelX + panelWidth - modulePadding, searchY + searchBarHeight, bgSearch)
        val searchLabel = if (searchText.isEmpty() && !searchActive) "Search..." else "> $searchText"
        val searchColor = if (searchText.isEmpty() && !searchActive) textDim else textWhite
        extractor.text(font, searchLabel, panelX + modulePadding + 6, searchY + 5, searchColor, true)

        // modules (filtered by category + search)
        val categoryModules = allModules
            .filter { searchText.isNotEmpty() || it.category == selectedCategory }
            .filter { searchText.isEmpty() || it.name.lowercase().contains(searchText.lowercase()) }

        var modY = searchY + searchBarHeight + 6

        for (module in categoryModules) {
            val hovered = mouseX in panelX + modulePadding..panelX + panelWidth - modulePadding &&
                mouseY in modY..modY + moduleHeight

            val bg = when {
                module.enabled -> bgModuleOn
                hovered -> bgModuleHover
                else -> bgModuleOff
            }

            extractor.fill(panelX + modulePadding, modY, panelX + panelWidth - modulePadding, modY + moduleHeight, bg)

            // enabled: left accent bar on the module
            if (module.enabled) {
                extractor.fill(panelX + modulePadding, modY, panelX + modulePadding + 2, modY + moduleHeight, accent)
            }

            val textColor = if (module.enabled) textWhite else textGrey
            extractor.text(font, module.name, panelX + modulePadding + 10, modY + 5, textColor, true)

            // keybind hint on the right
            if (module.key != GLFW.GLFW_KEY_UNKNOWN) {
                val kText = keyName(module.key)
                val kWidth = font.width(kText)
                extractor.text(font, kText, panelX + panelWidth - modulePadding - kWidth - 8, modY + 5, textDim, true)
            }

            if (expanded === module) {
                var valY = modY + moduleHeight + 2

                // keybind row
                val bindingNow = bindingModule === module
                val keyLabel = if (bindingNow) "Press a key..." else "Keybind: ${keyName(module.key)}"
                val keyColor = if (bindingNow) accent else textGrey
                extractor.fill(panelX + modulePadding + valueIndent, valY, panelX + panelWidth - modulePadding, valY + valueRowHeight, bgKeyRow)
                extractor.text(font, keyLabel, panelX + modulePadding + valueIndent + 6, valY + 4, keyColor, true)
                valY += valueRowHeight + 1

                // values
                for (value in module.values) {
                    val label = when (value) {
                        is BoolValue -> "${value.name}: ${if (value.value) "ON" else "OFF"}"
                        is FloatValue -> "${value.name}: ${"%.2f".format(value.value)}"
                        is IntValue -> "${value.name}: ${value.value}"
                        else -> value.name
                    }
                    extractor.fill(panelX + modulePadding + valueIndent, valY, panelX + panelWidth - modulePadding, valY + valueRowHeight, bgValue)
                    extractor.text(font, label, panelX + modulePadding + valueIndent + 6, valY + 4, textGrey, true)
                    valY += valueRowHeight + 1
                }
                modY = valY + 2
            } else {
                modY += moduleHeight + 2
            }
        }

        // input overlay for value editing
        if (inputActive) {
            val boxW = 200
            val boxH = 18
            val boxX = (width - boxW) / 2
            val boxY = height - 40
            extractor.fill(boxX, boxY, boxX + boxW, boxY + boxH, 0xFF1A1A1A.toInt())
            extractor.fill(boxX, boxY, boxX + boxW, boxY + 1, accent)
            extractor.text(font, "> $inputText", boxX + 6, boxY + 5, accent, true)
        }

        super.extractRenderState(extractor, mouseX, mouseY, delta)
    }

    override fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean {
        val mx = event.x()
        val my = event.y()
        val button = event.button()

        // tabs
        var tabX = panelX + 55
        for (cat in categories) {
            val tabW = font.width(cat.name) + 14
            if (mx in tabX.toDouble()..(tabX + tabW).toDouble() &&
                my in (panelY + 5).toDouble()..(panelY + titleBarHeight - 2).toDouble()
            ) {
                selectedCategory = cat
                expanded = null
                bindingModule = null
                return true
            }
            tabX += tabW + 3
        }

        // search bar click — activates search input
        val searchY = panelY + titleBarHeight + 4
        if (mx in (panelX + modulePadding).toDouble()..(panelX + panelWidth - modulePadding).toDouble() &&
            my in searchY.toDouble()..(searchY + searchBarHeight).toDouble()
        ) {
            searchActive = true
            inputActive = false
            return true
        }

        // modules
        val categoryModules = allModules
            .filter { searchText.isNotEmpty() || it.category == selectedCategory }
            .filter { searchText.isEmpty() || it.name.lowercase().contains(searchText.lowercase()) }

        var modY = searchY + searchBarHeight + 6

        for (module in categoryModules) {
            if (mx in (panelX + modulePadding).toDouble()..(panelX + panelWidth - modulePadding).toDouble() &&
                my in modY.toDouble()..(modY + moduleHeight).toDouble()
            ) {
                if (button == 0) module.enabled = !module.enabled
                if (button == 1) expanded = if (expanded === module) null else module
                return true
            }

            if (expanded === module) {
                var valY = modY + moduleHeight + 2

                // keybind row click
                if (mx in (panelX + modulePadding + valueIndent).toDouble()..(panelX + panelWidth - modulePadding).toDouble() &&
                    my in valY.toDouble()..(valY + valueRowHeight).toDouble()
                ) {
                    bindingModule = module
                    return true
                }
                valY += valueRowHeight + 1

                for (value in module.values) {
                    if (mx in (panelX + modulePadding + valueIndent).toDouble()..(panelX + panelWidth - modulePadding).toDouble() &&
                        my in valY.toDouble()..(valY + valueRowHeight).toDouble()
                    ) {
                        when (value) {
                            is BoolValue -> value.value = !value.value
                            is FloatValue -> { println("VALUE CLICK mx=" + event.x() + " my=" + event.y())
                                editingValue = value
                                inputText = value.value.toString()
                                inputActive = true
                                searchActive = false
                            }
                            is IntValue -> {
                                editingValue = value
                                inputText = value.value.toString()
                                inputActive = true
                                searchActive = false
                            }
                        }
                        return true
                    }
                    valY += valueRowHeight + 1
                }
                modY = valY + 2
            } else {
                modY += moduleHeight + 2
            }
        }

        return super.mouseClicked(event, doubleClick)
    }

    override fun charTyped(event: CharacterEvent): Boolean {
        val c = event.codepoint().toChar()
        if (searchActive) {
            if (c.isLetterOrDigit() || c == '_' || c == ' ') searchText += c
            return true
        }
        if (inputActive) {
            if (c.isDigit() || c == '.' || c == '-') inputText += c
            return true
        }
        return super.charTyped(event)
    }

    override fun keyPressed(event: KeyEvent): Boolean {
        // keybind capture
        val binding = bindingModule
        if (binding != null) {
            val key = event.key()
            if (key == GLFW.GLFW_KEY_ESCAPE) {
                bindingModule = null
                return true
            }
            binding.key = key
            bindingModule = null
            return true
        }

        // search input
        if (searchActive) {
            when (event.key()) {
                259 -> { // backspace
                    if (searchText.isNotEmpty()) searchText = searchText.dropLast(1)
                    return true
                }
                256 -> { // escape
                    searchActive = false
                    return true
                }
                257 -> { // enter
                    searchActive = false
                    return true
                }
            }
            return true
        }

        // value input
        if (inputActive) {
            when (event.key()) {
                257 -> {
                    val v = editingValue
                    try {
                        when (v) {
                            is FloatValue -> v.set(inputText.toFloat())
                            is IntValue -> v.set(inputText.toInt())
                        }
                    } catch (_: NumberFormatException) { }
                    inputActive = false
                    editingValue = null
                    inputText = ""
                    return true
                }
                259 -> {
                    if (inputText.isNotEmpty()) inputText = inputText.dropLast(1)
                    return true
                }
                256 -> {
                    inputActive = false
                    editingValue = null
                    inputText = ""
                    return true
                }
            }
        }
        return super.keyPressed(event)
    }

    override fun isPauseScreen(): Boolean = false
}
