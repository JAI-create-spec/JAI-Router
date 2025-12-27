package io.jai.router.core;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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

    /**
     * Routes the given input asynchronously to the most appropriate service.
     * <p>
     * This method provides non-blocking routing for better scalability in
     * high-throughput scenarios. The default implementation delegates to
     * {@link #route(String)} in a separate thread.
     * </p>
     *
     * @param input the request content to route, must not be null or empty
     * @return CompletableFuture containing the routing result
     * @throws NullPointerException if input is null
     */
    @NotNull
    default CompletableFuture<RoutingResult> routeAsync(@NotNull String input) {
        Objects.requireNonNull(input, "Input cannot be null");
        return CompletableFuture.supplyAsync(() -> route(input));
    }

    /**
     * Routes multiple inputs in batch.
     * <p>
     * This method processes multiple routing requests and returns results
     * in the same order as the inputs. The default implementation processes
     * each input sequentially.
     * </p>
     *
     * @param inputs list of request contents to route, must not be null
     * @return list of routing results in the same order as inputs
     * @throws NullPointerException if inputs is null or contains null elements
     */
    @NotNull
    default List<RoutingResult> routeBatch(@NotNull List<String> inputs) {
        Objects.requireNonNull(inputs, "Inputs cannot be null");
        return inputs.stream()
                .map(this::route)
                .collect(Collectors.toList());
    }

    /**
     * Routes multiple inputs asynchronously in batch.
     * <p>
     * This method processes multiple routing requests concurrently and returns
     * results in the same order as the inputs. This is more efficient than
     * calling {@link #routeAsync(String)} multiple times for independent requests.
     * </p>
     *
     * @param inputs list of request contents to route, must not be null
     * @return CompletableFuture containing list of routing results
     * @throws NullPointerException if inputs is null or contains null elements
     */
    @NotNull
    default CompletableFuture<List<RoutingResult>> routeBatchAsync(@NotNull List<String> inputs) {
        Objects.requireNonNull(inputs, "Inputs cannot be null");

        List<CompletableFuture<RoutingResult>> futures = inputs.stream()
                .map(this::routeAsync)
                .collect(Collectors.toList());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList()));
    }
}
