# Memory — Vibefolio

Long-term context Claude đọc on-demand giữa các session. Khác với:

- **`CLAUDE.md`** = active rules (auto-load mỗi session)
- **`.claude/rules/security.md`** = full security checklist (load on-demand qua CLAUDE.md trigger)
- **`docs/decisions/`** = ADRs cho architectural decisions (kiến trúc lớn, viết 1 lần khi chốt)

Memory folder này chứa **kiến thức tích lũy theo thời gian** mà không thuộc các loại trên.

## File index

| File | Purpose | Khi nào Claude đọc |
| --- | --- | --- |
| [`lessons_learned.md`](./lessons_learned.md) | Bài học từ Claude làm sai → rule mới được thêm vào. Append-only. | Trước khi đề xuất rule/pattern mới (tránh lặp sai lầm cũ) |
| [`domain_knowledge.md`](./domain_knowledge.md) | Reposition pattern, prompt examples, anti-pattern AI generated. Kiến thức core của moat Vibefolio. | Khi sửa AI prompt, viết test cho `ai/` package, hoặc tweak reposition logic |
| [`project_context.md`](./project_context.md) | Bối cảnh side project, Leonard background, working style, success metrics, "north star" reminders | Đầu mỗi major task — để Claude hiểu Leonard và Vibefolio sâu hơn |

## Format convention

### Append-only
Memory files là **append-only**. Không xóa entry cũ. Nếu rule sai/lỗi thời, append entry mới mark `[SUPERSEDED by 2026-XX-XX entry]`.

### Entry format chung
Mỗi entry có timestamp + ngữ cảnh + bài học. Format chuẩn:

```markdown
## YYYY-MM-DD — Short title

**Context**: 1-2 câu mô tả tình huống đang xảy ra
**What happened**: Claude làm gì, output thế nào
**Lesson**: Insight rút ra
**Rule added**: Link tới rule mới trong CLAUDE.md / security.md / domain_knowledge.md (nếu có)
```

### Khi entry ngắn quá
Nếu chỉ là quick note, dùng inline format:

```markdown
- **2026-XX-XX**: Quick note kèm 1 câu lý do.
```

## Cách Claude reference

Trong `CLAUDE.md` (root) hoặc module CLAUDE.md, thêm trigger:

```markdown
## AI Rules
- Trước khi đề xuất rule mới → đọc `.claude/memory/lessons_learned.md`
- Trước khi sửa AI prompt → đọc `.claude/memory/domain_knowledge.md`
- Đầu mỗi task lớn → skim `.claude/memory/project_context.md`
```

## Cách update từ Claude Code

Settings.json đã `allow Write(.claude/memory/**)` → Claude Code có thể append entry khi bạn dặn:

> "Ghi bài học hôm nay vào memory: Claude lần nào sửa BE schema cũng quên gen FE types, nên thêm rule cứng vào CLAUDE.md."

Claude Code sẽ append entry vào `lessons_learned.md` đúng format.

## Cách review định kỳ

Hàng tháng (hoặc cuối mỗi milestone), skim memory:
- Entry nào lỗi thời? Mark superseded.
- Pattern nào lặp lại nhiều? Promote lên thành rule cứng trong CLAUDE.md.
- Knowledge nào dày hơn? Tách file riêng (vd `prompt_library.md`).

---
*Memory không phải document — không cần đẹp. Cần đủ để Claude session sau hiểu vì sao thứ X tồn tại.*
