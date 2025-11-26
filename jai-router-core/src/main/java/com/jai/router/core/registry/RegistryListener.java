package com.jai.router.core.registry;

public interface RegistryListener {
    void onRegister(ServiceDescriptor descriptor);
    void onDeregister(String id);
}

