package com.vibefolio;

import com.vibefolio.ai.AnthropicClient;
import com.vibefolio.email.EmailSender;
import com.vibefolio.storage.PdfStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

/**
 * Smoke test: verifies the Spring application context loads without errors.
 * External dependencies (DB, Anthropic, R2, Resend) are mocked to avoid
 * requiring real infrastructure in CI.
 */
@SpringBootTest
@TestPropertySource(properties = {
        // Override secrets with safe test values
        "vibefolio.ai.api-key=test-anthropic-key",
        "vibefolio.storage.access-key-id=test-access-key",
        "vibefolio.storage.secret-access-key=test-secret-key",
        "vibefolio.storage.endpoint=https://test.r2.cloudflarestorage.com",
        "vibefolio.security.api-key=test-be-api-key",
        "vibefolio.email.resend-api-key=test-resend-key",
        // Disable Flyway so the test doesn't need a real DB
        "spring.flyway.enabled=false",
        // Use H2 in-memory DB for context load test
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
class VibeFolioApplicationTests {

    // Mock external integrations so the context loads without real credentials
    @MockBean
    AnthropicClient anthropicClient;

    @MockBean
    EmailSender emailSender;

    @MockBean
    PdfStorageService pdfStorageService;

    @Test
    void contextLoads() {
        // If the application context starts without throwing, the test passes.
        // This validates all @Bean definitions, @ConfigurationProperties, and wiring.
    }
}
