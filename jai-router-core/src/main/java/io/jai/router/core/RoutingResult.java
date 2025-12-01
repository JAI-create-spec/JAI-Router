package io.jai.router.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Immutable result of a routing decision.
 * <p>
 * Contains the target service identifier, confidence score (0.0-1.0),
 * and a human-readable explanation of why this service was chosen.
 * </p>
 *
 * @param service     the target service identifier, never null
 * @param confidence  confidence score between 0.0 and 1.0
 * @param explanation human-readable explanation, may be empty but never null
 * @author JAI Router Team
 * @since 1.0.0
 */
public record RoutingResult(
        @NotNull String service,
        double confidence,
        @NotNull String explanation
) {
    /**
     * Compact constructor with validation.
     *
     * @throws NullPointerException if service or explanation is null
     */
    public RoutingResult {
        Objects.requireNonNull(service, "Service cannot be null");
        Objects.requireNonNull(explanation, "Explanation cannot be null");
    }

    /**
     * Creates a routing result with the specified values.
     *
     * @param service     target service ID
     * @param confidence  confidence score (will be clamped to 0.0-1.0)
     * @param explanation routing explanation
     * @return new RoutingResult instance
     */
    @NotNull
    public static RoutingResult of(
            @NotNull String service,
            double confidence,
            @Nullable String explanation
    ) {
        double clampedConfidence = Math.max(0.0, Math.min(1.0, confidence));
        String safeExplanation = explanation != null ? explanation : "";
        return new RoutingResult(service, clampedConfidence, safeExplanation);
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
        return String.format("RoutingResult[service='%s', confidence=%.2f, explanation='%s']",
                service, confidence, explanation);
    }
}
