package com.vibefolio.ai.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Structured output produced by the Anthropic AI pipeline.
 * This record is the source of truth for the {@code portfolios.data} JSONB column
 * and is exported via OpenAPI to generate TypeScript types for the frontend.
 *
 * <p>All fields are validated by {@link com.vibefolio.ai.PortfolioJsonValidator}
 * after AI generation. Validation failure triggers escalation or hard fail.
 */
public record PortfolioJson(
        int schemaVersion,
        @Valid @NotNull Person person,
        @NotBlank @Size(max = 280) String summary,
        @Valid @NotNull @Size(min = 3, max = 3) List<Specialty> specialties,
        @Valid @NotNull @Size(max = 6) List<Project> projects,
        @Valid @NotNull List<Experience> experience,
        @Valid @NotNull Skills skills,
        @Valid @NotNull List<Education> education,
        @Valid @NotNull List<Credential> credentials
) {

    public record Person(
            @NotBlank String name,
            @NotBlank @Size(max = 80) String headline,
            @NotBlank String location,
            @NotNull AvailableFor availableFor,
            String pronouns,
            @Valid @NotNull Contact contact,
            String avatarUrl
    ) {}

    public record Contact(
            @NotBlank String email,
            String github,
            String linkedin,
            String twitter,
            String website,
            String phone
    ) {}

    public record Specialty(
            @NotNull SpecialtyIcon icon,
            @NotBlank @Size(max = 40) String title,
            @NotBlank @Size(max = 160) String description
    ) {}

    public record Project(
            @NotBlank String title,
            String role,
            String period,
            @NotBlank String problem,
            @NotBlank String approach,
            @NotBlank String impact,
            @Valid @NotNull @Size(max = 10) List<String> tech,
            @Valid @NotNull @Size(max = 3) List<Link> links,
            boolean featured
    ) {}

    public record Link(
            @NotBlank String label,
            @NotBlank String url
    ) {}

    public record Experience(
            @NotBlank String company,
            @NotBlank String role,
            @NotBlank String period,
            String location,
            @Valid @NotNull @Size(max = 5) List<String> highlights
    ) {}

    public record Skills(
            @Valid @NotNull @Size(max = 8) List<String> primary,
            @Valid @NotNull @Size(max = 20) List<String> secondary
    ) {}

    public record Education(
            @NotBlank String school,
            @NotBlank String degree,
            @NotBlank String period
    ) {}

    public record Credential(
            @NotNull CredentialType type,
            @NotBlank String title,
            @NotBlank String description
    ) {}

    // --- Enums ---

    public enum AvailableFor {
        FULLTIME, FREELANCE, BOTH, NOT_LOOKING
    }

    public enum SpecialtyIcon {
        CODE, SHIELD, SPARKLES, DATABASE, CLOUD, CPU
    }

    public enum CredentialType {
        MENTORSHIP, OSS, CERTIFICATION, SPEAKING, WRITING
    }
}
