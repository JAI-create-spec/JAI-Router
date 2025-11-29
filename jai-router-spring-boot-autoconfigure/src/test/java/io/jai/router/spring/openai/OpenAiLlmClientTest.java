package io.jai.router.spring.openai;

import io.jai.router.core.RoutingDecision;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class OpenAiLlmClientTest {

    @Test
    void parsePlainJson() {
        String json = "{\"service\":\"payment-service\",\"confidence\":0.92,\"explanation\":\"matched keywords\"}";
        Optional<RoutingDecision> r = OpenAiLlmClient.tryParseAssistantContent(json);
        assertThat(r).isPresent();
        assertThat(r.get().service()).isEqualTo("payment-service");
        assertThat(r.get().confidence()).isCloseTo(0.92, within(0.001));
    }

    @Test
    void parseSurroundedTextWithJson() {
        String text = "Here is the result:\n\n" +
                "{\"service\":\"bi-service\",\"confidence\":0.75,\"explanation\":\"ok\"}\n\nThank you.";
        Optional<RoutingDecision> r = OpenAiLlmClient.tryParseAssistantContent(text);
        assertThat(r).isPresent();
        assertThat(r.get().service()).isEqualTo("bi-service");
    }

    @Test
    void parseMalformedJsonReturnsEmpty() {
        String text = "Service: bi-service, Confidence: 0.5";
        Optional<RoutingDecision> r = OpenAiLlmClient.tryParseAssistantContent(text);
        assertThat(r).isEmpty();
    }

    private org.assertj.core.data.Offset<Double> within(double value) {
        return org.assertj.core.data.Offset.offset(value);
    }
}

