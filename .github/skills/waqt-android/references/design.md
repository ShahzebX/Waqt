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
  outline: '#CBD5E1'
  text-muted: '#475569'
typography:
  display-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 57px
    fontWeight: '400'
    lineHeight: 64px
    letterSpacing: -0.25px
  headline-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 32px
    fontWeight: '600'
    lineHeight: 40px
  headline-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 28px
    fontWeight: '600'
    lineHeight: 36px
  title-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 22px
    fontWeight: '500'
    lineHeight: 28px
  title-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 16px
    fontWeight: '500'
    lineHeight: 24px
  body-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  label-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
  label-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 11px
    fontWeight: '500'
    lineHeight: 16px
rounded:
  sm: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  xs: 4px
  sm: 8px
  md: 16px
  lg: 24px
  xl: 32px
  xxl: 48px
---

## Theme

Midnight Blue & Gold emphasizes calm clarity with rich navy foundations and a warm gold accent. It should feel crisp, modern, and high-contrast without harshness.

## Palette Usage

- **Primary/Headers/Icons:** #0F172A (Rich Navy Blue)
- **Main Background:** #F8FAFC (Cool Off-White)
- **Core Widget/Card Tones:** #DBEAFE (Soft Ice Blue)
- **Highlight/Active/CTA:** #D97706 (Warm Gold)
- **Error:** #B3261E (Standard Red)

## Typography

- Use Plus Jakarta Sans for all Latin text.
- Countdown and prayer times must use tabular numerals to prevent jitter.

## Layout & Spacing

- Use an 8px base grid.
- Default horizontal margins: 16px on mobile.
- Separate major sections with 24px vertical spacing.

## Components

- **Cards:** Soft Ice Blue background with a 1px outline (#CBD5E1).
- **Buttons:** Warm Gold for primary actions, Navy for icons/text.
- **Timeline:** TertiaryIce vertical line with circular prayer anchors.

## Strict UX Rules

- STRICT: Do not use bottom sheets or dialogs for tasks. Use inline quick-add fields only.
- STRICT: Do not use red or amber for task priority. Error red is strictly for system failures.
- Countdown must use tabular numerals.
