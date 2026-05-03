package com.vibefolio.service;

import com.vibefolio.persistence.repo.PortfolioRepo;
import org.springframework.stereotype.Service;

/**
 * Generates and validates portfolio usernames.
 * Rules: lowercase {@code [a-z0-9-]{3,30}}, unique, not in reserved/profanity blocklist.
 *
 * <p>Implementation stub — logic to be filled in M1 feature phase.
 */
@Service
public class UsernameService {

    private final PortfolioRepo portfolioRepo;

    public UsernameService(PortfolioRepo portfolioRepo) {
        this.portfolioRepo = portfolioRepo;
    }
}
