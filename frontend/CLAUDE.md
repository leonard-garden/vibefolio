# Frontend — Vibefolio Web

Next.js 15 (App Router) public site. Nhiệm vụ: landing page + form upload CV → gọi BE qua proxy → render preview → public portfolio render → magic link confirmation flow → re-upload flow.

> Module CLAUDE.md này load khi Claude Code làm việc trong `frontend/`. Root context xem [`../CLAUDE.md`](../CLAUDE.md).

## Stack

- **Next.js 15 (App Router)** + **TypeScript 5**
- **Tailwind CSS 3** + **shadcn/ui** primitives
- **TanStack Query v5** cho fetch BE + cache + retry
- **Zod 3** cho runtime validate response (defensive — phòng FE/BE drift)
- **OpenAPI TypeScript client** generate qua `openapi-typescript` từ BE spec
- **`@vercel/og`** cho OG image động (Edge runtime)
- **Cloudflare Turnstile** cho captcha (`@marsidev/react-turnstile`)
- **Framer Motion** (lightweight) cho animation
- **next-themes** cho dark/light toggle
- **pnpm** package manager
- **ESLint + Prettier + Vitest + Playwright** (test stack)

## Commands

```bash
pnpm install              # Install deps (dùng pnpm, KHÔNG npm/yarn)
pnpm dev                  # Dev server → http://localhost:3000
pnpm test                 # Vitest unit + component tests
pnpm test:e2e             # Playwright e2e
pnpm build                # Production build (chạy gen:api trước)
pnpm lint                 # ESLint + Prettier --write
pnpm typecheck            # tsc --noEmit
pnpm gen:api              # Pull OpenAPI spec từ BE → generate TS types vào src/lib/api/schemas.gen.ts
```

## Verification (chạy sau every change)

1. `pnpm typecheck` — TS errors fix trước
2. `pnpm lint` — ESLint + Prettier auto-fix
3. `pnpm test` — Vitest tests pass
4. `pnpm build` — production build clean
5. Nếu BE schema đổi: `pnpm gen:api` trước bước 1 (BE phải đang chạy hoặc có spec build sẵn)

## Project Structure

```
frontend/
├── src/
│   ├── app/                              # App Router routes
│   │   ├── (landing)/
│   │   │   ├── page.tsx                  # Landing page
│   │   │   └── layout.tsx
│   │   ├── [username]/
│   │   │   ├── page.tsx                  # Public portfolio (ISR, revalidate=3600)
│   │   │   ├── opengraph-image.tsx       # @vercel/og dynamic OG image
│   │   │   └── not-found.tsx
│   │   ├── preview/
│   │   │   └── [tempId]/page.tsx         # Preview trước claim
│   │   ├── update/
│   │   │   ├── page.tsx                  # Form nhập email request magic link
│   │   │   └── [token]/page.tsx          # Re-upload form
│   │   ├── api/
│   │   │   ├── og/[username]/route.ts    # Edge runtime OG image
│   │   │   ├── proxy/[...path]/route.ts  # GIẤU BE_API_KEY khỏi browser
│   │   │   └── magic-link/[token]/route.ts # Confirm flow handler
│   │   ├── layout.tsx                    # Root layout với providers
│   │   └── globals.css                   # Tailwind base + custom variables
│   ├── components/
│   │   ├── portfolio/                    # PURE renderer — props in, JSX out
│   │   │   ├── Portfolio.tsx             # Composition root
│   │   │   ├── Hero.tsx
│   │   │   ├── Specialties.tsx
│   │   │   ├── Projects.tsx
│   │   │   ├── Experience.tsx
│   │   │   ├── Skills.tsx
│   │   │   ├── Credentials.tsx
│   │   │   ├── Sidebar.tsx
│   │   │   └── Footer.tsx
│   │   ├── landing/
│   │   │   ├── Hero.tsx
│   │   │   ├── GenerateForm.tsx          # Form upload — uses TanStack Query mutation
│   │   │   └── Features.tsx
│   │   ├── preview/
│   │   │   └── PreviewBanner.tsx         # "Check your email to claim"
│   │   └── ui/                           # shadcn primitives
│   │       ├── button.tsx
│   │       ├── input.tsx
│   │       ├── form.tsx
│   │       └── ...
│   ├── lib/
│   │   ├── api/
│   │   │   ├── client.ts                 # OpenAPI client wrapper
│   │   │   ├── schemas.gen.ts            # GENERATED — KHÔNG edit thủ công
│   │   │   ├── schemas-runtime.ts        # Zod mirror cho defensive validate
│   │   │   ├── proxy.ts                  # Server-side fetch helper qua BE
│   │   │   └── queries.ts                # TanStack Query hooks (usePortfolio, ...)
│   │   ├── env.ts                        # Validated env vars (server vs client split)
│   │   └── utils/
│   │       ├── cn.ts                     # clsx + tailwind-merge
│   │       └── format.ts
│   └── styles/
│       └── fonts.ts                      # Inter + JetBrains Mono setup
├── public/
│   ├── favicon.ico
│   └── og-default.png
├── tests/
│   ├── unit/                             # Component tests Vitest + React Testing Library
│   └── e2e/                              # Playwright
├── next.config.ts
├── tailwind.config.ts
├── tsconfig.json
├── vitest.config.ts
├── playwright.config.ts
├── eslint.config.mjs
├── package.json
└── .env.example
```

