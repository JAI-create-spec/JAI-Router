package io.jai.router.registry;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Central registry for managing service definitions used by the routing engine.
 * <p>
 * Implementations of this interface provide access to service metadata that
 * helps the router make intelligent routing decisions based on input context.
 * </p>
 *
 * @author JAI Router Team
 * @since 1.0.0
 */
public interface ServiceRegistry {

    /**
     * Returns all registered services.
     * <p>
     * Implementations should return an immutable or defensive copy to prevent
     * external modification of the internal registry state.
     * </p>
     *
     * @return list of all service definitions, never null
     */
    @NotNull
    List<ServiceDefinition> listServices();
}
