package com.example.usbkeyboard

import android.content.Context
import android.os.PowerManager
import android.view.Window
import android.view.WindowManager

class PowerManager(private val context: Context, private val window: Window) {
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    private var wakeLock: PowerManager.WakeLock? = null
    private var isKeyboardActive = false
    
    init {
        // Configure window flags for power optimization
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    fun onKeyboardActive() {
        if (!isKeyboardActive) {
            isKeyboardActive = true
            acquireWakeLock()
        }
    }

    fun onKeyboardInactive() {
        if (isKeyboardActive) {
            isKeyboardActive = false
            releaseWakeLock()
        }
    }

    private fun acquireWakeLock() {
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "UsbKeyboard:KeyboardActive"
        ).apply {
            acquire(10 * 60 * 1000L) // 10 minutes timeout
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
        wakeLock = null
    }

    fun cleanup() {
        releaseWakeLock()
    }
} 