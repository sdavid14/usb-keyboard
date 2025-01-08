package com.example.usbkeyboard

import android.content.Context
import android.view.View
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.Timer
import java.util.TimerTask

class ErrorHandler(
    private val context: Context,
    private val rootView: View,
    private val statusTextView: TextView
) {
    private val errorQueue = ConcurrentLinkedQueue<String>()
    private var currentSnackbar: Snackbar? = null
    private val timer = Timer()
    
    init {
        // Schedule periodic error queue processing
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                processErrorQueue()
            }
        }, 0, ERROR_DISPLAY_DURATION)
    }

    fun handleError(error: UsbKeyboardError) {
        when (error) {
            is UsbKeyboardError.ConnectionError -> {
                showError("Connection error: ${error.message}", true)
            }
            is UsbKeyboardError.PermissionError -> {
                showError("Permission denied: ${error.message}", true)
            }
            is UsbKeyboardError.TransmissionError -> {
                showError("Failed to send keypress: ${error.message}", false)
            }
            is UsbKeyboardError.DeviceError -> {
                showError("Device error: ${error.message}", true)
            }
            is UsbKeyboardError.SecurityError -> {
                showError("Security error: ${error.message}", true)
            }
        }
    }

    private fun showError(message: String, persistent: Boolean) {
        if (persistent) {
            // Update status text for persistent errors
            statusTextView.post {
                statusTextView.text = message
            }
        } else {
            // Queue transient errors for Snackbar display
            errorQueue.offer(message)
        }
    }

    private fun processErrorQueue() {
        if (currentSnackbar?.isShown == true || errorQueue.isEmpty()) return

        errorQueue.poll()?.let { message ->
            rootView.post {
                currentSnackbar = Snackbar.make(
                    rootView,
                    message,
                    Snackbar.LENGTH_SHORT
                ).apply {
                    setAction("Dismiss") { dismiss() }
                    show()
                }
            }
        }
    }

    fun cleanup() {
        timer.cancel()
        currentSnackbar?.dismiss()
        errorQueue.clear()
    }

    companion object {
        private const val ERROR_DISPLAY_DURATION = 3000L // 3 seconds
    }
}

sealed class UsbKeyboardError(val message: String) {
    class ConnectionError(message: String) : UsbKeyboardError(message)
    class PermissionError(message: String) : UsbKeyboardError(message)
    class TransmissionError(message: String) : UsbKeyboardError(message)
    class DeviceError(message: String) : UsbKeyboardError(message)
    class SecurityError(message: String) : UsbKeyboardError(message)
} 