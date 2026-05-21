---
name: Midnight Blue & Gold
colors:
  primary: '#0F172A'
  background: '#F8FAFC'
  surface: '#DBEAFE'
  highlight: '#D97706'
  error: '#B3261E'
  on-primary: '#F8FAFC'
  on-surface: '#0F172A'
  text-muted: '#475569'
  outline: '#CBD5E1'
typography:
  display-font: Hanken Grotesk
  body-font: Inter
  display-lg:
    fontFamily: Hanken Grotesk
    fontSize: 57px
    fontWeight: '400'
    lineHeight: 64px
  headline-lg:
    fontFamily: Hanken Grotesk
    fontSize: 32px
    fontWeight: '600'
    lineHeight: 40px
  headline-md:
    fontFamily: Hanken Grotesk
    fontSize: 28px
    fontWeight: '600'
    lineHeight: 36px
  title-lg:
    fontFamily: Hanken Grotesk
    fontSize: 22px
    fontWeight: '500'
    lineHeight: 28px
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  label-md:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
rounded:
  card: 16dp
  hero-card: 24dp
  pill: 16dp
spacing:
  grid: 8px
  screen-horizontal: 16px
  section-vertical: 24px
---

# Waqt Design System

Midnight Blue & Gold — calm navy foundations with warm gold accents. Implemented in `com.example.waqt.ui.theme` (`Color.kt`, `Type.kt`, `Theme.kt`).

## Palette

| Token | Hex | Usage |
|-------|-----|--------|
| Primary Navy | `#0F172A` | Headers, icons, hero card background, compass ring |
| Neutral Background | `#F8FAFC` | Screen background |
| Soft Ice Blue | `#DBEAFE` | Cards, surfaces, compass face |
| Warm Gold | `#D97706` | CTAs, active chips, Qibla needle, switches (checked) |
| Outline | `#CBD5E1` | 1px card borders |
| Text Muted | `#475569` | Secondary copy |
| Error | `#B3261E` | System failures only |

Compose mapping: `PrimaryNavy`, `SecondaryGold`, `NeutralBackground`, `SoftIceBlue`, `OutlineStroke`, `ErrorColor`.

## Typography

- **Display / headlines:** Hanken Grotesk (`res/font/hanken_grotesk_variable.ttf`)
- **Body / labels:** Inter (`res/font/inter_variable.ttf`)
- **Prayer times & countdown:** `fontFeatureSettings = "tnum"`; countdown also uses monospace for stability

Do not substitute other fonts without updating this file and `Type.kt`.

## Layout

- Base grid: **8px**
- Screen horizontal padding: **16dp**
- Vertical gap between major sections: **24dp**
- Bottom navigation on all main tabs (no drawer / hamburger)

## Components

### Cards
- Background: Soft Ice Blue or `surfaceVariant`
- Border: 1dp `#CBD5E1`
- Corner radius: 16dp (24dp for hero cards)

### Buttons
- Primary action: Warm Gold fill, navy or on-secondary text
- Text buttons: gold content color for secondary actions

### Home
- Hero `NextPrayerCard`: navy background, gold countdown
- `PrayerTimesRow`: horizontal pills, gold border when active
- Manual city fallback card when location denied

### Planner
- Vertical timeline: `surfaceVariant` line, circular anchors (gold when active slot)
- **Inline quick-add** `OutlinedTextField` — Enter to submit
- **STRICT:** No bottom sheets or dialogs for tasks

### Qibla
- **Fixed 🕋 at top center** of compass — Qibla target (does not rotate)
- **Gold needle** on Canvas rotates by `qiblaBearing - deviceAzimuth`
- When needle points up at Kaaba → user faces Makkah ("Facing Qibla" label)
- **Do not** fix N/E/S/W labels to screen top (misleading)
- Compass diameter ~280dp; navy ring, soft ice face

### Settings
- Section cards: Location, Prayer calculation, Notifications, Display
- Shared `CalculationMethodSelector` (Karachi / ISNA / MWL chips)
- Notification switch: gold when enabled

## Strict UX Rules

1. No red or amber for task priority — error red for system failures only
2. No bottom sheets or dialogs on Planner for task entry
3. Tabular numerals on all prayer times and countdowns
4. Request permissions in context; provide fallbacks (manual city, compass-unavailable message)
5. Local-first data: minimal logging of location/task content

## Screen Checklist (Stage 1)

| Screen | Must have |
|--------|-----------|
| Home | Countdown, prayer row, planner summary, permission fallbacks |
| Planner | Timeline, study slots, inline quick-add |
| Qibla | Fixed Kaaba, rotating needle, bearing info |
| Settings | City, method, notifications, display |

## References

- Build plan: [waqt_build_plan.html](./waqt_build_plan.html)
- Agent skill: [SKILL.md](../SKILL.md)
