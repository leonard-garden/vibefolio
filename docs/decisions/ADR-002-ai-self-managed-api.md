# ADR-002: Anthropic API self-managed (không BYOK, không Claude Code subscription làm backend)

- **Status:** accepted
- **Date:** 2026-04-30
- **Deciders:** Leonard
- **Supersedes:** —

## Context

Vibefolio cần gọi LLM (Claude) để extract + reposition CV. 3 cách kỹ thuật khả thi:

1. **Self-managed Anthropic API** — Vibefolio dùng API key của Leonard, gọi qua Anthropic Java SDK
2. **BYOK (Bring Your Own Key)** — User nhập API key của họ vào Vibefolio
3. **Claude Code subscription as backend** — Login Claude Code trên server, backend `exec("claude -p '...'")`

Ràng buộc:
- Side project, lo cost runaway nếu viral
- Target user: dev/AI engineer mid-senior — có thể có Anthropic account, nhưng đa phần lười setup
- Cần bảo vệ moat (AI reposition là value chính)
- AI cost thực tế ở v1 scale rất nhỏ ($2-5/tháng cho 50 user)

## Decision

Chọn **Hướng 1 — Self-managed Anthropic API** với 5 đòn bẩy cost protection:

1. Default model **Claude Haiku 4.5**, escalate **Sonnet 4.6** chỉ khi Haiku fail Bean Validation
2. **Prompt caching** cho system prompt (reposition rules + examples)
3. **Cache by PDF SHA-256 hash** — re-upload cùng file = $0 (DB lookup only)
4. **Rate limit Bucket4j**: 3 gen/email/24h, 10 gen/IP/24h
5. **Hard cap toàn cục $50/tháng** — query `SUM(generations.cost_usd)` mỗi request, ≥ cap → return 503 với `{ waitlist: true }`

Anthropic API key lưu trong env BE only (`ANTHROPIC_API_KEY`), không bao giờ expose FE/browser. Anthropic console alert ở $40/tháng.

BYOK postpone tới M3 như **power-user mode option** (bên cạnh default Vibefolio-managed), không thay thế.

## Alternatives Considered

### BYOK (Bring Your Own Key) only
- **Pros:**
  - $0 AI cost cho founder
  - Capped naturally by user's own usage
- **Cons:**
  - **Friction giết conversion** — drop 90%+ ở bước "nhập API key"
  - Đóng cửa với 70% TAM (designer, freelancer, non-Anthropic-user)
  - **Từ bỏ moat** — user paid Anthropic thẳng = không có lý do trả Vibefolio
  - Trust issue, encrypt key, audit complexity
  - Looks unprofessional với non-dev visitor

### Claude Code subscription as backend
- **Pros:**
  - Cost capped theo subscription tier
- **Cons:**
  - **Vi phạm ToS Anthropic** — subscription là dev tool cá nhân, không phải redistribution license. Sẽ bị ban tài khoản.
  - Rate limit subscription thiết kế cho 1 con người, sập với 10 user upload đồng thời
  - Single point of failure — account lock → product die
  - Auth token Claude Code không phù hợp headless server (interactive browser flow)
  - Không có observability cho production AI calls
  - Math: Max plan $200/tháng so với API $6/tháng ở v1 scale → đắt gấp 33 lần

## Consequences

### Positive

- **Conversion cao** — user upload CV không cần signup, không cần API key, không cần card. Chỉ email + username + file.
- **Moat bảo toàn** — AI reposition là value chính, monetize được qua paid tier ($7/tháng cho Sonnet + custom domain + gỡ watermark)
- **Cost predictable** — hard cap $50/tháng, worst case = vào waitlist mode
- **Build với Anthropic Java SDK chính thức** — observability đầy đủ, prompt caching native, structured output qua tool use
- **Có path scale rõ** — M3 thêm BYOK power mode, M4 thêm BYOK + custom model selection cho power user

### Negative

- **Founder chịu cost variable** — ~$2-5/tháng ở v1 nhưng có thể tăng nếu viral
- **Phải build cost monitoring** — generations table + monthly rollup query + waitlist mode toggle
- **Phải pre-fund Anthropic credit** — nạp $20-50 trước khi launch
- **Default Haiku có thể miss reposition quality** — mitigation: escalate Sonnet khi validation fail

### Risks

- **AI cost runaway nếu viral đột ngột** — Mitigation: hard cap $50, alert $40, waitlist mode tự động
- **Anthropic pricing thay đổi** — Mitigation: model name + pricing trong `@ConfigurationProperties`, đổi nhanh
- **Spam abuse đốt cost** — Mitigation: Cloudflare Turnstile + Bucket4j rate limit
- **Haiku quality thấp hơn dự kiến** — Mitigation: A/B test với 10 CV thật của Leonard + đồng nghiệp; nếu Haiku miss > 30%, raise default lên Sonnet và bù bằng prompt caching cost saving

## References

- [Design Doc M1 §7 — AI pipeline](../superpowers/specs/2026-04-30-vibefolio-design.md)
- [Design Doc M1 §12 — Cost model & protection](../superpowers/specs/2026-04-30-vibefolio-design.md)
- Anthropic API docs: https://docs.claude.com/en/api
- Anthropic prompt caching: https://docs.claude.com/en/docs/build-with-claude/prompt-caching
