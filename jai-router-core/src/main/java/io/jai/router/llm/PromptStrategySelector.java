package io.jai.router.llm;

public class PromptStrategySelector {

    private final ExampleRegistry exampleRegistry;

    public PromptStrategySelector(ExampleRegistry exampleRegistry) {
        this.exampleRegistry = exampleRegistry;
    }

    public PromptStrategy select(String request, LLMProvider provider, PromptContext context) {
        double complexity = 0.5; // Placeholder
        if (complexity < 0.3) return new SimpleClassificationPrompt();
        if (complexity > 0.7 && provider.getName().equalsIgnoreCase("openai")) return new ChainOfThoughtPrompt();
        if (context != null && exampleRegistry != null) return new FewShotPrompt(exampleRegistry);
        return new SimpleClassificationPrompt();
    }
}
