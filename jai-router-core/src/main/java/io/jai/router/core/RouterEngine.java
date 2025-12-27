package io.jai.router.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Default implementation of the {@link Router} interface.
 * <p>
 * This engine delegates routing decisions to a configured {@link LlmClient}
 * and transforms the result into a {@link RoutingResult}.
 * </p>
 *
 * @author JAI Router Team
 * @since 1.0.0
 */
public class RouterEngine implements Router {

    private static final Logger log = LoggerFactory.getLogger(RouterEngine.class);

    private final LlmClient client;
    private final int maxRetries;
    private final boolean enableRetry;

    /**
     * Creates a new router engine with the specified LLM client.
     *
     * @param client the LLM client to use for routing decisions, must not be null
     * @throws NullPointerException if client is null
     */
    public RouterEngine(@NotNull LlmClient client) {
        this(client, 0, false);
    }

    /**
     * Creates a new router engine with the specified LLM client and retry configuration.
     *
     * @param client      the LLM client to use for routing decisions, must not be null
     * @param maxRetries  maximum number of retry attempts
     * @param enableRetry whether to enable automatic retry on failures
     * @throws NullPointerException if client is null
     */
    private RouterEngine(@NotNull LlmClient client, int maxRetries, boolean enableRetry) {
        this.client = Objects.requireNonNull(client, "LlmClient cannot be null");
        this.maxRetries = maxRetries;
        this.enableRetry = enableRetry;
        log.info("RouterEngine initialized with client: {}", client.getClass().getSimpleName());
    }

    /**
     * Creates a new builder for constructing RouterEngine instances.
     *
     * @return a new builder instance
     */
    @NotNull
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Routes the given input to the most appropriate service.
     *
     * @param input the request content to route, must not be null
     * @return routing result containing service ID, confidence, and explanation
     * @throws NullPointerException     if input is null
     * @throws IllegalArgumentException if input is invalid (via DecisionContext)
     * @throws IllegalStateException    if LLM client returns no decision
     */
    @Override
    @NotNull
    public RoutingResult route(@NotNull String input) {
        Objects.requireNonNull(input, "Input cannot be null");

        long startTime = System.currentTimeMillis();
        java.time.Instant timestamp = java.time.Instant.now();

        if (log.isDebugEnabled()) {
            log.debug("Routing request: '{}'", input.substring(0, Math.min(input.length(), 100)));
        }

        DecisionContext ctx = DecisionContext.of(input);
        RoutingDecision decision;

        if (enableRetry && maxRetries > 0) {
            decision = client.decideWithRetry(ctx, maxRetries);
        } else {
            decision = client.decide(ctx);
        }

        if (decision == null) {
            log.error("LLM client returned null decision for input");
            throw new IllegalStateException("LLM client returned no decision");
        }

        long processingTime = System.currentTimeMillis() - startTime;

        RoutingResult result = new RoutingResult(
                decision.service(),
                decision.confidence(),
                decision.explanation(),
                processingTime,
                timestamp
        );

        if (log.isDebugEnabled()) {
            log.debug("Routing result: service='{}' confidence={} processingTimeMs={}",
                    result.service(), result.confidence(), result.processingTimeMs());
        }

        return result;
    }

    /**
     * Builder for creating {@link RouterEngine} instances with custom configuration.
     * <p>
     * This builder provides a fluent API for configuring the router engine with
     * optional features like retry logic and custom settings.
     * </p>
     *
     * <h2>Usage Example</h2>
     * <pre>{@code
     * RouterEngine router = RouterEngine.builder()
     *     .client(myLlmClient)
     *     .maxRetries(3)
     *     .enableRetry(true)
     *     .build();
     * }</pre>
     */
    public static class Builder {
        private LlmClient client;
        private int maxRetries = 0;
        private boolean enableRetry = false;

        private Builder() {
        }

        /**
         * Sets the LLM client to use for routing decisions.
         *
         * @param client the LLM client, must not be null
         * @return this builder instance
         */
        @NotNull
        public Builder client(@NotNull LlmClient client) {
            this.client = client;
            return this;
        }

        /**
         * Sets the maximum number of retry attempts for failed routing decisions.
         *
         * @param maxRetries maximum retry attempts (must be >= 0)
         * @return this builder instance
         * @throws IllegalArgumentException if maxRetries is negative
         */
        @NotNull
        public Builder maxRetries(int maxRetries) {
            if (maxRetries < 0) {
                throw new IllegalArgumentException("maxRetries must be non-negative");
            }
            this.maxRetries = maxRetries;
            return this;
        }

        /**
         * Enables or disables automatic retry on routing failures.
         *
         * @param enableRetry true to enable retry, false to disable
         * @return this builder instance
         */
        @NotNull
        public Builder enableRetry(boolean enableRetry) {
            this.enableRetry = enableRetry;
            return this;
        }

        /**
         * Builds a new {@link RouterEngine} instance with the configured settings.
         *
         * @return a new RouterEngine instance
         * @throws NullPointerException if client is null
         */
        @NotNull
        public RouterEngine build() {
            Objects.requireNonNull(client, "LlmClient is required");
            return new RouterEngine(client, maxRetries, enableRetry);
        }
    }
}
