package com.jai.router.core;

import java.util.Optional;

/**
 * Primary interface for LLM-based routing decisions.
 * 
 * <p>Implementations should be thread-safe if used as shared beans.
 * 
 * <p>Implementations may throw {@link LlmClientException} or other
 * {@link RuntimeException}s on unrecoverable errors.
 */
@FunctionalInterface
public interface LlmClient {
    /**
     * Produce a routing decision given the provided context.
     * 
     * @param ctx decision context (never null)
     * @return routing decision (never null)
     * @throws LlmClientException if routing decision cannot be made
     * @throws IllegalArgumentException if context is invalid
     */
    RoutingDecision decide(DecisionContext ctx);

    /**
     * Non-throwing wrapper for {@link #decide(DecisionContext)}.
     * 
     * <p>Returns an empty Optional if the implementation throws a RuntimeException
     * or returns null.
     * 
     * @param ctx decision context
     * @return Optional containing routing decision, or empty if error occurs
     */
    default Optional<RoutingDecision> decideNullable(DecisionContext ctx) {
        try {
            return Optional.ofNullable(decide(ctx));
        } catch (RuntimeException ex) {
            return Optional.empty();
        }
    }
}
