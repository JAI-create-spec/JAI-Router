package io.jai.router.examples;

/** Response DTO for routing decision. */
public record RouteResponse(
    String service,
    double confidence,
    String explanation,
    long processingTimeMs
) {}
