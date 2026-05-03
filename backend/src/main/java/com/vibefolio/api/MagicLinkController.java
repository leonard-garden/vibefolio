package com.vibefolio.api;

import com.vibefolio.service.MagicLinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for magic link claim and update flows.
 * Controllers are thin: parse request → call service → format response.
 * No business logic here.
 */
@RestController
@RequestMapping("/v1/magic-links")
@Tag(name = "Magic Links", description = "Portfolio claim and update via one-time tokens")
public class MagicLinkController {

    private final MagicLinkService magicLinkService;

    public MagicLinkController(MagicLinkService magicLinkService) {
        this.magicLinkService = magicLinkService;
    }

    /**
     * Verify a CLAIM token and transition portfolio status from PENDING → LIVE.
     */
    @PostMapping("/claim")
    @Operation(summary = "Claim a portfolio using a one-time magic link token")
    ResponseEntity<Void> claim() {
        // TODO: parse token from request body, delegate to magicLinkService.claim(token)
        throw new UnsupportedOperationException("MagicLinkController.claim not yet implemented");
    }

    /**
     * Send a new UPDATE magic link to the portfolio owner's email.
     * Triggers the re-upload / regenerate flow.
     */
    @PostMapping("/request-update")
    @Operation(summary = "Request an update magic link for an existing portfolio")
    ResponseEntity<Void> requestUpdate() {
        // TODO: parse email + username from request body, delegate to magicLinkService.requestUpdate(...)
        throw new UnsupportedOperationException("MagicLinkController.requestUpdate not yet implemented");
    }
}
