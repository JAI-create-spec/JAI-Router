package com.jai.router.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Built-in AI LLM client using keyword-based routing.
 * 
 * <p>This implementation uses a {@link KeywordMatcher} to determine the best
 * matching service based on keywords in the input text.
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

    /**
     * Create a new BuiltinAiLlmClient with default keywords.
     */
    public BuiltinAiLlmClient() {
        this(DEFAULT_SERVICE_KEYWORDS);
    }

    /**
     * Create a new BuiltinAiLlmClient with custom keywords.
     * @param serviceKeywords mapping from keyword to service name
     * @throws LlmClientException if serviceKeywords is null
     */
    public BuiltinAiLlmClient(Map<String, String> serviceKeywords) {
        if (serviceKeywords == null) {
            throw new LlmClientException("serviceKeywords must not be null");
        }
        this.matcher = new ScoringKeywordMatcher(
            serviceKeywords,
            "default-service",
            0.5
        );
    }

    @Override
    public RoutingDecision decide(DecisionContext ctx) {
        if (ctx == null) {
            throw new LlmClientException("DecisionContext must not be null");
        }
        
        KeywordMatcher.MatchResult result = matcher.findBestMatch(ctx.payload());
        
        if (log.isDebugEnabled()) {
            log.debug("BuiltinAiLlmClient decided service='{}' confidence={} explanation='{}'",
                result.service(), result.confidence(), result.explanation());
        }

        return RoutingDecision.of(result.service(), result.confidence(), result.explanation());
    }
}
