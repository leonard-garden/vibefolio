# Decision Log — Lightweight Decisions (Y-Statements)

> Append decisions ở đây dùng Y-Statement format.
> Quyết định lớn → tạo full ADR (xem [README.md](README.md)).
> Module-scoped design decision → dùng [DDR-TEMPLATE.md](_TEMPLATES/DDR-TEMPLATE.md).

## Format

```
### YYYY-MM-DD — [Short title]

**Agent:** [claude-sonnet-4 / claude-opus-4 / human]
**Scope:** [file or module affected]

In the context of [situation],
facing [concern],
I decided [decision]
to achieve [goal],
accepting [tradeoff].
```

## Log

<!-- Append new decisions above this line -->

### 2026-04-30 — Wedge product: AI Reposition + Niche backend/AI engineer

**Agent:** human (Leonard)
**Scope:** product positioning, prompt design, marketing copy

In the context of [thị trường AI portfolio đông đúc với Resumey, Standard Resume, Nimb...],
facing [risk thành "yet another version" không có lý do user chọn],
I decided [combo wedge A (AI Reposition) + E (Niche backend/AI engineer)],
to achieve [moat sắc + segment cao giá + AI prompt focus rõ],
accepting [đóng cửa với designer/non-tech ở M1, mở lại sau M5+].

### 2026-04-30 — Monetization model: Freemium watermark (P1)

**Agent:** human (Leonard)
**Scope:** business model, FE footer, paid tier roadmap

In the context of [side project cần model đơn giản],
facing [chọn giữa P1 watermark / P2 feature gating / P3 lifetime deal],
I decided [P1 — free + watermark "Made with Vibefolio"; paid $7/tháng gỡ watermark + custom domain],
to achieve [conversion cao, viral loop qua watermark, paid tier khả thi sau khi có demand],
accepting [moat paid tier yếu hơn nếu user OK với watermark; phải build paid tier sau].

### 2026-04-30 — MVP scope: M1 = One-shot static (no login, no edit)

**Agent:** human (Leonard)
**Scope:** M1 feature set, ship timeline

In the context of [side project, ship 5-7 tuần],
facing [chọn giữa M1 one-shot / M2 editable / M3 vibe-code chat / M4 job-target],
I decided [M1 = one-shot static — upload → AI → URL, magic link claim, không edit UI, không login],
to achieve [ship nhanh, validate demand, không over-engineer feature chưa biết user cần],
accepting [user không sửa được portfolio, phải re-upload CV để đổi; postpone edit UI tới M2].

### 2026-04-30 — Launch strategy Phase 1: Team-first, không Product Hunt vội

**Agent:** human (Leonard)
**Scope:** GTM, marketing channel ưu tiên

In the context of [Vibefolio v1 chưa được test bởi user thật],
facing [pressure launch Product Hunt sớm để có signal],
I decided [Phase 1a self → 1b inner circle 3-5 dev → 1c extended network 10-20 → Phase 2 soft public với Daily.dev VN/Twitter dev VN],
to achieve [feedback chất lượng cao + testimonial trước khi expose rộng + tránh launch flop],
accepting [growth chậm 2-3 tháng đầu; Product Hunt deferred tới khi paid tier ready].
