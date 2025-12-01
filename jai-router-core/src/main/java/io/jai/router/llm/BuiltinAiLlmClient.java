package io.jai.router.llm;

import io.jai.router.core.LlmClient;
import io.jai.router.core.RoutingDecision;
import io.jai.router.core.DecisionContext;
import io.jai.router.core.KeywordMatcher;
import io.jai.router.core.LlmClientException;
import io.jai.router.core.ScoringKeywordMatcher;
import io.jai.router.registry.ServiceRegistry;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;

/**
 * Lightweight built-in LLM client that uses keyword scoring for routing.
 * <p>
 * This is a <strong>free, zero-cost</strong> routing solution that doesn't require
 * external API calls or API keys. It uses keyword matching and scoring to determine
 * the best service for a given request.
 * </p>
 *
 * <p><strong>Features:</strong></p>
 * <ul>
 *   <li>No external dependencies or API costs</li>
 *   <li>Fast, local processing</li>
 *   <li>Configurable keyword mappings</li>
 *   <li>Dynamic service registry support</li>
 *   <li>Suitable for development and production</li>
 * </ul>
 *
 * <p><strong>Usage Example:</strong></p>
 * <pre>
 * Map&lt;String, String&gt; keywords = Map.of(
 *     "login", "auth-service",
 *     "report", "bi-service"
 * );
 * LlmClient client = new BuiltinAiLlmClient(keywords, 0.7);
 * RoutingDecision decision = client.decide(DecisionContext.of("user wants to login"));
 * // Returns: service="auth-service", confidence=0.85
 * </pre>
 *
 * @author JAI Router Team
 * @since 0.4.0
 * @see LlmClient
 * @see ScoringKeywordMatcher
 */
public final class BuiltinAiLlmClient implements LlmClient {

    /**
     * Default keyword-to-service mappings for common use cases.
     */
    private static final Map<String, String> DEFAULT_SERVICE_KEYWORDS = Map.of(
        "encrypt", "cryptography-service",
        "decrypt", "cryptography-service",
        "login", "auth-service",
        "token", "auth-service",
        "report", "bi-service",
        "dashboard", "bi-service",
        "kpi", "bi-service"
    );

    private static final Logger log = LoggerFactory.getLogger(BuiltinAiLlmClient.class);

    private final KeywordMatcher matcher;

    /**
     * Creates a client with default keywords and confidence threshold.
     * <p>
     * Uses {@link #DEFAULT_SERVICE_KEYWORDS} and 0.5 confidence threshold.
     * </p>
     */
    public BuiltinAiLlmClient() {
        this(DEFAULT_SERVICE_KEYWORDS, 0.5);
    }

    /**
     * Creates a client with custom keywords and default confidence.
     *
     * @param serviceKeywords map of keywords to service IDs
     * @throws LlmClientException if serviceKeywords is null
     */
    public BuiltinAiLlmClient(@NotNull Map<String, String> serviceKeywords) {
        this(serviceKeywords, 0.5);
    }

    /**
     * Creates a client with custom keywords and service registry.
     * <p>
     * Backwards-compatible constructor for registry integration.
     * </p>
     *
     * @param serviceKeywords map of keywords to service IDs
     * @param registry        service registry for dynamic updates (can be null)
     * @throws LlmClientException if serviceKeywords is null
     */
    public BuiltinAiLlmClient(
            @NotNull Map<String, String> serviceKeywords,
            ServiceRegistry registry
    ) {
        this(serviceKeywords, 0.5, registry);
    }

    /**
     * Creates a client with custom keywords and confidence threshold.
     *
     * @param serviceKeywords   map of keywords to service IDs, must not be null
     * @param defaultConfidence default confidence score for non-matches (0.0-1.0)
     * @throws LlmClientException if serviceKeywords is null
     */
    public BuiltinAiLlmClient(
            @NotNull Map<String, String> serviceKeywords,
            double defaultConfidence
    ) {
        Objects.requireNonNull(serviceKeywords, "serviceKeywords must not be null");
        this.matcher = new ScoringKeywordMatcher(
                serviceKeywords,
                "default-service",
                defaultConfidence
        );
        log.info("BuiltinAiLlmClient initialized with {} keywords", serviceKeywords.size());
    }

    /**
     * Creates a client with full configuration including registry support.
     * <p>
     * The matcher will bind to the registry to receive dynamic service updates.
     * This is the most flexible constructor for production use.
     * </p>
     *
     * @param serviceKeywords   map of keywords to service IDs, must not be null
     * @param defaultConfidence default confidence score for non-matches (0.0-1.0)
     * @param registry          service registry for dynamic updates (can be null)
     * @throws LlmClientException if serviceKeywords is null
     */
    public BuiltinAiLlmClient(
            @NotNull Map<String, String> serviceKeywords,
            double defaultConfidence,
            ServiceRegistry registry
    ) {
        Objects.requireNonNull(serviceKeywords, "serviceKeywords must not be null");

        ScoringKeywordMatcher m = new ScoringKeywordMatcher(
                serviceKeywords,
                "default-service",
                defaultConfidence
        );

        if (registry != null) {
            m.bindRegistry(registry);
            log.info("BuiltinAiLlmClient bound to ServiceRegistry");
        }

        this.matcher = m;
        log.info("BuiltinAiLlmClient initialized with {} keywords and confidence threshold: {}",
                serviceKeywords.size(), defaultConfidence);
    }

    /**
     * Makes a routing decision based on keyword matching.
     * <p>
     * This method analyzes the input text for keyword matches and returns
     * a decision with the best matching service and confidence score.
     * </p>
     *
     * @param ctx the decision context containing the input text
     * @return routing decision with service, confidence, and explanation
     * @throws LlmClientException if ctx is null
     */
    @Override
    @NotNull
    public RoutingDecision decide(@NotNull DecisionContext ctx) {
        if (ctx == null) {
            throw new LlmClientException("DecisionContext must not be null");
        }

        KeywordMatcher.MatchResult result = matcher.findBestMatch(ctx.payload());

        if (log.isDebugEnabled()) {
            log.debug("BuiltinAiLlmClient decided service='{}' confidence={} explanation='{}'",
                    result.service(), result.confidence(), result.explanation());
        }

        return RoutingDecision.of(
                result.service(),
                result.confidence(),
                result.explanation()
        );
    }

    /**
     * Returns the name of this LLM client implementation.
     *
     * @return "BuiltinAiLlmClient"
     */
    @Override
    @NotNull
    public String getName() {
        return "BuiltinAiLlmClient";
    }
}
