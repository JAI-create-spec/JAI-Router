package io.jai.router.llm;

public class VectorSearchResult {
    private final String serviceId;
    private final double similarityScore;

    public VectorSearchResult(String serviceId, double similarityScore) {
        this.serviceId = serviceId;
        this.similarityScore = similarityScore;
    }

    public String getServiceId() { return serviceId; }
    public double getSimilarityScore() { return similarityScore; }
}

