# Unlock Counter API

NestJS + PostgreSQL. **Production: [Render](https://render.com)** — see [../DEPLOY_RENDER.md](../DEPLOY_RENDER.md).

## Local development

```bash
cd backend
cp .env.example .env
docker compose up -d
npm install
npx prisma db push
npm run start:dev
```

API: `http://localhost:3000/v1`

## Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/v1/auth/register` | — | Register user `{ name, phone, androidDeviceId }` → JWT |
| POST | `/v1/unlocks/sync` | Bearer JWT | Push daily summaries |
| GET | `/v1/unlocks/summary` | Bearer JWT | Current user's history |
| GET | `/v1/users/me` | Bearer JWT | Profile |
| GET | `/v1/admin/users` | `X-Admin-Key` header | All testers + stats |
| GET | `/v1/admin/users/:id/history` | `X-Admin-Key` | User history |

## Database tables

- `users` — phone, name, kyc_status
- `devices` — per-user Android device
- `user_settings` — amount_per_unlock, weekly_cap, fund_choice
- `daily_unlock_summary` — per-user daily counts
- `unlock_events` — optional event-level log

## Production (Render)

Deploy via Blueprint using [`../render.yaml`](../render.yaml).

Live URL (default service name): `https://unlock-counter-api.onrender.com`

Health: `GET /v1/health`

**Swagger UI:** `GET /docs` — browse and test all endpoints (use **Authorize** for JWT or `X-Admin-Key`).

## Android connection

| Build | API URL |
|-------|---------|
| **debug** (Android Studio Run) | `http://10.0.2.2:3000/` |
| **tester** / **release** APK for testers | `https://unlock-counter-api.onrender.com/` |

Build staging APK: `.\gradlew.bat assembleStaging` (see DEPLOY_RENDER.md).

## Admin: view all testers

```bash
curl -H "X-Admin-Key: dev-admin-key-change-me" http://localhost:3000/v1/admin/users
```

Or open **All testers** in the app (people icon → cloud section).
