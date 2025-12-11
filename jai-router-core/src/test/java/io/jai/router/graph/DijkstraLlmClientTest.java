package io.jai.router.graph;

import io.jai.router.core.DecisionContext;
import io.jai.router.core.LlmClient;
import io.jai.router.core.LlmClientException;
import io.jai.router.core.RoutingDecision;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DijkstraLlmClient.
 */
class DijkstraLlmClientTest {

    private LlmClient dijkstraClient;

    @BeforeEach
    void setUp() {
        ServiceGraph graph = createTestGraph();
        dijkstraClient = new DijkstraLlmClient(graph, "gateway");
    }

    /**
     * Creates a test service graph:
     * gateway -> auth -> user -> billing
     */
    private ServiceGraph createTestGraph() {
        ServiceGraph g = new ServiceGraph();

        g.addService("gateway", Map.of());
        g.addService("auth-service", Map.of());
        g.addService("user-service", Map.of());
        g.addService("billing-service", Map.of());

        // Gateway to auth
        g.addEdge("gateway", "auth-service", new EdgeMetrics(10.0, 0.0, 0.999));

        // Auth to user
        g.addEdge("auth-service", "user-service", new EdgeMetrics(20.0, 0.001, 0.99));

        // User to billing
        g.addEdge("user-service", "billing-service", new EdgeMetrics(30.0, 0.002, 0.98));

        // Direct gateway to user (more expensive)
        g.addEdge("gateway", "user-service", new EdgeMetrics(100.0, 0.01, 0.95));

        return g;
    }

    @Test
    void testFindShortestPathDirect() {
        DecisionContext ctx = DecisionContext.of("TARGET:auth-service");
        RoutingDecision decision = dijkstraClient.decide(ctx);

        assertNotNull(decision);
        assertEquals("auth-service", decision.service());
        assertTrue(decision.confidence() > 0.9);
    }

    @Test
    void testFindShortestPathMultiHop() {
        DecisionContext ctx = DecisionContext.of("TARGET:billing-service");
        RoutingDecision decision = dijkstraClient.decide(ctx);

        assertNotNull(decision);
        assertEquals("billing-service", decision.service());
        assertTrue(decision.explanation().contains("→"));
    }

    @Test
    void testOptimalPathSelection() {
        // Should prefer gateway -> auth -> user over gateway -> user (direct)
        DecisionContext ctx = DecisionContext.of("TARGET:user-service");
        RoutingDecision decision = dijkstraClient.decide(ctx);

        assertEquals("user-service", decision.service());
        // Path should be shorter/cheaper
        assertTrue(decision.explanation().contains("auth"));
    }

    @Test
    void testKeywordExtraction() {
        DecisionContext ctx = DecisionContext.of("I need to login to my account");
        RoutingDecision decision = dijkstraClient.decide(ctx);

        assertEquals("auth-service", decision.service());
    }

    @Test
    void testThrowsOnUnknownTarget() {
        DecisionContext ctx = DecisionContext.of("TARGET:unknown-service");

        assertThrows(LlmClientException.class, () ->
                dijkstraClient.decide(ctx));
    }

    @Test
    void testThrowsOnMissingTarget() {
        DecisionContext ctx = DecisionContext.of("Some random text");

        assertThrows(LlmClientException.class, () ->
                dijkstraClient.decide(ctx));
    }

    @Test
    void testGetName() {
        assertEquals("DijkstraRouter", dijkstraClient.getName());
    }

    @Test
    void testConfidenceDecreasesWithHops() {
        // One hop
        DecisionContext ctx1 = DecisionContext.of("TARGET:auth-service");
        RoutingDecision decision1 = dijkstraClient.decide(ctx1);

        // Three hops
        DecisionContext ctx2 = DecisionContext.of("TARGET:billing-service");
        RoutingDecision decision2 = dijkstraClient.decide(ctx2);

        // Confidence should decrease with more hops
        assertTrue(decision1.confidence() > decision2.confidence());
    }
}

