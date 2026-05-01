# Domain Knowledge — Vibefolio

> Kiến thức core của moat Vibefolio: **AI Reposition cho dev/AI engineer mid-senior**.
> Đây là know-how cụ thể cho prompt design, schema decision, và quality bar.
> Đọc trước khi: sửa AI prompt, viết test cho `ai/` package, tweak reposition logic, design template mới.

---

## Tóm tắt 1 câu

> Vibefolio không chỉ "format CV thành web" — nó **phát hiện user đang tự bán rẻ và đề xuất chức danh + headline cao hơn** dựa trên signal trong CV.

---

## Reposition pattern library

### Pattern 1: Java backend + cloud + AI signal → "Backend & AI Engineer"

**Detect signal trong CV:**
- Stack: Java/Spring/Kotlin + AWS/GCP/Azure + một trong (LangChain, LangGraph, OpenAI/Anthropic SDK, vector DB, RAG, agentic workflow, n8n, local LLM)
- Job title hiện tại thường là: "Java Engineer", "Java Backend Engineer", "Senior Java Developer"

**Reposition headline:**
- ❌ Original: "Java Backend Engineer"
- ✅ Reposed: "Backend & AI Engineer"
- ✅ Sharper (nếu có vertical): "Backend & AI Engineer for Fintech"

**Vì sao**: Năm 2026, "Java Backend Engineer" là commodity (cạnh tranh dev Ấn Độ, Đông Âu ở $25-50/giờ). "Backend & AI Engineer" lift qua nhóm rare ($120-200/giờ trên thị trường remote US/EU).

---

### Pattern 2: Security/Auth/Payment signal → thêm "specialized in [vertical]"

**Detect signal trong CV:**
- Keywords: payment flow, transaction, risk-based auth, PCI-DSS, AWS KMS, Passkey, WebAuthn, OAuth, JWT, Keycloak, biometric, liveness detection
- Industry trong job: bank, fintech, payment, e-commerce, healthcare, insurance

**Reposition headline:**
- ❌ Original: "Senior Software Engineer"
- ✅ Reposed: "Senior Backend Engineer specialized in Fintech Security"
- ✅ Sharper: "I help fintechs ship AI features without breaking compliance" (cho freelance/Upwork variant)

**Vì sao**: Niche specialization commands premium. "Engineer" generic vs "Engineer specialized in [hot vertical]" → +30-50% rate.

---

### Pattern 3: OSS + mentorship signal → leadership/community subtitle

**Detect signal trong CV:**
- OSS contribution với repo link
- Mentorship số liệu cụ thể (vd "mentored 50+ students", "guided 10+ first jobs")
- Speaking, writing, blog, conference talk

**Reposition addition** (sub-headline hoặc summary):
- Thêm cụm: "Active OSS contributor & mentor (50+ engineers placed)"
- Hoặc: "Open-source advocate and technical mentor"

**Vì sao**: Social proof + leadership signal cho recruiter senior+ role hoặc client cần lead/principal level engagement.

---

### Pattern 4: Bullet "did X" → "Problem → Approach → Impact"

**Detect signal**: Mọi bullet point dạng "Did X using Y" hoặc "Implemented Z" — passive voice, không impact.

**Reposition transformation**:

❌ Before:
> "Implemented Risk-based Authentication using PIN, Biometric, and Liveness Detection."

✅ After (Problem/Approach/Impact format):
> **Problem**: High-value payment flows had uniform auth, exposing $X risk per fraud event.
> **Approach**: Designed tiered Risk-based Auth — PIN baseline, Biometric for medium-value, Liveness Detection for high-value transactions.
> **Impact**: Reduced fraud rate by Y%, met regulatory tier-1 compliance.

**Caveat**: KHÔNG bịa số liệu nếu CV không có. Nếu thiếu impact, thay bằng business outcome chung ("ensured PCI-DSS compliance", "enabled rollout to 10M+ user base").

---

### Pattern 5: Cross-platform / polyglot signal → "Full-stack" hoặc "Cross-platform"

**Detect signal**:
- BE + FE + Mobile cùng project (vd Java + React + Kotlin Multiplatform)
- Multi-language (Java + Python + TS)

**Reposition headline addition**:
- "Cross-platform Engineer (Backend + Mobile)"
- "Full-stack Engineer with Backend Expertise"

**KHÔNG dùng** "Full-stack" nếu chỉ làm BE — sẽ bị recruiter discount khi sàng lọc senior BE role.

---

## Anti-patterns (AI generated cần block)

### A1: Buzzword salad
❌ "Synergistic, results-driven, value-creating engineer leveraging cutting-edge technologies"
✅ "Senior backend engineer building payment systems for digital banking"

