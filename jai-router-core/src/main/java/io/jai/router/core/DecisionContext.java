package io.jai.router.core;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Immutable context object containing input data for routing decisions.
 * <p>
 * This class validates and sanitizes input to ensure it meets requirements
 * before being processed by LLM clients.
 * </p>
 *
 * @author JAI Router Team
 * @since 1.0.0
 */
public final class DecisionContext {

    private static final int MAX_PAYLOAD_LENGTH = 10_000;
    private static final int MIN_PAYLOAD_LENGTH = 1;

    private final String payload;

    /**
     * Creates a new decision context with validation.
     *
     * @param payload the input text to route, must not be null or empty
     * @throws NullPointerException     if payload is null
     * @throws IllegalArgumentException if payload is empty or exceeds max size
     */
    public DecisionContext(@NotNull String payload) {
        Objects.requireNonNull(payload, "Payload cannot be null");

        String trimmed = payload.trim();

        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Payload cannot be empty");
        }

        if (trimmed.length() > MAX_PAYLOAD_LENGTH) {
            throw new IllegalArgumentException(
                    String.format("Payload exceeds maximum size of %d characters", MAX_PAYLOAD_LENGTH)
            );
        }

        this.payload = trimmed;
    }

    /**
     * Factory method to create a DecisionContext.
     *
     * @param payload the input text
     * @return new DecisionContext instance
     */
    @NotNull
    public static DecisionContext of(@NotNull String payload) {
        return new DecisionContext(payload);
    }

    /**
     * Returns the sanitized payload.
     *
     * @return the payload string, never null
     */
    @NotNull
    public String payload() {
        return payload;
    }

    /**
     * Returns the payload length.
     *
     * @return payload character count
     */
    public int length() {
        return payload.length();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof DecisionContext other)) return false;
        return payload.equals(other.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(payload);
    }

    @Override
    public String toString() {
        int previewLength = Math.min(50, payload.length());
        String preview = payload.substring(0, previewLength);
        return String.format("DecisionContext[payload='%s%s', length=%d]",
                preview,
                payload.length() > previewLength ? "..." : "",
                payload.length()
        );
    }
}
