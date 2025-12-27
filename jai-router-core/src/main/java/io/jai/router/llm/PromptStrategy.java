package io.jai.router.llm;

import io.jai.router.registry.ServiceDefinition;
import java.util.List;

public interface PromptStrategy {
    String buildPrompt(String request, List<ServiceDefinition> services, PromptContext context);
}

