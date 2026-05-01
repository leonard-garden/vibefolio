# Data Models — Vibefolio

> Last verified: 2026-04-30
> Schema source: `backend/src/main/resources/db/migration/` (Flyway SQL) + `backend/src/main/java/com/vibefolio/persistence/entity/` (JPA)
> Portfolio JSON schema source: `backend/src/main/java/com/vibefolio/ai/model/PortfolioJson.java` (Java records, OpenAPI-exported)

## Entity Relationship Overview

```
┌──────────────┐         ┌─────────────┐
│  portfolios  │ 1───N   │ generations │
│              │         │             │
│ id (UUID)    │         │ portfolioId │
│ username UQ  │         │ model       │
│ email IDX    │         │ tokens      │
│ cv_blob_url  │         │ cost_usd    │
│ cv_hash IDX  │         │ status      │
│ data JSONB   │         │ createdAt   │
│ status       │         └─────────────┘
│ createdAt    │
│ claimedAt    │         ┌─────────────┐
│ updatedAt    │ 1───N   │ magic_links │
└──────────────┘         │             │
                         │ portfolioId │
                         │ email       │
                         │ token UQ    │
                         │ purpose     │
                         │ expiresAt   │
                         │ usedAt      │
                         └─────────────┘
```

`portfolios.data` (JSONB) chứa **PortfolioJson** — schema nested do AI generate (xem mục cuối).

## Core Models

### portfolios

Portfolio chính của một user. 1 user = 1 portfolio (M1; M2+ có thể nhiều portfolio per user).

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| id | uuid | ✅ | Primary key, auto-gen |
| username | varchar(30) | ✅ | UNIQUE, indexed. Pattern `[a-z0-9-]{3,30}`. Reserved + blocklist. |
| email | varchar(255) | ✅ | Indexed cho lookup khi re-upload. Owner contact. |
| cv_blob_url | text | ✅ | URL R2 của PDF gốc |
| cv_hash | varchar(64) | ✅ | SHA-256 PDF bytes. Indexed cho cache lookup. |
| data | jsonb | ✅ | PortfolioJson (xem schema dưới). Hibernate `@JdbcTypeCode(SqlTypes.JSON)` |
| status | varchar(20) | ✅ | `PENDING` (chưa confirm email) / `LIVE` (public) |
| created_at | timestamp | ✅ | Auto |
| claimed_at | timestamp | nullable | Set khi user click magic link confirm |
| updated_at | timestamp | ✅ | Auto-update |

**Relationships:**
- 1:N `magic_links` (claim + update tokens)
- 1:N `generations` (lịch sử AI gen)

**Indexes:**
- `username` UNIQUE
- `email` (b-tree, cho re-upload lookup)
- `cv_hash` (b-tree, cho cache)
- `status` (cho query `WHERE status = 'LIVE'`)

### magic_links

Token một lần dùng để confirm portfolio claim hoặc trigger re-upload flow.

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| id | uuid | ✅ | PK |
| email | varchar(255) | ✅ | Email recipient |
| portfolio_id | uuid | nullable | FK → portfolios.id (nullable cho update flow chưa có portfolio link) |
| token | varchar(64) | ✅ | UNIQUE, base64url của 32 random bytes |
| purpose | varchar(20) | ✅ | `CLAIM` / `UPDATE` |
| expires_at | timestamp | ✅ | Tạo lúc + 24h |
| used_at | timestamp | nullable | Set khi token được verify thành công (one-time use) |

**Indexes:**
- `token` UNIQUE
- `email` (cho rate limit query)
- Composite `(email, used_at, expires_at)` cho rate limit + cleanup

**Constraints:**
- `purpose` CHECK trong (`CLAIM`, `UPDATE`)
- `used_at IS NULL` hoặc `used_at >= created_at`

### generations

