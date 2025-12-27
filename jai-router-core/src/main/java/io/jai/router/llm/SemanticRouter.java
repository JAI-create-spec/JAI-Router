package io.jai.router.llm;

import io.jai.router.registry.ServiceDefinition;
import io.jai.router.core.RoutingResult;
import io.jai.router.registry.ServiceRegistry;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class SemanticRouter {

    private final EmbeddingProvider embeddingProvider;
    private final ServiceRegistry serviceRegistry;
    private final VectorDatabase vectorDb;

    public SemanticRouter(EmbeddingProvider embeddingProvider, ServiceRegistry serviceRegistry, VectorDatabase vectorDb) {
        this.embeddingProvider = embeddingProvider;
        this.serviceRegistry = serviceRegistry;
        this.vectorDb = vectorDb;
    }

    public RoutingResult routeBySemanticSimilarity(String request) {
        Embedding requestEmbedding = embeddingProvider.embed(request);
        List<VectorSearchResult> results = vectorDb.searchSimilar(requestEmbedding, 5);

        Optional<VectorSearchResult> best = results.stream()
                .max(Comparator.comparingDouble(VectorSearchResult::getSimilarityScore));

        if (best.isEmpty()) {
            return RoutingResult.of("", 0.0, "no-match", 0);
        }

        VectorSearchResult r = best.get();
        return RoutingResult.of(r.getServiceId(), r.getSimilarityScore(), "semantic", 0);
    }
}

