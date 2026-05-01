# Vibefolio — Design Document (v1 / M1)

**Author:** Leonard (Trinh Van Dat)
**Date:** 2026-04-30
**Status:** Draft for review (v2 — split FE/BE architecture)
**Scope:** MVP (M1) — "One-shot static portfolio generator"

---

## 1. Tóm tắt 1 phút

Vibefolio là một web tool. User upload file CV PDF, AI đọc CV và sinh ra một website portfolio cho user, host trên một path (M1) hoặc subdomain (M1.5+) của Vibefolio. User share link đó để đi xin việc remote / Upwork / freelance.

Khác biệt cốt lõi so với các tool generate CV → portfolio đang có:

1. **AI Reposition (không chỉ format)** — AI phát hiện user đang "tự bán rẻ" và đề xuất chức danh & headline cao hơn (vd "Java Backend Engineer" → "Backend & AI Engineer for Fintech"). Đây là moat của Vibefolio.
2. **Niche-first** — phục vụ trước cho dev/AI engineer mid-senior nhắm thị trường remote (US/EU/SG). Mọi prompt, template, copy được tối ưu cho nhóm này, không phải fresh grad / non-tech.
3. **Output chất lượng senior-level** — không phải Linktree-style generic; output trông như portfolio mà dev senior sẽ tự build (kiểu Brittany Chiang / Lee Robinson).

**Architecture decision:** Tách FE (Next.js + TS) và BE (Spring Boot Java) thành 2 service riêng, giao tiếp qua REST API có OpenAPI contract.

---

## 2. Target user & wedge

### Persona canonical: "Leonard"
Senior backend engineer Vietnam, 6 năm exp, làm fintech (CIMB Thai Bank), có kỹ năng AI engineering thực chiến (LangChain, LangGraph, Local LLM), security (Passkey/WebAuthn, AWS KMS), open source (crypto wallet KMP), mentorship 50+ student. Đang nhắm:
- Remote full-time job ở US / EU / Singapore
- Upwork freelance gigs

Pain point Leonard đang gặp:
- CV PDF không paste được vào Upwork "portfolio link"
- LinkedIn không show project depth & technical detail
- Không có thời gian build portfolio website từ đầu (side project, đang full-time)
- Tự viết CV thường tự bán rẻ ("Java Backend Engineer" thay vì "Backend & AI Engineer for Fintech")

### Wedge sản phẩm
**"AI Reposition + Niche backend/AI engineer"** (combo A+E từ brainstorm).

Implication:
- Prompt AI có instruction về reposition pattern theo vertical (fintech, healthtech, AI infra...)
- Template default thiết kế cho dev (dark mode, monospace accent, code-friendly)
- Marketing copy nhắm dev mid-senior, không "career coach for everyone"

---

## 3. Scope MVP (M1)

### In scope (ship 5–7 tuần — đã tăng do tách FE/BE)
- Landing page (FE): hero + 1 form generate
- Upload PDF (max 5MB, PDF only)
- Form: email + username + file
- BE API endpoints (REST + OpenAPI contract):
  - `POST /v1/portfolios` — generate
  - `GET /v1/portfolios/{username}` — fetch public
  - `POST /v1/portfolios/{id}/regenerate` — re-upload
  - `POST /v1/magic-links/claim` — confirm portfolio
  - `POST /v1/magic-links/request-update` — gửi link update
- AI extract → JSON structured (Java DTO validated với Bean Validation + manually with Anthropic structured output)
- Magic link confirm via email (Resend)
- Render portfolio tại path `vibefolio.com/[username]` (M1 dùng path-based, KHÔNG subdomain)
- 1 template duy nhất ("Senior Engineer" style)
- Watermark "Made with Vibefolio" ở footer
- Re-upload qua magic link (override portfolio cũ)
- Username uniqueness + reserved/blocklist
- OG image động cho mỗi portfolio (`@vercel/og` ở FE; FE fetch data từ BE)
- Rate limit 3 gen / email / 24h + Cloudflare Turnstile trên form
- Hard cap AI cost $50/tháng → bật waitlist mode

### Out of scope (postpone)
- Edit dashboard / form sửa từng section → M2
- Nhiều template → M2
- Custom domain → M1.5 (sau khi có ≥10 free user dùng thật)
- Subdomain wildcard (`username.vibefolio.com`) → M1.5 cùng custom domain
- Vibe-code chat sửa portfolio → M3
- Job-target mode (paste JD) → M4
- BYOK (Bring Your Own Key) → M3 power-user mode
- Mobile app (KMP) → post-M4 nếu thị trường có signal
- Analytics, blog, RSS, multi-language

### Non-goals
- Không build CV editor / CV maker (Vibefolio đọc CV có sẵn, không tạo CV)
- Không build job board / matching service
- Không build resume parser API bán riêng

---

## 4. User flow (M1)

```
[FE — Landing page]
   ↓
User điền form: email + username + upload CV PDF
   ↓
Submit → Cloudflare Turnstile → FE validate (file size, type)
   ↓
FE: POST /v1/portfolios (multipart) → BE
   ↓
BE: validate username available, file type, rate limit
   ↓
BE: upload PDF → S3/R2 → URL + SHA256 hash
   ↓
BE: check cache by hash → nếu hit, dùng JSON cũ; miss → gọi Anthropic API
   ↓
BE: validate JSON với schema, lưu DB (status='pending', tạo magicLink claim)
   ↓
BE → FE: response { previewUrl, message: 'Check your email to claim' }
   ↓
FE: redirect /preview/[tempId] (FE fetch GET /v1/portfolios/preview/{tempId})
   ↓
BE: parallel — gửi magic link qua Resend
   ↓
User click email link → FE /api/magic-link/[token]
   ↓
FE: POST /v1/magic-links/claim { token } → BE
   ↓
BE: verify token → set portfolio.status='live', claimedAt=now
   ↓
BE → FE: response { username, redirect: '/[username]' }
   ↓
FE: redirect /[username] — portfolio public.
```

