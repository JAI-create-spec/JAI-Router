package io.jai.router.llm;

import io.jai.router.core.DecisionContext;
import io.jai.router.core.RoutingDecision;

public class OpenAIProvider implements LLMProvider {
    @Override public RoutingDecision decide(DecisionContext ctx) { throw new UnsupportedOperationException("OpenAI provider not implemented"); }
    @Override public String getName() { return "openai"; }
    @Override public boolean isAvailable() { return false; }
}

