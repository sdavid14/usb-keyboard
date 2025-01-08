package com.example.usbkeyboard

object HidDescriptor {
    // USB HID Keyboard descriptor based on USB HID specification v1.11
    val DESCRIPTOR = byteArrayOf(
        // Interface Descriptor
        0x09,        // bLength
        0x04,        // bDescriptorType (Interface)
        0x00,        // bInterfaceNumber
        0x00,        // bAlternateSetting
        0x01,        // bNumEndpoints
        0x03,        // bInterfaceClass (HID)
        0x01,        // bInterfaceSubClass (Boot Interface)
        0x01,        // bInterfaceProtocol (Keyboard)
        0x00,        // iInterface

        // HID Descriptor
        0x09,        // bLength
        0x21,        // bDescriptorType (HID)
        0x11, 0x01,  // bcdHID 1.11
        0x00,        // bCountryCode
        0x01,        // bNumDescriptors
        0x22,        // bDescriptorType (Report)
        0x3F, 0x00,  // wDescriptorLength 63

        // Report Descriptor
        0x05, 0x01,  // Usage Page (Generic Desktop)
        0x09, 0x06,  // Usage (Keyboard)
        0xA1, 0x01,  // Collection (Application)
        
        // Modifier byte
        0x05, 0x07,  // Usage Page (Key Codes)
        0x19, 0xE0,  // Usage Minimum (224)
        0x29, 0xE7,  // Usage Maximum (231)
        0x15, 0x00,  // Logical Minimum (0)
        0x25, 0x01,  // Logical Maximum (1)
        0x75, 0x01,  // Report Size (1)
        0x95, 0x08,  // Report Count (8)
        0x81, 0x02,  // Input (Data, Variable, Absolute)
        
        // Reserved byte
        0x95, 0x01,  // Report Count (1)
        0x75, 0x08,  // Report Size (8)
        0x81, 0x01,  // Input (Constant)
        
        // LED report
        0x95, 0x05,  // Report Count (5)
        0x75, 0x01,  // Report Size (1)
        0x05, 0x08,  // Usage Page (LEDs)
        0x19, 0x01,  // Usage Minimum (1)
        0x29, 0x05,  // Usage Maximum (5)
        0x91, 0x02,  // Output (Data, Variable, Absolute)
        
        // LED report padding
        0x95, 0x01,  // Report Count (1)
        0x75, 0x03,  // Report Size (3)
        0x91, 0x01,  // Output (Constant)
        
        // Key arrays (6 keys)
        0x95, 0x06,  // Report Count (6)
        0x75, 0x08,  // Report Size (8)
        0x15, 0x00,  // Logical Minimum (0)
        0x25, 0x65,  // Logical Maximum (101)
        0x05, 0x07,  // Usage Page (Key Codes)
        0x19, 0x00,  // Usage Minimum (0)
        0x29, 0x65,  // Usage Maximum (101)
        0x81, 0x00,  // Input (Data, Array)
        
        0xC0         // End Collection
    )

    // Standard USB HID keyboard report format
    data class KeyboardReport(
        var modifiers: Byte = 0,      // Modifier keys (Ctrl, Alt, Shift, etc.)
        var reserved: Byte = 0,       // Reserved byte
        var keys: ByteArray = ByteArray(6) // Up to 6 simultaneous key presses
    ) {
        fun toByteArray(): ByteArray {
            return byteArrayOf(modifiers, reserved) + keys
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as KeyboardReport

            if (modifiers != other.modifiers) return false
            if (reserved != other.reserved) return false
            if (!keys.contentEquals(other.keys)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = modifiers.toInt()
            result = 31 * result + reserved.toInt()
            result = 31 * result + keys.contentHashCode()
            return result
        }
    }

    // Modifier key masks
    object ModifierKeys {
        const val LEFT_CTRL   = 0x01.toByte()
        const val LEFT_SHIFT  = 0x02.toByte()
        const val LEFT_ALT    = 0x04.toByte()
        const val LEFT_GUI    = 0x08.toByte()
        const val RIGHT_CTRL  = 0x10.toByte()
        const val RIGHT_SHIFT = 0x20.toByte()
        const val RIGHT_ALT   = 0x40.toByte()
        const val RIGHT_GUI   = 0x80.toByte()
    }
} 