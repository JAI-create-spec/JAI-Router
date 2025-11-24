package com.jai.router.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Objects;

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

    private final Map<String, String> serviceKeywords;

    public BuiltinAiLlmClient() {
        this(DEFAULT_SERVICE_KEYWORDS);
    }

    public BuiltinAiLlmClient(Map<String, String> serviceKeywords) {
        if (serviceKeywords == null) {
            throw new LlmClientException("serviceKeywords must not be null");
        }
        this.serviceKeywords = Collections.unmodifiableMap(new HashMap<>(serviceKeywords));
    }

    @Override
    public RoutingDecision decide(DecisionContext ctx) {
        if (ctx == null) {
            throw new LlmClientException("DecisionContext must not be null");
        }
        String text = ctx.payload().toLowerCase();
        String chosen = "default-service";
        double confidence = 0.5;

        for (var e : serviceKeywords.entrySet()) {
            if (text.contains(e.getKey())) {
                chosen = e.getValue();
                confidence = 0.9;
                break;
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("BuiltinAiLlmClient decided service='{}' confidence={} for payloadLength={}", chosen, confidence, text.length());
        }

        return RoutingDecision.of(chosen, confidence, "Builtin-AI matched service based on lightweight keyword logic.");
    }
}
