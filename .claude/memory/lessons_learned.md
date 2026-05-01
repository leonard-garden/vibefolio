# Lessons Learned — Vibefolio

> Append-only log của bài học từ Claude làm sai → rule mới được thêm.
> Format chuẩn xem [`README.md`](./README.md).
> Trước khi đề xuất rule mới hoặc sửa rule trong CLAUDE.md, **đọc file này trước** để tránh lặp.

---

## Format reference

```markdown
## YYYY-MM-DD — Short descriptive title

**Context**: 1-2 câu tình huống
**What happened**: Claude làm gì → kết quả
**Lesson**: Insight
**Rule added**: Link rule mới (CLAUDE.md / security.md / pattern file)
```

---

## Seed entries (từ brainstorm M1)

### 2026-04-30 — KHÔNG dùng Claude Code subscription làm production AI backend

**Context**: Trong brainstorm M1, Leonard hỏi "có thể login Claude Code subscription trên server và backend gọi `claude -p` thay vì Anthropic API không?"

**What happened**: Idea sounds clever (tiết kiệm cost subscription thay vì pay-per-token), nhưng vi phạm Anthropic ToS (subscription là dev tool cá nhân, không phải redistribution license). Sẽ bị ban tài khoản, single point of failure, không có observability, rate limit subscription thiết kế cho 1 con người (sập với 10 user upload đồng thời).

**Lesson**: Subscription = dev tool. API = production. Hai cái khác nhau về license, kỹ thuật, và cost model. Math còn cho thấy ở v1 scale (50 user, 100 generations/tháng), API ~$6/tháng còn subscription Max $200/tháng → API rẻ hơn 33 lần ở M1.

**Rule added**:
- [CLAUDE.md §Cross-cutting security](../../CLAUDE.md) — "NEVER dùng Claude Code subscription làm production AI backend"
- [backend/CLAUDE.md §Don't](../../backend/CLAUDE.md) — same rule restated cho BE context
- [.claude/hooks/validate-command.py](../hooks/validate-command.py) — block pattern `claude -p .* >> .env`
- [docs/decisions/ADR-002-ai-self-managed-api.md](../../docs/decisions/ADR-002-ai-self-managed-api.md) — full decision context

---

### 2026-04-30 — KHÔNG để FE browser gọi BE trực tiếp

**Context**: Khi thiết kế communication FE↔BE, có ý tưởng để browser fetch thẳng `https://api.vibefolio.com/v1/portfolios` với header `X-API-Key`.

**What happened**: Nếu làm vậy, key lộ trong network tab DevTools — anyone xem được. Attacker copy key → đốt cost AI / spam form / DOS BE.

**Lesson**: Browser KHÔNG bao giờ chạm BE trực tiếp khi cần auth. Luôn proxy qua FE Route Handler (`/api/proxy/[...path]`) — Route Handler chạy server-side trên Vercel, attach key, forward request, trả response. Key chỉ tồn tại trong env Vercel, không bao giờ ship xuống browser.

**Rule added**:
- [CLAUDE.md §Cross-stack architecture rules](../../CLAUDE.md) — "Browser KHÔNG gọi BE trực tiếp — qua FE proxy"
- [frontend/CLAUDE.md §Don't](../../frontend/CLAUDE.md) — "Don't gọi BE trực tiếp từ browser — luôn qua /api/proxy/[...path]"
- [docs/ARCHITECTURE.md §API key giữa FE và BE](../../docs/ARCHITECTURE.md) — full pattern

---

## Append new entries below this line

<!-- New lessons go here, newest at bottom. Keep chronological order. -->
