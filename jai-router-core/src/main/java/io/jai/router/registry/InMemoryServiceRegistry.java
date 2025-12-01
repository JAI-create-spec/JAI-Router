package io.jai.router.registry;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Thread-safe, in-memory implementation of {@link ServiceRegistry}.
 * <p>
 * Uses a copy-on-write strategy to ensure thread-safety for concurrent reads
 * and writes. This implementation is suitable for dynamic service registration
 * scenarios where services may be added or removed at runtime.
 * </p>
 *
 * @author JAI Router Team
 * @since 1.0.0
 */
public class InMemoryServiceRegistry implements ServiceRegistry {

    private final CopyOnWriteArrayList<ServiceDefinition> services;

    /**
     * Creates a new in-memory service registry with the provided services.
     *
     * @param services initial list of service definitions, must not be null
     * @throws NullPointerException if services is null
     */
    public InMemoryServiceRegistry(@NotNull List<ServiceDefinition> services) {
        Objects.requireNonNull(services, "Services list cannot be null");
        this.services = new CopyOnWriteArrayList<>(services);
    }

    /**
     * Returns an immutable copy of all registered services.
     *
     * @return unmodifiable list of all service definitions
     */
    @Override
    @NotNull
    public List<ServiceDefinition> listServices() {
        return List.copyOf(services);
    }

    /**
     * Finds a service by its unique identifier.
     *
     * @param serviceId the service ID to search for
     * @return Optional containing the service if found, empty otherwise
     */
    @NotNull
    public Optional<ServiceDefinition> findServiceById(@NotNull String serviceId) {
        Objects.requireNonNull(serviceId, "Service ID cannot be null");
        return services.stream()
                .filter(s -> s.id().equals(serviceId))
                .findFirst();
    }

    /**
     * Registers a new service in the registry.
     * <p>
     * If a service with the same ID already exists, it will not be added again.
     * </p>
     *
     * @param service the service definition to register
     * @throws NullPointerException if service is null
     * @return true if the service was added, false if it already exists
     */
    public boolean registerService(@NotNull ServiceDefinition service) {
        Objects.requireNonNull(service, "Service cannot be null");

        // Check if service already exists
        if (findServiceById(service.id()).isPresent()) {
            return false;
        }

        return services.add(service);
    }

    /**
     * Removes a service from the registry by its ID.
     *
     * @param serviceId the ID of the service to remove
     * @return true if the service was removed, false if not found
     */
    public boolean deregisterService(@NotNull String serviceId) {
        Objects.requireNonNull(serviceId, "Service ID cannot be null");
        return services.removeIf(s -> s.id().equals(serviceId));
    }

    /**
     * Returns the number of registered services.
     *
     * @return the service count
     */
    public int size() {
        return services.size();
    }

    /**
     * Checks if the registry is empty.
     *
     * @return true if no services are registered
     */
    public boolean isEmpty() {
        return services.isEmpty();
    }

    /**
     * Removes all services from the registry.
     */
    public void clear() {
        services.clear();
    }
}
