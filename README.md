# VolumeBubble

VolumeBubble is a simple Android application that displays a floating bubble for quick volume adjustments. It is built on top of the open‑source Bubbles library and demonstrates how to use chat head style overlays for custom utilities.

![Screenshot](icons/vb_banner.png)

## Building

1. Install Android Studio or ensure that the Android SDK is available on your system.
2. Clone this repository and open it with Android Studio, or use Gradle from the command line:

```bash
gradle assembleDebug
```

A sample `google-services.json` is provided for development. The build requires permission to draw over other apps when running on the device or emulator.

## Usage

Run the app and grant overlay permissions when prompted. Tap **Add Bubble** to place the bubble on your screen. Tapping the bubble shows the system volume controls; dragging the bubble to the trash removes it.

## License

This project is licensed under the [Apache 2.0](LICENSE) license.
