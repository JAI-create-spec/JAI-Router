package io.jai.router.graph;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Thread-safe service graph for Dijkstra-based routing.
 * <p>
 * Represents microservices as nodes and their connections as weighted edges.
 * Weights are calculated from latency, cost, and reliability metrics.
 * </p>
 *
 * <p><strong>Thread Safety:</strong> This class is thread-safe for concurrent reads
 * after initialization. Updates can be made safely during runtime.</p>
 *
 * @author JAI Router Team
 * @since 0.6.0
 */
public class ServiceGraph {

    private static final Logger log = LoggerFactory.getLogger(ServiceGraph.class);

    private final Map<String, ServiceNode> nodes = new ConcurrentHashMap<>();
    private final Map<String, List<ServiceEdge>> adjacencyList = new ConcurrentHashMap<>();

    /**
     * Adds a service node to the graph.
     *
     * @param serviceId unique service identifier
     * @param metadata  additional service metadata
     * @throws NullPointerException if serviceId or metadata is null
     */
    public void addService(@NotNull String serviceId, @NotNull Map<String, Object> metadata) {
        Objects.requireNonNull(serviceId, "serviceId cannot be null");
        Objects.requireNonNull(metadata, "metadata cannot be null");

        nodes.put(serviceId, new ServiceNode(serviceId, new HashMap<>(metadata)));
        adjacencyList.putIfAbsent(serviceId, new CopyOnWriteArrayList<>());

        log.debug("Added service node: {}", serviceId);
    }

    /**
     * Adds a directed edge between two services.
     *
     * @param from    source service ID
     * @param to      target service ID
     * @param metrics edge metrics (latency, cost, reliability)
     * @throws NullPointerException if any parameter is null
     */
    public void addEdge(@NotNull String from, @NotNull String to, @NotNull EdgeMetrics metrics) {
        Objects.requireNonNull(from, "from cannot be null");
        Objects.requireNonNull(to, "to cannot be null");
        Objects.requireNonNull(metrics, "metrics cannot be null");

        ServiceEdge edge = new ServiceEdge(from, to, metrics);
        adjacencyList.computeIfAbsent(from, k -> new CopyOnWriteArrayList<>()).add(edge);
        adjacencyList.putIfAbsent(to, new CopyOnWriteArrayList<>()); // Ensure target exists

        log.debug("Added edge: {} -> {} (weight: {})", from, to, edge.weight());
    }

    /**
     * Returns all outgoing edges from a service.
     *
     * @param serviceId service identifier
     * @return list of edges, empty if service has no outgoing edges
     */
    @NotNull
    public List<ServiceEdge> getEdges(@NotNull String serviceId) {
        return adjacencyList.getOrDefault(serviceId, List.of());
    }

    /**
     * Checks if a service exists in the graph.
     *
     * @param serviceId service identifier
     * @return true if service exists
     */
    public boolean hasService(@NotNull String serviceId) {
        return nodes.containsKey(serviceId);
    }

    /**
     * Returns all service IDs in the graph.
     *
     * @return set of service IDs
     */
    @NotNull
    public Set<String> getAllServices() {
        return new HashSet<>(nodes.keySet());
    }

    /**
     * Updates the reliability metric for all edges originating from a service.
     * <p>
     * Useful for dynamic health-based routing adjustments.
     * </p>
     *
     * @param serviceId   service identifier
     * @param reliability new reliability score (0.0 to 1.0)
     */
    public void updateServiceReliability(@NotNull String serviceId, double reliability) {
        List<ServiceEdge> edges = adjacencyList.get(serviceId);
        if (edges != null) {
            edges.replaceAll(edge ->
                    new ServiceEdge(
                            edge.from(),
                            edge.to(),
                            new EdgeMetrics(
                                    edge.metrics().latencyMs(),
                                    edge.metrics().cost(),
                                    reliability
                            )
                    )
            );
            log.debug("Updated reliability for service {} to {}", serviceId, reliability);
        }
    }

    /**
     * Returns the number of services in the graph.
     *
     * @return service count
     */
    public int size() {
        return nodes.size();
    }

    /**
     * Represents a service node in the graph.
     *
     * @param id       unique service identifier
     * @param metadata additional metadata about the service
     */
    public record ServiceNode(@NotNull String id, @NotNull Map<String, Object> metadata) {
        public ServiceNode {
            Objects.requireNonNull(id, "id cannot be null");
            Objects.requireNonNull(metadata, "metadata cannot be null");
        }
    }

    /**
     * Represents a directed edge between two services.
     *
     * @param from    source service ID
     * @param to      target service ID
     * @param metrics edge metrics
     */
    public record ServiceEdge(@NotNull String from, @NotNull String to, @NotNull EdgeMetrics metrics) {
        public ServiceEdge {
            Objects.requireNonNull(from, "from cannot be null");
            Objects.requireNonNull(to, "to cannot be null");
            Objects.requireNonNull(metrics, "metrics cannot be null");
        }

        /**
         * Calculates the edge weight based on metrics.
         *
         * @return computed weight
         */
        public double weight() {
            return metrics.calculateWeight();
        }
    }
}

