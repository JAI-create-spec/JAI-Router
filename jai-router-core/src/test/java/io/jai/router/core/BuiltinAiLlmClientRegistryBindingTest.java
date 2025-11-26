package io.jai.router.core;

import io.jai.router.llm.BuiltinAiLlmClient;
import io.jai.router.registry.InMemoryServiceRegistry;
import io.jai.router.registry.ServiceDefinition;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

public class BuiltinAiLlmClientRegistryBindingTest {

    @Test
    void bindsToRegistryAndUsesRegistryKeywords() {
        var services = List.of(
            new ServiceDefinition("payment-service", "Payment", List.of("payment","invoice","billing")),
            new ServiceDefinition("auth-service", "Auth", List.of("token","login","authenticate"))
        );
        var registry = new InMemoryServiceRegistry(services);
        // start with empty keyword map but provide registry
        BuiltinAiLlmClient client = new BuiltinAiLlmClient(Map.of(), 0.5, registry);

        var decision = client.decide(DecisionContext.of("Please create an invoice and process payment"));
        assertThat(decision.service()).isEqualTo("payment-service");
        assertThat(decision.confidence()).isGreaterThan(0.6);
    }

    @Test
    void customDefaultConfidenceIsRespectedForUnknownKeywords() {
        BuiltinAiLlmClient client = new BuiltinAiLlmClient(Map.of(), 0.42);
        var d = client.decide(DecisionContext.of("unknown payload text"));
        assertThat(d.confidence()).isEqualTo(0.42);
    }
}

