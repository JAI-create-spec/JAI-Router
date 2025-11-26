package io.jai.router.core;

import io.jai.router.registry.InMemoryServiceRegistry;
import io.jai.router.registry.ServiceDefinition;
import io.jai.router.registry.ServiceRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class RegistryIntegrationTest {

    @Test
    public void registerService_then_client_decides_for_keyword() {
        ServiceDefinition education = new ServiceDefinition(
            "education-service",
            "Education Service",
            List.of("lesson", "course", "syllabus")
        );
        ServiceRegistry registry = new InMemoryServiceRegistry(List.of(education));

        Map<String, String> baseKeywords = Map.of("default", "default-service");
        io.jai.router.llm.BuiltinAiLlmClient client = new io.jai.router.llm.BuiltinAiLlmClient(baseKeywords, registry);

        io.jai.router.core.RoutingDecision decision = client.decide(new io.jai.router.core.DecisionContext("Create a lesson plan for algebra"));
        assertThat(decision.service()).isEqualTo("education-service");
        assertThat(decision.confidence()).isGreaterThan(0.5);
    }
}
