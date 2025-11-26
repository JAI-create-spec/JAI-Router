package io.jai.router.llm;

import io.jai.router.core.LlmClient;
import io.jai.router.core.RoutingDecision;
import io.jai.router.core.DecisionContext;
import io.jai.router.core.KeywordMatcher;
import io.jai.router.core.LlmClientException;

import io.jai.router.registry.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public final class BuiltinAiLlmClient implements LlmClient {

    private static final Map<String, String> DEFAULT_SERVICE_KEYWORDS = Map.of(
        "encrypt", "cryptography-service",
        "decrypt", "cryptography-service",
        "login", "auth-service",
        "token", "auth-service",
        "report", "bi-service",
        "dashboard", "bi-service",
        "kpi", "bi-service"
    );

    private static final Logger log = LoggerFactory.getLogger(BuiltinAiLlmClient.class);

    private final KeywordMatcher matcher;

    public BuiltinAiLlmClient() { this(DEFAULT_SERVICE_KEYWORDS); }

    public BuiltinAiLlmClient(Map<String, String> serviceKeywords) {
        if (serviceKeywords == null) {
            throw new RuntimeException("serviceKeywords must not be null");
        }
        this.matcher = new io.jai.router.core.ScoringKeywordMatcher(serviceKeywords, "default-service", 0.5);
    }

    /**
     * Create a new BuiltinAiLlmClient with custom keywords and a runtime ServiceRegistry.
     * The matcher will bind to the registry and receive dynamic updates.
     */
    public BuiltinAiLlmClient(Map<String, String> serviceKeywords, ServiceRegistry registry) {
        if (serviceKeywords == null) {
            throw new LlmClientException("serviceKeywords must not be null");
        }
        io.jai.router.core.ScoringKeywordMatcher m = new io.jai.router.core.ScoringKeywordMatcher(
            serviceKeywords,
            "default-service",
            0.5
        );
        if (registry != null) {
            m.bindRegistry(registry);
        }
        this.matcher = m;
    }

    @Override
    public RoutingDecision decide(DecisionContext ctx) {
        if (ctx == null) throw new LlmClientException("DecisionContext must not be null");
        KeywordMatcher.MatchResult result = matcher.findBestMatch(ctx.payload());
        if (log.isDebugEnabled()) {
            log.debug("BuiltinAiLlmClient decided service='{}' confidence={} explanation='{}',",
                result.service(), result.confidence(), result.explanation());
        }
        return RoutingDecision.of(result.service(), result.confidence(), result.explanation());
    }
}
