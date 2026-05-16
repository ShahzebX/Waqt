---
name: waqt-android
description: "Build the Waqt Android app with Jetpack Compose, Room, MVVM, Aladhan API, prayer times, planner, Qibla compass, and settings, following the project build plan and design system."
argument-hint: "Specify the feature or screen to implement (e.g., HomeScreen UI, Room entities, Aladhan API integration)."
---

# Waqt Android App Builder

Build the Waqt (وقت) prayer-times and study-planner app using Jetpack Compose, Room, and MVVM, aligned with the project's build plan and UI/UX rules.

## When to Use

- Implementing core Waqt features: prayer times, countdown, planner, Qibla, settings, notifications.
- Scaffolding MVVM architecture with Compose navigation.
- Wiring Aladhan API + Room caching + DataStore settings.
- Applying the Waqt design system and screen specifications.

## Required References

- Build plan: [waqt_build_plan.html](./references/waqt_build_plan.html)
- Design system: [design.md](./references/design.md)

## Procedure

1. **Align scope to Stage 1** unless the user explicitly asks for Stage 2 monetization features.
2. **Follow the architecture layout** from the build plan (ui/, viewmodel/, repository/, network/, database/, worker/, model/).
3. **Implement data flow**: UI → ViewModel → Repository → Network/Room. Avoid side effects in composables.
4. **Network + caching**:
   - Use Retrofit for Aladhan API (base URL and methods per build plan).
   - Cache prayer times in Room and fall back to cached data on network failure.
5. **Room schema**:
   - Use PrayerEntity and TaskEntity fields from the build plan.
   - Expose DAOs and WaqtDatabase singleton.
6. **Planner logic**:
   - Generate study slots from prayer gaps using the build plan algorithm.
   - Use inline quick-add fields (no bottom sheets) in Planner screen.
7. **Compose UI**:
   - Home: Hero card, lazy row, summary.
   - Planner: Vertical timeline.
   - Qibla: Canvas compass.
   - Settings: location, calculation method, notifications, display.
8. **Design constraints (must follow)**:
   - Use the "Serene Alignment" color palette from `design.md`.
   - STRICT: Do not use red/amber for tasks; error red is strictly for system failures.
   - STRICT: Do not use bottom sheets or dialogs for the Planner. You MUST use inline quick-add fields.
   - Tabular numerals must be used for the countdown.
   - Use BottomNavigationBar; no hamburger menus.
9. **Permissions + privacy handling**:
   - Implement only required permissions for selected features (INTERNET, ACCESS_FINE_LOCATION/ACCESS_COARSE_LOCATION, POST_NOTIFICATIONS for Android 13+, RECEIVE_BOOT_COMPLETED for notification restore).
   - Request runtime permissions in context with clear rationale; when denied, keep functional fallbacks (e.g., manual city input when location access is denied).
   - Declare compass sensor capability as optional and handle unsupported/low-accuracy sensor states gracefully in UI.
   - Keep user data minimal and local-first: do not log precise location/task content unnecessarily and do not send personal data beyond what the prayer-time API requires.
10. **Testing + verification requirements**:

- Add/update unit tests for planner slot generation, ViewModel state transitions, API parsing, and repository cache fallback behavior.
- Add/update Compose UI tests for critical flows (Home countdown render, Planner inline quick-add, Settings toggles, and denied-permission fallback states).
- Validate integration paths for Room read/write, offline fallback, and notification scheduling trigger behavior.
- For permission-dependent features, verify both grant and deny paths, first-run prompts, and post-denial recovery from Settings.

11. **Definition of done before output**:

- Build succeeds and relevant existing test suites pass.
- New or changed logic has matching test coverage.
- Offline mode, denied permissions, and missing compass sensor behavior are explicitly verified.

## Output Expectations

- Code compiles with API 26+.
- UI matches the Waqt design system and screen specs.
- MVVM separation is preserved.
- Offline behavior works via Room cache.
- Permission-sensitive features include graceful fallback UX.
- Privacy handling follows data-minimization and local-first storage rules.
- Testing and verification cover changed logic and critical user flows.
