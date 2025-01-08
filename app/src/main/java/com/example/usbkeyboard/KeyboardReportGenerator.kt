package com.example.usbkeyboard

class KeyboardReportGenerator {
    private val currentReport = HidDescriptor.KeyboardReport()
    private val pressedKeys = mutableSetOf<Byte>()
    
    companion object {
        // USB HID key codes for common keys
        object KeyCodes {
            const val KEY_A = 0x04.toByte()
            const val KEY_Z = 0x1D.toByte()
            const val KEY_1 = 0x1E.toByte()
            const val KEY_0 = 0x27.toByte()
            const val KEY_ENTER = 0x28.toByte()
            const val KEY_ESCAPE = 0x29.toByte()
            const val KEY_BACKSPACE = 0x2A.toByte()
            const val KEY_TAB = 0x2B.toByte()
            const val KEY_SPACE = 0x2C.toByte()
            const val KEY_MINUS = 0x2D.toByte()
            const val KEY_EQUAL = 0x2E.toByte()
            const val KEY_LEFT_BRACKET = 0x2F.toByte()
            const val KEY_RIGHT_BRACKET = 0x30.toByte()
            const val KEY_BACKSLASH = 0x31.toByte()
            const val KEY_SEMICOLON = 0x33.toByte()
            const val KEY_QUOTE = 0x34.toByte()
            const val KEY_GRAVE = 0x35.toByte()
            const val KEY_COMMA = 0x36.toByte()
            const val KEY_PERIOD = 0x37.toByte()
            const val KEY_SLASH = 0x38.toByte()
            const val KEY_CAPS_LOCK = 0x39.toByte()
            const val KEY_F1 = 0x3A.toByte()
            // ... Add more key codes as needed
        }
    }

    fun pressKey(keyCode: Byte, modifiers: Byte = 0) {
        // Update modifiers
        currentReport.modifiers = modifiers

        // Add key to pressed keys if not already pressed
        if (pressedKeys.add(keyCode)) {
            updateKeyArray()
        }
    }

    fun releaseKey(keyCode: Byte) {
        // Remove key from pressed keys
        if (pressedKeys.remove(keyCode)) {
            updateKeyArray()
        }
    }

    fun releaseAllKeys() {
        pressedKeys.clear()
        currentReport.modifiers = 0
        currentReport.keys.fill(0)
    }

    private fun updateKeyArray() {
        // Reset key array
        currentReport.keys.fill(0)
        
        // Fill in pressed keys (up to 6)
        pressedKeys.take(6).forEachIndexed { index, keyCode ->
            currentReport.keys[index] = keyCode
        }
    }

    fun getCurrentReport(): HidDescriptor.KeyboardReport {
        return currentReport.copy(
            keys = currentReport.keys.clone()
        )
    }

    // Helper function to convert ASCII character to USB HID key code
    fun charToKeyCode(char: Char): Pair<Byte, Byte> {
        val isUpperCase = char.isUpperCase()
        val c = char.toLowerCase()
        
        val keyCode = when (c) {
            in 'a'..'z' -> (KeyCodes.KEY_A + (c - 'a')).toByte()
            in '1'..'9' -> (KeyCodes.KEY_1 + (c - '1')).toByte()
            '0' -> KeyCodes.KEY_0
            ' ' -> KeyCodes.KEY_SPACE
            '\n', '\r' -> KeyCodes.KEY_ENTER
            '\t' -> KeyCodes.KEY_TAB
            '`' -> KeyCodes.KEY_GRAVE
            '-' -> KeyCodes.KEY_MINUS
            '=' -> KeyCodes.KEY_EQUAL
            '[' -> KeyCodes.KEY_LEFT_BRACKET
            ']' -> KeyCodes.KEY_RIGHT_BRACKET
            '\\' -> KeyCodes.KEY_BACKSLASH
            ';' -> KeyCodes.KEY_SEMICOLON
            '\'' -> KeyCodes.KEY_QUOTE
            ',' -> KeyCodes.KEY_COMMA
            '.' -> KeyCodes.KEY_PERIOD
            '/' -> KeyCodes.KEY_SLASH
            else -> 0.toByte()
        }

        val modifier = if (isUpperCase) HidDescriptor.ModifierKeys.LEFT_SHIFT else 0.toByte()
        return Pair(keyCode, modifier)
    }
} 