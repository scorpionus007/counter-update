# Unlock Counter — Validation Playbook

Use this checklist after installing the debug APK on real devices. Goal: confirm `KEYGUARD_HIDDEN` counting is accurate enough (±5% vs Digital Wellbeing) before building payment/backend features.

## Devices to test

| # | Device type | Example | Notes |
|---|-------------|---------|-------|
| 1 | Stock Android | Pixel / AVD API 34 | Baseline behavior |
| 2 | Xiaomi / Redmi | MIUI | Aggressive battery saver; whitelist app |
| 3 | Samsung or Realme | One UI / Realme UI | Check WorkManager after force-stop |

## 24-hour accuracy test

1. Install APK, grant Usage Access, complete backfill.
2. Note **our app** today count at end of day (before midnight).
3. Open **Settings > Digital Wellbeing** (or **Screen time**) and note "Times unlocked" / phone pickups for the same day.
4. Record in table below.

| Device | Our count | Wellbeing count | Delta % | Pass (±5%)? |
|--------|-----------|-----------------|---------|-------------|
|        |           |                 |         |             |

## Edge cases

| Test | Steps | Expected |
|------|-------|----------|
| App killed from Recents | Unlock 10×, reopen app, tap refresh | Count includes all 10 (UsageStats retained by OS) |
| Force-stop | Settings > Force stop, unlock 10×, reopen | May lag until refresh; document actual behavior |
| Reboot | Reboot, unlock 5×, open app | Backfill/refresh shows ≥5 for today |
| Midnight rollover | Test before 02:00 next day | Yesterday row appears; today starts near 0 |
| Permission revoked | Revoke Usage Access, reopen | Routes to permission screen |

## OEM battery settings

On Xiaomi/Realme/Samsung, if counts stall:

- Disable battery restrictions for Unlock Counter
- Allow autostart (Xiaomi)
- Lock app in recents (optional)

Document which setting fixed stalled counts.

## Failure criteria (pivot signals)

- **>15% error** vs Wellbeing on 2 of 3 devices → investigate `SCREEN_INTERACTIVE` fallback
- **Permission grant rate <50%** in informal user test → product UX risk for full app
- **Force-stop loses >30% of day's unlocks** with no recovery on reopen → need foreground service for production

## Sign-off

| Tester | Date | Stock OK | OEM OK | Ready for backend? |
|--------|------|----------|--------|-------------------|
|        |      | Y/N      | Y/N    | Y/N               |
