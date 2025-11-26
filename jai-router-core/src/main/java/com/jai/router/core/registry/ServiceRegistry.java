package com.jai.router.core.registry;

import java.util.List;
import java.util.Optional;

public interface ServiceRegistry {
    ServiceDescriptor register(ServiceDescriptor descriptor);
    Optional<ServiceDescriptor> get(String id);
    List<ServiceDescriptor> list();
    boolean deregister(String id);
    void addListener(RegistryListener listener);
    void removeListener(RegistryListener listener);
}

