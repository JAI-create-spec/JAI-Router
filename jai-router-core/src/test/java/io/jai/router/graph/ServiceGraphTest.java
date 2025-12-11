package io.jai.router.graph;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ServiceGraph.
 */
class ServiceGraphTest {

    private ServiceGraph graph;

    @BeforeEach
    void setUp() {
        graph = new ServiceGraph();
    }

    @Test
    void testAddService() {
        Map<String, Object> metadata = Map.of("endpoint", "http://service:8080");
        graph.addService("test-service", metadata);

        assertTrue(graph.hasService("test-service"));
        assertEquals(1, graph.size());
    }

    @Test
    void testAddServiceThrowsOnNull() {
        assertThrows(NullPointerException.class, () ->
                graph.addService(null, Map.of()));
        assertThrows(NullPointerException.class, () ->
                graph.addService("test", null));
    }

    @Test
    void testAddEdge() {
        graph.addService("service-a", Map.of());
        graph.addService("service-b", Map.of());

        EdgeMetrics metrics = new EdgeMetrics(50.0, 0.001, 0.99);
        graph.addEdge("service-a", "service-b", metrics);

        List<ServiceGraph.ServiceEdge> edges = graph.getEdges("service-a");
        assertEquals(1, edges.size());
        assertEquals("service-b", edges.get(0).to());
    }

    @Test
    void testAddEdgeCreatesTargetNode() {
        graph.addService("service-a", Map.of());

        EdgeMetrics metrics = new EdgeMetrics(50.0, 0.001, 0.99);
        graph.addEdge("service-a", "service-b", metrics);

        // Target node should exist in adjacency list
        assertNotNull(graph.getEdges("service-b"));
        assertTrue(graph.getEdges("service-b").isEmpty());
    }

    @Test
    void testGetEdgesReturnsEmptyForUnknownService() {
        List<ServiceGraph.ServiceEdge> edges = graph.getEdges("unknown");
        assertNotNull(edges);
        assertTrue(edges.isEmpty());
    }

    @Test
    void testUpdateServiceReliability() {
        graph.addService("service-a", Map.of());
        graph.addService("service-b", Map.of());

        EdgeMetrics metrics = new EdgeMetrics(50.0, 0.001, 0.99);
        graph.addEdge("service-a", "service-b", metrics);

        // Update reliability
        graph.updateServiceReliability("service-a", 0.5);

        List<ServiceGraph.ServiceEdge> edges = graph.getEdges("service-a");
        assertEquals(0.5, edges.get(0).metrics().reliability());
    }

    @Test
    void testGetAllServices() {
        graph.addService("service-a", Map.of());
        graph.addService("service-b", Map.of());
        graph.addService("service-c", Map.of());

        assertEquals(3, graph.getAllServices().size());
        assertTrue(graph.getAllServices().contains("service-a"));
        assertTrue(graph.getAllServices().contains("service-b"));
        assertTrue(graph.getAllServices().contains("service-c"));
    }

    @Test
    void testMultipleEdgesFromSameSource() {
        graph.addService("gateway", Map.of());
        graph.addService("auth", Map.of());
        graph.addService("user", Map.of());

        graph.addEdge("gateway", "auth", EdgeMetrics.defaults());
        graph.addEdge("gateway", "user", EdgeMetrics.defaults());

        List<ServiceGraph.ServiceEdge> edges = graph.getEdges("gateway");
        assertEquals(2, edges.size());
    }
}

