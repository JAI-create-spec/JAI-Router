package io.jai.router.core;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Objects;

/**
 * Interface for LLM (Language Model) clients that make routing decisions.
 * <p>
 * Implementations can use various strategies: keyword matching, ML models,
 * cloud AI services (OpenAI, Anthropic), or custom algorithms.
 * </p>
 *
 * @author JAI Router Team
 * @since 1.0.0
 */
@FunctionalInterface
public interface LlmClient {

    Logger log = LoggerFactory.getLogger(LlmClient.class);

    /**
     * Makes a routing decision based on the provided context.
     * <p>
     * Implementations should analyze the input and return a decision
     * indicating which service should handle the request.
     * </p>
     *
     * @param ctx the decision context containing input and metadata
     * @return routing decision, never null
     * @throws LlmClientException if the decision cannot be made
     * @throws NullPointerException if ctx is null
     */
    @NotNull
    RoutingDecision decide(@NotNull DecisionContext ctx);

    /**
     * Safe version of decide() that returns Optional instead of throwing exceptions.
     * <p>
     * This is useful for fallback scenarios where you want to gracefully handle
     * failures without catching exceptions.
     * </p>
     *
     * @param ctx the decision context
     * @return Optional containing the decision, or empty if it fails
     */
    @NotNull
    default Optional<RoutingDecision> decideNullable(@NotNull DecisionContext ctx) {
        try {
            RoutingDecision decision = decide(ctx);
            // decide() contract promises a non-null RoutingDecision, use Optional.of
            return Optional.of(Objects.requireNonNull(decision, "decide() returned null"));
        } catch (Exception ex) {
            if (log.isWarnEnabled()) {
                log.warn("LLM client decision failed: {}", ex.getMessage());
            }
            return Optional.empty();
        }
    }

    /**
     * Makes a routing decision with automatic retry on failure.
     * <p>
     * This method attempts to make a routing decision and retries on failure
     * using exponential backoff. This is useful for handling transient failures
     * in external LLM services.
     * </p>
     *
     * @param ctx        the decision context containing input and metadata
     * @param maxRetries maximum number of retry attempts (must be >= 0)
     * @return routing decision, never null
     * @throws LlmClientException   if all retry attempts fail
     * @throws NullPointerException if ctx is null
     * @throws IllegalArgumentException if maxRetries is negative
     */
    @NotNull
    default RoutingDecision decideWithRetry(@NotNull DecisionContext ctx, int maxRetries) {
        Objects.requireNonNull(ctx, "DecisionContext cannot be null");
        if (maxRetries < 0) {
            throw new IllegalArgumentException("maxRetries must be non-negative");
        }

        int attempts = 0;
        Exception lastException = null;

        while (attempts <= maxRetries) {
            try {
                return decide(ctx);
            } catch (Exception ex) {
                lastException = ex;
                attempts++;

                if (attempts <= maxRetries) {
                    long backoffMs = (long) Math.pow(2, attempts) * 100; // Exponential backoff
                    if (log.isDebugEnabled()) {
                        log.debug("Retry attempt {}/{} after {}ms due to: {}",
                                attempts, maxRetries, backoffMs, ex.getMessage());
                    }

                    try {
                        Thread.sleep(backoffMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new LlmClientException("Retry interrupted", ie);
                    }
                }
            }
        }

        throw new LlmClientException(
                String.format("Failed after %d retry attempts", maxRetries),
                lastException
        );
    }

    /**
     * Makes a routing decision with automatic retry and custom backoff strategy.
     * <p>
     * This method provides more control over the retry behavior by allowing
     * custom backoff delays between retry attempts.
     * </p>
     *
     * @param ctx              the decision context containing input and metadata
     * @param maxRetries       maximum number of retry attempts (must be >= 0)
     * @param initialBackoffMs initial backoff delay in milliseconds
     * @param maxBackoffMs     maximum backoff delay in milliseconds
     * @return routing decision, never null
     * @throws LlmClientException   if all retry attempts fail
     * @throws NullPointerException if ctx is null
     * @throws IllegalArgumentException if parameters are invalid
     */
    @NotNull
    default RoutingDecision decideWithRetry(
            @NotNull DecisionContext ctx,
            int maxRetries,
            long initialBackoffMs,
            long maxBackoffMs
    ) {
        Objects.requireNonNull(ctx, "DecisionContext cannot be null");
        if (maxRetries < 0) {
            throw new IllegalArgumentException("maxRetries must be non-negative");
        }
        if (initialBackoffMs <= 0) {
            throw new IllegalArgumentException("initialBackoffMs must be positive");
        }
        if (maxBackoffMs < initialBackoffMs) {
            throw new IllegalArgumentException("maxBackoffMs must be >= initialBackoffMs");
        }

        int attempts = 0;
        Exception lastException = null;

        while (attempts <= maxRetries) {
            try {
                return decide(ctx);
            } catch (Exception ex) {
                lastException = ex;
                attempts++;

                if (attempts <= maxRetries) {
                    long backoffMs = Math.min(
                            initialBackoffMs * (long) Math.pow(2, attempts - 1),
                            maxBackoffMs
                    );

                    if (log.isDebugEnabled()) {
                        log.debug("Retry attempt {}/{} after {}ms due to: {}",
                                attempts, maxRetries, backoffMs, ex.getMessage());
                    }

                    try {
                        Thread.sleep(backoffMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new LlmClientException("Retry interrupted", ie);
                    }
                }
            }
        }

        throw new LlmClientException(
                String.format("Failed after %d retry attempts", maxRetries),
                lastException
        );
    }

    /**
     * Returns a human-readable name for this client implementation.
     * <p>
     * Default implementation returns the simple class name.
     * </p>
     *
     * @return client name
     */
    @NotNull
    default String getName() {
        return getClass().getSimpleName();
    }
}
