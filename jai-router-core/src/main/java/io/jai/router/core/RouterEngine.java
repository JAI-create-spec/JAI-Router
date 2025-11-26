package io.jai.router.core;

import java.util.List;

public class RouterEngine implements Router {
    private final LlmClient client;
    public RouterEngine(LlmClient client) { this.client = client; }

    @Override
    public RoutingResult route(String input) {
        DecisionContext ctx = DecisionContext.of(input);
        RoutingDecision d = client.decide(ctx);
        return new RoutingResult(d.service(), d.confidence(), d.explanation());
    }
}

