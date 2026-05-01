# Architecture — Vibefolio

> Last verified: 2026-04-30
> Source of truth for full design: [`superpowers/specs/2026-04-30-vibefolio-design.md`](superpowers/specs/2026-04-30-vibefolio-design.md)

## System Overview

Vibefolio là web tool 2-tier có FE và BE tách rời, deploy riêng:
- **FE** (Next.js 15 + TypeScript) — public site, landing, form generate, portfolio render. Deploy Vercel Hobby.
- **BE** (Spring Boot 3.3 + Java 21) — REST API, AI pipeline gọi Anthropic, persistence, email, storage. Deploy Railway.

Communication: REST API qua HTTPS với OpenAPI 3 contract (springdoc auto-gen). FE generate TS types từ spec → end-to-end type safety mà vẫn cho phép tách stack.

External services: Anthropic API (Claude Haiku 4.5 / Sonnet 4.6), Cloudflare R2 (PDF storage), Neon Postgres (DB), Resend (email).

## System Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                         Browser                              │
└──────────────────────────┬──────────────────────────────────┘
                           │ HTTPS
┌──────────────────────────▼──────────────────────────────────┐
│            Next.js FE (Vercel — vibefolio.com)               │
│  Pages + components + Edge functions                          │
│  /api/proxy/[...path] — giấu BE_API_KEY khỏi browser         │
└───────────┬─────────────────────────────────────────────────┘
            │ HTTPS + X-API-Key
┌───────────▼─────────────────────────────────────────────────┐
│         Spring Boot BE (Railway — api.vibefolio.com)         │
│  REST API → Service → AI / Persistence / Storage / Email     │
└────────┬─────────────────────┬──────────────────┬──────────┘
         │                     │                  │
         ▼                     ▼                  ▼
┌────────────┐         ┌─────────────┐    ┌──────────────┐
│ Neon       │         │ Cloudflare  │    │ Anthropic    │
│ Postgres   │         │ R2 (PDF)    │    │ API (Claude) │
└────────────┘         └─────────────┘    └──────────────┘
                                          ┌──────────────┐
                                          │ Resend       │
                                          │ (email)      │
                                          └──────────────┘
