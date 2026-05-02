package com.vibefolio.email;

import com.vibefolio.config.VibefolioProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Resend-backed implementation of {@link EmailSender}.
 * Uses Spring {@link RestClient} to call the Resend REST API.
 * API key is read from {@link VibefolioProperties} — never hardcoded.
 *
 * <p>Implementation stub — logic to be filled in M1 email phase.
 */
@Service
class ResendEmailSender implements EmailSender {

    private final VibefolioProperties properties;
    private final RestClient restClient;

    ResendEmailSender(VibefolioProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.resend.com")
                .defaultHeader("Authorization", "Bearer " + properties.email().resendApiKey())
                .build();
    }

    @Override
    public void sendMagicLink(String to, String magicLinkUrl) {
        // TODO: POST /emails to Resend with HTML template containing magicLinkUrl
        throw new UnsupportedOperationException("ResendEmailSender.sendMagicLink not yet implemented");
    }
}
