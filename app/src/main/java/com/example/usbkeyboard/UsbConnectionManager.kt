package com.example.usbkeyboard

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import android.os.Build

class UsbConnectionManager(
    private val context: Context,
    private val errorHandler: ErrorHandler
) {
    private val usbManager: UsbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    private var usbDevice: UsbDevice? = null
    private var usbConnection: UsbDeviceConnection? = null
    private var usbInterface: UsbInterface? = null
    private var endpointIn: UsbEndpoint? = null
    private var endpointOut: UsbEndpoint? = null
    private val reportGenerator = KeyboardReportGenerator()
    private val transferBuffer = ByteArray(8) // Reuse buffer for transfers
    private val securityManager = SecurityManager(context)

    companion object {
        private const val ACTION_USB_PERMISSION = "com.example.usbkeyboard.USB_PERMISSION"
        private const val TRANSFER_TIMEOUT = 1000 // 1 second timeout
    }

    fun setupDevice(device: UsbDevice?) {
        if (device == null) {
            errorHandler.handleError(UsbKeyboardError.DeviceError("No USB device found"))
            return
        }
        
        try {
            // Verify device security
            when (val verification = securityManager.verifyDevice(device)) {
                is SecurityVerification.Success -> {
                    usbDevice = device
                    if (!usbManager.hasPermission(device)) {
                        requestPermission(device)
                    } else {
                        connectToDevice(device)
                    }
                }
                is SecurityVerification.Failure -> {
                    errorHandler.handleError(
                        UsbKeyboardError.SecurityError(verification.reason)
                    )
                }
            }
        } catch (e: Exception) {
            errorHandler.handleError(
                UsbKeyboardError.SecurityError("Security verification failed: ${e.message}")
            )
        }
    }

    private fun connectToDevice(device: UsbDevice) {
        try {
            // Find the interface for HID keyboard
            val interface_ = device.interfaces.find { 
                it.interfaceClass == UsbConstants.USB_CLASS_HID 
            } ?: throw IllegalStateException("No HID interface found")

            usbInterface = interface_
            usbConnection = usbManager.openDevice(device)
                ?: throw IllegalStateException("Failed to open USB connection")
            
            if (!usbConnection?.claimInterface(interface_, true)!!) {
                throw IllegalStateException("Failed to claim interface")
            }

            // Set up endpoints
            setupEndpoints(interface_)
            
            // Set HID descriptor
            setHidDescriptor()
            
            notifyConnectionStatus(true)
        } catch (e: Exception) {
            errorHandler.handleError(UsbKeyboardError.ConnectionError(e.message ?: "Unknown error"))
            closeConnection()
        }
    }

    private fun setHidDescriptor() {
        usbConnection?.controlTransfer(
            UsbConstants.USB_TYPE_CLASS or UsbConstants.USB_DIR_OUT,
            0x09,  // HID Set_Report
            0x200, // Report Type (2) and Report ID (0)
            0,     // Interface number
            HidDescriptor.DESCRIPTOR,
            HidDescriptor.DESCRIPTOR.size,
            0     // Timeout
        )
    }

    fun sendReport(report: HidDescriptor.KeyboardReport) {
        if (!isConnected()) {
            errorHandler.handleError(UsbKeyboardError.TransmissionError("Not connected to USB host"))
            return
        }

        try {
            // Reuse transfer buffer instead of creating new arrays
            report.toByteArray(transferBuffer)
            endpointOut?.let { endpoint ->
                val result = usbConnection?.bulkTransfer(
                    endpoint,
                    transferBuffer,
                    transferBuffer.size,
                    TRANSFER_TIMEOUT
                ) ?: -1

                if (result < 0) {
                    throw IllegalStateException("Failed to send report")
                }
            } ?: throw IllegalStateException("Output endpoint not available")
        } catch (e: Exception) {
            errorHandler.handleError(UsbKeyboardError.TransmissionError(e.message ?: "Unknown error"))
        }
    }

    fun closeConnection() {
        usbInterface?.let { usbConnection?.releaseInterface(it) }
        usbConnection?.close()
        usbConnection = null
        usbInterface = null
        usbDevice = null
    }

    private fun notifyConnectionStatus(connected: Boolean) {
        // We'll implement this later to update the UI
    }

    fun isConnected(): Boolean {
        return usbConnection != null && usbInterface != null
    }

    fun sendKeyPress(char: Char) {
        if (!isConnected()) return
        
        val (keyCode, modifier) = reportGenerator.charToKeyCode(char)
        if (keyCode == 0.toByte()) return

        // Press key and send report
        reportGenerator.pressKey(keyCode, modifier)
        sendReport(reportGenerator.getCurrentReport())

        // Release key and send report
        reportGenerator.releaseKey(keyCode)
        sendReport(reportGenerator.getCurrentReport())
    }

    fun sendKeyPress(keyCode: Byte, modifier: Byte = 0) {
        if (!isConnected()) return

        // Press key and send report
        reportGenerator.pressKey(keyCode, modifier)
        sendReport(reportGenerator.getCurrentReport())
    }

    fun sendKeyRelease(keyCode: Byte) {
        if (!isConnected()) return

        // Release key and send report
        reportGenerator.releaseKey(keyCode)
        sendReport(reportGenerator.getCurrentReport())
    }

    fun releaseAllKeys() {
        if (!isConnected()) return

        reportGenerator.releaseAllKeys()
        sendReport(reportGenerator.getCurrentReport())
    }
} 