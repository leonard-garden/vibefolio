# Project Context — Vibefolio

> Bối cảnh dự án + Leonard background + working style.
> Đọc đầu mỗi major task để Claude hiểu vì sao Vibefolio tồn tại và nên ưu tiên gì.

---

## Vibefolio là gì (1 câu)

Web tool: user upload CV PDF → AI sinh portfolio website → host trên `vibefolio.com/[username]` → share link đi xin việc remote / Upwork.

## Vì sao Vibefolio tồn tại

3 pain point market chưa giải quyết tốt:

1. **CV PDF không paste được vào Upwork "portfolio link"** — Upwork client cần URL có nội dung mô tả công việc của freelancer
2. **LinkedIn không show project depth** — recruiter cần xem case study technical, không phải bullet point
3. **Dev senior tự bán rẻ trong CV** — chức danh "Java Backend Engineer" thay vì "Backend & AI Engineer for Fintech" → mất cơ hội $120-200/giờ

Vibefolio giải quyết bằng AI **reposition** (không chỉ format), nhắm niche dev/AI engineer mid-senior.

## Wedge

**"AI Reposition + Niche backend/AI engineer"** (combo A+E từ brainstorm 2026-04-30).

Implication ưu tiên:
- AI prompt phải có reposition pattern library (xem `domain_knowledge.md`)
- Template default = vibe Brittany Chiang/Lee Robinson (dev senior aesthetic)
- Marketing copy nhắm dev mid-senior, KHÔNG fresh grad / non-tech
- Pricing $7/tháng paid tier (vừa tầm dev, không phải $1 race-to-bottom)

---

## Leonard (founder, user 0, primary persona)

### Background technical
- **6 năm exp**, Senior Java Engineer
- **3 năm fintech** (CIMB Thai Bank via CBTW) — payment, risk-based auth, ESB-to-Mendix migration, AWS KMS, 50+ microservices
- **AI engineering thực chiến**: LangChain + LangGraph + LangFuse + n8n + Local LLM RAG + GitLab AI Code Reviewer
- **Security expertise**: Passkey/WebAuthn, OAuth2/JWT, Keycloak, AWS KMS, Google Play Integrity
- **Cross-platform**: Java BE + KMP mobile + Python AI + React FE
- **Open source**: Mangala Wallet (KMP crypto wallet, MIT)
- **Mentorship**: 50+ student, 10+ placed first job

### Geography & target market
- Hanoi, Vietnam
- Nhắm remote: US, EU, Singapore
- Backup: Upwork freelance ($150+/giờ tier)

### Working style
- **Side project mode**: Vibefolio sau giờ làm full-time CIMB
- **Vibe coding** với Claude Code (Pro/Max plan)
- **Pragmatic**: ship fast > perfect; cắt scope mạnh
- **Java sở trường** (sẽ build BE Spring rất nhanh) + **TS biết** (FE Next.js OK với Claude Code assist)

### Communication preferences (khi chat với Claude)
- Tiếng Việt
- Prose-heavy, ít bullet point ngoài lúc cần liệt kê thật
- Concrete examples > abstract advice
- Không thích bị hỏi quá nhiều câu — "stop analysis paralysis, just ship"
- Confirm major decisions trước khi đi xa, nhưng đừng micro-confirm

---

## Stakeholders

**M1**: chỉ Leonard (user 0 + founder).
**Phase 1b** (sau M1 ship): 3-5 đồng nghiệp dev VN (free tier, feedback exchange).
**Phase 1c**: 10-20 từ network mentorship (free, qualitative interview).
**Phase 2** (M2+): public soft launch Daily.dev VN, Twitter/X dev VN, J2TEAM.

KHÔNG có: investor, board, designer thuê ngoài, marketer.
→ Mọi quyết định Leonard tự chốt, không cần approval flow.

---

## Stack chốt (xem ADRs để biết lý do)

- BE: Spring Boot 3.3 + Java 21 + Maven (~xem `backend/CLAUDE.md`)
- FE: Next.js 15 + TypeScript + Tailwind + shadcn (~xem `frontend/CLAUDE.md`)
- DB: Postgres (Neon free)
- Storage: Cloudflare R2 (free)
- Email: Resend (free)
- AI: Anthropic API self-managed (xem ADR-002)
- Deploy: Vercel (FE) + Railway (BE)
- URL routing: path-based ở M1, subdomain ở M1.5 (xem ADR-003)

---

## North star metrics

**Bắc đẩu**: số portfolio "live + có traffic" mỗi tháng — không phải đăng ký, mà thật sự share.

**M1 success**:
- 5 portfolio sống
- Leonard dùng cho ≥1 application thật (FT remote hoặc Upwork)

**M1.5 success**: 1 paid user $7/tháng (gỡ watermark + custom domain).

**M2 success**: 20 portfolio, 3 paid.

---

## Cost reality check

- Fixed: ~$6/tháng (Railway $5 + domain $1)
- Variable AI: ~$2-5/tháng ở 50 user × 2 gen
- Hard cap: $50/tháng (vào waitlist mode khi chạm)
- Breakeven: 1 paid user cover Railway, 5 paid user cover all infra + AI cho 100+ free users

KHÔNG over-engineer cho viral khi chưa biết có viral. Hard cap $50 là an toàn cho Leonard ngủ ngon.

---

## Boundaries (DO NOT CROSS)

- Vibefolio v1 KHÔNG phải CV maker (chỉ đọc CV có sẵn, không tạo CV)
- Vibefolio KHÔNG phải job board, CRM, social network
- KHÔNG support designer/non-tech ở M1 (cân nhắc M5+)
- KHÔNG dùng Claude Code subscription làm production AI (vi phạm ToS — xem `lessons_learned.md`)
- KHÔNG tự ý reposition aggressive (stretch hơn CV thật) ở M1 — conservative mặc định, M2 thêm "boost mode" toggle

---

## Reminder cho Claude session sau

Nếu Leonard hỏi gì, default ưu tiên:

1. **Ship fast over perfect**. Cắt scope, không thêm feature trừ khi rõ value.
2. **Java BE là comfort zone của Leonard** — đề xuất pattern enterprise quen (DI, AOP, Spring Security) khi hợp lý.
3. **Vibe coding pace** — Leonard pair với Claude Code, không 1 mình type. Code phải clear cho Claude generate đúng lần đầu.
4. **Side project = nights/weekends**. Mỗi task phải done được trong 1 session ngắn (≤ 2h).
5. **OpenAPI là source of truth** giữa BE/FE — không bao giờ viết types FE thủ công.
6. **Cost capped $50/tháng AI** — mọi feature phải tôn trọng cap này.

---

## Append new context below this line

<!-- New project context, persona insights, scope updates go here -->
