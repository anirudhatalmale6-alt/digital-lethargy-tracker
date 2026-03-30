# Digital Lethargy Tracker (DL Tracker)

Android app for capturing non-identifiable behavioural analytics for Digital Lethargy research (DLD-1 dataset).

## Features

- **App Usage Tracking** — Background service using UsageStatsManager polls foreground app every 1.5s, records app package + dwell duration
- **Session Tracking** — Automatic detection of phone usage sessions (screen on/off, unlock events)
- **Tap & Scroll Tracking** — AccessibilityService captures tap, scroll, and screen change events across all apps
- **Self-Report Labeling** — Users tag their engagement state (bored, engaged, distracted, habitual, purposeful, anxious, procrastinating, relaxing)
- **Reaction Time Test** — Tap-when-green cognitive response measurement with session statistics
- **CSV/JSON Export** — Export matching schema: `timestamp, app_package, screen_id, event_type, dwell_ms`
- **Auto ACI Computation** — App Cognitive Inertia score (0-10) calculated from dwell concentration, transition frequency, and interaction density
- **Pause/Resume Toggle** — Full control to start/stop tracking at any time
- **Privacy Disclosure** — Clear on-screen consent explaining exactly what is and isn't collected
- **Boot Auto-Start** — Service resumes automatically after device reboot
- **Encrypted Storage** — Sensitive preferences stored via EncryptedSharedPreferences (Android Keystore)
- **Optional REST Sync** — Configure an HTTPS endpoint to push data batches

## Requirements

- Android 10+ (API 29+)
- Usage Access permission (manual grant in Settings)
- Accessibility Service (manual enable in Settings)

## Data Schema

| Field | Description |
|-------|-------------|
| `timestamp` | Unix epoch milliseconds |
| `timestamp_readable` | Human-readable datetime |
| `app_package` | Package name of the foreground app |
| `screen_id` | Activity class name (from Accessibility) |
| `event_type` | `app_focus`, `tap`, `scroll`, `screen_change`, `self_report`, `reaction_test` |
| `dwell_ms` | Time spent on app (for app_focus) or reaction time (for reaction_test) |

## ACI Formula

The App Cognitive Inertia (ACI) score ranges from 0 to 10:

- **Dwell Concentration (40%)** — How concentrated usage is on few apps (max dwell / avg dwell)
- **Transition Rate (35%)** — App switches per minute (fewer = more inertia)
- **Interaction Density (25%)** — Taps + scrolls per minute (fewer = more passive)

Levels: Low (0-2.5) | Moderate (2.5-5) | High (5-7.5) | Very High (7.5-10)

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material3
- **Database**: Room (with migration support)
- **Security**: EncryptedSharedPreferences
- **Architecture**: MVVM with Repository pattern
- **Min SDK**: 29 (Android 10)
- **Target SDK**: 34

## Build

```bash
./gradlew assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`

## Privacy

No personal identifiers, login credentials, text content, photos, messages, contacts, or location data is collected. Only timing, event types, and package names are recorded.
