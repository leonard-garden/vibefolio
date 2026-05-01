# Vibefolio

> AI tool biến CV PDF → portfolio website cho dev/AI engineer mid-senior nhắm thị trường remote (US/EU/SG) và Upwork.

**Stack:** Spring Boot 3.3 (Java 21) + Next.js 15 (TypeScript). Monorepo với pnpm workspace + Maven.

---

## Quick Start

```bash
# Clone & install
git clone <repo>
cd vibefolio
pnpm install                        # Install FE deps cho tất cả workspace

# Backend (terminal 1)
cd backend
cp src/main/resources/application-local.yml.example src/main/resources/application-local.yml
# Edit application-local.yml: ANTHROPIC_API_KEY, DB_URL, R2_*, RESEND_API_KEY
./mvnw flyway:migrate
./mvnw spring-boot:run              # → http://localhost:8080 (Swagger UI: /swagger-ui.html)

# Frontend (terminal 2)
cd frontend
cp .env.example .env.local
# Edit .env.local: BE_API_URL=http://localhost:8080, BE_API_KEY=dev-key
pnpm gen:api                        # Generate TS types từ BE OpenAPI spec
pnpm dev                            # → http://localhost:3000
```

## Commands

### Backend (`backend/`)

See [`backend/CLAUDE.md`](backend/CLAUDE.md) for full command reference.

```bash
cd backend
./mvnw spring-boot:run              # Dev server
./mvnw test                         # Tests
./mvnw package -DskipTests          # Production JAR
./mvnw flyway:migrate               # DB migrations
./mvnw verify                       # Compile + lint + tests
```

### Frontend (`frontend/`)

See [`frontend/CLAUDE.md`](frontend/CLAUDE.md) for full command reference.

```bash
cd frontend
pnpm dev                            # Dev server
pnpm test                           # Vitest
pnpm build                          # Production build
pnpm lint                           # ESLint + Prettier
pnpm typecheck                      # tsc --noEmit
pnpm gen:api                        # Regenerate TS types từ BE
```

---

## Project Structure

```
backend/                  # Spring Boot BE — REST API, AI pipeline, DB, storage
frontend/                 # Next.js FE — public site, landing, preview, portfolio render
packages/api-types/       # Generated TS types từ OpenAPI (DO NOT edit manually)
docs/                     # Architecture, data models, decisions, design specs
scripts/                  # Dev helpers (gen-api-types.sh, dev.sh)
.claude/                  # Claude Code config (settings, hooks, rules)
```

---

## Architecture

Source of truth: [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md)

**Communication:** FE ↔ BE qua REST API có OpenAPI 3 contract (springdoc auto-gen). FE generate TS types từ spec → end-to-end type safety mà vẫn tách stack.

**Layer boundaries:**
- **FE pages** — routing, fetching qua TanStack Query, không business logic
- **FE components** — pure render, props in JSX out, không fetch
- **BE controllers** — thin, parse + delegate
- **BE services** — fat, business logic, không HTTP awareness
- **BE persistence** — JPA entities + repos, không business rules
- **BE ai** — Anthropic integration tách riêng, expose `generatePortfolio(pdfBytes)`

See [`docs/DATA-MODELS.md`](docs/DATA-MODELS.md) for entity relationships.
See [`docs/superpowers/specs/2026-04-30-vibefolio-design.md`](docs/superpowers/specs/2026-04-30-vibefolio-design.md) for full M1 design.

---

## Decisions

Architectural và micro-decisions logged trong [`docs/decisions/`](docs/decisions/).

Trước khi structural change, check decision log first — preserve reasoning từ session trước.

Major ADRs:
- [ADR-001](docs/decisions/ADR-001-spring-nextjs-split.md) — Tách BE Spring Boot + FE Next.js
- [ADR-002](docs/decisions/ADR-002-ai-self-managed-api.md) — Self-managed Anthropic API (không BYOK, không Claude Code subscription)
- [ADR-003](docs/decisions/ADR-003-path-based-routing-m1.md) — Path-based routing cho M1, subdomain ở M1.5

---

## Claude Code is ready

Project được configure cho Claude Code. Mở Claude Code và hỏi:

> "Summarize this project's architecture and security rules."

**What's set up:**
- Claude biết stack, commands, architecture, conventions, decision history qua `CLAUDE.md`
- `.claude/hooks/` — runtime hooks block dangerous commands trước khi execute
- `.claude/settings.json` — hard rules: no `.env` reads, no force-push, no destructive commands
- `.claude/rules/security.md` — full security checklist load on demand
- `docs/ARCHITECTURE.md` + `docs/DATA-MODELS.md` — structural source of truth
- `docs/decisions/` — ADRs để session sau không undo session trước

---

## Roadmap (high level)

| Mốc | Thời gian | Tính năng | Status |
|---|---|---|---|
| M1 | 5–7 tuần | Upload → AI → path URL, magic link, 1 template | 🟡 Design done, code chưa init |
| M1.5 | +2-3 tuần | Subdomain + custom domain + Stripe paid tier | ⬜ |
| M2 | +4-5 tuần | Edit dashboard + 2 template thêm | ⬜ |
| M3 | +6-8 tuần | Claude Agent SDK + vibe-code chat sửa portfolio | ⬜ |
| M4 | +4-5 tuần | Job-target mode + BYOK power mode | ⬜ |

Full roadmap: [docs/superpowers/specs/2026-04-30-vibefolio-design.md §15](docs/superpowers/specs/2026-04-30-vibefolio-design.md)