## Conventions

### TypeScript style
- **Indent**: 2 spaces
- **NO `any`** — dùng `unknown` + narrow, hoặc generic. Nếu thật sự cần, comment giải thích
- **NO `as` cast** trừ khi narrow type Zod-validated
- **Default export** cho pages (`page.tsx`, `layout.tsx`) — Next.js convention
- **Named export** cho components, hooks, utils
- **Naming**: PascalCase components/types/interfaces, camelCase functions/vars, UPPER_SNAKE constants, kebab-case file names (trừ pages của App Router)

### Next.js patterns
- **Server Component default**, opt-in Client với `"use client"` chỉ khi cần (state, effect, browser API)
- **Form submit**: dùng React Hook Form + shadcn Form + Zod resolver
- **Fetching client-side**: TanStack Query (useQuery / useMutation) — KHÔNG `fetch()` trần
- **Fetching server-side**: dùng `proxy.ts` helper (gọi BE qua `BE_API_URL`, attach `X-API-Key`)
- **Routing**: prefer Server Component để Next.js cache; Client chỉ khi cần interactivity
- **Image**: `next/image` cho mọi raster image (avatar, OG)
- **Font**: `next/font/google` cho Inter + JetBrains Mono — preload + auto-subset

### Component rules (KHÔNG VI PHẠM)
- **`components/portfolio/`** là **PURE** — props in, JSX out. KHÔNG `useQuery`, KHÔNG `useState`, KHÔNG `useEffect`, KHÔNG fetch. Renderer 100%.
- **Fetching** chỉ trong page-level Component (Server Component fetch trực tiếp, hoặc Client Component dùng TanStack Query)
- **State** chỉ trong leaf Client Component cần interactivity (toggle, form input)
- **Side effect** (analytics, scroll listener) tách thành custom hook trong `lib/utils/`
- **shadcn primitives** trong `components/ui/` — KHÔNG modify trực tiếp file shadcn (regen sẽ overwrite); extend bằng wrapper component

### API client rules
- **NEVER** viết types thủ công cho API response — chạy `pnpm gen:api`
- **ALWAYS** validate response qua Zod (`schemas-runtime.ts`) ở boundary nếu data sẽ render — defensive layer
- **NEVER** gọi BE từ Client Component trực tiếp (`fetch('https://api.vibefolio.com/...')`) — luôn qua `/api/proxy/[...path]` để giấu API key
- **TanStack Query keys** convention: `['portfolio', username]`, `['preview', tempId]` — array shape consistent

