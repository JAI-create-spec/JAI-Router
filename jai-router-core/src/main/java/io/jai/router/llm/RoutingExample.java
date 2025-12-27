package io.jai.router.llm;

public class RoutingExample {
    private final String request;
    private final String service;
    private final double confidence;

    public RoutingExample(String request, String service, double confidence) {
        this.request = request;
        this.service = service;
        this.confidence = confidence;
    }

    public String getRequest() { return request; }
    public String getService() { return service; }
    public double getConfidence() { return confidence; }
}

