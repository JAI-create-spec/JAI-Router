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
}

