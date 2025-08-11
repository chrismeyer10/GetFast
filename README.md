# GetFast

Android app using MVVM architecture to monitor eBay Kleinanzeigen for new housing listings. The app fetches listings from a configurable search URL, displays them in a list and triggers a local push notification for new entries.

## Build & Run

The project is built with Gradle. To build and install the debug APK on a connected device or emulator, run:

```bash
scripts/run.sh
```

The script will use the Gradle wrapper if available, or fall back to the system Gradle installation. Make sure the Android SDK is installed and that `ANDROID_HOME` (or `ANDROID_SDK_ROOT`) points to its location.

