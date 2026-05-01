# Backend — Vibefolio API

Spring Boot 3.3 service, Java 21, expose REST API có OpenAPI 3 contract. Nhiệm vụ: nhận PDF CV, gọi Anthropic AI extract + reposition, persist portfolio JSON, gửi magic link, serve public portfolio data cho FE render.

> Module CLAUDE.md này load khi Claude Code làm việc trong `backend/`. Root context xem [`../CLAUDE.md`](../CLAUDE.md).

## Stack

- **Spring Boot 3.3.x** + **Java 21 (LTS, virtual threads)**
- **Spring Web MVC** (blocking, đơn giản; M3+ có thể chuyển WebFlux nếu cần streaming)
- **Spring Data JPA + Hibernate** + **Flyway** migrations
- **springdoc-openapi** auto-gen OpenAPI 3 spec
- **Spring Security** (X-API-Key filter + CORS)
- **Bucket4j** rate limit
- **Anthropic Java SDK** (`com.anthropic:anthropic-java`)
- **AWS Java SDK v2** cho Cloudflare R2 (S3-compatible)
- **Resend** qua HTTP wrapper (Spring `RestClient`)
- **Bean Validation** (Jakarta Validation)
- **JUnit 5 + Mockito + Testcontainers**
- **Maven** (`pom.xml`)

## Commands

```bash
./mvnw spring-boot:run          # Dev server → http://localhost:8080 (Swagger: /swagger-ui.html)
./mvnw test                     # All tests
./mvnw package -DskipTests      # Production JAR → target/api-*.jar
./mvnw verify                   # Compile + lint + tests (chạy trước commit)
./mvnw flyway:migrate           # Apply DB migrations
./mvnw flyway:info              # Migration status
./mvnw springdoc-openapi:generate # Export OpenAPI spec → target/openapi.json
./mvnw versions:display-dependency-updates # Check security patches
```

## Verification (chạy sau every change)

1. `./mvnw verify` — fix mọi compile/lint/test fail trước khi tiếp
2. Nếu thay đổi REST API hoặc DTO: `./mvnw springdoc-openapi:generate` rồi `cd ../frontend && pnpm gen:api` để FE types đồng bộ
3. Nếu thay đổi DB schema: thêm Flyway migration mới (V{N}__{desc}.sql), test bằng `./mvnw flyway:migrate test`

## Project Structure

```
backend/
├── src/main/java/com/vibefolio/
│   ├── api/                          # @RestController + DTOs (thin layer)
│   │   ├── PortfolioController.java
│   │   ├── MagicLinkController.java
│   │   ├── dto/                      # Request/Response records với Bean Validation
│   │   └── exception/                # @ControllerAdvice, ApiErrorResponse
│   ├── service/                      # Business logic (no HTTP awareness)
│   │   ├── PortfolioService.java
│   │   ├── MagicLinkService.java
│   │   ├── GenerationService.java
│   │   ├── UsernameService.java
│   │   ├── RateLimitService.java
│   │   └── CostGuardService.java
│   ├── ai/                           # Anthropic integration
│   │   ├── AnthropicClient.java      # SDK wrapper
│   │   ├── PortfolioPromptBuilder.java
│   │   ├── PortfolioJsonValidator.java
│   │   └── model/PortfolioJson.java  # Java records mirror OpenAPI schema
│   ├── persistence/                  # JPA + Spring Data
│   │   ├── entity/                   # PortfolioEntity, MagicLinkEntity, GenerationEntity
│   │   └── repo/                     # PortfolioRepo, MagicLinkRepo, GenerationRepo
│   ├── storage/                      # R2/S3 abstraction
│   │   └── PdfStorageService.java
│   ├── email/                        # Resend
│   │   ├── ResendClient.java
│   │   └── MagicLinkEmailSender.java
│   ├── security/                     # Filters + CORS + rate limit
│   │   ├── ApiKeyAuthFilter.java
│   │   ├── SecurityConfig.java
│   │   └── RateLimitFilter.java
│   └── config/                       # @ConfigurationProperties + beans
│       ├── VibefolioProperties.java  # vibefolio.* config namespace
│       ├── AnthropicConfig.java
│       └── OpenApiConfig.java
├── src/main/resources/
│   ├── application.yml               # Default config (no secrets)
│   ├── application-local.yml.example # Template cho dev local (gitignored)
│   ├── db/migration/                 # Flyway V{N}__*.sql
│   └── prompts/                      # System prompt templates (.txt)
├── src/test/java/com/vibefolio/
│   ├── service/                      # Unit tests (no Spring context)
│   ├── api/                          # @WebMvcTest cho controller
│   ├── persistence/                  # @DataJpaTest + Testcontainers
│   └── integration/                  # @SpringBootTest end-to-end
└── pom.xml
```

## Conventions

### Java style
- **Indent**: 4 spaces, Java convention
- **Records cho DTOs**: ưu tiên `record` thay vì class POJO. Immutable, Jackson-native
- **Package-private default**: chỉ `public` khi thật sự cần expose ra package khác
- **Javadoc**: cho mọi `public` API (controller method, service method, public utility)
- **Naming**: PascalCase classes/records, camelCase methods/vars/fields, UPPER_SNAKE constants, lowercase packages

### Spring patterns
- **Constructor injection** only — KHÔNG `@Autowired` field
- **`@Service`** cho business logic, **`@Repository`** cho JPA repo, **`@RestController`** cho HTTP
- **`@Transactional`** ở service layer, không ở controller hay repo
- **`@ConfigurationProperties`** cho config — KHÔNG hardcode hoặc dùng raw `@Value` cho domain config (dùng cho test config OK)
- **`@ControllerAdvice`** centralize exception → HTTP mapping
- **Exception**: domain exception riêng (`UsernameTakenException`, `MagicLinkExpiredException`, `AiGenerationException`) → `@ControllerAdvice` map sang `ProblemDetail` (RFC 7807)

