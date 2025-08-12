# GetFast

Android app using MVVM architecture to monitor eBay Kleinanzeigen for new housing listings. The app fetches listings from a configurable search URL, displays them in a list and triggers a local push notification for new entries.

## Build & Run

The project is built with Gradle. On Windows, build the debug APK with:

```
gradle assembleDebug
```

If the build fails with `SDK location not found`, define the Android SDK path:

```
setx ANDROID_SDK_ROOT "C:\Users\<you>\AppData\Local\Android\Sdk"
```

After setting the variable, you can optionally run the helper script to generate `local.properties`:

```
scripts\setup-sdk.ps1
```

The PowerShell script only writes the `local.properties` file so Gradle can locate the SDK.
