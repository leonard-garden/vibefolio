package com.vibefolio.ai;

import org.springframework.stereotype.Component;

/**
 * Builds the system and user prompts sent to Anthropic for portfolio generation.
 * System prompt is loaded from {@code src/main/resources/prompts/portfolio-system.txt}.
 *
 * <p>Implementation stub — logic to be filled in M1 AI pipeline phase.
 */
@Component
public class PortfolioPromptBuilder {

    /**
     * Build the user-turn message containing the CV text and output instructions.
     *
     * @param cvText  plain text extracted from PDF
     * @return formatted user prompt
     */
    public String buildUserPrompt(String cvText) {
        // TODO: load template, inject cvText, add JSON schema instructions
        throw new UnsupportedOperationException("PortfolioPromptBuilder.buildUserPrompt not yet implemented");
    }

    /**
     * Build the static system prompt (cached; never contains PII).
     *
     * @return system prompt string
     */
    public String buildSystemPrompt() {
        // TODO: load from classpath:prompts/portfolio-system.txt
        throw new UnsupportedOperationException("PortfolioPromptBuilder.buildSystemPrompt not yet implemented");
    }
}
