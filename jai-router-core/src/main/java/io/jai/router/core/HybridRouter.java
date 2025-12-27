package io.jai.router.core;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Hybrid router that combines keyword matching, semantic analysis, and LLM for optimal routing.
 * <p>
 * This implementation uses a tiered approach to routing:
 * <ol>
 *   <li>Fast keyword matching for high-confidence matches</li>
 *   <li>Semantic/ML-based routing for medium confidence</li>
 *   <li>LLM-based routing for complex or ambiguous cases</li>
 * </ol>
 * This approach optimizes for both speed and accuracy while minimizing LLM API costs.
 * </p>
 *
 * <h2>Features</h2>
 * <ul>
 *   <li>Three-tier routing strategy (keyword → semantic → LLM)</li>
 *   <li>Configurable confidence thresholds for each tier</li>
 *   <li>Automatic fallback to more sophisticated methods</li>
 *   <li>Tracks which routing method was used</li>
 *   <li>Optimizes cost vs. accuracy trade-off</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * KeywordMatcher keywordMatcher = new ScoringKeywordMatcher(...);
 * Router semanticRouter = new SemanticRouter(...);  // Optional
 * LlmClient llmClient = new OpenAILlmClient();
 *
 * HybridRouter router = HybridRouter.builder()
 *     .keywordMatcher(keywordMatcher)
 *     .keywordConfidenceThreshold(0.8)
 *     .semanticRouter(semanticRouter)
 *     .semanticConfidenceThreshold(0.7)
 *     .llmClient(llmClient)
 *     .build();
 *
 * RoutingResult result = router.route("Process payment");
 * // Uses fastest method with sufficient confidence
 * }</pre>
 *
 * @author JAI Router Team
 * @since 1.0.0
 */
public class HybridRouter implements Router {

    private static final Logger log = LoggerFactory.getLogger(HybridRouter.class);

    private final KeywordMatcher keywordMatcher;
    private final Router semanticRouter;
    private final LlmClient llmClient;
    private final double keywordConfidenceThreshold;
    private final double semanticConfidenceThreshold;
    private final LearningRouter learningRouter;

    // Statistics
    private final AtomicLong totalRoutings = new AtomicLong(0);
    private final AtomicLong keywordRoutings = new AtomicLong(0);
    private final AtomicLong semanticRoutings = new AtomicLong(0);
    private final AtomicLong llmRoutings = new AtomicLong(0);

    /**
     * Creates a hybrid router with the specified configuration.
     *
     * @param keywordMatcher              keyword matcher for fast routing
     * @param keywordConfidenceThreshold  minimum confidence for keyword routing
     * @param semanticRouter              optional semantic router
     * @param semanticConfidenceThreshold minimum confidence for semantic routing
     * @param llmClient                   LLM client for complex routing
     * @param learningRouter               optional learning router
     */
    private HybridRouter(
            @NotNull KeywordMatcher keywordMatcher,
            double keywordConfidenceThreshold,
            Router semanticRouter,
            double semanticConfidenceThreshold,
            @NotNull LlmClient llmClient,
            LearningRouter learningRouter) {
        this.keywordMatcher = Objects.requireNonNull(keywordMatcher, "KeywordMatcher cannot be null");
        this.llmClient = Objects.requireNonNull(llmClient, "LlmClient cannot be null");
        this.semanticRouter = semanticRouter;
        this.keywordConfidenceThreshold = keywordConfidenceThreshold;
        this.semanticConfidenceThreshold = semanticConfidenceThreshold;
        this.learningRouter = learningRouter;
        log.info("HybridRouter initialized: keywordThreshold={}, semanticThreshold={}, hasSemanticRouter={}, hasLearningRouter={}",
                keywordConfidenceThreshold, semanticConfidenceThreshold, semanticRouter != null, learningRouter != null);
    }

