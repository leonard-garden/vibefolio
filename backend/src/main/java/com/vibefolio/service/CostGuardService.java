package com.vibefolio.service;

import com.vibefolio.config.VibefolioProperties;
import com.vibefolio.persistence.repo.GenerationRepo;
import org.springframework.stereotype.Service;

/**
 * Enforces the monthly AI cost cap (default $50/month).
 * Must be checked before every Anthropic API call.
 *
 * <p>Implementation stub — logic to be filled in M1 AI pipeline phase.
 */
@Service
public class CostGuardService {

    private final GenerationRepo generationRepo;
    private final VibefolioProperties properties;

    public CostGuardService(GenerationRepo generationRepo, VibefolioProperties properties) {
        this.generationRepo = generationRepo;
        this.properties = properties;
    }

    /**
     * Check if the monthly cost cap has been reached.
     *
     * @return {@code true} if spend this month is at or above the cap
     */
    public boolean isCapReached() {
        // TODO: query SUM(cost_usd) for current month, compare to monthlyCostCapUsd
        throw new UnsupportedOperationException("CostGuardService.isCapReached not yet implemented");
    }
}
