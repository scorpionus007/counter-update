# Deploy to Render

One-time setup. After this, testers only install the APK — data goes to your Render Postgres automatically.

## What gets deployed

| Render resource | Purpose |
|-----------------|---------|
| **PostgreSQL** (`unlock-counter-db`) | Your database (users, daily unlocks) |
| **Web Service** (`unlock-counter-api`) | NestJS API at `https://unlock-counter-api.onrender.com` |

Defined in [`render.yaml`](render.yaml) at repo root.

---

## Step 1 — Push code to GitHub

```bash
cd fintech
git init
git add .
git commit -m "Unlock counter MVP with Render config"
git remote add origin https://github.com/YOUR_USER/fintech.git
git push -u origin main
```

---

## Step 2 — Deploy on Render

1. Go to [https://dashboard.render.com](https://dashboard.render.com) and sign up / log in.
2. Click **New +** → **Blueprint**.
3. Connect your GitHub repo (`fintech`).
4. Render reads `render.yaml` and creates:
   - PostgreSQL database
   - Web service `unlock-counter-api`
5. Click **Apply**. Wait ~5–10 minutes for first deploy.

When done, your API URL is:

**`https://unlock-counter-api.onrender.com`**

Test it:

```bash
curl https://unlock-counter-api.onrender.com/v1/health
# → {"status":"ok","service":"unlock-counter-api"}
```

---

## Step 3 — Copy secrets from Render

1. Open **unlock-counter-api** → **Environment**.
2. Copy **`ADMIN_API_KEY`** (auto-generated).
3. Open `app/build.gradle.kts` → `release` / `tester` block.
4. Replace `PASTE_RENDER_ADMIN_API_KEY_HERE` with that value.

If you renamed the service on Render, update `API_BASE_URL` to match your URL.

---

## Step 4 — Build APK for testers

```powershell
cd C:\Users\Skyworth\Desktop\fintech
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat assembleStaging
```

APK path:

`app/build/outputs/apk/staging/app-staging.apk`

Send **this one APK** to all testers (WhatsApp, Drive, etc.).

---

## Step 5 — Tester experience (no setup for them)

1. Install `app-staging.apk`
2. Allow Usage Access
3. Enter **name + phone**
4. Use phone → tap refresh occasionally

Data is stored in **Render PostgreSQL**. You view all testers:

- **In app:** Home → people icon → **Cloud (all devices)**
- **Terminal:**
  ```bash
  curl -H "X-Admin-Key: YOUR_ADMIN_KEY" https://unlock-counter-api.onrender.com/v1/admin/users
  ```

---

## Free tier notes

- **Cold starts:** First request after ~15 min idle may take 30–60 seconds (Render free tier spins down).
- **Database:** Free Postgres expires after 90 days on Render — upgrade or export before then for production.
- **HTTPS:** Render provides SSL automatically — testers use `https://` (already in app).

---

## Local dev (unchanged)

```bash
cd backend
docker compose up -d
npm run start:dev
```

Android **debug** build still points to `http://10.0.2.2:3000/` (emulator).

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| App says "Cannot reach server" | Wait for cold start; open API health URL in browser first |
| Register fails | Check Render logs → **unlock-counter-api** → **Logs** |
| Empty cloud users list | Wrong `ADMIN_API_KEY` in `build.gradle.kts` — must match Render env |
| DB connection error on deploy | Re-deploy; ensure `DATABASE_URL` is linked to DB in Render env |
