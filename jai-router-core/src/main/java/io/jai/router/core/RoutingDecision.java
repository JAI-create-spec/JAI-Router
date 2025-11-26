package io.jai.router.core;

import java.util.Objects;

/**
 * Small value type representing a routing decision returned by an LLM/provider.
 */
public record RoutingDecision(String service, double confidence, String explanation) {

    public static RoutingDecision of(String service, double confidence, String explanation) {
        Objects.requireNonNull(service, "service must not be null");
        if (service.isBlank()) throw new IllegalArgumentException("service must not be blank");
        double c = Double.isFinite(confidence) ? confidence : 0.0;
        if (c < 0.0) c = 0.0;
        if (c > 1.0) c = 1.0;
        String e = explanation == null ? "" : explanation;
        return new RoutingDecision(service, c, e);
    }

}
