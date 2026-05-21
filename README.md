# Waqt (وقت)

Smart prayer-times and study planner for Android — built with Kotlin, Jetpack Compose, Room, and MVVM.

## Features (Stage 1)

- Prayer times from [Aladhan API](https://aladhan.com/prayer-api-api) with Room offline cache
- Live next-prayer countdown on Home
- Study planner with auto-generated slots between prayers
- Inline task quick-add (persisted in Room)
- Qibla compass (fixed Kaaba target + rotating needle)
- Settings: city, calculation method (Karachi / ISNA / MWL), notifications
- Prayer reminders via WorkManager (10 minutes before each prayer)

## Project docs (source of truth)

| Document | Path |
|----------|------|
| Build plan | [.github/skills/waqt-android/references/waqt_build_plan.html](.github/skills/waqt-android/references/waqt_build_plan.html) |
| Design system | [.github/skills/waqt-android/references/design.md](.github/skills/waqt-android/references/design.md) |
| Agent skill | [.github/skills/waqt-android/SKILL.md](.github/skills/waqt-android/SKILL.md) |

**When extending this app**, follow `SKILL.md` and `design.md`. Cursor agents should load the `waqt-android` skill automatically from `.github/skills/`.

## Tech stack

- Min SDK 26 · Package `com.example.waqt`
- Jetpack Compose Material 3 · Navigation Compose
- Room · Retrofit · DataStore · WorkManager · Play Services Location

## Build & run

```powershell
# Debug APK on connected device
.\gradlew.bat installDebug

# Unit tests
.\gradlew.bat test
```

Requires Android SDK and a device or emulator with API 26+.

## Architecture

```
UI (Compose) → ViewModel → Repository → Network / Room / DataStore / Sensors
```

See the build plan §03 for the full package layout (`ui/`, `viewmodel/`, `repository/`, `network/`, `database/`, `qibla/`, `settings/`, `worker/`).

## Calculation methods

Aladhan API `method` parameter (see `AladhanConstants.kt`):

| ID | Method |
|----|--------|
| 1 | Karachi (default) |
| 2 | ISNA |
| 3 | MWL |