Lịch sử mỗi lần gọi Anthropic AI. Dùng cho:
- Audit cost (`SUM(cost_usd) WHERE created_at > start_of_month` cho hard cap $50)
- Debug fail cases
- Rate limit per email/IP
- Future analytics

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| id | uuid | ✅ | PK |
| portfolio_id | uuid | nullable | FK → portfolios.id (nullable nếu gen fail trước khi save portfolio) |
| email | varchar(255) | ✅ | Cho rate limit |
| ip_address | varchar(45) | nullable | Real IP từ X-Forwarded-For (Vercel) |
| model | varchar(50) | ✅ | `claude-haiku-4-5` / `claude-sonnet-4-6` |
| input_tokens | int | nullable | Anthropic response usage |
| output_tokens | int | nullable | Anthropic response usage |
| cost_usd | decimal(8,5) | nullable | Tính từ tokens × pricing |
| duration_ms | int | nullable | Latency total call |
| status | varchar(20) | ✅ | `SUCCESS` / `FAIL` / `CACHED` |
| error_code | varchar(100) | nullable | Nếu fail (`SCHEMA_VALIDATION` / `ANTHROPIC_ERROR` / `TIMEOUT`) |
| created_at | timestamp | ✅ | Auto |

**Indexes:**
- `(email, created_at)` cho rate limit query
- `(ip_address, created_at)` cho rate limit query
- `created_at` cho cost rollup
- `status` cho fail analysis

## Key Constraints

- **Username uniqueness**: case-insensitive. Save lowercase, lookup lowercase.
- **Magic link one-time use**: `used_at` set sau khi verify thành công. Tái sử dụng = 410 Gone.
- **Magic link expiry**: 24h từ create. Expired = 410 Gone (UI prompt request mới).
- **Portfolio status transition**: `PENDING → LIVE` (qua claim magic link). Không có path quay lại.
- **Rate limit**: 3 generations / email / 24h, 10 / IP / 24h. Bucket4j check trong service trước khi gọi AI.
- **Cost hard cap**: trước mỗi gen mới, query `SUM(cost_usd) WHERE created_at > date_trunc('month', now())`. Nếu ≥ $50 → return 503 với `{ waitlist: true }`.
- **PDF retention**: BE cron xóa R2 object sau 30 ngày kể từ `updated_at` cuối; `cv_blob_url` set null. Giữ `data` JSON.
- **Privacy default**: AI prompt instruct ẩn phone/address khỏi `data.person.contact`. Chỉ show email.

## Migration Strategy

- **Tool**: Flyway. Migrations trong `backend/src/main/resources/db/migration/V{N}__{description}.sql`
- **Naming**: `V001__create_portfolios.sql`, `V002__create_magic_links.sql`, ...
- **Backward compat**: Không drop column trong cùng release với code dùng nó. Pattern: deprecate → wait release → remove.
- **No raw SQL trong code production**: tất cả schema change qua Flyway. Service dùng JPA hoặc Spring Data named queries.
- **Seed**: `backend/src/main/resources/db/seed/dev-seed.sql` cho dev local. Không chạy ở prod.
- **Rollback**: Flyway không auto-rollback. Manual: viết migration mới reverse.

## Portfolio JSON schema (nested trong `portfolios.data`)

Schema này là **output của AI**, validate bằng Bean Validation trong `PortfolioJsonValidator`. Source of truth: `com.vibefolio.ai.model.PortfolioJson.java` (Java records). FE consume qua OpenAPI generated TS types.

```
PortfolioJson {
  schemaVersion: 1
  person: {
    name, headline (max 80), location,
    availableFor: enum FULLTIME|FREELANCE|BOTH|NOT_LOOKING,
    pronouns?, contact: { email, github, linkedin, twitter, website, phone },
    avatarUrl?
  }
  summary: string (max 280)
  specialties: Specialty[3]   // exactly 3
  projects: Project[max 6]
  experience: Experience[]
  skills: { primary: string[max 8], secondary: string[max 20] }
  education: Education[]
  credentials: Credential[]
}

Specialty {
  icon: enum CODE|SHIELD|SPARKLES|DATABASE|CLOUD|CPU
  title (max 40), description (max 160)
}

Project {
  title, role?, period?,
  problem, approach, impact,    // 3 paragraphs (Problem→Approach→Impact format)
  tech: string[max 10],
  links: Link[max 3],
  featured: boolean
}

Experience {
  company, role, period, location?,
  highlights: string[max 5]   // bullet rewrites
}

Education { school, degree, period }

Credential {
  type: enum MENTORSHIP|OSS|CERTIFICATION|SPEAKING|WRITING,
  title, description
}
```

Full Java definitions: `backend/src/main/java/com/vibefolio/ai/model/PortfolioJson.java`.
Full design rationale: [`superpowers/specs/2026-04-30-vibefolio-design.md` §8](superpowers/specs/2026-04-30-vibefolio-design.md).
