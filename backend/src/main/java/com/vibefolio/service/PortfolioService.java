package com.vibefolio.service;

import com.vibefolio.ai.AnthropicClient;
import com.vibefolio.persistence.repo.PortfolioRepo;
import com.vibefolio.storage.PdfStorageService;
import org.springframework.stereotype.Service;

/**
 * Core business logic for portfolio creation, retrieval, and regeneration.
 * Orchestrates: PDF upload → AI generate → persist → send magic link.
 *
 * <p>Implementation stub — logic to be filled in M1 feature phase.
 */
@Service
public class PortfolioService {

    private final PortfolioRepo portfolioRepo;
    private final AnthropicClient anthropicClient;
    private final PdfStorageService pdfStorageService;
    private final UsernameService usernameService;
    private final RateLimitService rateLimitService;
    private final CostGuardService costGuardService;
    private final MagicLinkService magicLinkService;

    public PortfolioService(
            PortfolioRepo portfolioRepo,
            AnthropicClient anthropicClient,
            PdfStorageService pdfStorageService,
            UsernameService usernameService,
            RateLimitService rateLimitService,
            CostGuardService costGuardService,
            MagicLinkService magicLinkService
    ) {
        this.portfolioRepo = portfolioRepo;
        this.anthropicClient = anthropicClient;
        this.pdfStorageService = pdfStorageService;
        this.usernameService = usernameService;
        this.rateLimitService = rateLimitService;
        this.costGuardService = costGuardService;
        this.magicLinkService = magicLinkService;
    }
}
