# Security Rules — Vibefolio

> Loaded on demand bởi Claude khi làm việc với code có security implication.
> Hard rules trong CLAUDE.md là minimum. File này là full checklist.

## Secrets Management

### NEVER

- **NEVER** hardcode secret trong code: API key, DB password, JWT secret, signing key
- **NEVER** commit `.env`, `.env.local`, `.env.production`, `application-local.yml`, `application-prod.yml`, `*.pem`, `*.key`
- **NEVER** echo / print / log secret value
- **NEVER** đưa secret vào git history (kể cả commit cũ — phải `git filter-branch` nếu lỡ; better: rotate key)
- **NEVER** dùng Claude Code subscription làm production AI backend (vi phạm Anthropic ToS)
- **NEVER** đưa `BE_API_KEY` ra browser-side code (phải qua FE proxy `/api/proxy/[...path]`)

### ALWAYS

- **ALWAYS** dùng env var: `ANTHROPIC_API_KEY`, `BE_API_KEY`, `DB_URL`, `R2_ACCESS_KEY_ID`, `R2_SECRET_ACCESS_KEY`, `RESEND_API_KEY`, `STRIPE_SECRET_KEY`, `MAGIC_LINK_SIGNING_SECRET`
- **ALWAYS** validate env var present at startup (Spring `@ConfigurationProperties` + `@Validated`)
- **ALWAYS** rotate secret nếu nghi ngờ leak
- **ALWAYS** dùng `.env.example` để document env vars cần thiết (không có value)

## Input Validation

### Boundary validation rule

Mọi input từ user/external phải validate ở **boundary**:
- BE: `@RestController` method param có Bean Validation annotation (`@Valid`, `@NotBlank`, `@Email`, `@Size`, `@Pattern`)
- FE: form submit dùng Zod schema parse trước khi gửi BE
- AI output: `PortfolioJsonValidator` chạy Bean Validation sau khi Claude trả về

### Specific validations

- **PDF upload**: max 5MB, MIME type `application/pdf` (check magic bytes, không chỉ filename)
- **Username**: `^[a-z0-9-]{3,30}$`, lowercase only, không nằm trong reserved list, không nằm trong profanity blocklist
- **Email**: RFC 5322 compliant, max 255 chars, lowercase normalized
- **Magic link token**: base64url 32 bytes, không expired, chưa used, match purpose
- **CV blob URL**: phải là R2 URL của Vibefolio account (không cho user inject URL ngoài)

## Database Access

### NEVER

- **NEVER** dùng raw SQL string concatenation:
  ```java
  // WRONG
  em.createNativeQuery("SELECT * FROM portfolios WHERE username = '" + username + "'");
  ```
- **NEVER** trust input là "đã clean" — luôn parameterize

### ALWAYS

- **ALWAYS** dùng JPA hoặc named parameters:
  ```java
  // RIGHT
  portfolioRepo.findByUsername(username);
  // hoặc
  em.createQuery("SELECT p FROM PortfolioEntity p WHERE p.username = :u")
    .setParameter("u", username);
  ```
- **ALWAYS** dùng `@Transactional` cho operation nhiều bước modify DB
- **ALWAYS** Flyway cho schema change — không bao giờ `psql` trực tiếp prod

## AI Pipeline Security

### NEVER

- **NEVER** include user PII trong system prompt (system prompt static + cached)
- **NEVER** log toàn bộ AI response chứa email/phone — log hash + metadata thôi
- **NEVER** cho AI trả về raw HTML/script được render trong portfolio (XSS risk) — chỉ trả structured JSON, FE escape khi render
- **NEVER** retry AI call vô hạn nếu fail — max 1 escalate Haiku → Sonnet

### ALWAYS

- **ALWAYS** check `WAITLIST_MODE` flag trước mỗi AI call
- **ALWAYS** check rate limit (Bucket4j) trước AI call
- **ALWAYS** check cost cap (`SUM(generations.cost_usd)` tháng) trước AI call
- **ALWAYS** validate AI output với Bean Validation; fail → fail loud, không silent
- **ALWAYS** sanitize Markdown từ AI output trước khi render (nếu cho phép Markdown trong fields)

## Auth (Magic Link)

### Token requirements

- Format: base64url của 32 random bytes (`SecureRandom.nextBytes(new byte[32])`)
- Lifetime: 24h (`expiresAt = createdAt + 24h`)
- One-time use: `usedAt` set sau verify thành công
- Purpose-bound: `purpose` enum CHECK match khi verify (claim ≠ update)
- Rate limit: max 3 magic link / email / hour

