package com.vibefolio.api;

import com.vibefolio.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST endpoints for portfolio creation and retrieval.
 * Controllers are thin: parse request → call service → format response.
 * No business logic here.
 */
@RestController
@RequestMapping("/v1/portfolios")
@Tag(name = "Portfolios", description = "Portfolio generation and retrieval")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    /**
     * Upload a CV PDF and trigger AI portfolio generation.
     * Accepts multipart/form-data with fields: {@code file} (PDF) + {@code email}.
     */
    @PostMapping(consumes = "multipart/form-data")
    @Operation(summary = "Create portfolio from CV PDF")
    ResponseEntity<Void> create() {
        // TODO: parse MultipartFile + email, delegate to portfolioService.create(...)
        throw new UnsupportedOperationException("PortfolioController.create not yet implemented");
    }

    /**
     * Retrieve a public portfolio by username slug.
     */
    @GetMapping("/{username}")
    @Operation(summary = "Get portfolio by username")
    ResponseEntity<Void> getByUsername(@PathVariable String username) {
        // TODO: delegate to portfolioService.getByUsername(username)
        throw new UnsupportedOperationException("PortfolioController.getByUsername not yet implemented");
    }

    /**
     * Re-run AI generation for an existing portfolio (requires valid magic link token).
     */
    @PostMapping("/{id}/regenerate")
    @Operation(summary = "Regenerate portfolio from stored CV")
    ResponseEntity<Void> regenerate(@PathVariable UUID id) {
        // TODO: validate magic token header, delegate to portfolioService.regenerate(id, ...)
        throw new UnsupportedOperationException("PortfolioController.regenerate not yet implemented");
    }
}
