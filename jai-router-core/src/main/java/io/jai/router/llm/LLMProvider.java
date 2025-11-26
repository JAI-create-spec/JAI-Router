package io.jai.router.llm;

import io.jai.router.core.RoutingDecision;
import io.jai.router.core.DecisionContext;

public interface LLMProvider {
    RoutingDecision decide(DecisionContext ctx);
    String getName();
    boolean isAvailable();
}
