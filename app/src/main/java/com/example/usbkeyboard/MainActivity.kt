package com.example.usbkeyboard

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.MotionEvent
import android.view.View
import android.view.HapticFeedbackConstants
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {
    private lateinit var usbConnectionManager: UsbConnectionManager
    private lateinit var errorHandler: ErrorHandler
    private lateinit var statusText: TextView
    private lateinit var connectionIndicator: View
    private var activeModifiers: Byte = 0
    private val keyRepeatHandler = Handler(Looper.getMainLooper())
    private var currentRepeatingKey: String? = null
    private val vibrator by lazy { getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }
    private lateinit var powerManager: PowerManager
    private var lastActivityTime = 0L

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                    val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                    handleDeviceAttached(device)
                }
                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    handleDeviceDetached()
                }
                ACTION_USB_PERMISSION -> {
                    synchronized(this) {
                        val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            device?.let { usbConnectionManager.setupDevice(it) }
                        } else {
                            updateStatus("USB permission denied", false)
                        }
                    }
                }
            }
        }
    }

    private val keyRepeatRunnable = object : Runnable {
        override fun run() {
            currentRepeatingKey?.let { keyTag ->
                if (!keyTag.startsWith("LEFT_") && !keyTag.startsWith("RIGHT_")) {
                    val keyCode = keyTag.removePrefix("0x").toInt(16).toByte()
                    usbConnectionManager.sendKeyPress(keyCode, activeModifiers)
                }
                keyRepeatHandler.postDelayed(this, KEY_REPEAT_DELAY)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        connectionIndicator = findViewById(R.id.connectionIndicator)
        
        errorHandler = ErrorHandler(this, findViewById(android.R.id.content), statusText)
        usbConnectionManager = UsbConnectionManager(this, errorHandler)

        // Set up keyboard button listeners
        setupKeyboardButtons()

        // Register for USB events
        val filter = IntentFilter().apply {
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
            addAction(ACTION_USB_PERMISSION)
        }
        registerReceiver(usbReceiver, filter)

        // Check if device was already connected
        intent?.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)?.let {
            handleDeviceAttached(it)
        }

        powerManager = PowerManager(this, window)

        // Configure power-related settings
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun setupKeyboardButtons() {
        val keyboardLayout = findViewById<View>(R.id.keyboardLayout)
        keyboardLayout.findViewsWithType<MaterialButton>().forEach { button ->
            button.setOnTouchListener { view, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        handleKeyDown(button.tag.toString())
                        view.isPressed = true
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        
                        // Start key repeat after initial delay
                        currentRepeatingKey = button.tag.toString()
                        keyRepeatHandler.postDelayed(keyRepeatRunnable, KEY_REPEAT_INITIAL_DELAY)
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        handleKeyUp(button.tag.toString())
                        view.isPressed = false
                        
                        // Stop key repeat
                        currentRepeatingKey = null
                        keyRepeatHandler.removeCallbacks(keyRepeatRunnable)
                    }
                }
                true
            }
        }
    }

    private fun handleKeyDown(keyTag: String) {
        try {
            // Update activity timestamp
            lastActivityTime = System.currentTimeMillis()
            powerManager.onKeyboardActive()

            when {
                keyTag.startsWith("LEFT_") || keyTag.startsWith("RIGHT_") -> {
                    val modifier = HidDescriptor.ModifierKeys::class.java
                        .getDeclaredField(keyTag)
                        .get(null) as Byte
                    activeModifiers = (activeModifiers.toInt() or modifier.toInt()).toByte()
                    usbConnectionManager.sendKeyPress(0, activeModifiers)
                    updateModifierState(keyTag, true)
                }
                else -> {
                    if (!usbConnectionManager.isConnected()) {
                        updateStatus("Not connected to USB host", false)
                        return
                    }
                    val keyCode = keyTag.removePrefix("0x").toInt(16).toByte()
                    usbConnectionManager.sendKeyPress(keyCode, activeModifiers)
                }
            }
        } catch (e: Exception) {
            errorHandler.handleError(UsbKeyboardError.TransmissionError(e.message ?: "Unknown error"))
        }
    }

    private fun handleKeyUp(keyTag: String) {
        try {
            // Schedule inactivity check
            handler.postDelayed({
                if (System.currentTimeMillis() - lastActivityTime >= INACTIVITY_TIMEOUT) {
                    powerManager.onKeyboardInactive()
                }
            }, INACTIVITY_TIMEOUT)

            when {
                keyTag.startsWith("LEFT_") || keyTag.startsWith("RIGHT_") -> {
                    val modifier = HidDescriptor.ModifierKeys::class.java
                        .getDeclaredField(keyTag)
                        .get(null) as Byte
                    activeModifiers = (activeModifiers.toInt() and modifier.inv()).toByte()
                    usbConnectionManager.sendKeyPress(0, activeModifiers)
                    updateModifierState(keyTag, false)
                }
                else -> {
                    if (!usbConnectionManager.isConnected()) return
                    val keyCode = keyTag.removePrefix("0x").toInt(16).toByte()
                    usbConnectionManager.sendKeyRelease(keyCode)
                }
            }
        } catch (e: Exception) {
            errorHandler.handleError(UsbKeyboardError.TransmissionError(e.message ?: "Unknown error"))
        }
    }

    private fun updateModifierState(modifierTag: String, active: Boolean) {
        val keyboardLayout = findViewById<View>(R.id.keyboardLayout)
        keyboardLayout.findViewsWithType<MaterialButton>().forEach { button ->
            if (button.tag == modifierTag) {
                button.isChecked = active
            }
        }
    }

    private fun handleDeviceAttached(device: UsbDevice?) {
        usbConnectionManager.setupDevice(device)
        updateStatus("USB device attached", true)
    }

    private fun handleDeviceDetached() {
        usbConnectionManager.closeConnection()
        updateStatus("USB device detached", false)
    }

    private fun updateStatus(message: String, connected: Boolean) {
        statusText.text = message
        connectionIndicator.setBackgroundColor(
            if (connected) Color.GREEN else Color.RED
        )
    }

    override fun onPause() {
        super.onPause()
        powerManager.onKeyboardInactive()
    }

    override fun onResume() {
        super.onResume()
        if (usbConnectionManager.isConnected()) {
            powerManager.onKeyboardActive()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        keyRepeatHandler.removeCallbacks(keyRepeatRunnable)
        unregisterReceiver(usbReceiver)
        usbConnectionManager.closeConnection()
        errorHandler.cleanup()
        powerManager.cleanup()
    }

    companion object {
        private const val ACTION_USB_PERMISSION = "com.example.usbkeyboard.USB_PERMISSION"
        private const val KEY_REPEAT_INITIAL_DELAY = 500L // ms
        private const val KEY_REPEAT_DELAY = 50L // ms
        private const val INACTIVITY_TIMEOUT = 30_000L // 30 seconds
    }
}

// Extension function to find views of a specific type
inline fun <reified T : View> View.findViewsWithType(): List<T> {
    val result = mutableListOf<T>()
    if (this is T) {
        result.add(this)
    }
    if (this is android.view.ViewGroup) {
        for (i in 0 until childCount) {
            result.addAll(getChildAt(i).findViewsWithType<T>())
        }
    }
    return result
} 