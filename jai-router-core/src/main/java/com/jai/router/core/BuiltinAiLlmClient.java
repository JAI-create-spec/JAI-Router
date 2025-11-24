
package com.jai.router.core;

import java.util.*;

public class BuiltinAiLlmClient implements LlmClient {

    private static final Map<String, String> SERVICE_KEYWORDS = Map.of(
        "encrypt", "cryptography-service",
        "decrypt", "cryptography-service",
        "login", "auth-service",
        "token", "auth-service",
        "report", "bi-service",
        "dashboard", "bi-service",
        "kpi", "bi-service"
    );

    @Override
    public RoutingDecision infer(DecisionContext ctx) {
        String text = ctx.getPayload().toLowerCase();
        String chosen = "default-service";
        double confidence = 0.5;

        for (var e : SERVICE_KEYWORDS.entrySet()) {
            if (text.contains(e.getKey())) {
                chosen = e.getValue();
                confidence = 0.9;
                break;
            }
        }

        return new RoutingDecision(chosen, confidence, "Builtin-AI matched service based on lightweight keyword logic.");
    }
}
