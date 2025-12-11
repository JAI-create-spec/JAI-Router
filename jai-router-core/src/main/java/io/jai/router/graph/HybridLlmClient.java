package io.jai.router.graph;

import io.jai.router.core.DecisionContext;
import io.jai.router.core.LlmClient;
import io.jai.router.core.RoutingDecision;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Hybrid router that intelligently chooses between fast AI classification
 * and slower Dijkstra pathfinding based on request complexity.
 * <p>
 * This is the recommended routing strategy for production systems with
 * microservices architecture.
 * </p>
 *
 * <p><strong>Strategy Selection:</strong></p>
 * <ul>
 *   <li><strong>90% of requests:</strong> Fast AI classifier (~50-200ms)</li>
 *   <li><strong>10% complex:</strong> Dijkstra with caching (~3-16ms + cache)</li>
 * </ul>
 *
 * <p><strong>Performance Profile:</strong></p>
 * <pre>
 * Simple Request:
 *   ├─ Complexity Analysis: ~0.1ms
 *   ├─ AI Classification: 50-200ms
 *   └─ Total: ~50-200ms
 *
 * Complex Request (cache hit):
 *   ├─ Complexity Analysis: ~0.1ms
 *   ├─ Cache Lookup: ~0.1ms
 *   └─ Total: ~0.2ms
 *
 * Complex Request (cache miss):
 *   ├─ Complexity Analysis: ~0.1ms
 *   ├─ Dijkstra Pathfinding: 3-16ms
 *   └─ Total: ~3-16ms
 * </pre>
 *
 * <p><strong>Usage Example:</strong></p>
 * <pre>
 * // Simple request → AI classifier
 * decision = hybrid.decide(DecisionContext.of("Show me analytics dashboard"));
 * // → Uses AI classifier
 *
 * // Complex request → Dijkstra
 * decision = hybrid.decide(DecisionContext.of("Auth user and then fetch profile"));
 * // → Uses Dijkstra for optimal path
 * </pre>
 *
 * @author JAI Router Team
 * @since 0.6.0
 * @see ComplexityAnalyzer
 * @see DijkstraLlmClient
 * @see CachedDijkstraClient
 */
public class HybridLlmClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(HybridLlmClient.class);

    private final LlmClient fastAiClient;
    private final LlmClient dijkstraClient;
    private final ComplexityAnalyzer analyzer;

    /**
     * Creates a hybrid router with default complexity analyzer.
     *
     * @param fastAiClient   AI classifier for simple requests
     * @param dijkstraClient Dijkstra router for complex requests
     */
    public HybridLlmClient(
            @NotNull LlmClient fastAiClient,
            @NotNull LlmClient dijkstraClient) {
        this(fastAiClient, dijkstraClient, new ComplexityAnalyzer());
    }

    /**
     * Creates a hybrid router with custom complexity analyzer.
     *
     * @param fastAiClient   AI classifier for simple requests
     * @param dijkstraClient Dijkstra router for complex requests
     * @param analyzer       complexity analyzer
     */
    public HybridLlmClient(
            @NotNull LlmClient fastAiClient,
            @NotNull LlmClient dijkstraClient,
            @NotNull ComplexityAnalyzer analyzer) {
        this.fastAiClient = Objects.requireNonNull(fastAiClient, "fastAiClient cannot be null");
        this.dijkstraClient = Objects.requireNonNull(dijkstraClient, "dijkstraClient cannot be null");
        this.analyzer = Objects.requireNonNull(analyzer, "analyzer cannot be null");

        log.info("Initialized HybridLlmClient (fastClient: {}, dijkstraClient: {})",
                fastAiClient.getName(), dijkstraClient.getName());
    }

    @Override
    public @NotNull RoutingDecision decide(@NotNull DecisionContext ctx) {
        Objects.requireNonNull(ctx, "ctx cannot be null");

        // Analyze request complexity
        long startTime = System.currentTimeMillis();
        ComplexityAnalyzer.RequestComplexity complexity = analyzer.analyze(ctx);
        long analysisTime = System.currentTimeMillis() - startTime;

        log.debug("Complexity analysis took {}ms: {}", analysisTime, complexity);

        // Route based on complexity
        return switch (complexity) {
            case SIMPLE -> {
                log.debug("Using fast AI classifier for simple request");
                yield fastAiClient.decide(ctx);
            }
            case MULTI_HOP, COST_SENSITIVE, FAILOVER -> {
                log.debug("Using Dijkstra for complex request: {}", complexity);
                yield dijkstraClient.decide(ctx);
            }
        };
    }

    @Override
    public @NotNull String getName() {
        return "HybridRouter(AI+Dijkstra)";
    }

    /**
     * Returns the underlying AI client.
     *
     * @return AI client
     */
    @NotNull
    public LlmClient getFastAiClient() {
        return fastAiClient;
    }

    /**
     * Returns the underlying Dijkstra client.
     *
     * @return Dijkstra client
     */
    @NotNull
    public LlmClient getDijkstraClient() {
        return dijkstraClient;
    }
}

