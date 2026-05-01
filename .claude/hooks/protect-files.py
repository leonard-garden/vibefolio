#!/usr/bin/env python3
"""
Vibefolio — protect-files.py

Pre-tool hook: chặn Read/Write/Edit vào các file chứa secret hoặc nhạy cảm.
Run by Claude Code mỗi lần Read/Write/Edit tool được trigger.

Exit code 0 = allow. Non-zero = block + return stderr to Claude.

Install:
  - Copy file này về .claude/hooks/protect-files.py
  - chmod +x .claude/hooks/protect-files.py
"""

import json
import re
import sys
from pathlib import Path


# Patterns of files that should NEVER be touched by Claude
PROTECTED_PATTERNS = [
    # Generic secrets
    r"(^|/)\.env($|\.)",
    r"(^|/)\.env\.\w+",
    r"\.pem$",
    r"\.key$",
    r"\.p12$",
    r"\.pfx$",
    r"id_rsa$",
    r"id_ed25519$",
    r"id_ecdsa$",

    # SSH config
    r"(^|/)\.ssh/",

    # AWS / cloud creds
    r"(^|/)\.aws/credentials$",
    r"(^|/)\.aws/config$",

    # Vibefolio production config
    r"backend/src/main/resources/application-local\.yml$",
    r"backend/src/main/resources/application-prod\.yml$",
    r"backend/src/main/resources/secrets\.properties$",
    r"frontend/\.env\.local$",
    r"frontend/\.env\.production$",

    # Generated / build artifacts (read-only intent)
    r"node_modules/",
    r"\.next/",
    r"build/",
    r"target/",
    r"dist/",
    r"\.git/",

    # Lock files (Claude shouldn't edit; let package manager handle)
    r"pnpm-lock\.yaml$",
    r"package-lock\.json$",
    r"yarn\.lock$",
    r"gradle\.lockfile$",
]

# Read-only allowed (don't block reads, just writes/edits)
READ_ONLY_OK = [
    r"\.gitignore$",
    r"README\.md$",
]


def is_protected(path: str) -> tuple[bool, str]:
    """Return (blocked, reason)."""
    for pattern in PROTECTED_PATTERNS:
        if re.search(pattern, path):
            return True, f"matched protected pattern: {pattern}"
    return False, ""


def main():
    try:
        payload = json.load(sys.stdin)
    except (json.JSONDecodeError, ValueError):
        sys.exit(0)

    tool_name = payload.get("tool_name", "")
    tool_input = payload.get("tool_input", {})
    file_path = tool_input.get("file_path") or tool_input.get("path") or ""

    if not file_path:
        sys.exit(0)

    # Normalize
    try:
        normalized = str(Path(file_path).resolve())
    except (OSError, ValueError):
        normalized = file_path

    blocked, reason = is_protected(file_path) or is_protected(normalized)

    # Allow Read trên một số file read-only OK (vd .gitignore, README)
    if blocked and tool_name == "Read":
        for pattern in READ_ONLY_OK:
            if re.search(pattern, file_path):
                sys.exit(0)

    if blocked:
        print(
            f"[protect-files.py] BLOCKED — {tool_name} không được phép trên file này\n"
            f"Path: {file_path}\n"
            f"Reason: {reason}\n"
            f"Nếu thật sự cần access (vd: setup .env lần đầu), user phải làm thủ công ngoài Claude Code.",
            file=sys.stderr,
        )
        sys.exit(2)

    sys.exit(0)


if __name__ == "__main__":
    main()
