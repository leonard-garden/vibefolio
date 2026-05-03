package com.vibefolio.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * One-time-use magic link token for portfolio claim or update flows.
 *
 * <p>Security invariants enforced at service layer:
 * <ul>
 *   <li>{@code usedAt} must be null to be valid</li>
 *   <li>{@code expiresAt} must be in the future</li>
 *   <li>{@code purpose} must match the intended action (CLAIM vs UPDATE)</li>
 * </ul>
 */
@Entity
@Table(name = "magic_links")
public class MagicLinkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "email", length = 255, nullable = false)
    private String email;

    @Column(name = "portfolio_id")
    private UUID portfolioId;

    /** base64url of 32 random bytes — never UUID. */
    @Column(name = "token", length = 64, unique = true, nullable = false)
    private String token;

    /** {@code CLAIM} or {@code UPDATE}. */
    @Column(name = "purpose", length = 20, nullable = false)
    private String purpose;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "used_at")
    private Instant usedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = Instant.now();
    }

    // --- Getters ---

    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public UUID getPortfolioId() { return portfolioId; }
    public String getToken() { return token; }
    public String getPurpose() { return purpose; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getUsedAt() { return usedAt; }
    public Instant getCreatedAt() { return createdAt; }

    // --- Setters ---

    public void setId(UUID id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }
    public void setPortfolioId(UUID portfolioId) { this.portfolioId = portfolioId; }
    public void setToken(String token) { this.token = token; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public void setUsedAt(Instant usedAt) { this.usedAt = usedAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
