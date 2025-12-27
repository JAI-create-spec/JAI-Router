package io.jai.router.llm;

import io.jai.router.registry.ServiceDefinition;
import java.util.List;

public interface ProviderSelector {
    LLMProvider selectProvider(String request, SelectionContext context, List<ServiceDefinition> services);
}

