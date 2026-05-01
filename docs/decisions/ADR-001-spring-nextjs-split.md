# ADR-001: Tách Backend Spring Boot và Frontend Next.js

- **Status:** accepted
- **Date:** 2026-04-30
- **Deciders:** Leonard (Trinh Van Dat)
- **Supersedes:** —

## Context

Vibefolio cần kiến trúc cho M1 (5–7 tuần ship) và scale path tới M4. 3 ràng buộc chính:

1. **Side project, ít thời gian** — phải ship nhanh, không over-engineer
2. **Background dev**: 6 năm Java backend (CIMB Thai Bank), 3 năm Spring fintech-grade. Vibe coding với Claude Code (cả TS và Java).
3. **Future considerations**: M3 cần WebSocket/SSE cho vibe-code chat. Có thể build mobile app KMP sau M4.

3 phương án xem xét:
- **Hướng 1 — Monolithic Next.js full-stack** (Server Actions, không có BE riêng)
- **Hướng 2 — Tách BE Spring Boot + FE Next.js** (REST + OpenAPI contract)
- **Hướng 3 — Tách BE Hono/TS + FE Next.js** (lightweight TS BE)

## Decision

Chọn **Hướng 2 — Tách BE Spring Boot 3.3 (Java 21) + FE Next.js 15 (TypeScript)**, giao tiếp qua REST API có OpenAPI 3 contract (springdoc auto-gen), FE generate TypeScript types từ spec qua `openapi-typescript`. Monorepo với pnpm workspace + Maven.

Boundaries cứng:
- BE truy cập DB, R2, Anthropic API, Resend
- FE chỉ gọi BE qua proxy `/api/proxy/[...path]` để giấu `BE_API_KEY`
- OpenAPI spec là single source of truth; FE không viết types thủ công
- Repo layout: top-level `backend/` (Spring Boot Maven project) và `frontend/` (Next.js pnpm workspace), mỗi folder có `CLAUDE.md` riêng cho stack-specific context

## Alternatives Considered

### Hướng 1 — Monolithic Next.js full-stack
- **Pros:**
  - Ship nhanh nhất (~3-4 tuần thay vì 5-7)
  - 1 codebase, 1 deploy, type-safe Server Actions tự động
  - $0 fixed cost (không cần Railway BE host)
- **Cons:**
  - Bỏ phí 6 năm Java expertise của Leonard
  - Mobile app sau này phải build BE từ đầu hoặc dùng Server Actions từ mobile (awkward)
  - Tied vào Next.js patterns; khó swap stack

### Hướng 3 — Tách BE Hono (TypeScript) + FE Next.js
- **Pros:**
  - Type-share end-to-end native (Hono RPC client)
  - Cloudflare Workers free tier rộng, $0 fixed cost
  - Cùng ngôn ngữ TS giữa FE/BE
- **Cons:**
  - Leonard phải học Hono (1-2 ngày setup)
  - Anthropic Java SDK có thể vẫn cần khi scale (Java tooling ecosystem mạnh)
  - Bỏ phí Java expertise

## Consequences

### Positive

- **Leverage Java fintech expertise** — Leonard ship code BE Spring nhanh hơn TS với confidence cao hơn
- **Type safety end-to-end** qua OpenAPI contract dù tách stack — FE compile error nếu BE breaking change
- **Future-proof cho mobile** — KMP app reuse được BE 100%
- **Pattern enterprise quen** — DI, AOP, Spring Security, JPA — Leonard đã master
- **AI Java SDK đủ tốt** cho M1-M2; M3 nếu cần Claude Agent SDK Java chưa có thì viết tool-use loop thuần (Anthropic Java SDK hỗ trợ tool use native)
- **CV showcase nhất quán** — Vibefolio thành showcase "Java + AI" trên CV/Upwork của Leonard

### Negative

- **Timeline M1 +1-2 tuần** so với monolithic (5-7 vs 3-4)
- **Fixed cost +$5/tháng** (Railway hobby cho BE)
- **Phải maintain 2 codebase** — TS FE + Java BE
- **OpenAPI generation pipeline thêm 1 build step** — không đồng bộ thì FE compile error
- **Cold start Railway hobby ~3-10s** — cần UptimeRobot ping hoặc upgrade Pro nếu chạm
- **Anthropic Java SDK chậm update feature hơn Node SDK** — fallback REST direct call nếu cần feature mới

### Risks

- **BE/FE schema drift** nếu quên regen TS types — Mitigation: CI step `pnpm gen:api && git diff --exit-code packages/api-types` để fail nếu drift
- **Latency BE → Anthropic** từ Railway (region) — Mitigation: deploy Railway region gần nhất tới Anthropic (US East)
- **Java cold start chậm dev local** — Mitigation: Spring DevTools, hot reload qua `bootRun --continuous`

## References

- [Design Doc M1 §5 — Tech stack](../superpowers/specs/2026-04-30-vibefolio-design.md)
- [Design Doc M1 §6 — Kiến trúc tổng thể](../superpowers/specs/2026-04-30-vibefolio-design.md)
- Brainstorm conversation: 2026-04-30 với product-management:product-brainstorming
