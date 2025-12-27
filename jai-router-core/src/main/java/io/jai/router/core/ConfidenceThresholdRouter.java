package io.jai.router.core;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Router that falls back to a default service when confidence is below threshold.
 * <p>
 * This implementation wraps another router and monitors the confidence scores
 * of routing decisions. When the confidence falls below a configured threshold,
 * it automatically routes to a fallback service instead. This is useful for
 * ensuring reliability when the routing decision is uncertain.
 * </p>
 *
 * <h2>Features</h2>
 * <ul>
 *   <li>Configurable confidence threshold</li>
 *   <li>Automatic fallback to safe default service</li>
 *   <li>Tracks low-confidence routing statistics</li>
 *   <li>Preserves original routing explanation</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * Router baseRouter = new RouterEngine(llmClient);
 * Router safeRouter = new ConfidenceThresholdRouter(
 *     baseRouter,
 *     0.7,                    // minimum confidence threshold
 *     "human-review-queue"    // fallback service for low confidence
 * );
 *
 * // Low confidence routes go to human review
 * RoutingResult result = safeRouter.route("ambiguous request");
 * if (result.service().equals("human-review-queue")) {
 *     // Handle manual review
 * }
 * }</pre>
 *
 * @author JAI Router Team
 * @since 1.0.0
 */
public class ConfidenceThresholdRouter implements Router {

    private static final Logger log = LoggerFactory.getLogger(ConfidenceThresholdRouter.class);

    private final Router delegate;
    private final double minConfidence;
    private final String fallbackService;
    private final AtomicLong totalRoutings = new AtomicLong(0);
    private final AtomicLong lowConfidenceRoutings = new AtomicLong(0);

    /**
     * Creates a confidence threshold router.
     *
     * @param delegate        the underlying router to wrap
     * @param minConfidence   minimum acceptable confidence (0.0-1.0)
     * @param fallbackService service to use when confidence is too low
     * @throws NullPointerException     if delegate or fallbackService is null
     * @throws IllegalArgumentException if minConfidence is not between 0.0 and 1.0
     */
    public ConfidenceThresholdRouter(
            @NotNull Router delegate,
            double minConfidence,
            @NotNull String fallbackService) {
        this.delegate = Objects.requireNonNull(delegate, "Delegate router cannot be null");
        this.fallbackService = Objects.requireNonNull(fallbackService, "Fallback service cannot be null");

        if (minConfidence < 0.0 || minConfidence > 1.0) {
            throw new IllegalArgumentException(
                    String.format("minConfidence must be between 0.0 and 1.0, got: %.2f", minConfidence)
            );
        }
        this.minConfidence = minConfidence;

        log.info("ConfidenceThresholdRouter initialized: minConfidence={}, fallbackService={}",
                minConfidence, fallbackService);
    }

    /**
     * Routes the input, falling back to default service if confidence is too low.
     *
     * @param input the request content to route, must not be null
     * @return routing result (possibly with fallback service)
     * @throws NullPointerException if input is null
     */
    @Override
    @NotNull
    public RoutingResult route(@NotNull String input) {
        Objects.requireNonNull(input, "Input cannot be null");

        totalRoutings.incrementAndGet();

        // Get routing decision from delegate
        RoutingResult result = delegate.route(input);

        // Check confidence threshold
        if (result.confidence() < minConfidence) {
            lowConfidenceRoutings.incrementAndGet();

            if (log.isWarnEnabled()) {
                log.warn("Low confidence routing detected: confidence={:.2f} < {:.2f}, " +
                                "original_service='{}', fallback_service='{}', low_confidence_rate={:.2f}%",
                        result.confidence(), minConfidence, result.service(),
                        fallbackService, getLowConfidenceRate() * 100);
            }

            // Return fallback routing result
            return new RoutingResult(
                    fallbackService,
                    result.confidence(),
                    String.format("Low confidence (%.2f < %.2f) - routed to fallback. Original: %s -> %s",
                            result.confidence(), minConfidence, result.service(), result.explanation()),
                    result.processingTimeMs(),
                    result.timestamp()
            );
        }

        // Confidence is acceptable, return original result
        if (log.isDebugEnabled()) {
            log.debug("Routing confidence acceptable: {:.2f} >= {:.2f}, service='{}'",
                    result.confidence(), minConfidence, result.service());
        }

        return result;
    }

    /**
     * Returns the percentage of routings that fell below the confidence threshold.
     *
     * @return low confidence rate between 0.0 and 1.0
     */
    public double getLowConfidenceRate() {
        long total = totalRoutings.get();
        if (total == 0) {
            return 0.0;
        }
        return (double) lowConfidenceRoutings.get() / total;
    }

    /**
     * Returns statistics about confidence-based routing.
     *
     * @return routing statistics
     */
    @NotNull
    public ThresholdStats getStats() {
        return new ThresholdStats(
                totalRoutings.get(),
                lowConfidenceRoutings.get(),
                getLowConfidenceRate(),
                minConfidence,
                fallbackService
        );
    }

    /**
     * Resets routing statistics.
     */
    public void resetStats() {
        totalRoutings.set(0);
        lowConfidenceRoutings.set(0);
        log.info("Confidence threshold statistics reset");
    }

    /**
     * Returns the configured minimum confidence threshold.
     *
     * @return minimum confidence threshold
     */
    public double getMinConfidence() {
        return minConfidence;
    }

    /**
     * Returns the configured fallback service.
     *
     * @return fallback service identifier
     */
    @NotNull
    public String getFallbackService() {
        return fallbackService;
    }

    /**
     * Immutable statistics for confidence threshold routing.
     *
     * @param totalRoutings         total number of routing requests
     * @param lowConfidenceRoutings number of low-confidence routings
     * @param lowConfidenceRate     percentage of low-confidence routings (0.0-1.0)
     * @param threshold             configured confidence threshold
     * @param fallbackService       configured fallback service
     */
    public record ThresholdStats(
            long totalRoutings,
            long lowConfidenceRoutings,
            double lowConfidenceRate,
            double threshold,
            @NotNull String fallbackService
    ) {
        public ThresholdStats {
            Objects.requireNonNull(fallbackService, "fallbackService cannot be null");
        }

        @Override
        public String toString() {
            return String.format(
                    "ThresholdStats[total=%d, lowConfidence=%d (%.2f%%), threshold=%.2f, fallback='%s']",
                    totalRoutings, lowConfidenceRoutings, lowConfidenceRate * 100,
                    threshold, fallbackService
            );
        }

        /**
         * Returns true if the low confidence rate exceeds the given threshold.
         *
         * @param maxAcceptableRate maximum acceptable low confidence rate (0.0-1.0)
         * @return true if low confidence rate is too high
         */
        public boolean isLowConfidenceRateTooHigh(double maxAcceptableRate) {
            return lowConfidenceRate > maxAcceptableRate;
        }
    }
}
