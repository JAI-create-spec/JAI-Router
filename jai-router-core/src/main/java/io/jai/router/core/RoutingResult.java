package io.jai.router.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Objects;

/**
 * Immutable result of a routing decision.
 * <p>
 * Contains the target service identifier, confidence score (0.0-1.0),
 * a human-readable explanation of why this service was chosen,
 * processing time metrics, and timestamp information.
 * </p>
 *
 * @param service          the target service identifier, never null
 * @param confidence       confidence score between 0.0 and 1.0
 * @param explanation      human-readable explanation, may be empty but never null
 * @param processingTimeMs processing time in milliseconds
 * @param timestamp        when the routing decision was made, never null
 * @author JAI Router Team
 * @since 1.0.0
 */
public record RoutingResult(
        @NotNull String service,
        double confidence,
        @NotNull String explanation,
        long processingTimeMs,
        @NotNull Instant timestamp
) {
    /**
     * Compact constructor with validation.
     *
     * @throws NullPointerException if service, explanation, or timestamp is null
     */
    public RoutingResult {
        Objects.requireNonNull(service, "Service cannot be null");
        Objects.requireNonNull(explanation, "Explanation cannot be null");
        Objects.requireNonNull(timestamp, "Timestamp cannot be null");
        if (processingTimeMs < 0) {
            processingTimeMs = 0;
        }
    }

    /**
     * Creates a routing result with the specified values and current timestamp.
     *
     * @param service          target service ID
     * @param confidence       confidence score (will be clamped to 0.0-1.0)
     * @param explanation      routing explanation
     * @param processingTimeMs processing time in milliseconds
     * @return new RoutingResult instance
     */
    @NotNull
    public static RoutingResult of(
            @NotNull String service,
            double confidence,
            @Nullable String explanation,
            long processingTimeMs
    ) {
        double clampedConfidence = Math.max(0.0, Math.min(1.0, confidence));
        String safeExplanation = explanation != null ? explanation : "";
        return new RoutingResult(service, clampedConfidence, safeExplanation, processingTimeMs, Instant.now());
    }

    /**
     * Creates a routing result with the specified values (backward compatibility).
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
        return of(service, confidence, explanation, 0L);
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
        return String.format("RoutingResult[service='%s', confidence=%.2f, explanation='%s', processingTimeMs=%d, timestamp=%s]",
                service, confidence, explanation, processingTimeMs, timestamp);
    }
}
