package io.jai.router.core;

import java.util.Objects;

public record DecisionContext(String payload) {
    private static final int MAX_PAYLOAD_SIZE = 10_000;

    public DecisionContext {
        Objects.requireNonNull(payload, "Payload cannot be null");
        payload = payload.trim();
        if (payload.isEmpty()) throw new IllegalArgumentException("Payload cannot be empty");
        if (payload.length() > MAX_PAYLOAD_SIZE) throw new IllegalArgumentException("Payload exceeds maximum size");
    }

    public static DecisionContext of(String payload) { return new DecisionContext(payload); }
}
