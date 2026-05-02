# Git Workflow — Vibefolio

> Project-specific. Overrides `~/.claude/rules/common/git-workflow.md`.

## Branch Model (Gitflow)

```
main          ← production-ready only, protected
develop       ← integration branch, always deployable
feat/*        ← new features (from develop → PR → develop)
fix/*         ← bug fixes (from develop → PR → develop)
hotfix/*      ← urgent prod fixes (from main → PR → main + develop)
chore/*       ← tooling, deps, config (from develop → PR → develop)
release/*     ← release prep (from develop → PR → main + develop)
```

**Rules:**
- NEVER commit directly to `main` or `develop`
- All changes go through PR — no force push to protected branches
- `main` ← merge only from `release/*` or `hotfix/*`
- `develop` ← merge from `feat/*`, `fix/*`, `chore/*`
- Delete branch sau khi merge

## Branch Naming

```
feat/m1-<short-desc>       # feature thuộc milestone 1
feat/m2-<short-desc>       # feature thuộc milestone 2
fix/<short-desc>           # bug fix
hotfix/<short-desc>        # urgent prod fix
chore/<short-desc>         # tooling, config, deps
release/m1                 # release milestone 1
```

Examples:
```
feat/m1-backend-scaffold
feat/m1-frontend-scaffold
feat/m1-pdf-upload-api
feat/m1-ai-generate-portfolio
fix/magic-link-expiry-check
chore/update-spring-boot-3.4
hotfix/cost-guard-bypass
release/m1
```

## Commit Message Format

```
<type>(<scope>): <subject>

[optional body — WHY, not WHAT]

[optional trailers]
```

**Types:** `feat`, `fix`, `refactor`, `test`, `chore`, `docs`, `perf`, `ci`

**Scopes:** `api`, `web`, `auth`, `ai`, `db`, `infra`, `config`

**Subject rules:**
- Imperative mood, English, lowercase after colon
- Max 50 chars
- No period at end

**Body rules:**
- Blank line after subject
- Explain WHY (context, constraint, tradeoff) — không giải thích WHAT (code tự nói)
- Max 72 chars per line

**OMC trailers** (include khi relevant, bỏ qua trivial commits):
```
Constraint: <active constraint shaped this decision>
Rejected: <alternative> | <reason>
Directive: <warning for future modifiers>
Confidence: high | medium | low
Scope-risk: narrow | moderate | broad
Not-tested: <edge case not covered>
```

**Examples:**
```
feat(api): add magic link verification endpoint

Constraint: token must be one-time use + purpose-bound
Rejected: JWT | stateless but can't invalidate before expiry
Confidence: high
Scope-risk: narrow
```

```
feat(web): scaffold Next.js 15 app router structure
```

```
fix(ai): cap retry at 1 escalation haiku→sonnet

Silent infinite retry caused $12 runaway cost in staging.

Constraint: CostGuardService must run before every Anthropic call
Not-tested: concurrent requests hitting cap simultaneously
```

## PR Convention

- Title = commit subject format: `feat(api): add X`
- 1 PR = 1 logical change
- PR body phải có: Summary + Test plan
- Kèm test — không merge PR không có test (trừ chore/docs)
- Kèm migration nếu có schema change
- Cả BE check + FE check phải green trước merge
- Squash merge vào `develop` để giữ history clean

## Merge Strategy

| Target | Strategy | Reason |
|--------|----------|--------|
| `develop` ← `feat/*` | Squash merge | Clean linear history |
| `develop` ← `fix/*` | Squash merge | Clean linear history |
| `main` ← `release/*` | Merge commit | Preserve release boundary |
| `develop` ← `hotfix/*` | Cherry-pick | Minimal diff back to develop |

## Claude Code Rules

- Tạo branch từ `develop` trừ khi hotfix
- Commit message phải đúng format trên — không "wip", "fix", "update"
- Không commit file: `.env`, `.env.*`, `application-local.yml`, `*.pem`, `*.key`
- Không commit generated files: `src/lib/api/schemas.gen.ts` (chạy `pnpm gen:api` để gen)
- Trước khi tạo commit liên quan BE schema: chạy `./scripts/gen-api-types.sh` và commit cả FE types
- Không push trực tiếp — tạo PR