# USB Keyboard Android App

An Android application that turns your Android device into a USB keyboard using USB Host mode.

## Features

- USB HID keyboard emulation
- Virtual keyboard interface
- Support for modifier keys (Shift, Ctrl, Alt)
- Power-efficient operation
- Secure device verification
- Error handling and user feedback

## Requirements

- Android device with USB Host mode support
- Android 7.0 (API level 24) or higher
- USB OTG cable
- Windows 10+ host computer

## Installation

1. Clone the repository:
```bash
git clone https://github.com/yourusername/usb-keyboard.git
```

2. Open the project in Android Studio

3. Build and install the app:
```bash
./gradlew installDebug
```

## Usage

1. Connect your Android device to a PC using a USB OTG cable
2. Launch the USB Keyboard app
3. Grant USB permissions when prompted
4. Use the virtual keyboard to type on the connected PC

## Architecture

The app follows a modular architecture with these main components:

- `MainActivity`: UI and user interaction handling
- `UsbConnectionManager`: USB device communication
- `HidDescriptor`: USB HID protocol implementation
- `KeyboardReportGenerator`: HID report generation
- `SecurityManager`: Device verification and security
- `PowerManager`: Power optimization
- `ErrorHandler`: Error management and user feedback

## Security Features

- Device whitelisting
- Vendor verification
- Security risk assessment
- Encrypted communication
- Android Keystore integration

## Power Optimization

- Wake lock management
- Buffer reuse
- Inactivity detection
- Screen state management

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details. 