package io.jai.router.core;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Default implementation of the {@link Router} interface.
 * <p>
 * This engine delegates routing decisions to a configured {@link LlmClient}
 * and transforms the result into a {@link RoutingResult}.
 * </p>
 *
 * @author JAI Router Team
 * @since 1.0.0
 */
public class RouterEngine implements Router {

    private static final Logger log = LoggerFactory.getLogger(RouterEngine.class);

    private final LlmClient client;

    /**
     * Creates a new router engine with the specified LLM client.
     *
     * @param client the LLM client to use for routing decisions, must not be null
     * @throws NullPointerException if client is null
     */
    public RouterEngine(@NotNull LlmClient client) {
        this.client = Objects.requireNonNull(client, "LlmClient cannot be null");
        log.info("RouterEngine initialized with client: {}", client.getClass().getSimpleName());
    }

    /**
     * Routes the given input to the most appropriate service.
     *
     * @param input the request content to route, must not be null
     * @return routing result containing service ID, confidence, and explanation
     * @throws NullPointerException     if input is null
     * @throws IllegalArgumentException if input is invalid (via DecisionContext)
     * @throws IllegalStateException    if LLM client returns no decision
     */
    @Override
    @NotNull
    public RoutingResult route(@NotNull String input) {
        Objects.requireNonNull(input, "Input cannot be null");

        if (log.isDebugEnabled()) {
            log.debug("Routing request: '{}'", input.substring(0, Math.min(input.length(), 100)));
        }

        DecisionContext ctx = DecisionContext.of(input);
        RoutingDecision decision = client.decide(ctx);

        if (decision == null) {
            log.error("LLM client returned null decision for input");
            throw new IllegalStateException("LLM client returned no decision");
        }

        RoutingResult result = new RoutingResult(
                decision.service(),
                decision.confidence(),
                decision.explanation()
        );

        if (log.isDebugEnabled()) {
            log.debug("Routing result: service='{}' confidence={}",
                    result.service(), result.confidence());
        }

        return result;
    }
}
