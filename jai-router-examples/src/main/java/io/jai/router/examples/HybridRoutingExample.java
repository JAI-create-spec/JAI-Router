package io.jai.router.examples;

import io.jai.router.core.DecisionContext;
import io.jai.router.core.LlmClient;
import io.jai.router.core.RoutingDecision;
import io.jai.router.graph.*;
import io.jai.router.llm.BuiltinAiLlmClient;

import java.util.Map;

/**
 * Demonstrates hybrid routing with AI classifier and Dijkstra pathfinding.
 * <p>
 * This example shows:
 * <ul>
 *   <li>Simple single-service routing (AI classifier)</li>
 *   <li>Complex multi-hop routing (Dijkstra)</li>
 *   <li>Path caching for performance</li>
 * </ul>
 * </p>
 *
 * @author JAI Router Team
 * @since 0.6.0
 */
public class HybridRoutingExample {

    public static void main(String[] args) {
        System.out.println("=== JAI Router - Hybrid Routing Demo ===\n");

        // 1. Create service graph for microservices
        ServiceGraph graph = createMicroservicesGraph();

        // 2. Create AI classifier for simple requests
        LlmClient aiClient = createAiClassifier();

        // 3. Create Dijkstra client for complex requests
        LlmClient dijkstraClient = new DijkstraLlmClient(graph, "gateway");

        // 4. Wrap Dijkstra with caching
        LlmClient cachedDijkstra = new CachedDijkstraClient(dijkstraClient);

        // 5. Create hybrid client
        LlmClient hybridClient = new HybridLlmClient(aiClient, cachedDijkstra);

        // Test cases
        testSimpleRouting(hybridClient);
        testComplexRouting(hybridClient);
        testCostOptimization(hybridClient);
        testCaching(cachedDijkstra);

        System.out.println("\n=== Demo Complete ===");
    }

    /**
     * Creates a microservices graph with typical service dependencies.
     */
    private static ServiceGraph createMicroservicesGraph() {
        System.out.println("📊 Creating microservices graph...");

        ServiceGraph graph = new ServiceGraph();

        // Add services
        graph.addService("gateway", Map.of("type", "entry-point"));
        graph.addService("auth-service", Map.of("endpoint", "http://auth:8080"));
        graph.addService("user-service", Map.of("endpoint", "http://user:8081"));
        graph.addService("billing-service", Map.of("endpoint", "http://billing:8082"));
        graph.addService("notification-service", Map.of("endpoint", "http://notify:8083"));

        // Add edges with metrics
        // Gateway → Auth (fast, reliable)
        graph.addEdge("gateway", "auth-service", new EdgeMetrics(10.0, 0.0, 0.999));

        // Auth → User (medium speed)
        graph.addEdge("auth-service", "user-service", new EdgeMetrics(20.0, 0.001, 0.99));

        // User → Billing (slower, more expensive)
        graph.addEdge("user-service", "billing-service", new EdgeMetrics(30.0, 0.002, 0.98));

        // Billing → Notification (fast)
        graph.addEdge("billing-service", "notification-service", new EdgeMetrics(15.0, 0.001, 0.99));

        // Direct paths (more expensive alternatives)
        graph.addEdge("gateway", "user-service", new EdgeMetrics(100.0, 0.01, 0.95));
        graph.addEdge("gateway", "billing-service", new EdgeMetrics(200.0, 0.02, 0.90));

        System.out.println("✅ Graph created with " + graph.size() + " services\n");

        return graph;
    }

    /**
     * Creates an AI classifier for simple routing.
     */
    private static LlmClient createAiClassifier() {
        Map<String, String> keywords = Map.of(
                "login", "auth-service",
                "auth", "auth-service",
                "profile", "user-service",
                "user", "user-service",
                "billing", "billing-service",
                "payment", "billing-service",
                "notification", "notification-service",
                "email", "notification-service"
        );

        return new BuiltinAiLlmClient(keywords, 0.7);
    }

    /**
     * Test 1: Simple single-service routing (uses AI classifier).
     */
    private static void testSimpleRouting(LlmClient client) {
        System.out.println("🧪 Test 1: Simple Single-Service Routing");
        System.out.println("----------------------------------------");

        String[] simpleRequests = {
                "Show me user profile",
                "I need to login",
                "Display billing information"
        };

        for (String request : simpleRequests) {
            long start = System.nanoTime();
            RoutingDecision decision = client.decide(DecisionContext.of(request));
            long duration = (System.nanoTime() - start) / 1_000_000;

            System.out.printf("Request: \"%s\"%n", request);
            System.out.printf("  → Service: %s%n", decision.service());
            System.out.printf("  → Confidence: %.2f%n", decision.confidence());
            System.out.printf("  → Time: %dms%n", duration);
            System.out.println();
        }
    }

    /**
     * Test 2: Complex multi-hop routing (uses Dijkstra).
     */
    private static void testComplexRouting(LlmClient client) {
        System.out.println("🧪 Test 2: Complex Multi-Hop Routing");
        System.out.println("-------------------------------------");

        String[] complexRequests = {
                "TARGET:billing-service",
                "Auth user and then fetch billing data",
                "Orchestrate workflow from auth to billing"
        };

        for (String request : complexRequests) {
            long start = System.nanoTime();
            RoutingDecision decision = client.decide(DecisionContext.of(request));
            long duration = (System.nanoTime() - start) / 1_000_000;

            System.out.printf("Request: \"%s\"%n", request);
            System.out.printf("  → Service: %s%n", decision.service());
            System.out.printf("  → Explanation: %s%n", decision.explanation());
            System.out.printf("  → Time: %dms%n", duration);
            System.out.println();
        }
    }

    /**
     * Test 3: Cost optimization routing.
     */
    private static void testCostOptimization(LlmClient client) {
        System.out.println("🧪 Test 3: Cost-Optimized Routing");
        System.out.println("----------------------------------");

        String request = "Find cheapest route to billing service";
        long start = System.nanoTime();
        RoutingDecision decision = client.decide(DecisionContext.of(request));
        long duration = (System.nanoTime() - start) / 1_000_000;

        System.out.printf("Request: \"%s\"%n", request);
        System.out.printf("  → Service: %s%n", decision.service());
        System.out.printf("  → Explanation: %s%n", decision.explanation());
        System.out.printf("  → Time: %dms%n", duration);
        System.out.println();
    }

    /**
     * Test 4: Caching performance.
     */
    private static void testCaching(LlmClient cachedClient) {
        System.out.println("🧪 Test 4: Path Caching Performance");
        System.out.println("------------------------------------");

        String request = "TARGET:billing-service";
        DecisionContext ctx = DecisionContext.of(request);

        // First call (cache miss)
        long start1 = System.nanoTime();
        RoutingDecision decision1 = cachedClient.decide(ctx);
        long duration1 = (System.nanoTime() - start1) / 1_000_000;

        // Second call (cache hit)
        long start2 = System.nanoTime();
        RoutingDecision decision2 = cachedClient.decide(ctx);
        long duration2 = (System.nanoTime() - start2) / 1_000_000;

        System.out.printf("Request: \"%s\"%n", request);
        System.out.printf("  → First call (cache miss): %dms%n", duration1);
        System.out.printf("  → Second call (cache hit): %dms%n", duration2);
        System.out.printf("  → Speedup: %.1fx%n", (double) duration1 / duration2);

        // Print cache stats
        if (cachedClient instanceof CachedDijkstraClient cached) {
            System.out.printf("  → Cache stats: %s%n", cached.getStats());
        }
        System.out.println();
    }
}

