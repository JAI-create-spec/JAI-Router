package io.jai.router.graph;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * Represents an optimal routing path through the service graph.
 * <p>
 * Contains the ordered list of services to call, along with
 * total cost and estimated latency metrics.
 * </p>
 *
 * @param services          ordered list of service IDs to traverse
 * @param totalCost         total financial cost of the path
 * @param estimatedLatency  estimated end-to-end latency in milliseconds
 * @author JAI Router Team
 * @since 0.6.0
 */
public record RoutingPath(
        @NotNull List<String> services,
        double totalCost,
        double estimatedLatency
) {
    public RoutingPath {
        Objects.requireNonNull(services, "services cannot be null");
        if (services.isEmpty()) {
            throw new IllegalArgumentException("services cannot be empty");
        }
        if (totalCost < 0) {
            throw new IllegalArgumentException("totalCost must be >= 0");
        }
        if (estimatedLatency < 0) {
            throw new IllegalArgumentException("estimatedLatency must be >= 0");
        }
        // Make defensive copy
        services = List.copyOf(services);
    }

    /**
     * Returns the target service (last in the path).
     *
     * @return target service ID
     */
    @NotNull
    public String targetService() {
        return services.get(services.size() - 1);
    }

    /**
     * Returns the number of hops in the path.
     *
     * @return hop count
     */
    public int hopCount() {
        return services.size() - 1;
    }

    /**
     * Checks if this is a direct path (no intermediate services).
     *
     * @return true if path has only source and target
     */
    public boolean isDirect() {
        return services.size() == 2;
    }
}

