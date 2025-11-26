package io.jai.router.llm;

import io.jai.router.core.LlmClient;
import io.jai.router.core.RoutingDecision;
import io.jai.router.core.DecisionContext;
import io.jai.router.core.KeywordMatcher;
import io.jai.router.core.LlmClientException;
import io.jai.router.core.ScoringKeywordMatcher;

import io.jai.router.registry.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Lightweight built-in LLM client that uses keyword scoring to pick a service.
 * This is intended for development and local fallback behavior.
 */
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

    public BuiltinAiLlmClient() { this(DEFAULT_SERVICE_KEYWORDS, 0.5); }

    public BuiltinAiLlmClient(Map<String, String> serviceKeywords) { this(serviceKeywords, 0.5); }

    // Backwards-compatible constructor: (map, registry)
    public BuiltinAiLlmClient(Map<String, String> serviceKeywords, io.jai.router.registry.ServiceRegistry registry) {
        this(serviceKeywords, 0.5, registry);
    }

    /**
     * Create with explicit default confidence.
     */
    public BuiltinAiLlmClient(Map<String, String> serviceKeywords, double defaultConfidence) {
        if (serviceKeywords == null) {
            throw new LlmClientException("serviceKeywords must not be null");
        }
        this.matcher = new ScoringKeywordMatcher(serviceKeywords, "default-service", defaultConfidence);
    }

    /**
     * Create a new BuiltinAiLlmClient with custom keywords and a runtime ServiceRegistry.
     * The matcher will bind to the registry and receive dynamic updates.
     */
    public BuiltinAiLlmClient(Map<String, String> serviceKeywords, double defaultConfidence, ServiceRegistry registry) {
        if (serviceKeywords == null) {
            throw new LlmClientException("serviceKeywords must not be null");
        }
        ScoringKeywordMatcher m = new ScoringKeywordMatcher(
            serviceKeywords,
            "default-service",
            defaultConfidence
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
            // avoid trailing punctuation in structured log format
            log.debug("BuiltinAiLlmClient decided service='{}' confidence={} explanation='{}'",
                result.service(), result.confidence(), result.explanation());
        }
        return RoutingDecision.of(result.service(), result.confidence(), result.explanation());
    }
}
