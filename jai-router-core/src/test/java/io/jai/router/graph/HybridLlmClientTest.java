package io.jai.router.graph;

import io.jai.router.core.DecisionContext;
import io.jai.router.core.LlmClient;
import io.jai.router.core.RoutingDecision;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for HybridLlmClient.
 */
class HybridLlmClientTest {

    private LlmClient hybridClient;
    private MockLlmClient mockAiClient;
    private MockLlmClient mockDijkstraClient;

    @BeforeEach
    void setUp() {
        mockAiClient = new MockLlmClient("AI-Client");
        mockDijkstraClient = new MockLlmClient("Dijkstra-Client");
        hybridClient = new HybridLlmClient(mockAiClient, mockDijkstraClient);
    }

    @Test
    void testSimpleRequestUsesAiClient() {
        DecisionContext ctx = DecisionContext.of("Show me the dashboard");
        RoutingDecision decision = hybridClient.decide(ctx);

        assertEquals("AI-Client", decision.service());
        assertTrue(mockAiClient.wasCalled());
        assertFalse(mockDijkstraClient.wasCalled());
    }

    @Test
    void testMultiHopRequestUsesDijkstra() {
        DecisionContext ctx = DecisionContext.of("Auth user and then fetch profile");
        RoutingDecision decision = hybridClient.decide(ctx);

        assertEquals("Dijkstra-Client", decision.service());
        assertFalse(mockAiClient.wasCalled());
        assertTrue(mockDijkstraClient.wasCalled());
    }

    @Test
    void testCostSensitiveRequestUsesDijkstra() {
        DecisionContext ctx = DecisionContext.of("Find cheapest route");
        RoutingDecision decision = hybridClient.decide(ctx);

        assertEquals("Dijkstra-Client", decision.service());
        assertTrue(mockDijkstraClient.wasCalled());
    }

    @Test
    void testFailoverRequestUsesDijkstra() {
        DecisionContext ctx = DecisionContext.of("Use backup service");
        RoutingDecision decision = hybridClient.decide(ctx);

        assertEquals("Dijkstra-Client", decision.service());
        assertTrue(mockDijkstraClient.wasCalled());
    }

    @Test
    void testGetName() {
        assertEquals("HybridRouter(AI+Dijkstra)", hybridClient.getName());
    }

    @Test
    void testGetUnderlyingClients() {
        assertTrue(hybridClient instanceof HybridLlmClient);
        HybridLlmClient hybrid = (HybridLlmClient) hybridClient;

        assertEquals(mockAiClient, hybrid.getFastAiClient());
        assertEquals(mockDijkstraClient, hybrid.getDijkstraClient());
    }

    /**
     * Mock LLM client for testing.
     */
    private static class MockLlmClient implements LlmClient {
        private final String name;
        private boolean called = false;

        MockLlmClient(String name) {
            this.name = name;
        }

        @Override
        public RoutingDecision decide(DecisionContext ctx) {
            called = true;
            return RoutingDecision.of(name, 0.95, "Mock decision from " + name);
        }

        @Override
        public String getName() {
            return name;
        }

        boolean wasCalled() {
            return called;
        }
    }
}

