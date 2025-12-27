package io.jai.router.llm;

import io.jai.router.registry.ServiceDefinition;
import java.util.List;

public class FewShotPrompt implements PromptStrategy {

    private final ExampleRegistry exampleRegistry;

    public FewShotPrompt(ExampleRegistry exampleRegistry) {
        this.exampleRegistry = exampleRegistry;
    }

    @Override
    public String buildPrompt(String request, List<ServiceDefinition> services, PromptContext context) {
        List<RoutingExample> examples = exampleRegistry.findSimilar(request, 5);
        StringBuilder sb = new StringBuilder();
        sb.append("Learn from these examples:\n\n");
        for (RoutingExample ex : examples) {
            sb.append(String.format("Example:\nRequest: \"%s\"\nService: %s\nConfidence: %.2f\n\n",
                    ex.getRequest(), ex.getService(), ex.getConfidence()));
        }
        sb.append(String.format("Now classify this request:\nRequest: \"%s\"\n\nAvailable services:\n%s\n\nRespond with JSON: {\"service\": \"...\", \"confidence\": 0.0-1.0}\n",
                request,
                services.toString()));

        return sb.toString();
    }
}

