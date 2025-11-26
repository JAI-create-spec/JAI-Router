package io.jai.router.llm;

import io.jai.router.core.DecisionContext;
import io.jai.router.core.RoutingDecision;

public class AnthropicProvider implements LLMProvider {
    @Override public RoutingDecision decide(DecisionContext ctx) { throw new UnsupportedOperationException("Anthropic provider not implemented"); }
    @Override public String getName() { return "anthropic"; }
    @Override public boolean isAvailable() { return false; }
}