### Testing
- **Component test** với Vitest + React Testing Library — render component, assert DOM
- **Mock TanStack Query** dùng `QueryClient` test wrapper
- **E2E test** với Playwright — happy path: landing → upload → preview → claim → portfolio public
- **Coverage target**: components 70%+, utils 90%+
- **Naming**: `describe('ComponentName', () => it('should ... when ...', ...))`

## Don't

- Don't fetch API trong leaf Client Component — đẩy lên page-level hoặc dùng TanStack Query hook
- Don't gọi BE trực tiếp từ browser — luôn qua `/api/proxy/[...path]` Route Handler
- Don't expose `BE_API_KEY` qua `NEXT_PUBLIC_*` — chỉ env server-side
- Don't dùng `localStorage` để store session — magic link là stateless auth
- Don't modify `src/lib/api/schemas.gen.ts` — generated file, chạy `pnpm gen:api` để update
- Don't dùng `any` — thay bằng `unknown` + Zod narrow
- Don't dùng `<img>` raw — dùng `next/image`
- Don't dùng `useEffect` cho fetching — TanStack Query
- Don't dùng inline style — Tailwind utility classes (trừ dynamic value cần CSS variable)
- Don't modify `components/ui/` shadcn primitives — extend bằng wrapper
- Don't fetch trong `components/portfolio/*` — pure renderer, props only
- Don't dùng `npm` hoặc `yarn` — pnpm only (lockfile consistency)

## Security top-line

> Full checklist: [`../.claude/rules/security.md`](../.claude/rules/security.md)

- **NEVER** commit `.env.local`, `.env.production` — gitignored
- **NEVER** đưa `BE_API_KEY` ra browser (`NEXT_PUBLIC_*` là public, server-side only cho secret)
- **ALWAYS** Zod validate input form (React Hook Form + zodResolver)
- **ALWAYS** Zod validate response từ BE trước khi render (defensive)
- **ALWAYS** sanitize Markdown nếu cho phép trong portfolio fields (dùng `react-markdown` + safe schema, KHÔNG `dangerouslySetInnerHTML`)
- **ALWAYS** Cloudflare Turnstile token verify ở BE (FE chỉ embed widget, BE check token với Cloudflare API)

## Env variables

```
# .env.local (gitignored)

# Server-side only (KHÔNG có NEXT_PUBLIC_ prefix)
BE_API_URL=http://localhost:8080         # Dev: localhost:8080. Prod: https://api.vibefolio.com
BE_API_KEY=dev-key-change-in-prod        # Match với backend X-API-Key
RESEND_WEBHOOK_SECRET=...                # Optional, nếu dùng webhook

# Client-side OK (NEXT_PUBLIC_* expose trong bundle)
NEXT_PUBLIC_TURNSTILE_SITE_KEY=0x4...    # Cloudflare Turnstile public site key
NEXT_PUBLIC_APP_URL=http://localhost:3000
```

Validate env qua `src/lib/env.ts` (Zod schema, fail fast at startup).

## Current State

- **Status**: chưa init code (chỉ design + docs)
- **First task**: `pnpm create next-app@latest .` (App Router + TS + Tailwind), setup shadcn (`pnpm dlx shadcn@latest init`), tạo `lib/env.ts`, smoke test `pnpm dev`
- **Reference**: [Design Doc M1 §9](../docs/superpowers/specs/2026-04-30-vibefolio-design.md) cho template + [§4](../docs/superpowers/specs/2026-04-30-vibefolio-design.md) cho user flow

## SOT pointers

| | |
|---|---|
| Architecture | [`../docs/ARCHITECTURE.md`](../docs/ARCHITECTURE.md) |
| Data models (Portfolio JSON shape) | [`../docs/DATA-MODELS.md`](../docs/DATA-MODELS.md) |
| ADRs | [`../docs/decisions/`](../docs/decisions/) |
| Design doc M1 | [`../docs/superpowers/specs/2026-04-30-vibefolio-design.md`](../docs/superpowers/specs/2026-04-30-vibefolio-design.md) |
| Backend module | [`../backend/CLAUDE.md`](../backend/CLAUDE.md) |
