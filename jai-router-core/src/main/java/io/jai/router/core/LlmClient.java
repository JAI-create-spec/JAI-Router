package io.jai.router.core;

import java.util.Optional;

@FunctionalInterface
public interface LlmClient {
    RoutingDecision decide(DecisionContext ctx);

    default Optional<RoutingDecision> decideNullable(DecisionContext ctx) {
        try {
            return Optional.of(decide(ctx));
        } catch (RuntimeException ex) {
            return Optional.empty();
        }
    }
}