### A2: Bịa số liệu
❌ "Reduced latency by 87.3% saving the company $4.2M annually" (khi CV không có số)
✅ "Optimized batch processing pipeline; throughput improved measurably without sacrificing reliability"

### A3: Over-promising scope
❌ "Architected the entire microservices platform from scratch" (khi role là contributor)
✅ "Led migration of 8+ services from ESB to Mendix as part of platform modernization"

### A4: Generic specialization
❌ "Specialized in software engineering"
✅ "Specialized in banking-grade backend systems"

### A5: Lost personality
❌ Robot-tone với jargon dày đặc
✅ Có chút personality (vd "Globally minded engineer driving cross-border initiatives") nhưng không sến

---

## Vertical-specific knowledge

### Fintech
- **Hot keywords (premium)**: PCI-DSS, SWIFT, ISO 20022, payment rails, risk-based auth, KYC/AML, fraud detection, real-time settlement, ledger systems
- **Companies recruiter biết**: Stripe, Adyen, Ramp, Mercury, Brex, Wise, Revolut, Plaid, Brex
- **Headline angle**: "X for Fintech" hoặc "Banking-grade [Y]"

### AI Infrastructure
- **Hot keywords**: LangChain, LangGraph, LangFuse, vector DB (Pinecone, Weaviate, pgvector), RAG, agentic workflow, prompt eval, LLM observability, tool use, MCP, fine-tuning
- **Companies recruiter biết**: Anthropic, OpenAI, Hugging Face, Pinecone, LangChain, Cohere, Together AI, Replicate
- **Headline angle**: "AI Engineer" hoặc "LLM Infrastructure Engineer"

### Security & Identity
- **Hot keywords**: Passkey, WebAuthn, FIDO2, OIDC, SAML, zero-trust, AWS KMS, HashiCorp Vault, OPA, mTLS, certificate rotation
- **Companies recruiter biết**: Auth0, Okta, 1Password, Hashicorp, Cloudflare, Tailscale
- **Headline angle**: "Identity & Security Engineer" hoặc "Auth Platform Engineer"

---

## System prompt design notes

### Cấu trúc 3 phần (versioned trong `backend/src/main/resources/prompts/`)

**Part A — Role & objective** (~200 tokens):
"You are an expert career coach + technical writer for senior backend & AI engineers targeting remote markets (US/EU/SG). Your job: extract a CV and produce a Portfolio JSON optimized to win remote interviews and freelance contracts. You understand modern stack (Spring Boot, AWS, LangChain, ...) and know how to position senior engineers for $120-200/hr roles."

**Part B — Reposition rules** (~800 tokens, prompt-cached):
Liệt kê 5 pattern ở trên + vertical knowledge. Có examples cụ thể cho mỗi pattern.

**Part C — Output schema constraint** (~400 tokens):
JSON schema khớp `PortfolioJson`. Constraint via Anthropic tool use (`tools: [{ name: "extract_portfolio", input_schema: {...} }]`).

**Total system prompt**: ~1400 tokens. Prompt caching tiết kiệm ~95% phần này sau call đầu (hết hạn cache 5 phút).

### User message
Chỉ chứa: PDF document block + 1 câu instruction "Extract this CV. Reposition headline if needed. Format projects as Problem→Approach→Impact."

### Validation chain (sau response)
1. JSON.parse output
2. Bean Validation trên `PortfolioJson` record
3. Custom check: headline ≠ original CV title (đảm bảo có reposition); summary ≠ professional summary CV (đảm bảo có rewrite); 3 specialties đều có; ≥3 projects có Problem/Approach/Impact đầy đủ

Fail validation → escalate Haiku → Sonnet retry 1 lần. Sonnet cũng fail → return error message "AI generation failed, please try again or contact support."

---

## Quality bar (output checklist)

Mỗi portfolio AI generate phải pass:

- [ ] Headline ≤ 80 chars, có verb hoặc niche specialization
- [ ] Summary ≤ 280 chars, có "what + for whom + outcome"
- [ ] 3 specialties (luôn 3, không 2 không 4) — ngắn gọn, mỗi cái có icon mapping
- [ ] ≥ 3 projects format Problem/Approach/Impact (không bịa số)
- [ ] Skills tách primary (top 8) và secondary (rest)
- [ ] Phone & address ẩn mặc định (privacy default)
- [ ] Contact luôn có ít nhất email + 1 trong (GitHub, LinkedIn)
- [ ] Available status enum đúng (FULLTIME / FREELANCE / BOTH / NOT_LOOKING)

---

## Append new domain knowledge below this line

<!-- New patterns, anti-patterns, or vertical knowledge go here -->
