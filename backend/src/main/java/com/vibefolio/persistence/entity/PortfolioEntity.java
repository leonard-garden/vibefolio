package com.vibefolio.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/**
 * Persistent representation of a Vibefolio portfolio.
 * {@code data} column stores the full {@code PortfolioJson} as JSONB in PostgreSQL.
 *
 * <p>Status lifecycle: {@code PENDING} → {@code LIVE} (via magic link claim).
 */
@Entity
@Table(name = "portfolios")
public class PortfolioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "username", length = 30, unique = true, nullable = false)
    private String username;

    @Column(name = "email", length = 255, nullable = false)
    private String email;

    @Column(name = "cv_blob_url", columnDefinition = "text")
    private String cvBlobUrl;

    @Column(name = "cv_hash", length = 64, nullable = false)
    private String cvHash;

    /**
     * Stores {@code PortfolioJson} as JSONB. Deserialized in service layer via Jackson.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data", columnDefinition = "jsonb", nullable = false)
    private String data;

    /**
     * Portfolio status: {@code PENDING} or {@code LIVE}.
     */
    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "claimed_at")
    private Instant claimedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = "PENDING";
        }
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = Instant.now();
    }

    // --- Getters ---

    public UUID getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getCvBlobUrl() { return cvBlobUrl; }
    public String getCvHash() { return cvHash; }
    public String getData() { return data; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getClaimedAt() { return claimedAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    // --- Setters ---

    public void setId(UUID id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setCvBlobUrl(String cvBlobUrl) { this.cvBlobUrl = cvBlobUrl; }
    public void setCvHash(String cvHash) { this.cvHash = cvHash; }
    public void setData(String data) { this.data = data; }
    public void setStatus(String status) { this.status = status; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public void setClaimedAt(Instant claimedAt) { this.claimedAt = claimedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
