package com.vibefolio.ai;

import com.vibefolio.ai.model.PortfolioJson;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Validates AI-generated {@link PortfolioJson} using Jakarta Bean Validation.
 * A validation failure indicates the AI produced malformed output and should
 * trigger escalation (Haiku → Sonnet) or a hard fail.
 *
 * <p>Implementation stub — full error reporting to be added in M1 AI pipeline phase.
 */
@Component
public class PortfolioJsonValidator {

    private final Validator validator;

    public PortfolioJsonValidator(Validator validator) {
        this.validator = validator;
    }

    /**
     * Validate all Bean Validation constraints on the given portfolio JSON.
     *
     * @param portfolioJson the AI-generated output to validate
     * @return set of constraint violations (empty = valid)
     */
    public Set<ConstraintViolation<PortfolioJson>> validate(PortfolioJson portfolioJson) {
        return validator.validate(portfolioJson);
    }

    /**
     * Convenience method: throws if validation fails.
     *
     * @param portfolioJson the AI-generated output to validate
     * @throws IllegalArgumentException if any constraints are violated
     */
    public void validateOrThrow(PortfolioJson portfolioJson) {
        Set<ConstraintViolation<PortfolioJson>> violations = validate(portfolioJson);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .reduce((a, b) -> a + "; " + b)
                    .orElse("unknown validation error");
            throw new IllegalArgumentException("AI output failed validation: " + message);
        }
    }
}
