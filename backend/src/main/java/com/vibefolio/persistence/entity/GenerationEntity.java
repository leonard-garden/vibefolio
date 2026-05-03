package com.vibefolio.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Audit record for every Anthropic AI generation attempt.
 * Used for cost accounting, rate limiting, and failure analysis.
 *
 * <p>Status values: {@code SUCCESS}, {@code FAIL}, {@code CACHED}.
 */
@Entity
@Table(name = "generations")
public class GenerationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /** Nullable: generation may fail before a portfolio is saved. */
    @Column(name = "portfolio_id")
    private UUID portfolioId;

    @Column(name = "email", length = 255, nullable = false)
    private String email;

    /** Real client IP from X-Forwarded-For (set by Vercel proxy). */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "model", length = 50, nullable = false)
    private String model;

    @Column(name = "input_tokens")
    private Integer inputTokens;

    @Column(name = "output_tokens")
    private Integer outputTokens;

    @Column(name = "cost_usd", precision = 8, scale = 5)
    private BigDecimal costUsd;

    @Column(name = "duration_ms")
    private Integer durationMs;

    /** {@code SUCCESS}, {@code FAIL}, or {@code CACHED}. */
    @Column(name = "status", length = 20, nullable = false)
    private String status;

    /** Error codes: {@code SCHEMA_VALIDATION}, {@code ANTHROPIC_ERROR}, {@code TIMEOUT}. */
    @Column(name = "error_code", length = 100)
    private String errorCode;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = Instant.now();
    }

    // --- Getters ---

    public UUID getId() { return id; }
    public UUID getPortfolioId() { return portfolioId; }
    public String getEmail() { return email; }
    public String getIpAddress() { return ipAddress; }
    public String getModel() { return model; }
    public Integer getInputTokens() { return inputTokens; }
    public Integer getOutputTokens() { return outputTokens; }
    public BigDecimal getCostUsd() { return costUsd; }
    public Integer getDurationMs() { return durationMs; }
    public String getStatus() { return status; }
    public String getErrorCode() { return errorCode; }
    public Instant getCreatedAt() { return createdAt; }

    // --- Setters ---

    public void setId(UUID id) { this.id = id; }
    public void setPortfolioId(UUID portfolioId) { this.portfolioId = portfolioId; }
    public void setEmail(String email) { this.email = email; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public void setModel(String model) { this.model = model; }
    public void setInputTokens(Integer inputTokens) { this.inputTokens = inputTokens; }
    public void setOutputTokens(Integer outputTokens) { this.outputTokens = outputTokens; }
    public void setCostUsd(BigDecimal costUsd) { this.costUsd = costUsd; }
    public void setDurationMs(Integer durationMs) { this.durationMs = durationMs; }
    public void setStatus(String status) { this.status = status; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
