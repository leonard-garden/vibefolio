# Decision Records

Folder này track các quyết định quan trọng trong project lifetime của Vibefolio.

## Decision Types

| Type | When to use | Template |
|------|-------------|----------|
| **ADR** (Architecture Decision Record) | Quyết định kiến trúc ảnh hưởng toàn hệ thống | [_TEMPLATES/ADR-TEMPLATE.md](_TEMPLATES/ADR-TEMPLATE.md) |
| **DDR** (Design Decision Record) | Quyết định design trong scope module | [_TEMPLATES/DDR-TEMPLATE.md](_TEMPLATES/DDR-TEMPLATE.md) |
| **Y-Statement** | Lightweight decisions trong lúc coding | Append vào [DECISION-LOG.md](DECISION-LOG.md) |

## When to Create an ADR

Trả lời YES cho ≥ 2 câu sau:
- Có affect code across many modules?
- Hard to reverse (> 1 day to revert)?
- Đổi external interface (API, DB schema)?
- Other team members cần biết để code đúng?
- Đổi important dependency?

## How to Create an ADR

1. Copy `_TEMPLATES/ADR-TEMPLATE.md` → `ADR-NNN-short-title.md`
2. Fill in ALL sections — context và alternatives quan trọng nhất
3. Status: `proposed` → review → `accepted`
4. Add entry vào index dưới đây

## Decision Index

| ID | Title | Status | Date |
|----|-------|--------|------|
| [ADR-001](ADR-001-spring-nextjs-split.md) | Tách BE Spring Boot + FE Next.js | accepted | 2026-04-30 |
| [ADR-002](ADR-002-ai-self-managed-api.md) | Anthropic API self-managed (không BYOK, không Claude Code subscription) | accepted | 2026-04-30 |
| [ADR-003](ADR-003-path-based-routing-m1.md) | Path-based routing M1, wildcard subdomain M1.5+ | accepted | 2026-04-30 |
