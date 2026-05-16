# Waqt (وقت) — Smart Prayer & Study Planner
## UI / UX Design Specification (Optimized)
**Platform:** Android 10.0+ (API 26) | **Framework:** Kotlin + Jetpack Compose (Material Design 3)

---

### 1. Brand & Psychological Core
The app is named Waqt (وقت). It is a daily planner anchored by prayer times.
* **Calm & Rooted:** The app must feel like a gentle reminder, never an alarming task-master. Prayer is a pause.
* **Cognitive Separation:** Spiritual elements (prayers, countdowns) are separated visually and interaction-wise from stressful elements (task lists, studying). 
* **Color Psychology:** We strictly avoid Red/Amber for standard tasks. Red induces a stress response and breaks trust in a calming app.

---

### 2. Color Palette (Material 3)
Generated using Material Theme Builder (Seed: #1B6B4A). 
* **Primary Green (#1B6B4A):** Active states, primary buttons, high-priority tasks, active prayer highlight.
* **Primary Light (#EAF3ED) / Mid (#C2DFD0):** Card backgrounds, dividers, progress bars.
* **Dark Green (#0F3D2B):** Used exclusively for the Home & Qibla screen backgrounds to create a focused, immersive feel.
* **Background (#F7F5F0) & Surface (#FFFFFF):** Used for Planner and Settings screens to differentiate "work/utility" modes from "spiritual" modes.
* **Error (#C0392B):** Strictly reserved for system failures (e.g., offline/network errors). Never used for overdue tasks.

---

### 3. Typography
* **Arabic/Urdu:** Noto Naskh Arabic (Regular/Bold).
* **Latin Text:** Google Sans or system Roboto.
* **Numerals:** Tabular numerals (monospace) MUST be used for the countdown timer to prevent layout jitter as seconds tick.

---

### 4. Screen Specifications

#### 4.1 Home Screen (The Spiritual Hub)
**Background:** `#0F3D2B` (Dark Green)
**Psychology:** Minimalist. Do not show individual tasks here to prevent the Zeigarnik effect (anxiety over unfinished tasks).
* **Date Header:** Gregorian date + Hijri date (muted white).
* **Hero Card (Next Prayer):** Center of the screen. Frosted glass effect (10% white opacity). Shows Prayer Name (headlineLarge) and live Countdown (displayLarge, tabular numerals).
* **Prayer Row:** A horizontal scrolling row (LazyRow) of all 5 prayers. The active/next prayer has a Primary Green border.
* **Planner Summary:** A small, collapsed summary card at the bottom. Text: *"3 study blocks remaining today"* with a simple "View Planner" text button. 

#### 4.2 Planner Screen (The Utility Zone)
**Background:** `#F7F5F0` (Light)
**Psychology:** High-efficiency data entry. Zero friction. 
* **Date Strip:** Horizontal scrolling row of days.
* **Vertical Timeline:** A continuous green line down the left side.
* **Prayer Anchors:** Green dots on the timeline with the prayer time (e.g., "12:15 Dhuhr"). 
* **Study Blocks:** Surface-colored cards sitting in the gaps between prayers. 
* **Inline Quick-Add (Zero Friction):** NO bottom sheets for adding tasks. Inside every Study Block card, place an `OutlinedTextField` that says *"Press Enter to add task"*. Users type and hit submit on their keyboard to instantly append to the list.
* **Task Priority UI:** Do not use colors. Use visual weight. High Priority = Filled Green Chip. Medium = Outlined Green Chip. Low = No border, muted text.

#### 4.3 Qibla Compass Screen
**Background:** `#0F3D2B` (Dark Green)
* **The Compass:** A 280dp Canvas-drawn circle. The needle rotates smoothly using `animateFloatAsState` tied to the device sensor.
* **Data Readout:** Text displaying the exact bearing in degrees.
* **Calibration Affordance:** If sensor accuracy drops to 'Low', display a looping Lottie animation of a phone moving in a figure-8 motion alongside the text "Calibrate Compass". Do not rely purely on text instructions.

#### 4.4 Settings Screen
**Background:** `#F7F5F0` (Light)
* Standard list layout divided into sections: Location (Auto GPS vs Manual City), Calculation Method (MWL, ISNA, Karachi), Notifications, and Display.

---

### 5. Compose Theme Implementation (Direct Copy)

```kotlin
// ui/theme/Color.kt
val PrimaryGreen      = Color(0xFF1B6B4A)
val PrimaryGreenLight = Color(0xFFEAF3ED)
val DarkGreen         = Color(0xFF0F3D2B)
val AppBackground     = Color(0xFFF7F5F0)
val SurfaceColor      = Color(0xFFFFFFFF)
val Surface2Color     = Color(0xFFF0EDE8)
val BorderColor       = Color(0xFFE2DDD6)
val TextPrimary       = Color(0xFF1A1714)
val TextMuted         = Color(0xFF7A7470)
val ErrorColor        = Color(0xFFC0392B)

// ui/theme/Theme.kt
private val WaqtColorScheme = lightColorScheme(
    primary          = PrimaryGreen,
    onPrimary        = Color.White,
    primaryContainer = PrimaryGreenLight,
    background       = AppBackground,
    surface          = SurfaceColor,
    error            = ErrorColor,
    onBackground     = TextPrimary,
    onSurface        = TextPrimary,
)

---
### 6. Design Do's & Don'ts for the AI Agent

- DO use `LaunchedEffect` for the countdown timer ticking.
- DO use `androidx.compose.material.icons.outlined` for all UI icons.
- DON'T use side-drawers or hamburger menus; use a fixed `BottomNavigationBar`.
- DON'T generate red error text for incomplete tasks.
- DON'T build separate Add Task screens or modals; everything must go through the inline Quick-Add fields on the Planner screen.