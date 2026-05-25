# Unlock Counter MVP

Android app + cloud API. **Deploy backend to Render** → build **tester APK** → send to testers.

## Quick start (you)

1. **Deploy backend:** follow [DEPLOY_RENDER.md](DEPLOY_RENDER.md)
2. **Paste `ADMIN_API_KEY`** from Render into `app/build.gradle.kts`
3. **Build staging APK:** `.\gradlew.bat assembleStaging`
4. **Share APK** with testers — they only install and register (name + phone)

## Local dev only

1. Grant Usage Access (permission popup)
2. Enter **name + mobile number** → registers on cloud
3. Use phone normally; tap refresh to sync
4. **People icon** → see all testers from cloud + this device

## Sync behavior

- Register → creates `users` + `devices` in Postgres
- Each refresh → upserts `daily_unlock_summary` locally + cloud
- Admin view → `GET /v1/admin/users` (key in app BuildConfig)

## Build APK

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`

## Validation

See [VALIDATION.md](VALIDATION.md).
