package io.jai.router.graph;

import io.jai.router.core.DecisionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ComplexityAnalyzer.
 */
class ComplexityAnalyzerTest {

    private ComplexityAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new ComplexityAnalyzer();
    }

    @Test
    void testDetectSimpleRequest() {
        DecisionContext ctx = DecisionContext.of("Show me the analytics dashboard");
        ComplexityAnalyzer.RequestComplexity complexity = analyzer.analyze(ctx);

        assertEquals(ComplexityAnalyzer.RequestComplexity.SIMPLE, complexity);
    }

    @Test
    void testDetectMultiHopRequest() {
        DecisionContext ctx = DecisionContext.of("Authenticate user and then fetch profile");
        ComplexityAnalyzer.RequestComplexity complexity = analyzer.analyze(ctx);

        assertEquals(ComplexityAnalyzer.RequestComplexity.MULTI_HOP, complexity);
    }

    @Test
    void testDetectMultiHopWithChainKeyword() {
        DecisionContext ctx = DecisionContext.of("Chain the auth service with billing");
        ComplexityAnalyzer.RequestComplexity complexity = analyzer.analyze(ctx);

        assertEquals(ComplexityAnalyzer.RequestComplexity.MULTI_HOP, complexity);
    }

    @Test
    void testDetectMultiHopWithOrchestrateKeyword() {
        DecisionContext ctx = DecisionContext.of("Orchestrate workflow between services");
        ComplexityAnalyzer.RequestComplexity complexity = analyzer.analyze(ctx);

        assertEquals(ComplexityAnalyzer.RequestComplexity.MULTI_HOP, complexity);
    }

    @Test
    void testDetectCostSensitiveRequest() {
        DecisionContext ctx = DecisionContext.of("Find the cheapest route for this request");
        ComplexityAnalyzer.RequestComplexity complexity = analyzer.analyze(ctx);

        assertEquals(ComplexityAnalyzer.RequestComplexity.COST_SENSITIVE, complexity);
    }

    @Test
    void testDetectCostOptimizationRequest() {
        DecisionContext ctx = DecisionContext.of("Optimize cost for this workflow");
        ComplexityAnalyzer.RequestComplexity complexity = analyzer.analyze(ctx);

        assertEquals(ComplexityAnalyzer.RequestComplexity.COST_SENSITIVE, complexity);
    }

    @Test
    void testDetectFailoverRequest() {
        DecisionContext ctx = DecisionContext.of("Use failover if primary is down");
        ComplexityAnalyzer.RequestComplexity complexity = analyzer.analyze(ctx);

        assertEquals(ComplexityAnalyzer.RequestComplexity.FAILOVER, complexity);
    }

    @Test
    void testDetectBackupRequest() {
        DecisionContext ctx = DecisionContext.of("Route to backup service");
        ComplexityAnalyzer.RequestComplexity complexity = analyzer.analyze(ctx);

        assertEquals(ComplexityAnalyzer.RequestComplexity.FAILOVER, complexity);
    }

    @Test
    void testExplicitTargetWithMultiHop() {
        DecisionContext ctx = DecisionContext.of("TARGET:billing-service and then notify user");
        ComplexityAnalyzer.RequestComplexity complexity = analyzer.analyze(ctx);

        assertEquals(ComplexityAnalyzer.RequestComplexity.MULTI_HOP, complexity);
    }

    @Test
    void testExplicitTargetSimple() {
        DecisionContext ctx = DecisionContext.of("TARGET:auth-service");
        ComplexityAnalyzer.RequestComplexity complexity = analyzer.analyze(ctx);

        assertEquals(ComplexityAnalyzer.RequestComplexity.SIMPLE, complexity);
    }

    @Test
    void testCaseInsensitive() {
        DecisionContext ctx = DecisionContext.of("AND THEN followed by AFTER orchestrate");
        ComplexityAnalyzer.RequestComplexity complexity = analyzer.analyze(ctx);

        assertEquals(ComplexityAnalyzer.RequestComplexity.MULTI_HOP, complexity);
    }
}

