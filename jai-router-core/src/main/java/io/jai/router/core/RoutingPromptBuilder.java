package io.jai.router.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Prompt template builder for LLM routing decisions.
 * <p>
 * This class helps construct well-formatted prompts for LLM-based routing
 * by providing a fluent API for building structured prompts with service
 * descriptions, examples, and formatting instructions.
 * </p>
 *
 * <h2>Features</h2>
 * <ul>
 *   <li>Structured prompt generation with service descriptions</li>
 *   <li>Support for few-shot learning with examples</li>
 *   <li>Customizable system prompts and instructions</li>
 *   <li>JSON response format specification</li>
 *   <li>Template-based prompt construction</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * RoutingPromptBuilder builder = new RoutingPromptBuilder()
 *     .withSystemPrompt("You are an intelligent request router.")
 *     .addService("payment-service", "Handles payment processing and transactions")
 *     .addService("analytics-service", "Generates reports and analytics")
 *     .addExample("Process my payment", "payment-service")
 *     .addExample("Show me sales report", "analytics-service")
 *     .withResponseFormat(ResponseFormat.JSON);
 *
 * String prompt = builder.buildPrompt("Charge my credit card");
 * }</pre>
 *
 * @author JAI Router Team
 * @since 1.0.0
 */
public class RoutingPromptBuilder {

    private String systemPrompt;
    private final Map<String, String> serviceDescriptions;
    private final List<RoutingExample> examples;
    private ResponseFormat responseFormat;
    private String customInstructions;
    private boolean includeConfidenceGuidance;

    /**
     * Creates a new routing prompt builder with default settings.
     */
    public RoutingPromptBuilder() {
        this.serviceDescriptions = new LinkedHashMap<>();
        this.examples = new ArrayList<>();
        this.responseFormat = ResponseFormat.JSON;
        this.includeConfidenceGuidance = true;
    }

    /**
     * Sets the system prompt that defines the LLM's role.
     *
     * @param systemPrompt the system prompt text
     * @return this builder instance
     */
    @NotNull
    public RoutingPromptBuilder withSystemPrompt(@Nullable String systemPrompt) {
        this.systemPrompt = systemPrompt;
        return this;
    }

    /**
     * Adds a service with its description.
     *
     * @param serviceId   the service identifier
     * @param description description of what the service does
     * @return this builder instance
     */
    @NotNull
    public RoutingPromptBuilder addService(@NotNull String serviceId, @NotNull String description) {
        Objects.requireNonNull(serviceId, "Service ID cannot be null");
        Objects.requireNonNull(description, "Description cannot be null");
        this.serviceDescriptions.put(serviceId, description);
        return this;
    }

    /**
     * Adds multiple services from a map.
     *
     * @param services map of service IDs to descriptions
     * @return this builder instance
     */
    @NotNull
    public RoutingPromptBuilder addServices(@NotNull Map<String, String> services) {
        Objects.requireNonNull(services, "Services map cannot be null");
        this.serviceDescriptions.putAll(services);
        return this;
    }

    /**
     * Adds a routing example for few-shot learning.
     *
     * @param input          example input text
     * @param expectedService expected service for this input
     * @return this builder instance
     */
    @NotNull
    public RoutingPromptBuilder addExample(@NotNull String input, @NotNull String expectedService) {
        Objects.requireNonNull(input, "Example input cannot be null");
        Objects.requireNonNull(expectedService, "Expected service cannot be null");
        this.examples.add(new RoutingExample(input, expectedService, null));
        return this;
    }

    /**
     * Adds a routing example with explanation.
     *
     * @param input          example input text
     * @param expectedService expected service for this input
     * @param explanation    explanation of why this routing is correct
     * @return this builder instance
     */
    @NotNull
    public RoutingPromptBuilder addExample(
            @NotNull String input,
            @NotNull String expectedService,
            @Nullable String explanation) {
        Objects.requireNonNull(input, "Example input cannot be null");
        Objects.requireNonNull(expectedService, "Expected service cannot be null");
        this.examples.add(new RoutingExample(input, expectedService, explanation));
        return this;
    }

    /**
     * Sets the response format for the LLM output.
     *
     * @param format the desired response format
     * @return this builder instance
     */
    @NotNull
    public RoutingPromptBuilder withResponseFormat(@NotNull ResponseFormat format) {
        this.responseFormat = Objects.requireNonNull(format, "Response format cannot be null");
        return this;
    }

    /**
     * Adds custom instructions to the prompt.
     *
     * @param instructions custom instructions for the LLM
     * @return this builder instance
     */
    @NotNull
    public RoutingPromptBuilder withCustomInstructions(@Nullable String instructions) {
        this.customInstructions = instructions;
        return this;
    }

