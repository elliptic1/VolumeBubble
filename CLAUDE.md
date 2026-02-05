# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Clean and build
./gradlew clean assembleDebug

# Install debug build on connected device/emulator
./gradlew installDebug
```

## Project Overview

VolumeBubble is an Android app that displays a floating bubble overlay for quick volume adjustments. Tapping the bubble shows system volume controls.

## Architecture

**Two-module structure:**
- `app` - Main application (Kotlin) containing activities and UI
- `bubbles` - Reusable library module (Java) providing the floating bubble overlay system

**Key Components:**

`bubbles` module (the overlay system):
- `BubblesManager` - Singleton entry point. Binds to `BubblesService` and manages bubble lifecycle. Use the `Builder` pattern to configure.
- `BubblesService` - Foreground service that holds bubble views in the window manager. Required for overlays to persist when app is backgrounded.
- `BubbleLayout` - The draggable bubble view. Supports click listeners, remove listeners, and wall-sticking behavior.
- `BubbleTrashLayout` - Drop target for removing bubbles.
- `BubblesLayoutCoordinator` - Coordinates bubble drag interactions with trash.

`app` module:
- `MainActivity` - Handles overlay permission flow and creates bubbles via `BubblesManager`

**Bubble Creation Flow:**
1. `MainActivity` creates `BubblesManager` with `applicationContext` (important for persistence)
2. `BubblesManager.initialize()` starts/binds to `BubblesService` as foreground service
3. `addBubble()` inflates a `BubbleLayout` and adds it to `WindowManager`
4. Bubble persists until dragged to trash or programmatically removed

## Technical Notes

- Requires `SYSTEM_ALERT_WINDOW` permission (overlay permission)
- Uses `TYPE_APPLICATION_OVERLAY` on Android O+ for overlay windows
- Runs as foreground service with notification channel for Android O+ compliance
- Uses view binding (`ActivityMainBinding`, `BubbleLayoutBinding`)
- Firebase Analytics is integrated but optional for development (sample `google-services.json` provided)
