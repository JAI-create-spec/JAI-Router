package io.jai.router.llm;

import io.jai.router.core.DecisionContext;
import io.jai.router.core.RoutingDecision;
import io.jai.router.registry.ServiceDefinition;
import java.util.List;

public class BuiltInAIProvider implements LLMProvider {
    private final io.jai.router.llm.BuiltinAiLlmClient client;
    public BuiltInAIProvider() { this.client = new BuiltinAiLlmClient(); }
    @Override public RoutingDecision decide(DecisionContext ctx) { return client.decide(ctx); }
    @Override public String getName() { return "builtin-ai"; }
    @Override public boolean isAvailable() { return true; }

    /**
     * Estimate confidence for built-in provider on given request and services.
     * This is a lightweight heuristic; real implementation should use KeywordMatcher
     * or other fast heuristics.
     */
    public double estimateConfidence(String request, List<ServiceDefinition> services) {
        if (request == null || request.isBlank() || services == null || services.isEmpty()) return 0.0;
        String lower = request.toLowerCase();
        int matches = 0;
        for (ServiceDefinition s : services) {
            for (String kw : s.keywords()) {
                if (lower.contains(kw.toLowerCase())) {
                    matches++;
                    break;
                }
            }
        }
        double ratio = (double) matches / (double) services.size();
        // map ratio to confidence [0.0, 1.0]
        return Math.max(0.0, Math.min(1.0, 0.5 + ratio * 0.5));
    }
}
