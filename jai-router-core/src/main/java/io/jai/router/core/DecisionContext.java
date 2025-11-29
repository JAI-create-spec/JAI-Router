package io.jai.router.core;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class DecisionContext {
    private final String payload;

    public DecisionContext(@NotNull String payload) {
        this.payload = Objects.requireNonNull(payload, "payload cannot be null");
        if (this.payload.isEmpty()) throw new IllegalArgumentException("Payload cannot be empty");
    }

    public static DecisionContext of(@NotNull String payload) { return new DecisionContext(payload); }

    public String payload() { return payload; }
}
