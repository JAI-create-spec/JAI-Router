package io.jai.router.core;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Immutable value type representing a routing decision returned by an LLM/provider.
 * <p>
 * Contains the target service identifier, confidence score (0.0-1.0),
 * and a human-readable explanation of the routing decision.
 * </p>
 *
 * @param service     the target service identifier, never null or blank
 * @param confidence  confidence score between 0.0 and 1.0
 * @param explanation human-readable explanation, never null (may be empty)
 * @author JAI Router Team
 * @since 1.0.0
 */
public record RoutingDecision(
        @NotNull String service,
        double confidence,
        @NotNull String explanation
) {

    /**
     * Compact constructor with validation and normalization.
     *
     * @throws NullPointerException     if service or explanation is null
     * @throws IllegalArgumentException if service is blank
     */
    public RoutingDecision {
        Objects.requireNonNull(service, "service must not be null");
        if (service.isBlank()) {
            throw new IllegalArgumentException("service must not be blank");
        }

        // Normalize confidence to valid range
        if (!Double.isFinite(confidence)) {
            confidence = 0.0;
        }
        confidence = Math.max(0.0, Math.min(1.0, confidence));

        // Ensure explanation is never null
        explanation = explanation == null ? "" : explanation;
    }

    /**
     * Factory method to create a routing decision with validation.
     * <p>
     * This method provides the same validation as the constructor but
     * offers a more fluent API for creating instances.
     * </p>
     *
     * @param service     target service ID
     * @param confidence  confidence score (will be clamped to 0.0-1.0)
     * @param explanation routing explanation (null will be converted to empty string)
     * @return new RoutingDecision instance
     * @throws NullPointerException     if service is null
     * @throws IllegalArgumentException if service is blank
     */
    @NotNull
    public static RoutingDecision of(
            @NotNull String service,
            double confidence,
            String explanation
    ) {
        return new RoutingDecision(service, confidence, explanation);
    }

    /**
     * Returns true if the confidence is above the given threshold.
     *
     * @param threshold minimum confidence threshold
     * @return true if confidence >= threshold
     */
    public boolean isConfident(double threshold) {
        return confidence >= threshold;
    }

    /**
     * Returns a formatted string representation suitable for logging.
     *
     * @return formatted string
     */
    @Override
    public String toString() {
        return String.format("RoutingDecision[service='%s', confidence=%.2f, explanation='%s']",
                service, confidence, explanation);
    }
}
