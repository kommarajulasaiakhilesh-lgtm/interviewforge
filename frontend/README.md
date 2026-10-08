# InterviewForge — Frontend

The web app for InterviewForge: practice sessions, company and role
preparation, readiness insights, and written mock interviews.

It talks to the InterviewForge Spring Boot API over HTTP. Nothing here stores
data — the API owns accounts, questions, progress, and grading.

## Stack

- TanStack Start (React 19 + TypeScript)
- Tailwind CSS v4
- TanStack Router + Query
- A hand-written API client in `src/lib/api/` (no generated SDK)

## Run it locally

Needs Node.js 20 or newer.

```sh
npm install
cp .env.example .env
npm run dev
```

`npm run dev` prints the address it is listening on — open that in a browser.

## Pointing it at the backend

`VITE_API_BASE_URL` in `.env` decides where API calls go. Every request is
sent to `<VITE_API_BASE_URL>/api/v1/...`, so give the host only — no trailing
slash and no `/api/v1`.

```sh
# .env
VITE_API_BASE_URL=http://localhost:8080
```

Two things to check if pages show a connection error:

1. **Port clash.** If the backend already uses the port the dev server picked,
   change one of them.
2. **CORS.** The backend must allow requests from the frontend's origin. In
   Spring Boot that is `@CrossOrigin` or a `WebMvcConfigurer` mapping the
   dev origin.

For local development, use the exact frontend origin `http://localhost:5173`; the backend defaults to allowing it. If you change the Vite port or hostname, update `APP_CORS_ALLOWED_ORIGINS` to match. The local frontend `.env` should point to `http://localhost:8080`.

## Where things live

| Path | Purpose |
| --- | --- |
| `src/lib/api/client.ts` | fetch wrapper — base URL, JSON, bearer token, error messages |
| `src/lib/api/endpoints.ts` | one function per documented API route |
| `src/lib/api/types.ts` | typed request and response models |
| `src/lib/session.ts` | signed-in user and token, kept in `sessionStorage` |
| `src/lib/prefs.ts` | the last role you opened, remembered on this device |
| `src/components/kit.tsx` | shared buttons, cards, badges, meters |
| `src/routes/` | one file per page |

## Commands

| Command | Does |
| --- | --- |
| `npm run dev` | start the dev server |
| `npm run build` | production build |
| `npm run preview` | serve the built app |
| `npm run test` | run the tests |
| `npm run lint` | ESLint |
| `npm run format` | Prettier |

## Notes

- The session token lives in `sessionStorage`, so signing in lasts for the
  browser tab only. Passwords are never stored.
- Correct answers and explanations appear only after you submit, exactly as
  the API returns them.
- The readiness score summarises questions you have actually answered. It is a
  study guide, not a hiring prediction or a guarantee.
