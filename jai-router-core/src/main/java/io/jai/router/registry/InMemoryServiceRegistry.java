package io.jai.router.registry;

import java.util.*;

public class InMemoryServiceRegistry implements ServiceRegistry {
    private final List<ServiceDefinition> services;
    public InMemoryServiceRegistry(List<ServiceDefinition> services) { this.services = List.copyOf(services); }
    @Override public List<ServiceDefinition> listServices() { return services; }
}
