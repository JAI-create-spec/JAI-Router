package io.jai.router.core.exception;

public class RoutingException extends RuntimeException {
    private final String serviceId;
    private final double confidence;

    public RoutingException(String message) {
        super(message);
        this.serviceId = null;
        this.confidence = 0.0;
    }

    public RoutingException(String message, Throwable cause) {
        super(message, cause);
        this.serviceId = null;
        this.confidence = 0.0;
    }

    public RoutingException(String message, String serviceId, double confidence) {
        super(message);
        this.serviceId = serviceId;
        this.confidence = confidence;
    }

    public String getServiceId() { return serviceId; }
    public double getConfidence() { return confidence; }
}

