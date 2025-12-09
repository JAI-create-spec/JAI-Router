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
