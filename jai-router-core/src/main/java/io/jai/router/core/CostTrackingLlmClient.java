package io.jai.router.core;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Wrapper that tracks LLM API costs and usage metrics.
 * <p>
 * This implementation monitors LLM usage by tracking the number of calls,
 * estimated token usage, and associated costs. This is essential for
 * monitoring and controlling LLM API expenses in production environments.
 * </p>
 *
 * <h2>Features</h2>
 * <ul>
 *   <li>Tracks total API calls (successful and failed)</li>
 *   <li>Estimates token usage based on input/output length</li>
 *   <li>Calculates cumulative costs</li>
 *   <li>Thread-safe metrics collection</li>
 *   <li>Detailed cost reporting</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * LlmClient openAI = new OpenAILlmClient();
 * LlmClient tracked = new CostTrackingLlmClient(
 *     openAI,
 *     new BigDecimal("0.002")  // $0.002 per 1K tokens (GPT-3.5)
 * );
 *
 * // Make routing decisions
 * RoutingDecision decision = tracked.decide(ctx);
 *
 * // Check costs
 * CostMetrics metrics = tracked.getMetrics();
 * System.out.println("Total cost: $" + metrics.totalCost());
 * System.out.println("Total calls: " + metrics.totalCalls());
 * }</pre>
 *
 * @author JAI Router Team
 * @since 1.0.0
 */
