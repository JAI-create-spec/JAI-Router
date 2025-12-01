package io.jai.router.core;

import org.jetbrains.annotations.NotNull;

/**
 * Core routing interface for directing requests to appropriate services.
 * <p>
 * Implementations analyze input content and determine the best matching
 * service based on various strategies (keyword matching, AI/ML models, etc.).
 * </p>
 *
 * @author JAI Router Team
 * @since 1.0.0
 */
public interface Router {

    /**
     * Routes the given input to the most appropriate service.
     * <p>
     * This method analyzes the input content and returns a routing decision
     * that includes the target service identifier, confidence score, and
     * an explanation of the routing logic.
     * </p>
     *
     * @param input the request content to route, must not be null or empty
     * @return routing result containing service, confidence, and explanation
     * @throws NullPointerException     if input is null
     * @throws IllegalArgumentException if input is invalid (empty, too long, etc.)
     */
    @NotNull
    RoutingResult route(@NotNull String input);
}
