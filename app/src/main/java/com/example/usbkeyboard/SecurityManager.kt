package com.example.usbkeyboard

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class SecurityManager(private val context: Context) {
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    private val allowedVendors = setOf(
        // Common USB vendor IDs
        0x046D,  // Logitech
        0x04F2,  // Chicony
        0x0461,  // Primax
        0x045E   // Microsoft
    )

    init {
        generateEncryptionKey()
    }

    fun verifyDevice(device: UsbDevice): SecurityVerification {
        // Check if device is in system whitelist
        if (!isSystemWhitelistedDevice(device)) {
            return SecurityVerification.Failure("Device not in system whitelist")
        }

        // Verify vendor ID
        if (!isAllowedVendor(device.vendorId)) {
            return SecurityVerification.Failure("Unauthorized vendor ID: ${device.vendorId}")
        }

        // Verify device class
        if (!isValidDeviceClass(device)) {
            return SecurityVerification.Failure("Invalid device class")
        }

        // Check for potential security risks
        if (hasSecurityRisks(device)) {
            return SecurityVerification.Failure("Security risk detected")
        }

        return SecurityVerification.Success
    }

    private fun isSystemWhitelistedDevice(device: UsbDevice): Boolean {
        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        return usbManager.deviceList.containsValue(device)
    }

    private fun isAllowedVendor(vendorId: Int): Boolean {
        return allowedVendors.contains(vendorId)
    }

    private fun isValidDeviceClass(device: UsbDevice): Boolean {
        // Check if device implements HID interface
        return device.interfaces.any { 
            it.interfaceClass == UsbConstants.USB_CLASS_HID 
        }
    }

    private fun hasSecurityRisks(device: UsbDevice): Boolean {
        // Check for known security vulnerabilities
        return when {
            device.deviceClass == UsbConstants.USB_CLASS_VENDOR_SPEC -> true
            device.interfaceCount > 5 -> true  // Suspicious number of interfaces
            device.deviceProtocol == 0xFF -> true  // Vendor-specific protocol
            else -> false
        }
    }

    private fun generateEncryptionKey() {
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                "AndroidKeyStore"
            )
            val keyGenSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setUserAuthenticationRequired(false)
                .build()

            keyGenerator.init(keyGenSpec)
            keyGenerator.generateKey()
        }
    }

    fun getEncryptionKey(): SecretKey {
        return keyStore.getKey(KEY_ALIAS, null) as SecretKey
    }

    companion object {
        private const val KEY_ALIAS = "UsbKeyboardKey"
    }
}

sealed class SecurityVerification {
    object Success : SecurityVerification()
    class Failure(val reason: String) : SecurityVerification()
} 