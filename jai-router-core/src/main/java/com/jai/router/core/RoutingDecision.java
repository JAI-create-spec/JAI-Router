package com.jai.router.core;

/**
 * Immutable routing decision produced by an LLM client.
 * service: non-null, non-blank
 * confidence: range [0.0, 1.0]
 */
public record RoutingDecision(String service, double confidence, String explanation) {
    public RoutingDecision {
        if (service == null || service.isBlank()) {
            throw new IllegalArgumentException("service must not be null or blank");
        }
        if (confidence < 0.0 || confidence > 1.0) {
            throw new IllegalArgumentException("confidence must be between 0.0 and 1.0");
        }
        if (explanation == null) {
            explanation = "";
        }
    }

    public static RoutingDecision of(String service, double confidence, String explanation) {
        return new RoutingDecision(service, confidence, explanation);
    }
}
