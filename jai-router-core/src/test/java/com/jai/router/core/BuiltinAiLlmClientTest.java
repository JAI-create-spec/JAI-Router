package com.jai.router.core;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

public class BuiltinAiLlmClientTest {

    @Test
    void defaultMappingMatchesKnownKeyword() {
        BuiltinAiLlmClient c = new BuiltinAiLlmClient();
        RoutingDecision d = c.decide(DecisionContext.of("Please encrypt this message"));
        assertThat(d.service()).isEqualTo("cryptography-service");
        assertThat(d.confidence()).isGreaterThan(0.8);
    }

    @Test
    void unknownDefaultsToDefaultService() {
        BuiltinAiLlmClient c = new BuiltinAiLlmClient();
        RoutingDecision d = c.decide(DecisionContext.of("hello world"));
        assertThat(d.service()).isEqualTo("default-service");
        assertThat(d.confidence()).isEqualTo(0.5);
    }

    @Test
    void nullContextThrowsLlmClientException() {
        BuiltinAiLlmClient c = new BuiltinAiLlmClient();
        assertThatThrownBy(() -> c.decide(null)).isInstanceOf(LlmClientException.class);
    }

    @Test
    void doesNotMatchPartialWords() {
        BuiltinAiLlmClient c = new BuiltinAiLlmClient();
        RoutingDecision d = c.decide(DecisionContext.of("Please tokenize this data"));
        // Should not match "token" in "tokenize"
        assertThat(d.service()).isEqualTo("default-service");
    }

    @Test
    void matchesMultipleKeywords() {
        BuiltinAiLlmClient c = new BuiltinAiLlmClient();
        RoutingDecision d = c.decide(DecisionContext.of("encrypt and decrypt data"));
        assertThat(d.service()).isEqualTo("cryptography-service");
        assertThat(d.confidence()).isGreaterThan(0.8);
    }

    @Test
    void isCaseInsensitive() {
        BuiltinAiLlmClient c = new BuiltinAiLlmClient();
        RoutingDecision d1 = c.decide(DecisionContext.of("ENCRYPT data"));
        RoutingDecision d2 = c.decide(DecisionContext.of("encrypt data"));
        assertThat(d1.service()).isEqualTo(d2.service());
        assertThat(d1.confidence()).isEqualTo(d2.confidence());
    }
}

