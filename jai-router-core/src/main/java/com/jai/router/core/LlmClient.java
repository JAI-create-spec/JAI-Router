package com.jai.router.core;

import java.util.Optional;

/**
 * Primary interface for LLM-based routing decisions.
 * Implementations should be thread-safe if used as shared beans.
 */
@FunctionalInterface
public interface LlmClient {
    /**
     * Produce a routing decision given the provided context.
     * Implementations may throw a RuntimeException on unrecoverable errors.
     * @param ctx decision context (never null)
     * @return routing decision (may be null in legacy implementations)
     */
    RoutingDecision decide(DecisionContext ctx);

    /**
     * Non-throwing wrapper for {@link #decide(DecisionContext)}. Returns an
     * empty Optional if the implementation throws a RuntimeException or
     * returns null.
     */
    default Optional<RoutingDecision> decideNullable(DecisionContext ctx) {
        try {
            return Optional.ofNullable(decide(ctx));
        } catch (RuntimeException ex) {
            return Optional.empty();
        }
    }
}
