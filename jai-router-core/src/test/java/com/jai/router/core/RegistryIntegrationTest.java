package com.jai.router.core;

import com.jai.router.core.registry.InMemoryServiceRegistry;
import com.jai.router.core.registry.ServiceDescriptor;
import com.jai.router.core.registry.ServiceRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class RegistryIntegrationTest {

    @Test
    public void registerService_then_client_decides_for_keyword() {
        ServiceRegistry registry = new InMemoryServiceRegistry();
        Map<String, String> baseKeywords = Map.of("default", "default-service");
        BuiltinAiLlmClient client = new BuiltinAiLlmClient(baseKeywords, registry);

        ServiceDescriptor education = new ServiceDescriptor(
            "education-service",
            "Education Service",
            "http://education.internal/api",
            List.of("lesson", "course", "syllabus")
        );

        registry.register(education);

        RoutingDecision decision = client.decide(new DecisionContext("Create a lesson plan for algebra"));
        assertThat(decision.service()).isEqualTo("education-service");
        assertThat(decision.confidence()).isGreaterThan(0.5);
    }
}

