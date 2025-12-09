package io.jai.router.spring.openai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jai.router.core.DecisionContext;
import io.jai.router.core.LlmClient;
import io.jai.router.core.RoutingDecision;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * OpenAI-powered LLM client for intelligent routing using GPT models.
 * <p>
 * <strong>PAID SERVICE - Requires OpenAI API Key and incurs costs</strong>
 * </p>
 *
 * <p><strong>Features:</strong></p>
 * <ul>
 *   <li>AI-powered routing with natural language understanding</li>
 *   <li>Circuit breaker pattern for fault tolerance</li>
 *   <li>Automatic retry with exponential backoff</li>
 *   <li>Metrics integration with Micrometer</li>
 *   <li>Configurable timeout and retry settings</li>
 * </ul>
 *
 * <p><strong>Requirements:</strong></p>
 * <ul>
 *   <li>OpenAI API key (get from <a href="https://platform.openai.com">OpenAI platform</a>)</li>
 *   <li>API costs apply per request</li>
 *   <li>Internet connectivity required</li>
 * </ul>
 *
 * <p><strong>Configuration Example:</strong></p>
 * <pre>
 * jai:
 *   router:
 *     llm-provider: openai
 *     openai:
 *       api-key: ${OPENAI_API_KEY}
 *       model: gpt-4o-mini
 *       temperature: 0.0
 *       max-retries: 2
 *       timeout-seconds: 30
 * </pre>
 *
 * <p><strong>Usage Example:</strong></p>
 * <pre>
 * MeterRegistry metrics = ...;
 * LlmClient client = new OpenAiLlmClient(
 *     "sk-your-api-key",
 *     "gpt-4o-mini",
 *     0.0,        // temperature
 *     2,          // max retries
 *     30000,      // timeout ms
 *     500,        // retry backoff ms
 *     metrics
 * );
 * RoutingDecision decision = client.decide(DecisionContext.of("user wants to login"));
 * </pre>
 *
 * <p><strong>Cost Considerations:</strong></p>
 * <ul>
 *   <li>Each routing decision makes 1 API call to OpenAI</li>
 *   <li>Costs depend on model chosen (gpt-4o-mini is cheapest)</li>
 *   <li>Failed requests are retried (may incur additional costs)</li>
 *   <li>Consider using BuiltinAiLlmClient for free alternative</li>
 * </ul>
 *
 * @author JAI Router Team
 * @since 0.5.0
 * @see LlmClient
 * @see io.jai.router.llm.BuiltinAiLlmClient
 */
