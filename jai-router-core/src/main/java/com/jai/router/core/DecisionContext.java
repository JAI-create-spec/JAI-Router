package com.jai.router.core;

import java.util.Objects;

/**
 * Immutable decision context holding the raw payload used for routing decisions.
 * 
 * <p>The payload is automatically trimmed and validated to be non-empty.
 * Payloads exceeding the maximum size are rejected to prevent DoS attacks.
 * 
 * @param payload the raw input text for routing (never null or empty after construction)
 * @throws IllegalArgumentException if payload is null, empty, or exceeds maximum size
 */
public record DecisionContext(String payload) {
    private static final int MAX_PAYLOAD_SIZE = 10_000;
    private static final String PAYLOAD_NULL_MSG = "Payload cannot be null";
    private static final String PAYLOAD_EMPTY_MSG = "Payload cannot be empty";
    private static final String PAYLOAD_TOO_LARGE_MSG = 
        "Payload exceeds maximum size of " + MAX_PAYLOAD_SIZE + " characters";

    public DecisionContext {
        Objects.requireNonNull(payload, PAYLOAD_NULL_MSG);
        payload = payload.trim();
        
        if (payload.isEmpty()) {
            throw new IllegalArgumentException(PAYLOAD_EMPTY_MSG);
        }
        
        if (payload.length() > MAX_PAYLOAD_SIZE) {
            throw new IllegalArgumentException(PAYLOAD_TOO_LARGE_MSG);
        }
    }

    /**
     * Factory method for creating a DecisionContext.
     * @param payload the input text
     * @return a new DecisionContext
     * @throws IllegalArgumentException if payload is invalid
     */
    public static DecisionContext of(String payload) {
        return new DecisionContext(payload);
    }
}