### Re-upload flow
1. User vào `/update`, FE form nhập email
2. FE: POST /v1/magic-links/request-update { email } → BE
3. BE lookup portfolio by email → tạo magicLink purpose='update', gửi email
4. User click link → FE `/update/[token]` form upload PDF mới
5. FE: POST /v1/portfolios/{id}/regenerate (multipart, header `X-Magic-Token`) → BE
6. BE verify token → regen JSON, ghi đè portfolio.data, increment updatedAt
7. FE: redirect /[username]

---

## 5. Tech stack

### Frontend (Next.js)
- **Next.js 15 (App Router) + TypeScript**
- **Tailwind CSS + shadcn/ui** cho UI
- **TanStack Query** cho fetch BE + cache + retry
- **Zod** cho validate response từ BE (mirror JSON shape)
- **OpenAPI TypeScript client** — generate từ BE OpenAPI spec bằng `openapi-typescript` hoặc `orval`. Build script: `pnpm gen:api` chạy trước build.
- **`@vercel/og`** cho OG image động (Edge runtime)
- **Cloudflare Turnstile** cho captcha (free, ít bị adblock hơn reCAPTCHA)
- Deploy: **Vercel Hobby tier** (free)

### Backend (Spring Boot)
- **Spring Boot 3.3+ với Java 21** (LTS, virtual threads)
- **Spring Web MVC** (blocking, đơn giản; M3 có thể chuyển WebFlux nếu cần streaming)
- **Spring Data JPA + Hibernate** cho DB access
- **Flyway** cho migrations
- **springdoc-openapi** cho auto-generate OpenAPI 3 spec + Swagger UI
- **Spring Security** cho API key + CORS + rate limit basic
- **Bucket4j** cho rate limiting per email/IP
- **Anthropic Java SDK** (`com.anthropic:anthropic-java`) cho AI calls
- **AWS Java SDK v2** cho S3 (lưu PDF gốc); hoặc Cloudflare R2 SDK (S3-compatible)
- **Resend Java client** (HTTP wrapper) cho gửi email
- **Bean Validation (Jakarta Validation)** cho DTO validation
- **JUnit 5 + Mockito + Testcontainers** cho test
- **Maven** (`pom.xml`)
- Deploy: **Railway hobby plan** ($5/tháng) hoặc **Fly.io** (free tier 3 small VM)

### Database
- **Neon Postgres** (free 0.5GB) — chia sẻ giữa BE & FE? KHÔNG. Chỉ BE access. FE đi qua BE API.

### Storage
- **Cloudflare R2** (free 10GB storage + egress free) — đề xuất hơn S3 vì không tính egress khi serve PDF
- Backup option: AWS S3

### AI
- **Dev tooling**: Claude Code subscription (cá nhân Leonard) để code Vibefolio (cả FE và BE)
- **Production AI**: Anthropic API self-managed (key của Leonard, lưu env BE only)
  - Default: **Claude Haiku 4.5** (rẻ, đủ tốt 80% case)
  - Escalate: **Claude Sonnet 4.6** khi Haiku output fail Bean Validation hoặc CV >5 trang
  - **Prompt caching** bật cho system prompt (giảm 90% cost phần repeated)
  - Sử dụng **structured output** (tool use pattern) để Claude trả JSON khớp schema
- **Migration path**: M1–M2 dùng Anthropic Java SDK thuần (1 call structured output). M3+ migrate sang **Claude Agent SDK** (Java port nếu có; nếu chưa, viết tool-use loop thuần — Java SDK hỗ trợ tool use native).

### Lý do chọn stack này
- **BE Spring Boot**: leverage 6 năm Java + 3 năm fintech-grade Spring của bạn. Bạn ship code BE Spring nhanh hơn bất kỳ stack nào khác.
- **FE Next.js**: idiomatic React stack 2026, Claude Code productive nhất, deploy Vercel free.
- **OpenAPI contract**: type-safe end-to-end mà vẫn tách stack. Đổi BE endpoint → regenerate TS client → FE compile error nếu break.
- **Java + TS**: bạn dùng cả hai trong CV (CIMB + KMP), không phải tech mới với bạn.
- **Cost vẫn rẻ**: ~$6/tháng fixed (Railway $5 + domain $1).

---

## 6. Kiến trúc tổng thể