public class OpenAiLlmClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAiLlmClient.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final HttpClient http;
    private final String apiKey;
    private final String model;
    private final double temperature;
    private final URI endpoint;
    private final int maxRetries;
    private final int retryBackoffMillis;
    private final int requestTimeoutMillis;

    // resilience
    private final Retry retry;
    private final CircuitBreaker circuitBreaker;

    // metrics
    private final Timer requestTimer;
    private final MeterRegistry registry;

    /**
     * Creates OpenAI client with default settings.
     *
     * @param apiKey      OpenAI API key (required), must not be null
     * @param model       GPT model name (e.g., "gpt-4o-mini")
     * @param temperature generation temperature (0.0-2.0, lower is more deterministic)
     */
    public OpenAiLlmClient(@NotNull String apiKey, String model, double temperature) {
        this(apiKey, model, temperature, 2, 30000, 500, null);
    }

    /**
     * Creates OpenAI client with full configuration.
     *
     * @param apiKey              OpenAI API key (required), must not be null
     * @param model               GPT model name (e.g., "gpt-4o-mini")
     * @param temperature         generation temperature (0.0-2.0)
     * @param maxRetries          maximum retry attempts for failed requests
     * @param timeoutMillis       request timeout in milliseconds
     * @param retryBackoffMillis  initial retry backoff delay in milliseconds
     * @param registry            optional Micrometer registry for metrics
     * @throws NullPointerException if apiKey is null
     */
    public OpenAiLlmClient(
            @NotNull String apiKey,
            String model,
            double temperature,
            int maxRetries,
            int timeoutMillis,
            int retryBackoffMillis,
            MeterRegistry registry
    ) {
        this.apiKey = Objects.requireNonNull(apiKey, "OpenAI apiKey is required");
        this.model = model == null ? "gpt-4o-mini" : model;
        this.temperature = temperature;
        this.maxRetries = Math.max(0, maxRetries);
        this.retryBackoffMillis = Math.max(0, retryBackoffMillis);
        this.requestTimeoutMillis = Math.max(1000, timeoutMillis);
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(Math.max(1000, timeoutMillis)))
                .build();
        this.endpoint = URI.create("https://api.openai.com/v1/chat/completions");

        // configure resilience4j retry and circuit breaker
        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(this.maxRetries + 1)
                .intervalFunction(IntervalFunction.ofExponentialBackoff(this.retryBackoffMillis, 2.0))
                .retryExceptions(RetryableOpenAiException.class)
                .failAfterMaxAttempts(true)
                .build();
        this.retry = Retry.of("openai-retry", retryConfig);

        CircuitBreakerConfig cbConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .slidingWindowSize(20)
                .build();
        this.circuitBreaker = CircuitBreaker.of("openai-cb", cbConfig);

        this.registry = registry;
        this.requestTimer = registry != null ? Timer.builder("jai_router_llm_request_latency_ms").publishPercentiles(0.5, 0.95).register(registry) : null;

        log.info("OpenAiLlmClient initialized with model: {}, maxRetries: {}, timeout: {}ms",
                this.model, this.maxRetries, this.requestTimeoutMillis);
    }

    /**
     * Makes an AI-powered routing decision using OpenAI's GPT model.
     * <p>
     * This method:
     * <ul>
     *   <li>Sends the input to OpenAI API</li>
     *   <li>Applies circuit breaker pattern for fault tolerance</li>
     *   <li>Automatically retries on transient failures</li>
     *   <li>Records metrics if MeterRegistry is configured</li>
     * </ul>
     * </p>
     *
     * @param ctx the decision context containing the input text
     * @return routing decision with service, confidence, and explanation
     * @throws NullPointerException if ctx is null
     * @throws RuntimeException     if API call fails after all retries
     */
    @Override
    @NotNull
    public RoutingDecision decide(@NotNull DecisionContext ctx) {
        Objects.requireNonNull(ctx, "DecisionContext is required");
        String prompt = buildPrompt(ctx.payload());
        String body;
        try {
            body = buildRequestBody(prompt);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to build request body", e);
        }

        Callable<RoutingDecision> call = () -> executeHttpCall(body);

        // wrap with circuitBreaker and retry
        Callable<RoutingDecision> decorated = CircuitBreaker.decorateCallable(circuitBreaker, call);
        decorated = Retry.decorateCallable(retry, decorated);

        try {
            if (requestTimer != null) {
                return requestTimer.recordCallable(decorated);
            } else {
                return decorated.call();
            }
        } catch (Exception e) {
            // record failure metric
            if (registry != null) registry.counter("jai_router_llm_failures_total").increment();
            throw new RuntimeException("OpenAI call failed", e);
        }
    }

    /**
     * Returns the name of this LLM client implementation.
     *
     * @return "OpenAiLlmClient"
     */
    @Override
    @NotNull
    public String getName() {
        return "OpenAiLlmClient (model: " + model + ")";
    }

    private RoutingDecision executeHttpCall(String body) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(endpoint)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .timeout(Duration.ofMillis(requestTimeoutMillis))
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        int status = resp.statusCode();
        if (status >= 200 && status < 300) {
            if (registry != null) registry.counter("jai_router_llm_requests_total").increment();
            return parseResponse(resp.body()).orElse(defaultDecision());
        }

        // For 429 and 5xx consider retrying via Resilience4j; throw a RetryableOpenAiException to trigger retry
        if (status == 429 || (status >= 500 && status < 600)) {
            throw new RetryableOpenAiException("OpenAI transient error: " + status + " body=" + resp.body());
        }

        // non-retryable
        throw new RuntimeException("OpenAI request failed: " + status + " body=" + resp.body());
    }

    // Package-private for unit testing
    static Optional<RoutingDecision> tryParseAssistantContent(String assistantText) {
        if (assistantText == null || assistantText.isBlank()) return Optional.empty();
        try {
            JsonNode parsed = MAPPER.readTree(assistantText);
            String service = parsed.path("service").asText(null);
            double confidence = parsed.path("confidence").asDouble(0.0);
            String explanation = parsed.path("explanation").asText("");
            if (service == null || service.isBlank()) return Optional.empty();
            return Optional.of(RoutingDecision.of(service, Math.max(0.0, Math.min(1.0, confidence)), explanation));
        } catch (JsonProcessingException e) {
            // fallback: try to extract a JSON object substring
            int first = assistantText.indexOf('{');
            int last = assistantText.lastIndexOf('}');
            if (first >= 0 && last > first) {
                String sub = assistantText.substring(first, last + 1);
                try {
                    JsonNode parsed = MAPPER.readTree(sub);
                    String service = parsed.path("service").asText(null);
                    double confidence = parsed.path("confidence").asDouble(0.0);
                    String explanation = parsed.path("explanation").asText("");
                    if (service == null || service.isBlank()) return Optional.empty();
                    return Optional.of(RoutingDecision.of(service, Math.max(0.0, Math.min(1.0, confidence)), explanation));
                } catch (JsonProcessingException ex) {
                    return Optional.empty();
                }
            }
            return Optional.empty();
        }
    }

    private Optional<RoutingDecision> parseResponse(String respBody) {
        try {
            JsonNode root = MAPPER.readTree(respBody);
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            if (content.isMissingNode()) return Optional.empty();
            String assistant = content.asText();

            return tryParseAssistantContent(assistant);
        } catch (JsonProcessingException e) {
            return Optional.empty();
        }
    }


    private RoutingDecision defaultDecision() {
        return RoutingDecision.of("none", 0.0, "openai-failed");
    }

    private String buildRequestBody(String prompt) throws JsonProcessingException {
        Map<String, Object> messageSys = Map.of("role", "system", "content", "You are a routing assistant. Respond ONLY with a JSON object containing: service (string), confidence (0.0-1.0), explanation (string). Do not include any other text.");
        Map<String, Object> messageUser = Map.of("role", "user", "content", prompt);
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", model);
        payload.put("messages", new Object[] { messageSys, messageUser });
        payload.put("temperature", temperature);
        return MAPPER.writeValueAsString(payload);
    }

    private String buildPrompt(String input) {
        return "Route the following request to the best matching service: \n\n" + input;
    }

    // Small marker exception to signal a transient error that should be retried by Resilience4j
    private static final class RetryableOpenAiException extends IOException {
        public RetryableOpenAiException(String message) {
            super(message);
        }
    }
}