```

## Layer Responsibilities

### Backend (Spring Boot)

| Layer | Location | Responsibilities | Does NOT |
|-------|----------|-----------------|----------|
| API | `backend/src/main/java/com/vibefolio/api/` | `@RestController`, request DTOs, response DTOs, validation annotations, OpenAPI documentation | Business logic, DB access, AI calls |
| Service | `backend/src/main/java/com/vibefolio/service/` | Business logic, orchestration giữa AI/persistence/email/storage, transaction boundary | HTTP concepts, framework imports beyond `@Service`/`@Transactional` |
| AI | `backend/src/main/java/com/vibefolio/ai/` | Anthropic SDK wrapper, prompt building, schema validation, structured output parsing | DB access, HTTP, business decisions |
| Persistence | `backend/src/main/java/com/vibefolio/persistence/` | JPA entities, Spring Data repositories, Flyway migrations | Business rules, HTTP concepts, AI calls |
| Storage | `backend/src/main/java/com/vibefolio/storage/` | R2/S3 abstraction (`PdfStorageService`), upload/download/hash | Business decisions |
| Email | `backend/src/main/java/com/vibefolio/email/` | Resend HTTP client wrapper, magic link templates | Token generation logic (đó là `service` concern) |
| Security | `backend/src/main/java/com/vibefolio/security/` | CORS, `X-API-Key` filter, Bucket4j rate limit, magic link verification | Business rules |
| Config | `backend/src/main/java/com/vibefolio/config/` | `@ConfigurationProperties`, beans setup, OpenAPI config | Logic |

### Frontend (Next.js)

| Layer | Location | Responsibilities | Does NOT |
|-------|----------|-----------------|----------|
| Pages | `frontend/src/app/` | Routing, fetching qua TanStack Query (Client Component) hoặc Server Component, layout | Business logic, presentation details |
| Components — portfolio | `frontend/src/components/portfolio/` | Pure render, props in JSX out (Hero, Specialties, Projects, ...) | Fetching, state, side effects |
| Components — landing | `frontend/src/components/landing/` | Landing sections (Hero, Features, CTA) | Generic UI primitives |
| Components — ui | `frontend/src/components/ui/` | shadcn primitives (Button, Input, Form, ...) | Domain logic |
| API client | `frontend/src/lib/api/` | Generated OpenAPI client, Zod runtime validate (defensive) | Business logic |
| Utils | `frontend/src/lib/utils/` | Pure helpers (cn, format date, ...) | Side effects |
| Edge functions | `frontend/src/app/api/` | OG image gen (`/api/og/[username]`), proxy (`/api/proxy/[...path]`) | Business logic — chỉ proxy + transform |

## Key Boundaries

- **FE components không gọi BE API trực tiếp từ leaf**. TanStack Query trong page-level hoặc Server Component cấp cao. Leaf nhận data qua props.
- **Browser không gọi BE trực tiếp**. Mọi request qua FE Route Handler `/api/proxy/[...path]` để giấu `BE_API_KEY`.
- **BE controllers chỉ là thin layer**: parse request DTO → gọi service → format response DTO. Không có `if/else` business logic trong controller.
- **BE services không import `jakarta.servlet.*` hoặc `org.springframework.web.*`**. Service test được mà không cần Spring MVC context.
- **DB queries chỉ trong `persistence/`**. Service gọi qua repository interface. Không `EntityManager.createQuery()` ngoài `persistence/`.
- **AI calls chỉ trong `ai/`**. Service gọi qua `AnthropicClient` abstraction. Đổi từ Anthropic Java SDK sang REST direct, hoặc thêm Claude Agent SDK ở M3 → chỉ động `ai/`.
- **OpenAPI spec là source of truth contract**. FE KHÔNG viết types thủ công cho API response. CI fail nếu types không sync.

## Data Flow

### Generate portfolio (M1 happy path)

```
1. Browser POST /api/proxy/v1/portfolios (multipart: email, username, file)
   FE Route Handler: attach X-API-Key, forward → BE

2. BE PortfolioController.create():
   a. Validate DTO (Bean Validation: email format, username pattern)
   b. Delegate → PortfolioService.create(email, username, pdfBytes)

3. PortfolioService.create():
   a. UsernameService.checkAvailable(username) — DB unique + reserved + blocklist
   b. RateLimitService.check(email, ip) — Bucket4j
   c. CostGuardService.check() — DB SUM(generations.cost_usd) < $50
   d. PdfStorageService.upload(pdfBytes) → R2 URL + SHA256 hash
   e. CacheService.findByHash(hash) → nếu hit, dùng JSON cũ
   f. Nếu miss: GenerationService.generate(pdfBytes) →
      - AnthropicClient.generatePortfolio() (Haiku default)
      - PortfolioJsonValidator.validate() (Bean Validation)
      - Nếu fail → escalate Sonnet retry 1 lần
      - Log GenerationEntity (cost, tokens, duration)
   g. PortfolioRepo.save(PortfolioEntity với status=PENDING)
   h. MagicLinkService.create(email, portfolioId, purpose=CLAIM)
   i. MagicLinkEmailSender.send(email, token)
   j. Return { tempId, previewUrl }

4. FE redirect /preview/[tempId]
   → fetch GET /v1/portfolios/preview/{tempId} → render Portfolio component

5. User click email link → /api/magic-link/[token]
   → POST /v1/magic-links/claim { token }
   → BE MagicLinkController.claim():
      a. MagicLinkService.verify(token, purpose=CLAIM)
      b. Set portfolio.status = LIVE, claimedAt = now
      c. Mark magic link used
      d. Trigger Next.js revalidate webhook for /[username]
   → FE redirect /[username]
