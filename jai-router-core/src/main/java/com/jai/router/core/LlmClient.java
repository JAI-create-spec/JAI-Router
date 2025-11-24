
package com.jai.router.core;

public interface LlmClient {
    RoutingDecision infer(DecisionContext ctx);
}
