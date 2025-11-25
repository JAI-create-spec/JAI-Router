package com.jai.router.core;

/**
 * Immutable routing decision produced by an LLM client.
 * 
 * <p>Represents the result of a routing decision with the selected service,
 * confidence score, and explanation.
 * 
 * @param service the selected service name (non-null, non-blank)
 * @param confidence confidence score in range [0.0, 1.0]
 * @param explanation explanation of the routing decision (may be empty)
 */
public record RoutingDecision(String service, double confidence, String explanation) {
    
    /**
     * Compact constructor for validation.
     * @throws IllegalArgumentException if service is null/blank or confidence is out of range
     */
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

    /**
     * Factory method for creating a RoutingDecision.
     * @param service the selected service name
     * @param confidence the confidence score
     * @param explanation the explanation
     * @return a new RoutingDecision
     * @throws IllegalArgumentException if parameters are invalid
     */
    public static RoutingDecision of(String service, double confidence, String explanation) {
        return new RoutingDecision(service, confidence, explanation);
    }
}