```
┌─────────────────────────────────────────────────────────────┐
│                         Browser                              │
│  Landing │ Form generate │ Preview │ Public portfolio        │
└──────────────────────────┬──────────────────────────────────┘
                           │ HTTPS
┌──────────────────────────▼──────────────────────────────────┐
│            Next.js FE (Vercel — vibefolio.com)               │
│                                                              │
│  ┌──────────────────┐  ┌──────────────────────────────────┐ │
│  │ App Router pages │  │ Edge functions / Route handlers  │ │
│  │  /               │  │  /api/og/[username]  (OG image)  │ │
│  │  /[username]     │  │  /api/proxy/*  (tiny BE proxy    │ │
│  │  /preview/[id]   │  │    để hide BE_API_KEY khỏi      │ │
│  │  /update         │  │    browser)                      │ │
│  └────────┬─────────┘  └────────┬─────────────────────────┘ │
└───────────┼─────────────────────┼───────────────────────────┘
            │                     │
            │  HTTPS + X-API-Key  │
            ▼                     ▼
┌─────────────────────────────────────────────────────────────┐
│         Spring Boot BE (Railway — api.vibefolio.com)         │
│                                                              │
│  ┌────────────────┐ ┌────────────────┐ ┌─────────────────┐ │
│  │ REST API       │ │ Service layer  │ │ AI Pipeline     │ │
│  │ /v1/portfolios │ │ - Portfolio    │ │ - PDF reader    │ │
│  │ /v1/magic-links│ │ - MagicLink    │ │ - Anthropic SDK │ │
│  │ Swagger UI     │ │ - Generation   │ │ - Schema valid  │ │
│  └────────────────┘ └────────────────┘ └─────────────────┘ │
│                                                              │
│  ┌────────────────┐ ┌────────────────┐ ┌─────────────────┐ │
│  │ Spring Data JPA│ │ Bucket4j       │ │ Spring Security │ │
│  └────────────────┘ │ rate limit     │ │ CORS + API key  │ │
│                     └────────────────┘ └─────────────────┘ │
└────────┬─────────────────────┬──────────────────┬──────────┘
         │                     │                  │
         ▼                     ▼                  ▼
┌────────────┐         ┌─────────────┐    ┌──────────────┐
│ Neon       │         │ Cloudflare  │    │ Anthropic    │
│ Postgres   │         │ R2 (PDF)    │    │ API (Claude) │
└────────────┘         └─────────────┘    └──────────────┘
                                                   ▲
                                                   │
                              ┌────────────────────┘
                              │
                       ┌──────────────┐
                       │ Resend       │
                       │ (email)      │
                       └──────────────┘
```

### Tách module BE (Spring Boot)

```
com.vibefolio
├── api              # @RestController layer
│   ├── PortfolioController
│   ├── MagicLinkController
│   └── dto          # Request/Response DTOs (validation annotations)
├── service          # Business logic
│   ├── PortfolioService
│   ├── MagicLinkService
│   ├── GenerationService
│   └── UsernameService
├── ai               # AI integration
│   ├── AnthropicClient
│   ├── PortfolioPromptBuilder
│   ├── PortfolioJsonValidator
│   └── PortfolioJsonModel    # Java records mirror schema
├── persistence      # Repositories + JPA entities
│   ├── entity
│   │   ├── PortfolioEntity
│   │   ├── MagicLinkEntity
│   │   └── GenerationEntity
│   └── repo
├── storage          # R2/S3 abstraction
│   └── PdfStorageService
├── email            # Resend
│   └── MagicLinkEmailSender
├── security         # CORS, API key filter, rate limit
└── config           # @ConfigurationProperties, OpenAPI config
```

### Tách module FE (Next.js)

```
src/
├── app/
│   ├── (landing)/
│   │   └── page.tsx
│   ├── [username]/
│   │   ├── page.tsx                  # Public portfolio (ISR)
│   │   └── opengraph-image.tsx
│   ├── preview/
│   │   └── [tempId]/page.tsx
│   ├── update/
│   │   ├── page.tsx
│   │   └── [token]/page.tsx
│   └── api/
│       ├── og/[username]/route.ts
│       └── proxy/[...path]/route.ts  # Tiny proxy hiding BE_API_KEY
├── components/
│   ├── portfolio/                    # Pure renderer (props in, JSX out)
│   ├── landing/
│   └── ui/                           # shadcn primitives
├── lib/
│   ├── api/
│   │   ├── client.ts                 # Generated OpenAPI client
│   │   └── schemas.ts                # Zod mirror cho response validate
│   └── utils/
└── public/
```

### Boundary rõ
- **`components/portfolio/`** chỉ nhận props là `Portfolio` schema, không gọi API. Pure renderer → tái dùng cho M2 (multi-template), M3 (preview trong chat).
- **BE service layer** không biết HTTP. Controllers mỏng, service dày. Test service không cần Spring context.
- **AI module** chỉ expose `generatePortfolio(pdfBytes): PortfolioJson`. Đổi model / prompt / SDK không ảnh hưởng caller.
- **OpenAPI = source of truth contract.** FE không tự viết types — generate từ spec.

### API key giữa FE và BE

- BE có `X-API-Key` filter. FE biết key qua `BE_API_KEY` env var (server-side only).
- Browser **KHÔNG** gọi BE trực tiếp. FE Route Handler `/api/proxy/[...path]` forward request từ browser → BE, attach API key.
- Lý do: nếu browser gọi thẳng BE, key lộ trong network tab. Proxy giấu key.
- CORS BE: chỉ allow `https://vibefolio.com` + `http://localhost:3000` (dev).
- Rate limit: BE limit per IP (real IP từ `X-Forwarded-For` của Vercel) + per email.

---

## 7. AI pipeline (chi tiết)

AI calls **chỉ chạy trên BE** (Spring Boot service). FE không bao giờ chạm Anthropic SDK hoặc API key.