### NEVER

- **NEVER** dùng UUID làm magic link token (entropy thấp, predictable nếu attacker biết MAC address)
- **NEVER** include token trong URL query string log (server log + browser history)
- **NEVER** reuse token sau khi `usedAt` set
- **NEVER** send token qua kênh không secure (HTTP plain) — chỉ HTTPS + email

### ALWAYS

- **ALWAYS** check expiry + used + purpose trước verify
- **ALWAYS** invalidate tất cả magic link active của email khi user request mới (security best practice)
- **ALWAYS** SPF/DKIM/DMARC config cho email domain (Resend domain verified)

## CORS & API Key (FE ↔ BE)

### Spring Security config (BE)

- CORS allowedOrigins: `https://vibefolio.com`, `http://localhost:3000` (dev)
- CORS allowedMethods: `GET`, `POST` only
- CORS allowedHeaders: `X-API-Key`, `X-Magic-Token`, `Content-Type`
- `X-API-Key` filter chạy trước Spring Security chain, return 401 nếu thiếu/sai

### FE (Next.js)

- `BE_API_KEY` chỉ trong env var Vercel (server-side), không expose qua `NEXT_PUBLIC_*`
- Browser request luôn qua `/api/proxy/[...path]` Route Handler — Route Handler attach key, forward BE
- Nếu key leak (bị lộ trong client bundle, GitHub commit), rotate ngay: regenerate key trong BE config, deploy FE Vercel với env mới

## Privacy & PII

### Default privacy settings

- AI prompt instruct: ẩn `phone` và `address` khỏi `data.person.contact` trừ khi CV explicit có
- User opt-in show phone (M2 setting)
- Email luôn show (vì là contact chính)

### Data retention

- PDF gốc: lưu R2 30 ngày kể từ `updated_at` cuối; cron BE xóa R2 object, set `cv_blob_url = null`
- Portfolio JSON: giữ vô hạn (nội dung chính)
- Magic link expired/used: cron xóa sau 7 ngày
- Generation log: giữ 90 ngày cho audit, sau đó archive (hoặc delete email + ip để anonymize)

### GDPR-ish requirements (M2+)

- User có thể request "delete my portfolio" qua email → BE xóa portfolio + R2 + magic links + generations với email đó
- Privacy policy + Terms link ở footer (M1.5+)

## Logging

### NEVER

- **NEVER** log secret value (API key, DB password, magic link token)
- **NEVER** log user PII raw (email, phone, full name) — log hash hoặc redacted
- **NEVER** log full PDF content
- **NEVER** log full AI response chứa person data — chỉ log metadata

### ALWAYS

- **ALWAYS** structured log (JSON) ở prod cho query
- **ALWAYS** redact field nhạy cảm: `email` → `e***@***.com`, `token` → `***`
- **ALWAYS** log error context (request ID, user email hash) cho debug

## Dependency Management

### Backend

- **ALWAYS** check `./gradlew dependencyUpdates` trước release minor — security patches
- **ALWAYS** review CVE mới cho Spring Boot, Anthropic SDK, Hibernate
- **NEVER** add dependency không known maintainer (supply chain risk)

### Frontend

- **ALWAYS** `pnpm audit` trước release
- **ALWAYS** review CVE cho Next.js, React, dependencies > 1k weekly downloads
- **NEVER** install package chưa search được trên npm + GitHub repo legit

## Deployment

- **NEVER** push secret qua git → CI → deploy. Dùng platform secret store (Vercel env vars, Railway secrets)
- **NEVER** deploy với `application-local.yml` chưa rename — phải dùng env var override
- **ALWAYS** smoke test sau deploy: health check + 1 generation thử
- **ALWAYS** rollback plan: Vercel & Railway có 1-click rollback, biết cách dùng

## Incident Response

Nếu nghi ngờ leak / breach:

1. **Rotate** ngay key bị nghi: Anthropic, Resend, R2, BE_API_KEY
2. **Invalidate** all active magic links: `UPDATE magic_links SET used_at = now() WHERE used_at IS NULL`
3. **Revoke** Vercel/Railway access nếu compromised account
4. **Audit** generations log + magic_links log cho activity bất thường
5. **Notify** affected users nếu PII leak (GDPR 72h notification)
6. **Postmortem** ghi vào `docs/decisions/` với root cause + remediation