public class CostTrackingLlmClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(CostTrackingLlmClient.class);

    // Rough approximation: 1 token ≈ 4 characters for English text
    private static final int CHARS_PER_TOKEN = 4;

    // Estimated tokens for routing decision output (service name + explanation)
    private static final int ESTIMATED_OUTPUT_TOKENS = 50;

    private final LlmClient delegate;
    private final BigDecimal costPerThousandTokens;
    private final AtomicLong totalCalls = new AtomicLong(0);
    private final AtomicLong successfulCalls = new AtomicLong(0);
    private final AtomicLong failedCalls = new AtomicLong(0);
    private final AtomicLong totalTokens = new AtomicLong(0);
    private final AtomicReference<BigDecimal> totalCost = new AtomicReference<>(BigDecimal.ZERO);

    /**
     * Creates a cost tracking LLM client.
     *
     * @param delegate               the underlying LLM client to track
     * @param costPerThousandTokens  cost per 1000 tokens (e.g., 0.002 for GPT-3.5)
     * @throws NullPointerException     if delegate or cost is null
     * @throws IllegalArgumentException if cost is negative
     */
    public CostTrackingLlmClient(
            @NotNull LlmClient delegate,
            @NotNull BigDecimal costPerThousandTokens) {
        this.delegate = Objects.requireNonNull(delegate, "Delegate client cannot be null");
        this.costPerThousandTokens = Objects.requireNonNull(costPerThousandTokens,
                "Cost per thousand tokens cannot be null");

        if (costPerThousandTokens.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Cost per thousand tokens cannot be negative");
        }

        log.info("CostTrackingLlmClient initialized: costPer1KTokens=${}", costPerThousandTokens);
    }

    /**
     * Makes a routing decision while tracking costs.
     *
     * @param ctx the decision context containing input and metadata
     * @return routing decision from the delegate client
     * @throws LlmClientException if the delegate client fails
     */
    @Override
    @NotNull
    public RoutingDecision decide(@NotNull DecisionContext ctx) {
        Objects.requireNonNull(ctx, "DecisionContext cannot be null");

        long startTime = System.currentTimeMillis();
        totalCalls.incrementAndGet();

        try {
            RoutingDecision decision = delegate.decide(ctx);
            successfulCalls.incrementAndGet();

            // Estimate tokens used
            long estimatedTokens = estimateTokens(ctx.payload(), decision);
            totalTokens.addAndGet(estimatedTokens);

            // Calculate cost for this call
            BigDecimal callCost = calculateCost(estimatedTokens);
            totalCost.updateAndGet(current -> current.add(callCost));

            long duration = System.currentTimeMillis() - startTime;

            if (log.isDebugEnabled()) {
                log.debug("LLM call completed: tokens={}, cost=${}, duration={}ms, total_cost=${}",
                        estimatedTokens,
                        callCost.setScale(6, RoundingMode.HALF_UP),
                        duration,
                        totalCost.get().setScale(4, RoundingMode.HALF_UP));
            }

            return decision;

        } catch (Exception e) {
            failedCalls.incrementAndGet();
            log.warn("LLM call failed after {}ms: {}", System.currentTimeMillis() - startTime, e.getMessage());
            throw e;
        }
    }

    /**
     * Estimates the number of tokens used for a routing decision.
     *
     * @param input    the input text
     * @param decision the routing decision
     * @return estimated token count
     */
    private long estimateTokens(String input, RoutingDecision decision) {
        // Input tokens
        long inputTokens = input.length() / CHARS_PER_TOKEN;

        // Output tokens (service name + explanation)
        long outputTokens = (decision.service().length() + decision.explanation().length()) / CHARS_PER_TOKEN;

        // Add some overhead for system prompts and formatting
        long overhead = 20;

        return inputTokens + outputTokens + overhead;
    }

    /**
     * Calculates the cost for a given number of tokens.
     *
     * @param tokens number of tokens
     * @return cost in dollars
     */
    private BigDecimal calculateCost(long tokens) {
        BigDecimal tokensBD = new BigDecimal(tokens);
        return tokensBD.multiply(costPerThousandTokens)
                .divide(new BigDecimal(1000), 10, RoundingMode.HALF_UP);
    }

    /**
     * Returns current cost and usage metrics.
     *
     * @return immutable metrics snapshot
     */
    @NotNull
    public CostMetrics getMetrics() {
        return new CostMetrics(
                totalCalls.get(),
                successfulCalls.get(),
                failedCalls.get(),
                totalTokens.get(),
                totalCost.get().setScale(4, RoundingMode.HALF_UP),
                getAverageCostPerCall(),
                getSuccessRate()
        );
    }

    /**
     * Calculates the average cost per successful call.
     *
     * @return average cost per call
     */
    private BigDecimal getAverageCostPerCall() {
        long successful = successfulCalls.get();
        if (successful == 0) {
            return BigDecimal.ZERO;
        }
        return totalCost.get().divide(
                new BigDecimal(successful),
                6,
                RoundingMode.HALF_UP
        );
    }

    /**
     * Calculates the success rate of LLM calls.
     *
     * @return success rate between 0.0 and 1.0
     */
    private double getSuccessRate() {
        long total = totalCalls.get();
        if (total == 0) {
            return 0.0;
        }
        return (double) successfulCalls.get() / total;
    }

    /**
     * Resets all metrics to zero.
     */
    public void resetMetrics() {
        totalCalls.set(0);
        successfulCalls.set(0);
        failedCalls.set(0);
        totalTokens.set(0);
        totalCost.set(BigDecimal.ZERO);
        log.info("Cost tracking metrics reset");
    }

    /**
     * Logs a summary of current metrics.
     */
    public void logMetricsSummary() {
        CostMetrics metrics = getMetrics();
        log.info("=== LLM Cost Tracking Summary ===");
        log.info("Total calls: {} (successful: {}, failed: {})",
                metrics.totalCalls(), metrics.successfulCalls(), metrics.failedCalls());
        log.info("Total tokens: {}", metrics.totalTokens());
        log.info("Total cost: ${}", metrics.totalCost());
        log.info("Average cost per call: ${}", metrics.averageCostPerCall());
        log.info("Success rate: {:.2f}%", metrics.successRate() * 100);
        log.info("================================");
    }

    @Override
    @NotNull
    public String getName() {
        return "CostTrackingLlmClient[delegate=" + delegate.getName() + "]";
    }

    /**
     * Immutable cost and usage metrics.
     *
     * @param totalCalls         total number of LLM calls
     * @param successfulCalls    number of successful calls
     * @param failedCalls        number of failed calls
     * @param totalTokens        total estimated tokens used
     * @param totalCost          total cost in dollars
     * @param averageCostPerCall average cost per successful call
     * @param successRate        success rate (0.0-1.0)
     */
    public record CostMetrics(
            long totalCalls,
            long successfulCalls,
            long failedCalls,
            long totalTokens,
            @NotNull BigDecimal totalCost,
            @NotNull BigDecimal averageCostPerCall,
            double successRate
    ) {
        public CostMetrics {
            Objects.requireNonNull(totalCost, "totalCost cannot be null");
            Objects.requireNonNull(averageCostPerCall, "averageCostPerCall cannot be null");
        }

        @Override
        public String toString() {
            return String.format(
                    "CostMetrics[calls=%d (success=%d, failed=%d), tokens=%d, cost=$%s, avgCost=$%s, successRate=%.2f%%]",
                    totalCalls, successfulCalls, failedCalls, totalTokens,
                    totalCost, averageCostPerCall, successRate * 100
            );
        }

        /**
         * Returns the estimated cost savings if a given percentage of calls were cached.
         *
         * @param cacheHitRate expected cache hit rate (0.0-1.0)
         * @return estimated cost savings
         */
        public BigDecimal estimateSavingsWithCache(double cacheHitRate) {
            if (cacheHitRate < 0.0 || cacheHitRate > 1.0) {
                throw new IllegalArgumentException("Cache hit rate must be between 0.0 and 1.0");
            }
            return totalCost.multiply(new BigDecimal(cacheHitRate))
                    .setScale(4, RoundingMode.HALF_UP);
        }
    }
}