    /**
     * Creates a new builder for constructing HybridRouter instances.
     *
     * @return a new builder instance
     */
    @NotNull
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Routes the input using a tiered approach.
     * <p>
     * This method tries routing strategies in order of speed/cost:
     * 1. Keyword matching (fastest, cheapest)
     * 2. Semantic routing (medium speed/cost) - if configured
     * 3. LLM routing (slowest, most expensive, most accurate)
     * </p>
     *
     * @param input the request content to route, must not be null
     * @return routing result from the first method with sufficient confidence
     * @throws NullPointerException if input is null
     */
    @Override
    @NotNull
    public RoutingResult route(@NotNull String input) {
        Objects.requireNonNull(input, "Input cannot be null");
        if (learningRouter != null) {
            return learningRouter.route(input);
        }

        long startTime = System.currentTimeMillis();
        totalRoutings.incrementAndGet();

        // Tier 1: Try fast keyword matching first
        KeywordMatcher.MatchResult keywordResult = keywordMatcher.findBestMatch(input);
        if (keywordResult.confidence() >= keywordConfidenceThreshold) {
            keywordRoutings.incrementAndGet();

            long processingTime = System.currentTimeMillis() - startTime;

            if (log.isDebugEnabled()) {
                log.debug("Hybrid routing: KEYWORD match (confidence={} >= {}), service='{}', time={}ms, keyword_usage={}",
                        String.format("%.2f", keywordResult.confidence()),
                        String.format("%.2f", keywordConfidenceThreshold),
                        keywordResult.service(),
                        processingTime,
                        String.format("%.2f%%", getKeywordUsageRate() * 100));
            }

            return RoutingResult.of(
                    keywordResult.service(),
                    keywordResult.confidence(),
                    "Keyword routing: " + keywordResult.explanation(),
                    processingTime
            );
        }

        // Tier 2: Try semantic routing if available
        if (semanticRouter != null) {
            RoutingResult semanticResult = semanticRouter.route(input);
            if (semanticResult.confidence() >= semanticConfidenceThreshold) {
                semanticRoutings.incrementAndGet();

                long processingTime = System.currentTimeMillis() - startTime;

                if (log.isDebugEnabled()) {
                    log.debug("Hybrid routing: SEMANTIC match (confidence={} >= {}), service='{}', time={}ms, semantic_usage={}",
                            String.format("%.2f", semanticResult.confidence()),
                            String.format("%.2f", semanticConfidenceThreshold),
                            semanticResult.service(),
                            processingTime,
                            String.format("%.2f%%", getSemanticUsageRate() * 100));
                }

                return new RoutingResult(
                        semanticResult.service(),
                        semanticResult.confidence(),
                        "Semantic routing: " + semanticResult.explanation(),
                        processingTime,
                        semanticResult.timestamp()
                );
            }
        }

        // Tier 3: Fall back to LLM for complex cases
        llmRoutings.incrementAndGet();

        DecisionContext ctx = DecisionContext.of(input);
        RoutingDecision llmDecision = llmClient.decide(ctx);

        long processingTime = System.currentTimeMillis() - startTime;

        if (log.isDebugEnabled()) {
            log.debug("Hybrid routing: LLM routing (keyword={} < {}{}), service='{}', time={}ms, llm_usage={}",
                    String.format("%.2f", keywordResult.confidence()),
                    String.format("%.2f", keywordConfidenceThreshold),
                    semanticRouter != null ? ", semantic < " + String.format("%.2f", semanticConfidenceThreshold) : "",
                    llmDecision.service(),
                    processingTime,
                    String.format("%.2f%%", getLlmUsageRate() * 100));
        }

        return RoutingResult.of(
                llmDecision.service(),
                llmDecision.confidence(),
                "LLM routing: " + llmDecision.explanation(),
                processingTime
        );
    }

    /**
     * Returns the percentage of routings handled by keyword matching.
     *
     * @return keyword usage rate (0.0-1.0)
     */
    public double getKeywordUsageRate() {
        long total = totalRoutings.get();
        return total == 0 ? 0.0 : (double) keywordRoutings.get() / total;
    }

    /**
     * Returns the percentage of routings handled by semantic routing.
     *
     * @return semantic usage rate (0.0-1.0)
     */
    public double getSemanticUsageRate() {
        long total = totalRoutings.get();
        return total == 0 ? 0.0 : (double) semanticRoutings.get() / total;
    }

    /**
     * Returns the percentage of routings handled by LLM.
     *
     * @return LLM usage rate (0.0-1.0)
     */
    public double getLlmUsageRate() {
        long total = totalRoutings.get();
        return total == 0 ? 0.0 : (double) llmRoutings.get() / total;
    }

    /**
     * Returns statistics about hybrid routing performance.
     *
     * @return hybrid routing statistics
     */
    @NotNull
    public HybridStats getStats() {
        return new HybridStats(
                totalRoutings.get(),
                keywordRoutings.get(),
                semanticRoutings.get(),
                llmRoutings.get(),
                getKeywordUsageRate(),
                getSemanticUsageRate(),
                getLlmUsageRate()
        );
    }

    /**
     * Resets routing statistics.
     */
    public void resetStats() {
        totalRoutings.set(0);
        keywordRoutings.set(0);
        semanticRoutings.set(0);
        llmRoutings.set(0);
        log.info("Hybrid router statistics reset");
    }

    /**
     * Logs a summary of hybrid routing statistics.
     */
    public void logStatsSummary() {
        HybridStats stats = getStats();
        log.info("=== Hybrid Router Statistics ===");
        log.info("Total routings: {}", stats.totalRoutings());
        log.info("Keyword routings: {} ({}%)", stats.keywordRoutings(), String.format("%.2f", stats.keywordUsageRate() * 100));
        log.info("Semantic routings: {} ({}%)", stats.semanticRoutings(), String.format("%.2f", stats.semanticUsageRate() * 100));
        log.info("LLM routings: {} ({}%)", stats.llmRoutings(), String.format("%.2f", stats.llmUsageRate() * 100));
        log.info("Cost efficiency: {}% fast routing", String.format("%.2f", (stats.keywordUsageRate() + stats.semanticUsageRate()) * 100));
        log.info("================================");
    }

