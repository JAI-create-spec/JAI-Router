package io.jai.router.core;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Strategy interface for keyword-based routing decisions.
 * <p>
 * Implementations analyze text input and match against configured keywords
 * to determine the most appropriate service for handling the request.
 * This provides a fast, local alternative to external LLM-based routing.
 * </p>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * Map<String, String> keywords = Map.of(
 *     "payment", "payment-service",
 *     "invoice", "payment-service",
 *     "report", "analytics-service"
 * );
 * KeywordMatcher matcher = new ScoringKeywordMatcher(keywords, "default-service", 0.5);
 * MatchResult result = matcher.findBestMatch("Process my payment");
 * System.out.println("Service: " + result.service());
 * System.out.println("Confidence: " + result.confidence());
 * }</pre>
 *
 * @author JAI Router Team
 * @since 1.0.0
 * @see ScoringKeywordMatcher
 */
public interface KeywordMatcher {

    /**
     * Finds the best matching service for the given text.
     * <p>
     * This method analyzes the input text, matches it against configured keywords,
     * and returns the service with the highest match score along with a confidence
     * value and explanation.
     * </p>
     *
     * @param text the input text to analyze, must not be null
     * @return match result containing service, confidence, and explanation
     * @throws NullPointerException if text is null
     */
    @NotNull
    MatchResult findBestMatch(@NotNull String text);

    /**
     * Immutable result of a keyword matching operation.
     * <p>
     * Contains the matched service identifier, confidence score (0.0-1.0),
     * and a human-readable explanation of why this service was selected.
     * </p>
     *
     * @param service     the matched service identifier, never null
     * @param confidence  confidence score between 0.0 and 1.0
     * @param explanation human-readable explanation of the match, never null
     */
    record MatchResult(
            @NotNull String service,
            double confidence,
            @NotNull String explanation
    ) {
        /**
         * Compact constructor with validation.
         *
         * @throws NullPointerException     if service or explanation is null
         * @throws IllegalArgumentException if confidence is not between 0.0 and 1.0
         */
        public MatchResult {
            Objects.requireNonNull(service, "Service cannot be null");
            Objects.requireNonNull(explanation, "Explanation cannot be null");
            if (confidence < 0.0 || confidence > 1.0) {
                throw new IllegalArgumentException(
                        String.format("Confidence must be between 0.0 and 1.0, got: %.2f", confidence)
                );
            }
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
            return String.format("MatchResult[service='%s', confidence=%.2f, explanation='%s']",
                    service, confidence, explanation);
        }
    }
}
