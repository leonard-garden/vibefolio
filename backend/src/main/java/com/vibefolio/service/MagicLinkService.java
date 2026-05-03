package com.vibefolio.service;

import com.vibefolio.email.EmailSender;
import com.vibefolio.persistence.repo.MagicLinkRepo;
import com.vibefolio.persistence.repo.PortfolioRepo;
import org.springframework.stereotype.Service;

/**
 * Manages magic link generation, delivery, and verification.
 * Enforces: token one-time use, 24h expiry, purpose binding (CLAIM vs UPDATE).
 *
 * <p>Implementation stub — logic to be filled in M1 auth phase.
 */
@Service
public class MagicLinkService {

    private final MagicLinkRepo magicLinkRepo;
    private final PortfolioRepo portfolioRepo;
    private final EmailSender emailSender;

    public MagicLinkService(
            MagicLinkRepo magicLinkRepo,
            PortfolioRepo portfolioRepo,
            EmailSender emailSender
    ) {
        this.magicLinkRepo = magicLinkRepo;
        this.portfolioRepo = portfolioRepo;
        this.emailSender = emailSender;
    }
}