### Single API call (M1)
```
Input:
  - PDF bytes (từ R2 download hoặc multipart upload)
  - System prompt (cached qua Anthropic prompt caching)

Java code (pseudo):
  AnthropicClient client = AnthropicOkHttpClient.fromEnv();
  MessageCreateParams params = MessageCreateParams.builder()
    .model(Model.CLAUDE_HAIKU_4_5)
    .maxTokens(4096)
    .system(List.of(systemPromptCached, userInstructionCached))
    .messages(List.of(
      MessageParam.user(List.of(
        ContentBlockParam.document(documentBlockParam(pdfBytes)),
        ContentBlockParam.text("Extract & reposition this CV.")
      ))
    ))
    .tools(List.of(extractPortfolioTool))     // structured output
    .toolChoice(ToolChoice.tool("extract_portfolio"))
    .build();

  Message resp = client.messages().create(params);
  PortfolioJson json = parseToolUse(resp);
  validator.validate(json);   // Bean Validation
  return json;
```

### System prompt structure

3 phần (xem mục 7 ở v1 design — không thay đổi nội dung, chỉ thay impl ngôn ngữ):

**Part A — Role & objective:**
"You are an expert career coach + technical writer for senior backend & AI engineers targeting remote markets (US/EU/SG)..."

**Part B — Reposition rules** (thư viện pattern):
- Java/Spring + AWS + AI/LangChain → "Backend & AI Engineer"
- Security/auth/payments → "specialized in [vertical]"
- OSS + mentorship → leadership/community subtitle
- "did X" → "Problem → Approach → Impact"

**Part C — Output schema constraint:**
JSON khớp tool definition, ràng buộc qua `tools` parameter.

### Cost protection (5 đòn bẩy)

1. **Default Haiku, escalate Sonnet** chỉ khi Haiku output fail Bean Validation
2. **Prompt caching** cho system prompt (Anthropic Java SDK hỗ trợ qua `cacheControl`)
3. **Cache by PDF hash (SHA-256)**: cùng PDF = trả cached JSON từ DB, không gọi AI
4. **Rate limit** (Bucket4j): 3 gen / email / 24h, 10 gen / IP / 24h
5. **Hard cap toàn cục**: query `SUM(cost_usd) WHERE created_at > start_of_month`. Nếu ≥ $50 → set Spring `@Value` flag → controller return 503 "waitlist mode"

### Realistic cost estimate

| Scale | Generations / tháng | AI cost (Haiku 80% / Sonnet 20%) |
| --- | --- | --- |
| 50 user × 2 gen | 100 | ~$2–4 |
| 500 user × 2 gen | 1,000 | ~$20–40 |
| 5,000 user × 2 gen | 10,000 | ~$200–400 |

→ Ở M1 scale, AI cost không đáng kể.

---

## 8. Schema dữ liệu

### Single source of truth: OpenAPI 3 spec

BE define schema bằng Java class + `@Schema` annotation từ springdoc-openapi → Swagger UI hiện ở `/swagger-ui.html` → spec JSON ở `/v3/api-docs` → FE generate TS types từ đây.

### Java Portfolio model (BE)

Dùng Java records (immutable, concise, JSON-native với Jackson):

```java
// com.vibefolio.ai.model.PortfolioJson

public record PortfolioJson(
    @NotNull Integer schemaVersion,
    @Valid @NotNull Person person,
    @Size(max = 280) @NotBlank String summary,
    @Size(min = 3, max = 3) List<Specialty> specialties,
    @Size(max = 6) List<Project> projects,
    List<Experience> experience,
    @NotNull Skills skills,
    List<Education> education,
    List<Credential> credentials
) {
    public record Person(
        @NotBlank String name,
        @Size(max = 80) @NotBlank String headline,
        @NotBlank String location,
        @NotNull AvailableFor availableFor,
        String pronouns,
        @Valid Contact contact,
        @URL String avatarUrl
    ) {}

    public record Contact(
        @Email String email,
        @URL String github,
        @URL String linkedin,
        @URL String twitter,
        @URL String website,
        String phone
    ) {}

    public record Specialty(
        @NotNull SpecialtyIcon icon,
        @Size(max = 40) @NotBlank String title,
        @Size(max = 160) @NotBlank String description
    ) {}

    public record Project(
        @NotBlank String title,
        String role,
        String period,
        @NotBlank String problem,
        @NotBlank String approach,
        @NotBlank String impact,
        @Size(max = 10) List<String> tech,
        @Size(max = 3) List<Link> links,
        boolean featured
    ) {}

    public record Link(@NotBlank String label, @URL String url) {}

    public record Experience(
        @NotBlank String company,
        @NotBlank String role,
        @NotBlank String period,
        String location,
        @Size(max = 5) List<String> highlights
    ) {}

    public record Skills(
        @Size(max = 8) List<String> primary,
        @Size(max = 20) List<String> secondary
    ) {}

    public record Education(
        @NotBlank String school,
        @NotBlank String degree,
        @NotBlank String period
    ) {}

    public record Credential(
        @NotNull CredentialType type,
        @NotBlank String title,
        @NotBlank String description
    ) {}

    public enum AvailableFor { FULLTIME, FREELANCE, BOTH, NOT_LOOKING }
    public enum SpecialtyIcon { CODE, SHIELD, SPARKLES, DATABASE, CLOUD, CPU }
    public enum CredentialType { MENTORSHIP, OSS, CERTIFICATION, SPEAKING, WRITING }
}
```

### TypeScript mirror (FE — auto-generated)

`pnpm gen:api` chạy `openapi-typescript` → output `lib/api/schemas.gen.ts`. FE component nhận type này, không cần viết Zod thủ công cho từng field. Zod chỉ dùng nếu cần runtime validate response (defensive — phòng BE/FE drift trong môi trường dev).

