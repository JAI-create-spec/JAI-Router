
package com.jai.router.core;

public class RoutingDecision {
    private final String service;
    private final double confidence;
    private final String explanation;

    public RoutingDecision(String service, double confidence, String explanation) {
        this.service = service;
        this.confidence = confidence;
        this.explanation = explanation;
    }

    public String getService() { return service; }
    public double getConfidence() { return confidence; }
    public String getExplanation() { return explanation; }
}
