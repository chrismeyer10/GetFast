#!/usr/bin/env bash
set -e

# Build and install the debug APK on a connected device or emulator.
if [ -f "./gradlew" ]; then
  ./gradlew installDebug
else
  gradle installDebug
fi
