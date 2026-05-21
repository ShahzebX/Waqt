---
name: waqt-android
description: "Build the Waqt Android app with Jetpack Compose, Room, MVVM, Aladhan API, prayer times, planner, Qibla compass, and settings, following the project build plan and design system."
argument-hint: "Specify the feature or screen to implement (e.g., HomeScreen UI, Room entities, Aladhan API integration)."
---

# Waqt Android App Builder

Build the Waqt (وقت) prayer-times and study-planner app using Jetpack Compose, Room, and MVVM. The **build plan** defines scope; **`design.md`** defines visual and UX rules; this skill defines how agents implement and verify work.

## When to Use

- Stage 1 features: prayer times, countdown, planner, Qibla, settings, notifications
- Scaffolding MVVM, navigation, API/Room/DataStore wiring
- Applying the Midnight Blue & Gold design system
- Fixing permission, offline, or sensor fallback behavior

**Stage 2** (AdMob, billing, widget, Play Store) — only when the user explicitly requests monetization or publishing work.

## Required References

| Document | Path | Purpose |
|----------|------|---------|
| Build plan | [waqt_build_plan.html](./references/waqt_build_plan.html) | Features, architecture, API, algorithms, sprint |
| Design system | [design.md](./references/design.md) | Colors, typography, screen UX, strict rules |

Read both before changing UI or architecture.

## Project Facts (canonical)

- **Package:** `com.example.waqt`
- **Min SDK:** 26
- **Aladhan base URL:** `https://api.aladhan.com/v1/`
- **Calculation methods (Aladhan `method` param):** `1` = Karachi (default), `2` = ISNA, `3` = MWL — see `PrayerRepository` constants
- **Default city/country:** Karachi, PK

## Procedure

### 1. Scope
Default to **Stage 1** only unless the user asks for Stage 2.

### 2. Architecture
Follow package layout in build plan §03:

`ui/`, `viewmodel/` (+ `*ViewModelFactory`), `repository/`, `network/`, `database/`, `location/`, `settings/`, `qibla/`, `worker/`, `model/`, `ui/components/`

**Data flow:** UI → ViewModel → Repository → (Retrofit | Room | DataStore | sensors). No network or DB calls inside `@Composable` bodies.

### 3. Network + caching
- Retrofit `AladhanApi`: `timings`, `timingsByCity`
- Parse `PrayerResponse` including `date.gregorian.date` for cache keys
- `PrayerRepository`: network first → `insertPrayers` → on failure return today's Room cache

### 4. Room
- `PrayerEntity`, `TaskEntity` per build plan
- `TaskRepository` for persisted tasks when implementing full task manager

### 5. Planner
- `generateStudySlots()` gap rules: &lt;45 skip, 45–89 short study, 90–179 study, ≥180 study + break
- **STRICT:** Inline quick-add on Planner — **no** bottom sheets or dialogs

### 6. Screens

| Screen | Requirements |
|--------|----------------|
| **Home** | Hero countdown (1s tick), prayer lazy row, planner summary, GPS/city fallback, notification prompt |
| **Planner** | Vertical timeline, study slots from cached prayers, inline task field |
| **Qibla** | Fixed 🕋 at top; gold Canvas needle rotates (`qiblaBearing - azimuth`); portrait sensor remap; city/GPS fallback — see `design.md` |
| **Settings** | City save + API refresh, GPS refresh, method chips, notification toggle, display section — `SettingsViewModel` + DataStore |

### 7. Design (must follow `design.md`)
- Colors: `#0F172A`, `#F8FAFC`, `#DBEAFE`, `#D97706`, `#CBD5E1`
- Fonts: **Hanken Grotesk** (headlines), **Inter** (body) — not Plus Jakarta unless design is updated
- Tabular numerals (`tnum`) for prayer times and countdown
- 16dp horizontal margins, 24dp between sections
- Bottom `NavigationBar` only — no hamburger menu

### 8. Permissions + privacy
- Manifest: INTERNET, location, POST_NOTIFICATIONS, RECEIVE_BOOT_COMPLETED; optional compass
- Runtime prompts with rationale; fallbacks: manual city (Home/Settings), Qibla city map, compass-unavailable copy
- Local-first; no logging of precise location or task text

### 9. Testing
Update when changing logic:

**Unit:** planner slots, Prayer/Settings/Qibla ViewModels, API parsing, repository cache fallback

**Compose UI:** Home countdown, Planner quick-add, Settings toggles, permission-denied states

### 10. Definition of done
- `./gradlew test` passes
- Build succeeds (`assembleDebug`)
- New logic has tests
- Offline, permission deny, and missing compass paths verified on device or via tests

## Output Expectations

- Compiles at API 26+
- Matches `design.md` and build plan for the screen touched
- MVVM preserved; DataStore settings shared across Home/Settings/Qibla
- Stage 2 code not added unless requested

## Common Mistakes (avoid)

1. Using Aladhan `method=18` for Karachi — use **`1`** per `PrayerRepository`
2. Fixed N/E/S/W on Qibla screen top — use **fixed Kaaba + rotating needle**
3. Bottom sheet for planner tasks — use **inline field**
4. Wrong package name `com.waqt.planner` in new code — use **`com.example.waqt`**
5. Rotating the Kaaba emoji instead of the needle
