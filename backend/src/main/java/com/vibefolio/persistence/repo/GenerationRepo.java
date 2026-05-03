package com.vibefolio.persistence.repo;

import com.vibefolio.persistence.entity.GenerationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface GenerationRepo extends JpaRepository<GenerationEntity, UUID> {

    /**
     * Count generations for email after a given timestamp.
     * Used for per-email rate limiting (3/email/24h).
     */
    long countByEmailAndCreatedAtAfter(String email, Instant since);

    /**
     * Count generations for IP address after a given timestamp.
     * Used for per-IP rate limiting (10/IP/24h).
     */
    long countByIpAddressAndCreatedAtAfter(String ipAddress, Instant since);

    /**
     * Sum cost_usd for all generations since a given timestamp.
     * Used by {@link com.vibefolio.service.CostGuardService} to enforce the $50/month cap.
     * Returns 0 (not null) when no rows match.
     */
    @Query("SELECT COALESCE(SUM(g.costUsd), 0) FROM GenerationEntity g WHERE g.createdAt > :since")
    BigDecimal sumCostSince(@Param("since") Instant since);
}