```

## Key Technical Decisions

| Decision | Choice | Reason | ADR |
|----------|--------|--------|-----|
| BE/FE architecture | Tách Spring Boot + Next.js | Leverage Java fintech expertise; type safety qua OpenAPI; future mobile (KMP) reuse BE | [ADR-001](decisions/ADR-001-spring-nextjs-split.md) |
| AI provider strategy | Anthropic API self-managed (default Haiku, escalate Sonnet) | Cost rẻ ở v1; reposition quality cao; tránh BYOK friction | [ADR-002](decisions/ADR-002-ai-self-managed-api.md) |
| URL routing M1 | Path-based `/[username]`, subdomain ở M1.5 | Free Vercel Hobby, $0 fixed; migration path sạch | [ADR-003](decisions/ADR-003-path-based-routing-m1.md) |
| Database | Postgres (Neon serverless free tier) + Spring Data JPA | Familiar; JSONB cho portfolio data; serverless free | — |
| BE language | Java 21 (LTS) + Spring Boot 3.3 + Web MVC | Sở trường Leonard; virtual threads; mature ecosystem | — |
| FE framework | Next.js 15 (App Router) + Tailwind + shadcn | Idiomatic 2026; Vercel free; Claude Code productive nhất | — |
| API contract | OpenAPI 3 (springdoc-openapi) | Source of truth; FE generate TS types qua `openapi-typescript` | — |
| Validation | Bean Validation (BE) + Zod mirror (FE defensive) | Annotation-based BE; runtime validate FE phòng drift | — |
| Auth (FE↔BE) | `X-API-Key` header qua FE proxy | Đơn giản cho M1; không expose key browser | — |
| Auth (user) | Magic link via Resend, không password | M1 no session; xác thực email; tránh password reset flow | — |
| Storage | Cloudflare R2 (free 10GB + free egress) | Rẻ hơn S3; S3-compatible SDK | — |
| Rate limit | Bucket4j (in-memory M1, Redis từ M2) | Đơn giản; Spring integration tốt | — |

## External Integrations

| Service | Purpose | Location | Auth |
|---------|---------|----------|------|
| Anthropic API | AI generation (Claude Haiku/Sonnet) | `backend/.../com/vibefolio/ai/AnthropicClient` | API key in `ANTHROPIC_API_KEY` env (BE only) |
| Cloudflare R2 | PDF storage | `backend/.../com/vibefolio/storage/R2StorageService` | Access key + secret in env (BE only) |
| Neon Postgres | DB | Spring Data JPA, `application.yml` | Connection string in `DB_URL` env (BE only) |
| Resend | Magic link email | `backend/.../com/vibefolio/email/ResendClient` | API key in `RESEND_API_KEY` env (BE only) |
| Cloudflare Turnstile | Captcha trên form | FE landing page | Site key public + secret in BE |
| Stripe (M1.5+) | Paid tier subscription | `backend/.../com/vibefolio/billing/StripeService` | Secret key in env (BE only) |

## Performance Considerations

- **AI generation latency target p95 < 30s** (từ submit form đến preview ready). Haiku ~5-10s; Sonnet ~10-25s; PDF upload + DB save ~2s.
- **BE non-AI endpoints p95 < 200ms**. JPA queries phải có index (username unique, email indexed, magic_links.token unique).
- **FE portfolio page**: ISR với `revalidate: 3600`. Lighthouse Performance ≥95, LCP <1.5s, CLS <0.05.
- **Cache by PDF hash**: re-upload cùng file = $0 AI cost, latency <500ms (DB hit only).
- **BE cold start trên Railway hobby**: ~3-10s sau idle. Mitigation: UptimeRobot ping `/actuator/health` mỗi 5 phút (free).
- **Static assets** (CSS, JS, font, OG images): qua Vercel Edge CDN, không qua BE.
- **DB connection pool**: HikariCP default 10 connections — đủ cho M1 (Railway hobby giới hạn ~50 connections trên Neon free).
