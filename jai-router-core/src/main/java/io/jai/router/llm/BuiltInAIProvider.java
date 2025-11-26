package io.jai.router.llm;

import io.jai.router.core.DecisionContext;
import io.jai.router.core.RoutingDecision;

public class BuiltInAIProvider implements LLMProvider {
    private final io.jai.router.llm.BuiltinAiLlmClient client;
    public BuiltInAIProvider() { this.client = new BuiltinAiLlmClient(); }
    @Override public RoutingDecision decide(DecisionContext ctx) { return client.decide(ctx); }
    @Override public String getName() { return "builtin-ai"; }
    @Override public boolean isAvailable() { return true; }
}
