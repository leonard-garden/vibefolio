package com.vibefolio.service;

import com.vibefolio.config.VibefolioProperties;
import com.vibefolio.persistence.repo.GenerationRepo;
import org.springframework.stereotype.Service;

/**
 * Enforces per-email and per-IP generation rate limits using Bucket4j.
 * Limits: 3 generations / email / 24h, 10 / IP / 24h (configured in {@link VibefolioProperties}).
 *
 * <p>Implementation stub — logic to be filled in M1 AI pipeline phase.
 */
@Service
public class RateLimitService {

    private final GenerationRepo generationRepo;
    private final VibefolioProperties properties;

    public RateLimitService(GenerationRepo generationRepo, VibefolioProperties properties) {
        this.generationRepo = generationRepo;
        this.properties = properties;
    }
}
