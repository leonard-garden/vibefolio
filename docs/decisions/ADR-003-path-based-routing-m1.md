# ADR-003: Path-based routing ở M1, wildcard subdomain ở M1.5+

- **Status:** accepted
- **Date:** 2026-04-30
- **Deciders:** Leonard
- **Supersedes:** —

## Context

Vibefolio cần URL public cho mỗi portfolio. 2 pattern phổ biến:

1. **Path-based**: `vibefolio.com/leonard`
2. **Subdomain**: `leonard.vibefolio.com`

Subdomain trông pro hơn (đặc biệt cho Upwork link), nhưng wildcard subdomain trên Vercel **cần Pro plan ($20/tháng)**. Vibefolio M1 là side project, mục tiêu fixed cost $0 hoặc gần $0.

Cùng lúc, M1 chưa có paid tier (P1 freemium watermark, build paid ở M1.5 sau khi có ≥10 free user). Custom domain (CNAME) là feature paid. Subdomain wildcard và custom domain cùng yêu cầu Vercel Pro.

## Decision

**M1 dùng path-based routing**: `vibefolio.com/[username]`. Vercel Hobby tier (free).

**M1.5 chuyển sang wildcard subdomain + custom domain** cùng lúc với launch paid tier:
- Upgrade Vercel Pro
- DNS wildcard `*.vibefolio.com` → Vercel
- Next.js middleware detect host header → rewrite subdomain → `/[username]`
- Path `/[username]` redirect 301 → subdomain (preserve SEO)
- Stripe Checkout cho paid tier ($7/tháng)

URL convention M1:
- `vibefolio.com/` — landing
- `vibefolio.com/[username]` — public portfolio
- `vibefolio.com/preview/[tempId]` — preview trước claim
- `vibefolio.com/update` — re-upload form
- `api.vibefolio.com/v1/*` — BE API (Railway custom domain CNAME)

## Alternatives Considered

### Subdomain wildcard từ M1
- **Pros:**
  - Trông pro hơn ngay từ đầu
  - Dễ migrate user sang custom domain sau (cùng pattern)
  - Tốt cho SEO (mỗi portfolio có "domain authority" riêng — debate)
- **Cons:**
  - $20/tháng Vercel Pro từ ngày 1 (vs $0 Hobby)
  - DNS wildcard phức tạp setup ban đầu, dev local cần `*.localhost` hosts trick
  - Add complexity middleware + host detection — mất 2-3 ngày dev không đáng giá

### Hybrid (cả path + subdomain redirect lẫn nhau)
- **Pros:** Linh hoạt
- **Cons:** SEO duplicate content nếu không config canonical đúng; UX confusing

### Subdomain via Cloudflare Pages thay Vercel
- **Pros:** Free wildcard
- **Cons:** Next.js trên Cloudflare Pages có limitations (Server Actions, ISR khác Vercel); migration risk; Claude Code training data dày nhất với Vercel pattern

## Consequences

### Positive

- **$0 fixed cost ở M1** — Vercel Hobby + Railway hobby ($5) + domain ($1) = $6/tháng total
- **Đơn giản — KHÔNG middleware host detection, KHÔNG DNS wildcard, KHÔNG dev local hosts hack
- **Migration M1 → M1.5 dễ** — chỉ thêm middleware rewrite + 301 redirect, code routing không đổi
- **SEO không bị mất** ở M1.5 vì 301 redirect preserve link juice
- **Username uniqueness logic giống nhau** giữa path và subdomain — code reuse 100%

### Negative

- **URL kém pro hơn** ở M1 — `vibefolio.com/leonard` vs `leonard.vibefolio.com`
- **Mất 1 selling point** cho Upwork client paste link (nhưng path vẫn workable)
- **Reserved username list dài hơn 1 chút** vì phải reserve cả route name (`/api`, `/preview`, `/update`, `/swagger-ui`...)

### Risks

- **User feedback "URL trông không xịn"** — Mitigation: roadmap M1.5 có subdomain trong vòng 2-3 tuần sau M1; communicate rõ với early user
- **Path-based routing conflict với reserved word** — Mitigation: blocklist + DB check + test edge cases
- **301 redirect M1.5 break old shared links** — Mitigation: redirect đúng pattern, không mất URL

## References

- [Design Doc M1 §10 — Subdomain & hosting strategy](../superpowers/specs/2026-04-30-vibefolio-design.md)
- [Design Doc M1 §13 — Monetization](../superpowers/specs/2026-04-30-vibefolio-design.md)
- Vercel pricing: https://vercel.com/pricing
- Next.js middleware host rewrite pattern: https://nextjs.org/docs/app/building-your-application/routing/middleware
