package io.jai.router.llm;

import io.jai.router.registry.ServiceDefinition;
import java.util.List;

public class IntelligentProviderSelector implements ProviderSelector {

    private final BuiltInAIProvider builtIn;
    private final OpenAIProvider openAI;
    private final AnthropicProvider anthropic;

    public IntelligentProviderSelector(BuiltInAIProvider builtIn, OpenAIProvider openAI, AnthropicProvider anthropic) {
        this.builtIn = builtIn;
        this.openAI = openAI;
        this.anthropic = anthropic;
    }

    @Override
    public LLMProvider selectProvider(String request, SelectionContext context, List<ServiceDefinition> services) {
        double complexity = 0.5; // placeholder; real impl should analyze request
        double builtInConfidence = builtIn.estimateConfidence(request, services);

        if (complexity < 0.3 && builtInConfidence > 0.85) {
            return builtIn;
        }

        if (context.getLatencyBudgetMs() != null && context.getLatencyBudgetMs() < 100) {
            return builtIn;
        }

        // default: use OpenAI if allowed
        if (context.getAllowedProviders() == null || context.getAllowedProviders().contains("openai")) {
            return openAI;
        }

        return anthropic;
    }
}

