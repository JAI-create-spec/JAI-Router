package io.jai.router.core;

import java.util.Objects;

public class RouterEngine implements Router {
    private final LlmClient client;
    public RouterEngine(LlmClient client) { this.client = Objects.requireNonNull(client); }

    @Override
    public RoutingResult route(String input) {
        Objects.requireNonNull(input, "input cannot be null");
        DecisionContext ctx = DecisionContext.of(input);
        RoutingDecision d = client.decide(ctx);
        if (d == null) throw new IllegalStateException("LLM client returned no decision");
        return new RoutingResult(d.service(), d.confidence(), d.explanation());
    }
}
