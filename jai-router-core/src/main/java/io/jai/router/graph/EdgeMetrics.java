package io.jai.router.graph;

import org.jetbrains.annotations.NotNull;

/**
 * Metrics for calculating edge weight in service graph.
 * <p>
 * Weight is calculated as a weighted sum of latency, cost, and unreliability:
 * <pre>
 * weight = α*latency + β*cost + γ*(1 - reliability)*1000
 * </pre>
 * </p>
 *
 * @param latencyMs   average latency in milliseconds (must be >= 0)
 * @param cost        financial cost per call (must be >= 0)
 * @param reliability reliability score 0.0 (always fails) to 1.0 (always succeeds)
 * @author JAI Router Team
 * @since 0.6.0
 */
public record EdgeMetrics(double latencyMs, double cost, double reliability) {

    // Configurable weights for edge calculation
    private static final double LATENCY_WEIGHT = 0.5;
    private static final double COST_WEIGHT = 0.3;
    private static final double RELIABILITY_WEIGHT = 0.2;

    /**
     * Canonical constructor with validation.
     */
    public EdgeMetrics {
        if (latencyMs < 0) {
            throw new IllegalArgumentException("Latency must be >= 0, got: " + latencyMs);
        }
        if (cost < 0) {
            throw new IllegalArgumentException("Cost must be >= 0, got: " + cost);
        }
        if (reliability < 0 || reliability > 1) {
            throw new IllegalArgumentException("Reliability must be 0-1, got: " + reliability);
        }
    }

    /**
     * Creates default metrics for testing or initial setup.
     *
     * @return default metrics with 50ms latency, 0.001 cost, 0.99 reliability
     */
    @NotNull
    public static EdgeMetrics defaults() {
        return new EdgeMetrics(50.0, 0.001, 0.99);
    }

    /**
     * Calculates the edge weight based on configured weights.
     * <p>
     * Lower weight = better path. Formula weights latency and cost positively,
     * and unreliability (scaled by 1000 to match latency scale) positively.
     * </p>
     *
     * @return calculated weight
     */
    public double calculateWeight() {
        return LATENCY_WEIGHT * latencyMs +
                COST_WEIGHT * cost +
                RELIABILITY_WEIGHT * (1.0 - reliability) * 1000; // Scale unreliability
    }
}

