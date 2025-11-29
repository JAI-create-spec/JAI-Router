package io.jai.router.core.exception;

public class LowConfidenceException extends RoutingException {
    public LowConfidenceException(String serviceId, double confidence) {
        super(String.format("Low confidence: %.2f for service: %s", confidence, serviceId), serviceId, confidence);
    }
}