### JPA Entities (BE persistence)

```java
@Entity @Table(name = "portfolios")
public class PortfolioEntity {
    @Id @GeneratedValue(strategy = UUID)
    private UUID id;

    @Column(unique = true, length = 30, nullable = false)
    private String username;

    @Column(nullable = false)
    private String email;

    @Column(name = "cv_blob_url", nullable = false)
    private String cvBlobUrl;

    @Column(name = "cv_hash", length = 64, nullable = false)
    private String cvHash;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private PortfolioJson data;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private PortfolioStatus status;   // PENDING, LIVE

    @CreationTimestamp private Instant createdAt;
    private Instant claimedAt;
    @UpdateTimestamp private Instant updatedAt;
}

@Entity @Table(name = "magic_links")
public class MagicLinkEntity {
    @Id @GeneratedValue(strategy = UUID) private UUID id;
    @Column(nullable = false) private String email;
    @ManyToOne private PortfolioEntity portfolio;
    @Column(unique = true, length = 64, nullable = false) private String token;
    @Enumerated(EnumType.STRING) private MagicLinkPurpose purpose;  // CLAIM, UPDATE
    @Column(nullable = false) private Instant expiresAt;
    private Instant usedAt;
}

@Entity @Table(name = "generations")
public class GenerationEntity {
    @Id @GeneratedValue(strategy = UUID) private UUID id;
    @ManyToOne private PortfolioEntity portfolio;
    @Column(nullable = false) private String email;
    @Column(name = "ip_address", length = 45) private String ipAddress;
    @Column(length = 50) private String model;
    @Column(name = "input_tokens") private Integer inputTokens;
    @Column(name = "output_tokens") private Integer outputTokens;
    @Column(name = "cost_usd", precision = 8, scale = 5) private BigDecimal costUsd;
    @Column(name = "duration_ms") private Integer durationMs;
    @Enumerated(EnumType.STRING) private GenerationStatus status;  // SUCCESS, FAIL, CACHED
    @CreationTimestamp private Instant createdAt;
}
```

### REST API contract (mục chính)

| Method | Path | Auth | Body | Response |
| --- | --- | --- | --- | --- |
| POST | `/v1/portfolios` | API key | multipart (email, username, file) | `{ tempId, previewUrl, magicLinkSent: true }` |
| GET | `/v1/portfolios/{username}` | none | — | `Portfolio JSON` (full) |
| GET | `/v1/portfolios/preview/{tempId}` | API key | — | `Portfolio JSON` |
| POST | `/v1/portfolios/{id}/regenerate` | API key + `X-Magic-Token` | multipart (file) | `{ updated: true }` |
| POST | `/v1/magic-links/claim` | API key | `{ token }` | `{ username }` |
| POST | `/v1/magic-links/request-update` | API key | `{ email }` | `{ sent: true }` |

OpenAPI spec auto-gen, Swagger UI ở `/swagger-ui.html` (ẩn behind basic auth ở prod).

---

## 9. Template "Senior Engineer" (1 template duy nhất ở M1)

Template này là **FE concern only** — BE chỉ trả JSON, FE render. Đổi template không động BE.

### Triết lý design

Template phải trông như portfolio mà 1 dev senior tự build nếu có thời gian — không phải template generic Wix. Tham chiếu:
- Brittany Chiang (brittanychiang.com)
- Lee Robinson (leerob.io)
- Josh Comeau (joshwcomeau.com)
- Cassidy Williams (cassidoo.co)

### Visual identity

- Dark mode default. Light toggle ở góc, persist trong localStorage.
- Font: **Inter** (sans), **JetBrains Mono** (mono accent)
- Color palette:
  - Background: slate-950
  - Foreground: slate-100
  - Accent: emerald-400 (default; future: cho user chọn)
  - Muted: slate-400
- Spacing: container max-w-5xl, generous padding
- Animation: fade-in on scroll (Framer Motion), hover state mượt, không dùng heavy parallax

### Layout

Single-page với sticky sidebar nav (desktop) hoặc top nav (mobile):

```
┌─────────────────────────────────────────────────────────┐
│  Sidebar (sticky, desktop only)   │  Main content       │
│  ─────────                        │  ─────────          │
│  Avatar (initials)                │  HERO               │
│  Name                             │    Headline         │
│  Headline                         │    Tagline          │
│                                   │    Status badge     │
│  About                            │    CTA buttons      │
│  Specialties                      │                     │
│  Projects                         │  SPECIALTIES (3)    │
│  Experience                       │                     │
│  Skills                           │  PROJECTS           │
│  Credentials                      │                     │
│                                   │  EXPERIENCE         │
│  Contact                          │                     │
│  ─────────                        │  SKILLS             │
│  GitHub  LinkedIn                 │                     │
│                                   │  CREDENTIALS        │
│                                   │                     │
│                                   │  FOOTER             │
│                                   │   "Made with        │
│                                   │    Vibefolio"       │
└─────────────────────────────────────────────────────────┘
```

### Section details

- **Hero**: name (h1) + headline (h2 reposed) + 1-line summary + 2 CTA (based on `availableFor`)
- **Specialties** (3 cards horizontal): icon + title + description
- **Projects** (vertical list): card với problem/approach/impact format, tech badges, links
- **Experience** (timeline): company / role / period / 3-5 highlights rewritten
- **Skills**: 2 nhóm (Primary / Secondary), badges nhỏ
- **Credentials**: mentorship, OSS, talks
- **Footer**: contact links + "Made with Vibefolio"

