package io.jai.router.core;

import io.jai.router.registry.InMemoryServiceRegistry;
import io.jai.router.registry.ServiceDescriptor;
import io.jai.router.registry.ServiceRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class RegistryIntegrationTest {

    @Test
    public void registerService_then_client_decides_for_keyword() {
        ServiceRegistry registry = new InMemoryServiceRegistry();
        Map<String, String> baseKeywords = Map.of("default", "default-service");
        io.jai.router.llm.BuiltinAiLlmClient client = new io.jai.router.llm.BuiltinAiLlmClient(baseKeywords, registry);

        ServiceDescriptor education = new ServiceDescriptor(
            "education-service",
            "Education Service",
            "http://education.internal/api",
            List.of("lesson", "course", "syllabus")
        );

        registry.register(education);

        io.jai.router.core.RoutingDecision decision = client.decide(new io.jai.router.core.DecisionContext("Create a lesson plan for algebra"));
        assertThat(decision.service()).isEqualTo("education-service");
        assertThat(decision.confidence()).isGreaterThan(0.5);
    }
}