    /**
     * Builder for creating {@link HybridRouter} instances.
     */
    public static class Builder {
        private KeywordMatcher keywordMatcher;
        private double keywordConfidenceThreshold = 0.8;
        private Router semanticRouter;
        private double semanticConfidenceThreshold = 0.7;
        private LlmClient llmClient;
        private LearningRouter learningRouter;

        private Builder() {
        }

        /**
         * Sets the keyword matcher for fast routing.
         *
         * @param keywordMatcher the keyword matcher
         * @return this builder instance
         */
        @NotNull
        public Builder keywordMatcher(@NotNull KeywordMatcher keywordMatcher) {
            this.keywordMatcher = keywordMatcher;
            return this;
        }

        /**
         * Sets the minimum confidence threshold for keyword routing.
         *
         * @param threshold confidence threshold (0.0-1.0)
         * @return this builder instance
         */
        @NotNull
        public Builder keywordConfidenceThreshold(double threshold) {
            if (threshold < 0.0 || threshold > 1.0) {
                throw new IllegalArgumentException("Threshold must be between 0.0 and 1.0");
            }
            this.keywordConfidenceThreshold = threshold;
            return this;
        }

        /**
         * Sets the semantic router (optional).
         *
         * @param semanticRouter the semantic router
         * @return this builder instance
         */
        @NotNull
        public Builder semanticRouter(Router semanticRouter) {
            this.semanticRouter = semanticRouter;
            return this;
        }

        /**
         * Sets the minimum confidence threshold for semantic routing.
         *
         * @param threshold confidence threshold (0.0-1.0)
         * @return this builder instance
         */
        @NotNull
        public Builder semanticConfidenceThreshold(double threshold) {
            if (threshold < 0.0 || threshold > 1.0) {
                throw new IllegalArgumentException("Threshold must be between 0.0 and 1.0");
            }
            this.semanticConfidenceThreshold = threshold;
            return this;
        }

        /**
         * Sets the LLM client for complex routing.
         *
         * @param llmClient the LLM client
         * @return this builder instance
         */
        @NotNull
        public Builder llmClient(@NotNull LlmClient llmClient) {
            this.llmClient = llmClient;
            return this;
        }

        /**
         * Sets the learning router (optional).
         *
         * @param learningRouter the learning router
         * @return this builder instance
         */
        public Builder learningRouter(LearningRouter learningRouter) {
            this.learningRouter = learningRouter;
            return this;
        }

        /**
         * Builds a new {@link HybridRouter} instance.
         *
         * @return a new HybridRouter
         * @throws NullPointerException if required components are null
         */
        @NotNull
        public HybridRouter build() {
            Objects.requireNonNull(keywordMatcher, "KeywordMatcher is required");
            Objects.requireNonNull(llmClient, "LlmClient is required");
            return new HybridRouter(
                    keywordMatcher,
                    keywordConfidenceThreshold,
                    semanticRouter,
                    semanticConfidenceThreshold,
                    llmClient,
                    learningRouter
            );
        }
    }

    /**
     * Immutable statistics for hybrid routing.
     *
     * @param totalRoutings      total number of routing requests
     * @param keywordRoutings    routings handled by keyword matching
     * @param semanticRoutings   routings handled by semantic routing
     * @param llmRoutings        routings handled by LLM
     * @param keywordUsageRate   percentage of keyword routings (0.0-1.0)
     * @param semanticUsageRate  percentage of semantic routings (0.0-1.0)
     * @param llmUsageRate       percentage of LLM routings (0.0-1.0)
     */
    public record HybridStats(
            long totalRoutings,
            long keywordRoutings,
            long semanticRoutings,
            long llmRoutings,
            double keywordUsageRate,
            double semanticUsageRate,
            double llmUsageRate
    ) {
        @Override
        public String toString() {
            return String.format(
                    "HybridStats[total=%d, keyword=%d (%.1f%%), semantic=%d (%.1f%%), llm=%d (%.1f%%)]",
                    totalRoutings,
                    keywordRoutings, keywordUsageRate * 100,
                    semanticRoutings, semanticUsageRate * 100,
                    llmRoutings, llmUsageRate * 100
            );
        }

        /**
         * Returns the percentage of routings handled by fast methods (keyword + semantic).
         *
         * @return fast routing rate (0.0-1.0)
         */
        public double getFastRoutingRate() {
            return keywordUsageRate + semanticUsageRate;
        }

        /**
         * Estimates cost savings compared to using only LLM routing.
         *
         * @param llmCostPerRequest cost per LLM request
         * @return estimated cost savings
         */
        public double estimateCostSavings(double llmCostPerRequest) {
            long fastRoutings = keywordRoutings + semanticRoutings;
            return fastRoutings * llmCostPerRequest;
        }
    }
}