### Responsive

- Desktop ≥1024px: sidebar layout
- Tablet 768–1024: top nav + main
- Mobile <768: top nav collapsed, single column

### OG image (động per portfolio)

FE Edge runtime `app/[username]/opengraph-image.tsx` dùng `@vercel/og`:
1. Fetch `GET /v1/portfolios/{username}` từ BE
2. Render thành image: gradient slate + initials + name + headline + Vibefolio logo

### Performance target

- Lighthouse Performance ≥95
- LCP <1.5s
- CLS <0.05
- Page render: ISR với `revalidate: 3600`. Khi user re-upload, BE trigger revalidate qua `/api/revalidate?username=xxx&secret=...` (Next.js built-in).

---

## 10. Subdomain & hosting strategy (path-based ở M1)

### Quyết định: M1 dùng path-based (`vibefolio.com/[username]`)

Lý do:
- Wildcard subdomain cần Vercel Pro ($20/tháng) — side project né
- Path-based chạy tốt trên Hobby tier
- M1.5 chuyển sang subdomain dễ vì FE routing modular + BE không bị ảnh hưởng

### Username rules (BE enforced)

- Pattern: `[a-z0-9-]{3,30}`
- Reserved: `app`, `www`, `api`, `admin`, `blog`, `about`, `pricing`, `login`, `signup`, `dashboard`, `settings`, `update`, `preview`, `terms`, `privacy`, `support`, `help`, `docs`, `swagger-ui`
- Profanity blocklist: load từ resource file (curated word list)
- Uniqueness: DB unique constraint
- Case-insensitive lookup, lowercase storage

### URL structure

**FE routes (vibefolio.com):**
- `/` — landing
- `/[username]` — public portfolio
- `/preview/[tempId]` — preview trước claim
- `/update` — request magic link
- `/update/[token]` — re-upload form

**BE routes (api.vibefolio.com):**
- `/v1/...` — REST API
- `/swagger-ui.html` — docs (basic auth ở prod)
- `/actuator/health` — health check cho Railway

### M1.5 migration path

1. Upgrade Vercel Pro
2. DNS wildcard `*.vibefolio.com` → Vercel
3. Next.js middleware: detect `host` header → nếu subdomain → rewrite về `/[username]`
4. Path `/[username]` redirect 301 → subdomain (preserve SEO)
5. BE không cần đổi gì — vẫn cùng API.

---

## 11. Auth model

### Magic link giữa user và Vibefolio

- BE generate token, gửi qua Resend
- Token: 32 byte random, base64url-encoded, lưu DB
- Expires 24h, one-time use
- BE endpoint: `POST /v1/magic-links/claim` và `POST /v1/magic-links/request-update`

### API key giữa FE và BE

- BE check header `X-API-Key` cho mọi endpoint trừ `GET /v1/portfolios/{username}` (public read).
- FE giữ key trong env var server-side (`BE_API_KEY`) — không expose browser
- FE Route Handler `/api/proxy/[...path]` forward request → BE, attach key
- Nếu key leak → rotate (env var, deploy lại FE)

