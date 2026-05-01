#!/usr/bin/env python3
"""
Vibefolio — validate-command.py

Pre-tool hook: chặn các bash command nguy hiểm trước khi execute.
Run by Claude Code mỗi lần Bash tool được trigger.

Exit code 0 = allow. Non-zero = block + return stderr to Claude.

Install:
  - Copy file này về .claude/hooks/validate-command.py
  - chmod +x .claude/hooks/validate-command.py
  - Đảm bảo .claude/settings.json có hook entry trỏ tới file này
"""

import json
import re
import sys


# Patterns that should ALWAYS be blocked
DANGEROUS_PATTERNS = [
    # Filesystem destruction
    r"rm\s+-rf\s+/(?!\w)",          # rm -rf / (not /something)
    r"rm\s+-rf\s+~",                  # rm -rf ~
    r"rm\s+-rf\s+\.{1,2}(\s|$)",     # rm -rf . or ..
    r"rm\s+-rf\s+\$HOME",
    r":\(\)\s*\{.*\}\s*;.*:",        # fork bomb

    # Privilege escalation
    r"\bsudo\b",
    r"\bsu\s+-",

    # Permission disasters
    r"chmod\s+-R\s+777",
    r"chmod\s+777\s+/",
    r"chown\s+-R\s+root",

    # Git history destruction
    r"git\s+push\s+--force(?!-with-lease)",
    r"git\s+push\s+-f(?!-with-lease)",
    r"git\s+filter-branch",
    r"git\s+update-ref\s+-d",

    # Pipe-to-shell from network
    r"curl\s+[^|]*\|\s*(sh|bash|zsh)",
    r"wget\s+[^|]*\|\s*(sh|bash|zsh)",

    # Publishing to public registry
    r"npm\s+publish(?!\s+--dry-run)",
    r"pnpm\s+publish(?!\s+--dry-run)",
    r"yarn\s+publish(?!\s+--dry-run)",

    # Dropping production data
    r"DROP\s+DATABASE\s+(?!.*test)",
    r"DROP\s+TABLE\s+(?!.*test)",
    r"TRUNCATE\s+(?!.*test)",
]

# Vibefolio-specific blocks
VIBEFOLIO_BLOCKS = [
    # Don't bypass Flyway
    (r"psql.*-c\s+['\"]ALTER\s+TABLE",
     "Schema changes phải qua Flyway migration trong backend/src/main/resources/db/migration/"),
    (r"psql.*-c\s+['\"]CREATE\s+TABLE",
     "Schema changes phải qua Flyway migration"),
    (r"psql.*-c\s+['\"]DROP",
     "DROP statements phải qua Flyway migration với review"),

    # Don't expose AI key
    (r"echo\s+\$ANTHROPIC_API_KEY",
     "KHÔNG echo API key — vi phạm security policy"),
    (r"echo\s+\$BE_API_KEY",
     "KHÔNG echo BE_API_KEY"),
    (r"printenv\s+ANTHROPIC",
     "KHÔNG dump env chứa secret"),

    # Don't use Claude Code subscription as backend
    (r"claude\s+-p\s+.*>>?\s+\.env",
     "KHÔNG pipe Claude Code output vào env config — production AI phải qua Anthropic API"),
]


def main():
    try:
        payload = json.load(sys.stdin)
    except (json.JSONDecodeError, ValueError):
        # Nếu không parse được, allow (Claude Code có thể đổi format hook)
        sys.exit(0)

    tool_input = payload.get("tool_input", {})
    command = tool_input.get("command", "")

    if not command:
        sys.exit(0)

    # Check generic dangerous patterns
    for pattern in DANGEROUS_PATTERNS:
        if re.search(pattern, command, re.IGNORECASE):
            print(
                f"[validate-command.py] BLOCKED — matched dangerous pattern: {pattern}\n"
                f"Command: {command}\n"
                f"Nếu thật sự cần, user phải confirm explicit và Claude tạo command an toàn hơn.",
                file=sys.stderr,
            )
            sys.exit(2)

    # Check Vibefolio-specific blocks
    for pattern, reason in VIBEFOLIO_BLOCKS:
        if re.search(pattern, command, re.IGNORECASE):
            print(
                f"[validate-command.py] BLOCKED — vibefolio rule violation\n"
                f"Reason: {reason}\n"
                f"Command: {command}",
                file=sys.stderr,
            )
            sys.exit(2)

    sys.exit(0)


if __name__ == "__main__":
    main()
