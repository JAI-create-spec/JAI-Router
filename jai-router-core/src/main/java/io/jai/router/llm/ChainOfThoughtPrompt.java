package io.jai.router.llm;

import io.jai.router.registry.ServiceDefinition;
import java.util.List;
import java.util.stream.Collectors;

public class ChainOfThoughtPrompt implements PromptStrategy {
    @Override
    public String buildPrompt(String request, List<ServiceDefinition> services, PromptContext context) {
        String svcList = services.stream()
                .map(s -> s.id() + ": " + s.displayName())
                .collect(Collectors.joining("\n"));

        return String.format("Analyze this request step by step:\n\nRequest: \"%s\"\n\nStep 1: Identify key intent and entities\nStep 2: Match to service capabilities\nStep 3: Evaluate confidence\n\nAvailable services:\n%s\n\nProvide detailed reasoning, then respond with JSON: {\"service\": \"...\", \"confidence\": 0.0-1.0, \"reasoning\": \"...\"}\n",
                request,
                svcList);
    }
}
