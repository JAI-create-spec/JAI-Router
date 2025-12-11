package io.jai.router.graph;

import io.jai.router.core.DecisionContext;
import io.jai.router.core.LlmClient;
import io.jai.router.core.LlmClientException;
import io.jai.router.core.RoutingDecision;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Dijkstra-based LLM client for multi-hop service orchestration.
 * <p>
 * Uses Dijkstra's shortest path algorithm to find the optimal route through
 * a service graph based on weighted metrics (latency, cost, reliability).
 * </p>
 *
 * <p><strong>When to use:</strong></p>
 * <ul>
 *   <li>Multi-service workflows (e.g., auth → user → billing)</li>
 *   <li>Cost optimization across service chains</li>
 *   <li>Dynamic failover scenarios</li>
 *   <li>Latency-sensitive multi-hop requests</li>
 * </ul>
 *
 * <p><strong>Performance:</strong></p>
 * <ul>
 *   <li>Time Complexity: O((V + E) log V)</li>
 *   <li>Typical overhead: 3-16ms for medium graphs (20-50 services)</li>
 *   <li>Recommended: Use with path caching for repeated workflows</li>
 * </ul>
 *
 * @author JAI Router Team
 * @since 0.6.0
 * @see ServiceGraph
 */
public class DijkstraLlmClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(DijkstraLlmClient.class);

    private final ServiceGraph graph;
    private final String sourceService;

    /**
     * Creates a new Dijkstra router.
     *
     * @param graph         service graph
     * @param sourceService starting service (typically "gateway" or "api-gateway")
     * @throws NullPointerException if any parameter is null
     */
    public DijkstraLlmClient(@NotNull ServiceGraph graph, @NotNull String sourceService) {
        this.graph = Objects.requireNonNull(graph, "graph cannot be null");
        this.sourceService = Objects.requireNonNull(sourceService, "sourceService cannot be null");

        if (!graph.hasService(sourceService)) {
            throw new IllegalArgumentException("Source service not in graph: " + sourceService);
        }
    }

    @Override
    public @NotNull RoutingDecision decide(@NotNull DecisionContext ctx) {
        Objects.requireNonNull(ctx, "ctx cannot be null");

        // Extract target service from payload or throw
        String target = extractTargetService(ctx);

        if (!graph.hasService(target)) {
            throw new LlmClientException("Target service not in graph: " + target);
        }

        // Find optimal path using Dijkstra
        long startTime = System.currentTimeMillis();
        RoutingPath path = findShortestPath(sourceService, target);
        long duration = System.currentTimeMillis() - startTime;

        log.debug("Dijkstra found path in {}ms: {} -> {} (hops: {}, latency: {}ms, cost: {})",
                duration, sourceService, target, path.hopCount(),
                path.estimatedLatency(), path.totalCost());

        // Build routing decision
        String explanation = String.format(
                "Optimal path: %s (hops: %d, latency: %.1fms, cost: %.4f)",
                String.join(" → ", path.services()),
                path.hopCount(),
                path.estimatedLatency(),
                path.totalCost()
        );

        return RoutingDecision.of(
                path.targetService(),
                calculateConfidence(path),
                explanation
        );
    }

    /**
     * Extracts target service from context payload.
     * Expected format: "TARGET:service-name" or falls back to keyword analysis.
     */
    private String extractTargetService(DecisionContext ctx) {
        String payload = ctx.payload();

        // Check for explicit target marker
        if (payload.startsWith("TARGET:")) {
            String target = payload.substring("TARGET:".length()).trim();
            if (!target.isEmpty()) {
                return target;
            }
        }

        // Try to infer from keywords (simplified)
        String lower = payload.toLowerCase();
        if (lower.contains("auth") || lower.contains("login")) return "auth-service";
        if (lower.contains("user") || lower.contains("profile")) return "user-service";
        if (lower.contains("billing") || lower.contains("payment")) return "billing-service";
        if (lower.contains("notif") || lower.contains("email")) return "notification-service";

        throw new LlmClientException(
                "Cannot determine target service from payload. " +
                "Use 'TARGET:service-name' prefix or provide explicit keywords."
        );
    }

    /**
     * Implements Dijkstra's shortest path algorithm.
     *
     * @param source source service ID
     * @param target target service ID
     * @return routing path with ordered services and metrics
     * @throws LlmClientException if no path exists
     */
    @NotNull
    private RoutingPath findShortestPath(@NotNull String source, @NotNull String target) {
        Map<String, Double> distances = new HashMap<>();
        Map<String, String> predecessors = new HashMap<>();
        PriorityQueue<NodeDistance> pq = new PriorityQueue<>(
                Comparator.comparingDouble(NodeDistance::distance)
        );

        // Initialize
        distances.put(source, 0.0);
        pq.offer(new NodeDistance(source, 0.0));

        // Dijkstra's algorithm
        while (!pq.isEmpty()) {
            NodeDistance current = pq.poll();

            // Reached target
            if (current.serviceId().equals(target)) {
                return reconstructPath(source, target, predecessors, distances);
            }

            // Skip if we've found a better path already
            if (current.distance() > distances.getOrDefault(current.serviceId(), Double.MAX_VALUE)) {
                continue;
            }

            // Explore neighbors
            for (ServiceGraph.ServiceEdge edge : graph.getEdges(current.serviceId())) {
                String neighbor = edge.to();
                double newDistance = distances.get(current.serviceId()) + edge.weight();

                // Found better path
                if (newDistance < distances.getOrDefault(neighbor, Double.MAX_VALUE)) {
                    distances.put(neighbor, newDistance);
                    predecessors.put(neighbor, current.serviceId());
                    pq.offer(new NodeDistance(neighbor, newDistance));
                }
            }
        }

        // No path found
        throw new LlmClientException("No path found from " + source + " to " + target);
    }

    /**
     * Reconstructs the path from predecessors map.
     */
    private RoutingPath reconstructPath(
            String source,
            String target,
            Map<String, String> predecessors,
            Map<String, Double> distances) {

        List<String> path = new ArrayList<>();
        String current = target;

        // Build path backwards
        while (current != null) {
            path.add(0, current);
            current = predecessors.get(current);
        }

        double totalWeight = distances.get(target);

        // Estimate latency and cost from weight
        // This is an approximation; for exact values, you'd sum edge metrics
        return new RoutingPath(path, totalWeight * 0.3, totalWeight * 0.5);
    }

    /**
     * Calculates confidence based on path quality.
     * Shorter paths with lower cost/latency get higher confidence.
     */
    private double calculateConfidence(RoutingPath path) {
        // Simple heuristic: confidence decreases with hop count
        int hops = path.hopCount();
        if (hops == 0) return 1.0; // Direct (same service)
        if (hops == 1) return 0.95; // One hop
        if (hops == 2) return 0.90; // Two hops
        if (hops == 3) return 0.85; // Three hops
        return Math.max(0.7, 0.95 - (hops * 0.05)); // Decrease by 5% per hop
    }

    @Override
    public @NotNull String getName() {
        return "DijkstraRouter";
    }

    /**
     * Helper record for priority queue in Dijkstra's algorithm.
     */
    private record NodeDistance(String serviceId, double distance) {}
}