### Layer rules (KHÔNG VI PHẠM)

- **Controller** chỉ làm: parse request DTO → gọi service → format response DTO. KHÔNG `if/else` business logic.
- **Service** KHÔNG import `jakarta.servlet.*` hoặc `org.springframework.web.*` (test được không cần Spring MVC context)
- **Service** KHÔNG gọi `EntityManager` trực tiếp — qua `Repository` interface
- **AI calls** chỉ trong `ai/` package — service gọi qua `AnthropicClient` abstraction
- **DB queries** chỉ trong `persistence/` package — không native SQL ngoài migration files
- **Storage/Email** qua interface (`PdfStorageService`, `EmailSender`) — service không biết R2 hay S3 hay Resend cụ thể

### Testing
- **Unit test service** với Mockito, KHÔNG Spring context (fast)
- **`@WebMvcTest`** cho controller — mock service layer
- **`@DataJpaTest` + Testcontainers Postgres** cho persistence layer (real DB)
- **`@SpringBootTest`** chỉ cho integration test end-to-end (chậm, ít)
- **Coverage target**: services 80%+, controllers 60%+, AI module 70%+ (test prompt building + JSON validation, không test Anthropic API thật)
- **Naming**: `MethodName_Condition_ExpectedResult` (vd `createPortfolio_whenUsernameTaken_throwsUsernameTakenException`)

## Don't

- Don't put business logic trong `@RestController` — controller chỉ HTTP concern
- Don't gọi `EntityManager.createNativeQuery()` ngoài `persistence/` (raw SQL = SQL injection risk)
- Don't dùng raw SQL string concatenation — luôn parameterize qua JPA
- Don't hardcode model name (`"claude-haiku-4-5"`) — wrap qua `VibefolioProperties.ai().defaultModel()`
- Don't swallow Anthropic API error — log với context + propagate `AiGenerationException`
- Don't bỏ qua Bean Validation fail của AI output — escalate Haiku → Sonnet hoặc throw rõ ràng
- Don't log toàn bộ PDF content hoặc AI response chứa email/phone — log hash + metadata
- Don't dùng `@Autowired` field injection — constructor injection only
- Don't dùng Lombok `@Data` cho entity (tạo equals/hashCode lỗi với JPA proxy) — viết tay hoặc dùng `@Getter @Setter` riêng
- Don't gọi service từ service trong cùng `@Transactional` mà không hiểu propagation — đọc Spring docs trước
- Don't commit `application-local.yml`, `application-prod.yml`, `secrets.properties` — gitignored
- Don't dùng Claude Code subscription làm production AI backend (vi phạm ToS Anthropic — sẽ bị ban)

## Security top-line

> Full checklist: [`../.claude/rules/security.md`](../.claude/rules/security.md)

- **NEVER** hardcode `ANTHROPIC_API_KEY`, `BE_API_KEY`, `DB_URL`, `R2_*`, `RESEND_API_KEY` — luôn env var qua `VibefolioProperties`
- **ALWAYS** Bean Validation ở controller method (`@Valid`)
- **ALWAYS** check magic link token: chưa expired + chưa used + match purpose (3 điều kiện ĐỦ)
- **ALWAYS** Bucket4j rate limit trước AI call (3 gen/email/24h, 10/IP/24h)
- **ALWAYS** check `WAITLIST_MODE` flag + monthly cost cap $50 trước AI call
- **NEVER** log AI response raw — chỉ log token count, model, duration, status

## Config namespace

Tất cả config riêng của Vibefolio nằm dưới prefix `vibefolio.*`:

```yaml
vibefolio:
  ai:
    default-model: claude-haiku-4-5
    escalate-model: claude-sonnet-4-6
    monthly-cost-cap-usd: 50
    api-key: ${ANTHROPIC_API_KEY}
  storage:
    bucket: vibefolio-pdfs
    region: auto
    access-key-id: ${R2_ACCESS_KEY_ID}
    secret-access-key: ${R2_SECRET_ACCESS_KEY}
    endpoint: ${R2_ENDPOINT}
  security:
    api-key: ${BE_API_KEY}
    allowed-origins:
      - https://vibefolio.com
      - http://localhost:3000
  email:
    resend-api-key: ${RESEND_API_KEY}
    from-address: noreply@vibefolio.com
  rate-limit:
    per-email-per-day: 3
    per-ip-per-day: 10
```

Map qua `@ConfigurationProperties("vibefolio")` — `VibefolioProperties` record nested.

## Current State

- **Status**: chưa init code (chỉ design + docs)
- **First task**: scaffold Spring Boot project (Spring Initializr → import Maven), set up Flyway, tạo entity skeletons, smoke test `./mvnw spring-boot:run`
- **Reference**: [Design Doc M1 §6](../docs/superpowers/specs/2026-04-30-vibefolio-design.md) cho kiến trúc + [§7](../docs/superpowers/specs/2026-04-30-vibefolio-design.md) cho AI pipeline

## SOT pointers

| | |
|---|---|
| Architecture | [`../docs/ARCHITECTURE.md`](../docs/ARCHITECTURE.md) |
| Data models + JPA entities | [`../docs/DATA-MODELS.md`](../docs/DATA-MODELS.md) |
| ADRs | [`../docs/decisions/`](../docs/decisions/) |
| Design doc M1 | [`../docs/superpowers/specs/2026-04-30-vibefolio-design.md`](../docs/superpowers/specs/2026-04-30-vibefolio-design.md) |
| Frontend module | [`../frontend/CLAUDE.md`](../frontend/CLAUDE.md) |
