package io.jai.router.core;

import java.util.Objects;

public final class DecisionContext {
    private static final int MAX_PAYLOAD = 10_000;
    private final String payload;

    public DecisionContext(String payload) {
        if (payload == null) throw new NullPointerException("Payload cannot be null");
        String trimmed = payload.trim();
        if (trimmed.isEmpty()) throw new IllegalArgumentException("Payload cannot be empty");
        if (trimmed.length() > MAX_PAYLOAD) throw new IllegalArgumentException("Payload exceeds maximum size");
        this.payload = trimmed;
    }

    public static DecisionContext of(String payload) { return new DecisionContext(payload); }

    public String payload() { return payload; }
}
