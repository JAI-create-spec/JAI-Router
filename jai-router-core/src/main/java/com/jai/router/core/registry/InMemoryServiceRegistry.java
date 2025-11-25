package com.jai.router.core.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class InMemoryServiceRegistry implements ServiceRegistry {
    private final ConcurrentHashMap<String, ServiceDescriptor> map = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<RegistryListener> listeners = new CopyOnWriteArrayList<>();

    @Override
    public ServiceDescriptor register(ServiceDescriptor descriptor) {
        Objects.requireNonNull(descriptor, "descriptor must not be null");
        map.put(descriptor.id(), descriptor);
        // notify listeners
        for (var l : listeners) {
            try { l.onRegister(descriptor); } catch (Exception ignored) {}
        }
        return descriptor;
    }

    @Override
    public Optional<ServiceDescriptor> get(String id) {
        return Optional.ofNullable(map.get(id));
    }

    @Override
    public List<ServiceDescriptor> list() {
        return Collections.unmodifiableList(new ArrayList<>(map.values()));
    }

    @Override
    public boolean deregister(String id) {
        ServiceDescriptor removed = map.remove(id);
        if (removed != null) {
            for (var l : listeners) {
                try { l.onDeregister(id); } catch (Exception ignored) {}
            }
            return true;
        }
        return false;
    }

    @Override
    public void addListener(RegistryListener listener) {
        if (listener != null) listeners.add(listener);
    }

    @Override
    public void removeListener(RegistryListener listener) {
        listeners.remove(listener);
    }
}

