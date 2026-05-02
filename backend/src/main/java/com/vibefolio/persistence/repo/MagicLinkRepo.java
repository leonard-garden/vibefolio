package com.vibefolio.persistence.repo;

import com.vibefolio.persistence.entity.MagicLinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface MagicLinkRepo extends JpaRepository<MagicLinkEntity, UUID> {

    Optional<MagicLinkEntity> findByToken(String token);

    /**
     * Count active (unused, non-expired) magic links for a given email.
     * Used to enforce the 3 magic links / email / hour rate limit.
     */
    long countByEmailAndUsedAtIsNullAndExpiresAtAfter(String email, Instant now);
}
