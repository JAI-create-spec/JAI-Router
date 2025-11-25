package com.jai.router.core.registry;

import java.util.List;
import java.util.Objects;

/**
 * Descriptor for a candidate service that can be registered with the router at runtime.
 */
public record ServiceDescriptor(
    String id,
    String name,
    String endpoint,
    List<String> keywords
) {
    public ServiceDescriptor {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(name, "name must not be null");
    }
}

