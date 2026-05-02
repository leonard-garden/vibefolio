package com.vibefolio.ai;

import com.vibefolio.ai.model.PortfolioJson;
import org.springframework.stereotype.Component;

/**
 * Wrapper around the Anthropic Java SDK.
 * Encapsulates model selection, prompt assembly, retry logic (Haiku → Sonnet escalation),
 * and token/cost tracking.
 *
 * <p>Implementation stub — logic to be filled in M1 AI pipeline phase.
 */
@Component
public class AnthropicClient {

    private final com.anthropic.client.AnthropicClient sdkClient;
    private final com.vibefolio.config.VibefolioProperties properties;

    public AnthropicClient(
            com.anthropic.client.AnthropicClient sdkClient,
            com.vibefolio.config.VibefolioProperties properties
    ) {
        this.sdkClient = sdkClient;
        this.properties = properties;
    }

    /**
     * Generate a {@link PortfolioJson} from raw PDF text extracted from a CV.
     *
     * @param cvText  plain text extracted from the uploaded PDF
     * @param email   owner email, used for audit logging (never sent to Anthropic)
     * @return validated {@link PortfolioJson}
     */
    public PortfolioJson generate(String cvText, String email) {
        // TODO: implement prompt building, API call, JSON parsing, escalation
        throw new UnsupportedOperationException("AnthropicClient.generate not yet implemented");
    }
}
