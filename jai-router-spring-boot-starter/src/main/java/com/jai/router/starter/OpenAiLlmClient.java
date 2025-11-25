package com.jai.router.starter;

import com.jai.router.core.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Minimal OpenAI-based LlmClient implementation.
 * <p>
 * This implementation is intentionally small: it sends a chat completion request
 * to an OpenAI-compatible endpoint and expects the assistant to return a JSON
 * object with keys: service (string), confidence (number, 0..1), explanation (string).
 * If parsing fails, it falls back to a default decision.
 */
public class OpenAiLlmClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAiLlmClient.class);

    private final OpenAiProperties props;
    private final LlmProperties llmProps;
    private final HttpClient httpClient;
    private final ObjectMapper mapper = new ObjectMapper();

    public OpenAiLlmClient(OpenAiProperties props, LlmProperties llmProps) {
        this.props = props;
        this.llmProps = llmProps;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(Math.max(1, props.getTimeoutSeconds())))
            .build();
    }

    @Override
    public RoutingDecision decide(DecisionContext ctx) {
        if (ctx == null) {
            throw new LlmClientException("DecisionContext must not be null");
        }
        if (props.getApiKey() == null || props.getApiKey().isBlank()) {
            throw new LlmClientException("OpenAI API key is not configured (jai.router.openai.api-key)");
        }

        String model = llmProps != null && llmProps.getModel() != null ? llmProps.getModel() : "gpt-4";
        URI uri = URI.create(props.getEndpoint().replaceAll("/+$", "") + "/chat/completions");

        try {
            ObjectNode root = mapper.createObjectNode();
            root.put("model", model);

            ArrayNode messages = mapper.createArrayNode();
            ObjectNode system = mapper.createObjectNode();
            system.put("role", "system");
            system.put("content", "You are a router. Respond with a JSON object only, no surrounding text, containing keys: \"service\" (string), \"confidence\" (number between 0 and 1), \"explanation\" (string).");
            messages.add(system);

            ObjectNode user = mapper.createObjectNode();
            user.put("role", "user");
            user.put("content", ctx.payload());
            messages.add(user);

            root.set("messages", messages);

            String body = mapper.writeValueAsString(root);

            HttpRequest req = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofSeconds(Math.max(1, props.getTimeoutSeconds())))
                .header("Authorization", "Bearer " + props.getApiKey())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() / 100 != 2) {
                throw new LlmClientException("OpenAI request failed: " + resp.statusCode() + " " + resp.body());
            }

            JsonNode respRoot = mapper.readTree(resp.body());
            JsonNode choice = respRoot.path("choices").isArray() && respRoot.path("choices").size() > 0
                ? respRoot.path("choices").get(0)
                : null;

            String assistantText = null;
            if (choice != null) {
                assistantText = choice.path("message").path("content").asText(null);
            }

            if (assistantText == null) {
                // Fallback: try top-level text or entire response
                assistantText = resp.body();
            }

            assistantText = assistantText.trim();

            // Try parsing assistantText as JSON
            try {
                JsonNode decisionNode = mapper.readTree(assistantText);
                String service = decisionNode.path("service").asText("default-service");
                double confidence = decisionNode.path("confidence").asDouble(0.5);
                String explanation = decisionNode.path("explanation").asText("");
                return RoutingDecision.of(service, confidence, explanation);
            } catch (Exception e) {
                // Non-JSON reply: log and fall back
                log.warn("OpenAI returned non-JSON reply; falling back to heuristic parsing: {}", assistantText);
                // rudimentary heuristic: default service and confidence
                return RoutingDecision.of("default-service", 0.5, assistantText);
            }
        } catch (LlmClientException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new LlmClientException("OpenAI client error", ex);
        }
    }
}
