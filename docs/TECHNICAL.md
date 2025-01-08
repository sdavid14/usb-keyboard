# Technical Documentation

## USB HID Implementation

### HID Descriptor

The app implements a standard USB HID keyboard descriptor with:
- 8-bit modifier byte
- 8-bit reserved byte
- 6 simultaneous keypress bytes

```kotlin
val DESCRIPTOR = byteArrayOf(
    0x05, 0x01,  // Usage Page (Generic Desktop)
    0x09, 0x06,  // Usage (Keyboard)
    // ... full descriptor ...
)
```

### Report Format

Each keyboard report consists of 8 bytes:
1. Modifier keys (bit-mapped)
2. Reserved byte
3-8. Active key codes (up to 6)

## Security Implementation

### Device Verification

1. System whitelist check
2. Vendor ID verification
3. Device class validation
4. Security risk assessment

### Encryption

- Uses Android Keystore for key management
- AES encryption for sensitive data
- CBC block mode with PKCS7 padding

## Power Management

### Wake Lock Strategy

- PARTIAL_WAKE_LOCK during active typing
- Automatic release after inactivity
- Screen-on management during connection

### Buffer Management

- Reusable transfer buffers
- Optimized memory allocation
- Efficient report generation

## Error Handling

### Error Types

1. ConnectionError
2. PermissionError
3. TransmissionError
4. DeviceError
5. SecurityError

### Error Display

- Persistent errors in status bar
- Transient errors in Snackbar
- Queue-based message management 