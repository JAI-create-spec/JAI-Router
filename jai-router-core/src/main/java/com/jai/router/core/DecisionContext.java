package com.jai.router.core;

/**
 * Immutable decision context holding the raw payload used for routing decisions.
 */
public record DecisionContext(String payload) {
    public DecisionContext {
        if (payload == null) {
            throw new IllegalArgumentException("Payload cannot be null or empty");
        }
        payload = payload.trim();
        if (payload.isEmpty()) {
            throw new IllegalArgumentException("Payload cannot be null or empty");
        }
    }

    public static DecisionContext of(String payload) {
        return new DecisionContext(payload);
    }
}
