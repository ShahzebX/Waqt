# Agent instructions — Waqt Android

This project uses the **waqt-android** skill. Read and follow it before any implementation work.

## Required reading (in order)

1. [.github/skills/waqt-android/SKILL.md](.github/skills/waqt-android/SKILL.md) — scope, architecture, testing, common mistakes
2. [.github/skills/waqt-android/references/design.md](.github/skills/waqt-android/references/design.md) — UI/UX rules
3. [.github/skills/waqt-android/references/waqt_build_plan.html](.github/skills/waqt-android/references/waqt_build_plan.html) — features, API, algorithms

## Quick facts

- Package: `com.example.waqt`
- Stage 1 only unless the user requests monetization (Stage 2)
- Aladhan methods: `1` Karachi, `2` ISNA, `3` MWL
- Planner: inline quick-add only — no bottom sheets
- Qibla: fixed 🕋 at top, gold needle rotates (`qiblaBearing - azimuth`)
- Settings: DataStore via `SettingsViewModel`

## Verify before finishing

```powershell
.\gradlew.bat test
.\gradlew.bat assembleDebug
```
