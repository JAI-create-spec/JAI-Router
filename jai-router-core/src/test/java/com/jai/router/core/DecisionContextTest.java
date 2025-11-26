package io.jai.router.core;

import io.jai.router.core.DecisionContext;
import io.jai.router.core.RoutingDecision;
import io.jai.router.core.ScoringKeywordMatcher;
import io.jai.router.core.KeywordMatcher;
import io.jai.router.core.LlmClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class DecisionContextTest {

    @Test
    void nullPayloadShouldThrow() {
        assertThatThrownBy(() -> DecisionContext.of(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("Payload cannot be null");
    }

    @Test
    void emptyPayloadShouldThrow() {
        assertThatThrownBy(() -> DecisionContext.of(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Payload cannot be empty");
    }

    @Test
    void blankPayloadShouldThrow() {
        assertThatThrownBy(() -> DecisionContext.of("   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Payload cannot be empty");
    }

    @Test
    void oversizedPayloadShouldThrow() {
        String largePayload = "a".repeat(10_001);
        assertThatThrownBy(() -> DecisionContext.of(largePayload))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("exceeds maximum size");
    }

    @Test
    void payloadIsTrimmed() {
        DecisionContext ctx = DecisionContext.of("  hello world  ");
        assertThat(ctx.payload()).isEqualTo("hello world");
    }

    @Test
    void validPayloadIsAccepted() {
        DecisionContext ctx = DecisionContext.of("valid payload");
        assertThat(ctx.payload()).isEqualTo("valid payload");
    }
}
