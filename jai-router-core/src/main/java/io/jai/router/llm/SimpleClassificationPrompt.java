package io.jai.router.llm;

import io.jai.router.registry.ServiceDefinition;
import java.util.List;
import java.util.stream.Collectors;

public class SimpleClassificationPrompt implements PromptStrategy {
    @Override
    public String buildPrompt(String request, List<ServiceDefinition> services, PromptContext context) {
        String svcList = services.stream()
                .map(s -> s.id() + ": " + s.displayName())
                .collect(Collectors.joining("\n"));

        return String.format("Classify this user request to one service:\nRequest: \"%s\"\n\nAvailable services:\n%s\n\nRespond with JSON: {\"service\": \"...\", \"confidence\": 0.0-1.0}\n",
                request,
                svcList);
    }
}