### CORS config (BE Spring Security)

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of(
        "https://vibefolio.com",
        "http://localhost:3000"  // dev
    ));
    config.setAllowedMethods(List.of("GET", "POST"));
    config.setAllowedHeaders(List.of("X-API-Key", "X-Magic-Token", "Content-Type"));
    config.setMaxAge(3600L);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/v1/**", config);
    return source;
}
```

### Rate limit (Bucket4j)

- Per email: 3 gen / 24h
- Per IP (real IP từ `X-Forwarded-For` Vercel forward): 10 gen / 24h
- Per email magic link request: 3 / hour

---

## 12. Cost model & protection

### Fixed monthly cost ở v1

| | Cost |
| --- | --- |
| Vercel Hobby (FE) | $0 |
| Railway hobby (BE) | $5 |
| Neon Postgres free | $0 |
| Cloudflare R2 free (10GB) | $0 |
| Resend free | $0 |
| Domain (annual / 12) | ~$1 |
| **Fixed total** | **~$6/tháng** |

### Variable cost (AI)

Như mục 7 — không đổi:
- Haiku: ~$0.016/gen
- Sonnet: ~$0.06/gen
- Blended với prompt caching: ~$0.02–0.05/gen
- 50 user × 2 gen ≈ $2–4/tháng

### Hard cap & protection

- **Anthropic console**: spend alert $40
- **Code-level (BE)**: middleware/filter check `SELECT SUM(cost_usd) FROM generations WHERE created_at > date_trunc('month', now())`. Nếu ≥ $50 → return 503 với body `{ waitlist: true }`
- **FE handle 503**: hiện UI "Bạn đã vào waitlist" + lưu email → BE waitlist table (build sau M1.5)

---

## 13. Monetization (P1 — Freemium watermark)

### Free tier (M1)
- Unlimited portfolio generation (rate limited)
- Path-based URL (`vibefolio.com/[username]`)
- Watermark "Made with Vibefolio" ở footer
- Default Haiku model
- Cache by PDF hash

### Paid tier — $7/tháng (M1.5+ build)
- Custom domain (CNAME)
- Subdomain (`username.vibefolio.com`) sau khi M1.5 enable wildcard
- Gỡ watermark
- Sonnet model cho generation
- Re-generate không cache
- Priority support

### Pricing rationale
- Tier $7/tháng = AI cost x100 → margin tốt
- Breakeven: 5 paid users = $35/tháng đủ cover Railway $5 + AI cho 100+ free users + buffer

### Build paid tier — KHI NÀO?

KHÔNG build trong M1. Lộ trình:
1. M1 launch, ≥10 free portfolio thật sự dùng
2. Phỏng vấn 5 user → confirm/deny custom domain mong muốn
3. M1.5 build paid: Stripe Checkout (BE webhook handle) + custom domain flow + subdomain DNS automation

---

## 14. GTM Phase 1 — "Launch in team"

### Phase 1a (Tuần 1–2 sau M1 ship): Self
- Leonard dùng Vibefolio cho chính mình
- Paste link vào Upwork profile, LinkedIn About, signature email job application
- Đo: recruiter click? Upwork client reply?

### Phase 1b (Tuần 3–4): Inner circle
- Mời 3–5 đồng nghiệp dev VN, sit-with-them khi họ generate lần đầu
- Quan sát: confused chỗ nào, AI extract sai gì, output có làm họ proud không
- Thu testimonial nếu OK

### Phase 1c (Tháng 2): Extended network
- Mở rộng → 10–20 người (qua mentorship list 50+ student)
- Free, yêu cầu feedback form
- Identify ≥1 "champion user" sẵn sàng paid

### Phase 2 (Tháng 3+): Public soft launch
Khi ≥10 portfolio sống + 2-3 testimonial:
- Daily.dev VN, J2TEAM, Facebook group dev VN
- Twitter/X dev VN
- KHÔNG Product Hunt vội

### Marketing message

Tagline: **"From CV to portfolio in 30 seconds. Built for backend & AI engineers."**
Sub: **"Stop selling yourself short. AI that knows how to position senior engineers for remote jobs."**

---

## 15. Roadmap M1 → M4

| Mốc | Thời gian | Tính năng chính | Success metric |
| --- | --- | --- | --- |
| **M1** | 5–7 tuần | FE + BE setup + OpenAPI contract + Upload → AI → path URL + magic link + 1 template + watermark | 5 portfolio sống, Leonard dùng cho ≥1 application thật |
| **M1.5** | +2-3 tuần | Wildcard subdomain + custom domain + Stripe paid tier | 1 paid user trả $7/tháng |
| **M2** | +4-5 tuần | Edit dashboard (form per section) + 2 template thêm | 20 portfolio, 3 paid |
| **M3** | +6-8 tuần | Migrate sang Claude Agent SDK (Java tool-use loop), vibe-code chat sửa portfolio (BE WebSocket hoặc SSE) | Demo viral Twitter, 50 portfolio |
| **M4** | +4-5 tuần | Job-target mode + BYOK power mode | 100 portfolio, 10 paid |

> Note: timeline +1-2 tuần so với monolithic vì overhead setup BE Spring + OpenAPI generation pipeline. Sau M1, mỗi feature mới ship nhanh hơn vì foundation đã sẵn.

> **Path convention update (2026-04-30)**: Repo dùng top-level `backend/` và `frontend/` thay vì `apps/api/` và `apps/web/`. Module CLAUDE.md riêng trong từng folder load context theo nhu cầu.

---

## 16. Rủi ro & mitigation

| Rủi ro | Severity | Mitigation |
| --- | --- | --- |
| AI extract sai/thiếu | High | (1) Preview ngay sau generate, "Re-generate" 1 lần free; (2) M2 build edit UI; (3) log fail cases |
| Watermark xấu khiến user không share | Medium | Watermark tasteful: 1 dòng nhỏ ở footer |
| Username squatting | Low | Reserved list + profanity blocklist + email confirm bắt buộc |
| AI cost runaway khi viral | High | Hard cap $50/tháng → waitlist mode; rate limit; default Haiku; cache by hash |
| Spam/abuse trên form | Medium | Cloudflare Turnstile + Bucket4j rate limit IP & email |
| Anthropic ToS issue | Low | Dùng API thuần, không dùng Claude Code subscription làm backend |
| Leonard burnout (side project) | High | Scope M1 hẹp; KHÔNG feature creep; mỗi M có deadline cứng |
| Email magic link bị spam folder | Medium | SPF/DKIM/DMARC config; Resend domain verified; subject line không trigger spam |
| Portfolio chứa thông tin nhạy cảm | Medium | AI prompt instruct ẩn phone/address mặc định, chỉ show email |
| BE/FE schema drift | Medium | OpenAPI spec là source of truth; FE generate types từ spec; CI fail nếu types mismatch |
| Railway BE bị sleep cold start | Medium | Hobby plan có hibernate sau idle. Mitigation: cron `/actuator/health` mỗi 5p (UptimeRobot free) hoặc upgrade Pro nếu chạm |
| Anthropic Java SDK chậm update | Medium | Có thể fallback REST direct call qua RestTemplate/WebClient nếu cần feature mới |

---

## 17. Success metrics

### Bắc đẩu (north star)
**Số portfolio "live + có traffic" mỗi tháng** — không phải đăng ký, mà thật sự được share.

### Metrics theo phase

**Phase 1a (Leonard self):**
- ≥1 recruiter click portfolio link
- ≥1 Upwork client reply proposal có link portfolio

**Phase 1b–1c (network):**
- 5–10 portfolio sống
- ≥3 user trả lời "tôi sẽ dùng cái này nếu free"
- ≥1 user trả lời "tôi sẵn sàng trả $7/tháng cho custom domain"

**Phase 2 (soft public):**
- 50 portfolio sống
- AI generation success rate ≥85%
- Re-generation rate <2 lần / user trung bình
- Avg time on preview screen > 2 phút

### Metrics anti
- Bounce rate trên `/[username]` <40%
- AI cost per portfolio <$0.05 blended
- p95 generation time <30s (từ submit form đến preview)
- BE p95 latency cho non-AI endpoints <200ms

---

## 18. Open questions (cần resolve trong implementation)

1. **PDF max size**: 5MB đủ chưa? Limit ở BE Spring `multipart.max-file-size`.
2. **Avatar source**: M1 dùng initials. M2 cho upload? Hay AI sinh avatar?
3. **Email confirmation flow UX**: confirm trên link → land trực tiếp `/[username]` (instant gratification) hay land `/welcome` page có tutorial ngắn?
4. **Multi-language**: AI sinh portfolio English mặc định? Detect từ CV, hay user chọn?
5. **GDPR / privacy**: lưu PDF gốc bao lâu? Đề xuất 30 ngày sau update cuối, sau đó BE cron xóa R2 object, giữ JSON.
6. **Domain**: `vibefolio.com` đã available chưa? Backup name?
7. **Reposition aggressiveness**: AI nên reposition mạnh (stretch hơn CV) hay conservative? Mặc định = conservative; M2 toggle "boost mode"?
8. **BE deploy region**: Railway có US, EU. Chọn EU (gần VN hơn? gần target market US/EU?). Latency từ VN dev → EU ~250ms; user EU → EU ~30ms; user US → EU ~100ms. Đề xuất EU.
9. **OpenAPI generation tool**: `openapi-typescript` (lightweight, chỉ types) hay `orval` (gồm React Query hooks)? Đề xuất `openapi-typescript` cho M1 vì đơn giản.
10. **Java version**: Java 21 LTS hay 23 latest? Đề xuất 21 LTS vì Railway base image hỗ trợ tốt + virtual threads đã GA.

---

## 19. Out of scope (khẳng định lại)

Vibefolio v1 KHÔNG phải:
- CV maker / CV editor
- Job board
- Resume parser API for sale
- LinkedIn alternative
- Personal CRM
- Portfolio cho non-tech (designer, sales, marketer) — cân nhắc M5+
- Mobile app (KMP) — cân nhắc post-M4 nếu user request

---

## Phụ lục A — Cấu trúc repo

Đề xuất **monorepo top-level `backend/` + `frontend/`** + Maven (BE) + pnpm workspace (FE) trong cùng git repo:

```
vibefolio/
├── frontend/                         # Next.js FE
│   ├── CLAUDE.md                     # Module-specific Claude context
│   ├── src/
│   │   ├── app/
│   │   ├── components/
│   │   ├── lib/
│   │   └── ...
│   ├── package.json
│   └── next.config.ts
├── backend/                          # Spring Boot BE
│   ├── CLAUDE.md                     # Module-specific Claude context
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/vibefolio/
│   │   │   │   ├── api/
│   │   │   │   ├── service/
│   │   │   │   ├── ai/
│   │   │   │   ├── persistence/
│   │   │   │   ├── storage/
│   │   │   │   ├── email/
│   │   │   │   ├── security/
│   │   │   │   └── config/
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       └── db/migration/     # Flyway
│   │   └── test/
│   └── pom.xml
├── packages/
│   └── api-types/                    # Generated TS types từ OpenAPI
│       ├── schemas.gen.ts
│       └── package.json
├── docs/
│   ├── ARCHITECTURE.md
│   ├── DATA-MODELS.md
│   ├── decisions/                    # ADRs + Y-statements
│   └── superpowers/specs/
│       └── 2026-04-30-vibefolio-design.md
├── scripts/
│   ├── gen-api-types.sh              # Pull spec từ BE → generate TS
│   └── dev.sh                        # Start cả FE + BE local
├── .claude/                          # Claude Code config (settings, hooks, rules)
├── CLAUDE.md                         # Root context (slim, cross-cutting)
├── pnpm-workspace.yaml
├── package.json
├── .gitignore
└── README.md
```

### Rationale monorepo
- Đổi BE schema → BE rebuild → spec update → script `gen:api` → FE TS types update → FE compile error nếu break = phát hiện drift sớm
- 1 git history thấy được full feature
- 1 PR cho 1 feature thường touch cả FE và BE
- **Module-level CLAUDE.md** (backend/CLAUDE.md, frontend/CLAUDE.md) load on demand khi Claude Code work trong module — tiết kiệm context, ít distraction

### Deploy
- **FE**: Vercel — connect GitHub repo, root directory `frontend`, build command `pnpm install && pnpm gen:api && pnpm build`
- **BE**: Railway — connect GitHub repo, root directory `backend`, build qua Nixpacks hoặc Dockerfile (`./mvnw package -DskipTests` → `java -jar target/*.jar`)

---

## Phụ lục B — Bước tiếp theo

1. **Bạn review file này** (đặc biệt mục 5–8 — phần thay đổi lớn) → comment chỗ nào muốn sửa.
2. **Mình apply changes** nếu có.
3. Khi spec final → chuyển sang **implementation plan** (skill `superpowers:writing-plans`) — break down M1 thành milestones nhỏ với deliverable cụ thể (BE skeleton → AI module → REST endpoints → FE landing → FE form → FE preview → FE portfolio render → magic link → polish → ship).
4. Bắt đầu code M1 — Leonard ngồi với Claude Code (cả Java và TS), ship 5-7 tuần.

---

*End of design document v2.*
