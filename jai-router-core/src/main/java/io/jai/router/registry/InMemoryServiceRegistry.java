package io.jai.router.registry;

import java.util.List;
import java.util.Objects;

public class InMemoryServiceRegistry implements ServiceRegistry {
    private final List<ServiceDefinition> services;
    public InMemoryServiceRegistry(List<ServiceDefinition> services) { this.services = List.copyOf(Objects.requireNonNull(services)); }
    @Override public List<ServiceDefinition> listServices() { return services; }
}
