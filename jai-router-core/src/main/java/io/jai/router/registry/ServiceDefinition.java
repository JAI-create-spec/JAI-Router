package io.jai.router.registry;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * Immutable service definition containing metadata for routing decisions.
 * <p>
 * Each service is uniquely identified by its ID and contains keywords
 * that help the routing engine match incoming requests.
 * </p>
 *
 * @param id          unique service identifier, must not be null or blank
 * @param displayName human-readable service name, must not be null
 * @param keywords    list of keywords associated with this service, must not be null
 * @author JAI Router Team
 * @since 1.0.0
 */
public record ServiceDefinition(
        @NotNull String id,
        @NotNull String displayName,
        @NotNull List<String> keywords
) {
    /**
     * Compact constructor with validation.
     *
     * @throws NullPointerException     if any parameter is null
     * @throws IllegalArgumentException if id is blank
     */
    public ServiceDefinition {
        Objects.requireNonNull(id, "Service ID cannot be null");
        Objects.requireNonNull(displayName, "Display name cannot be null");
        Objects.requireNonNull(keywords, "Keywords list cannot be null");

        if (id.isBlank()) {
            throw new IllegalArgumentException("Service ID cannot be blank");
        }

        // Make keywords immutable
        keywords = List.copyOf(keywords);
    }

    /**
     * Creates a new ServiceDefinition with the specified parameters.
     *
     * @param id          unique service identifier
     * @param displayName human-readable service name
     * @param keywords    list of keywords for matching
     * @return a new ServiceDefinition instance
     */
    @NotNull
    public static ServiceDefinition of(
            @NotNull String id,
            @NotNull String displayName,
            @NotNull List<String> keywords
    ) {
        return new ServiceDefinition(id, displayName, keywords);
    }
}