    /**
     * Sets whether to include confidence scoring guidance.
     *
     * @param include true to include confidence guidance
     * @return this builder instance
     */
    @NotNull
    public RoutingPromptBuilder includeConfidenceGuidance(boolean include) {
        this.includeConfidenceGuidance = include;
        return this;
    }

    /**
     * Builds the complete prompt for the given user input.
     *
     * @param userInput the user's request to route
     * @return formatted prompt string
     * @throws IllegalStateException if no services have been added
     */
    @NotNull
    public String buildPrompt(@NotNull String userInput) {
        Objects.requireNonNull(userInput, "User input cannot be null");

        if (serviceDescriptions.isEmpty()) {
            throw new IllegalStateException("At least one service must be added before building prompt");
        }

        StringBuilder prompt = new StringBuilder();

        // System prompt
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            prompt.append(systemPrompt).append("\n\n");
        } else {
            prompt.append("You are an intelligent request router that analyzes user requests ")
                    .append("and determines the most appropriate service to handle them.\n\n");
        }

        // Available services
        prompt.append("## Available Services\n\n");
        for (Map.Entry<String, String> entry : serviceDescriptions.entrySet()) {
            prompt.append("- **").append(entry.getKey()).append("**: ")
                    .append(entry.getValue()).append("\n");
        }
        prompt.append("\n");

        // Examples (few-shot learning)
        if (!examples.isEmpty()) {
            prompt.append("## Examples\n\n");
            for (RoutingExample example : examples) {
                prompt.append("Input: \"").append(example.input).append("\"\n");
                prompt.append("Service: ").append(example.service).append("\n");
                if (example.explanation != null) {
                    prompt.append("Reason: ").append(example.explanation).append("\n");
                }
                prompt.append("\n");
            }
        }

        // Confidence guidance
        if (includeConfidenceGuidance) {
            prompt.append("## Confidence Scoring Guidelines\n\n");
            prompt.append("- **0.9-1.0**: Perfect match, clear and unambiguous\n");
            prompt.append("- **0.7-0.9**: Good match, high confidence\n");
            prompt.append("- **0.5-0.7**: Reasonable match, moderate confidence\n");
            prompt.append("- **0.3-0.5**: Weak match, low confidence\n");
            prompt.append("- **0.0-0.3**: Very uncertain, consider fallback\n\n");
        }

        // Custom instructions
        if (customInstructions != null && !customInstructions.isBlank()) {
            prompt.append("## Additional Instructions\n\n");
            prompt.append(customInstructions).append("\n\n");
        }

        // User request
        prompt.append("## User Request\n\n");
        prompt.append("\"").append(userInput).append("\"\n\n");

        // Response format
        prompt.append("## Response Format\n\n");
        prompt.append(getResponseFormatInstructions());

        return prompt.toString();
    }

    /**
     * Returns the response format instructions based on the configured format.
     *
     * @return response format instructions
     */
    private String getResponseFormatInstructions() {
        return switch (responseFormat) {
            case JSON -> """
                    Respond with a JSON object in the following format:
                    {
                      "service": "<service-id>",
                      "confidence": <0.0-1.0>,
                      "explanation": "<brief explanation of your routing decision>"
                    }

                    Ensure the response is valid JSON and includes all three fields.
                    """;
            case STRUCTURED_TEXT -> """
                    Respond in the following format:
                    Service: <service-id>
                    Confidence: <0.0-1.0>
                    Explanation: <brief explanation of your routing decision>
                    """;
            case NATURAL_LANGUAGE -> """
                    Provide your routing decision in natural language, clearly stating:
                    1. Which service should handle this request
                    2. Your confidence level (0.0 to 1.0)
                    3. A brief explanation of your reasoning
                    """;
        };
    }

    /**
     * Clears all configured services, examples, and settings.
     *
     * @return this builder instance
     */
    @NotNull
    public RoutingPromptBuilder clear() {
        this.systemPrompt = null;
        this.serviceDescriptions.clear();
        this.examples.clear();
        this.responseFormat = ResponseFormat.JSON;
        this.customInstructions = null;
        this.includeConfidenceGuidance = true;
        return this;
    }

    /**
     * Response format options for LLM output.
     */
    public enum ResponseFormat {
        /**
         * JSON format with structured fields.
         */
        JSON,

        /**
         * Structured text format with labeled fields.
         */
        STRUCTURED_TEXT,

        /**
         * Natural language response.
         */
        NATURAL_LANGUAGE
    }

    /**
     * Internal record for storing routing examples.
     */
    private record RoutingExample(
            @NotNull String input,
            @NotNull String service,
            @Nullable String explanation
    ) {
    }
}
