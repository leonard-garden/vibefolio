package com.vibefolio.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.List;

/**
 * Typed configuration for all Vibefolio-specific settings.
 * All secrets are read from environment variables — never hardcoded.
 *
 * @see application.yml for the full property namespace
 */
@ConfigurationProperties("vibefolio")
@Validated
public record VibefolioProperties(
        @Valid @NotNull Ai ai,
        @Valid @NotNull Storage storage,
        @Valid @NotNull Security security,
        @Valid @NotNull Email email,
        @Valid @NotNull RateLimit rateLimit
) {

    public record Ai(
            @NotBlank String defaultModel,
            @NotBlank String escalateModel,
            @NotNull @DecimalMin("0") BigDecimal monthlyCostCapUsd,
            @NotBlank String apiKey
    ) {}

    public record Storage(
            @NotBlank String bucket,
            @NotBlank String region,
            @NotBlank String accessKeyId,
            @NotBlank String secretAccessKey,
            @NotBlank String endpoint
    ) {}

    public record Security(
            @NotBlank String apiKey,
            @NotEmpty List<String> allowedOrigins
    ) {}

    public record Email(
            @NotBlank String resendApiKey,
            @NotBlank String fromAddress
    ) {}

    public record RateLimit(
            @Min(1) int perEmailPerDay,
            @Min(1) int perIpPerDay
    ) {}
}
