package io.jai.router.spring;

import io.jai.router.core.LlmClient;
import io.jai.router.core.Router;
import io.jai.router.core.RouterEngine;
import io.jai.router.graph.*;
import io.jai.router.llm.BuiltinAiLlmClient;
import io.jai.router.registry.InMemoryServiceRegistry;
import io.jai.router.registry.ServiceDefinition;
import io.jai.router.registry.ServiceRegistry;
import io.jai.router.spring.openai.OpenAiLlmClient;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Auto-configuration for JAI Router.
 * <p>
 * This configuration automatically sets up:
 * <ul>
 *   <li>Router engine with configured LLM client</li>
 *   <li>LLM client (BuiltinAi or OpenAI based on configuration)</li>
 *   <li>Service registry with configured services</li>
 *   <li>Health probe for monitoring</li>
 * </ul>
 * </p>
 *
 * <p>The LLM client is selected based on {@code jai.router.llm-provider} property:</p>
 * <ul>
 *   <li>{@code builtin-ai} (default) - Free keyword-based routing</li>
 *   <li>{@code openai} - AI-powered routing using OpenAI API (requires API key)</li>
 * </ul>
 *
 * @author JAI Router Team
 * @since 0.5.0
 */
@Configuration
@EnableScheduling
@EnableConfigurationProperties(JAIRouterProperties.class)
public class JAIRouterAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(JAIRouterAutoConfiguration.class);

    private final JAIRouterProperties props;

    public JAIRouterAutoConfiguration(JAIRouterProperties props) {
        this.props = props;
        log.info("Initializing JAI Router with provider: {}", props.getLlmProvider());
    }

    /**
     * Creates the main Router bean using the configured LLM client.
     * <p>
     * This is the primary entry point for routing requests.
     * </p>
     *
     * @param llmClientProvider provides the configured LLM client
     * @return configured Router instance
     */
    @Bean
    @ConditionalOnMissingBean
    public Router router(ObjectProvider<LlmClient> llmClientProvider) {
        LlmClient client = llmClientProvider.getIfAvailable();
        if (client == null) {
            log.warn("No LlmClient bean found, router may not function correctly");
            throw new IllegalStateException("No LlmClient configured. Please check your configuration.");
        }
        log.info("Creating Router with LLM client: {}", client.getClass().getSimpleName());
        return new RouterEngine(client);
    }

    /**
     * Creates a ServiceRegistry bean with configured services.
     * <p>
     * Services can be configured via {@code jai.router.services} property.
     * </p>
     *
     * @return configured ServiceRegistry
     */
    @Bean
    @ConditionalOnMissingBean
    public ServiceRegistry serviceRegistry() {
        List<ServiceDefinition> services = new ArrayList<>();

        if (props.getServices() != null && !props.getServices().isEmpty()) {
            for (JAIRouterProperties.Service s : props.getServices()) {
                if (s.getId() != null && !s.getId().isBlank()) {
                    services.add(ServiceDefinition.of(
                            s.getId(),
                            s.getDisplayName() != null ? s.getDisplayName() : s.getId(),
                            s.getKeywords() != null ? s.getKeywords() : new ArrayList<>()
                    ));
                }
            }
            log.info("Registered {} services from configuration", services.size());
        } else {
            log.info("No services configured, registry will be empty");
        }

        return new InMemoryServiceRegistry(services);
    }

    /**
     * Creates ServiceGraph for Dijkstra routing when enabled.
     * <p>
     * Activated when {@code jai.router.dijkstra.enabled=true}.
     * </p>
     *
     * @return configured ServiceGraph
     */
    @Bean
    @ConditionalOnProperty(name = "jai.router.dijkstra.enabled", havingValue = "true")
    @ConditionalOnMissingBean
    public ServiceGraph serviceGraph() {
        ServiceGraph graph = new ServiceGraph();
        JAIRouterProperties.Dijkstra dijkstra = props.getDijkstra();

        // Add services to graph
        if (props.getServices() != null) {
            for (JAIRouterProperties.Service service : props.getServices()) {
                if (service.getId() != null && !service.getId().isBlank()) {
                    Map<String, Object> metadata = new HashMap<>();
                    if (service.getDisplayName() != null) {
                        metadata.put("displayName", service.getDisplayName());
                    }
                    metadata.put("keywords", service.getKeywords());

                    graph.addService(service.getId(), metadata);
                }
            }
        }

        // Add edges to graph
        if (dijkstra.getEdges() != null) {
            for (JAIRouterProperties.Dijkstra.Edge edge : dijkstra.getEdges()) {
                if (edge.getFrom() != null && edge.getTo() != null) {
                    EdgeMetrics metrics = new EdgeMetrics(
                        edge.getLatency(),
                        edge.getCost(),
                        edge.getReliability()
                    );
                    graph.addEdge(edge.getFrom(), edge.getTo(), metrics);
                }
            }
        }

        log.info("Created ServiceGraph with {} services and {} edges",
                 graph.size(),
                 dijkstra.getEdges() != null ? dijkstra.getEdges().size() : 0);

        return graph;
    }

    /**
     * Creates DijkstraLlmClient when enabled.
     * <p>
     * Activated when {@code jai.router.dijkstra.enabled=true}.
     * </p>
     *
     * @param graphProvider provides the ServiceGraph
     * @return configured DijkstraLlmClient
     */
    @Bean
    @ConditionalOnProperty(name = "jai.router.dijkstra.enabled", havingValue = "true")
    @ConditionalOnMissingBean(name = "dijkstraLlmClient")
    public LlmClient dijkstraLlmClient(ObjectProvider<ServiceGraph> graphProvider) {
        ServiceGraph graph = graphProvider.getIfAvailable();
        if (graph == null) {
            throw new IllegalStateException("ServiceGraph is required for Dijkstra routing");
        }

        String sourceService = props.getDijkstra().getSourceService();
        log.info("Creating DijkstraLlmClient with source service: {}", sourceService);

        return new DijkstraLlmClient(graph, sourceService);
    }

    /**
     * Creates CachedDijkstraClient when caching is enabled.
     * <p>
     * Activated when {@code jai.router.dijkstra.enabled=true} and
     * {@code jai.router.dijkstra.cache.enabled=true}.
     * </p>
     *
     * @param dijkstraProvider provides the DijkstraLlmClient
     * @return configured CachedDijkstraClient
     */
    @Bean
    @ConditionalOnProperty(name = "jai.router.dijkstra.enabled", havingValue = "true")
    @ConditionalOnMissingBean(name = "cachedDijkstraLlmClient")
    public LlmClient cachedDijkstraLlmClient(ObjectProvider<LlmClient> dijkstraProvider) {
        // Check if caching is enabled (default true)
        boolean cacheEnabled = props.getDijkstra().getCache().isEnabled();
        if (!cacheEnabled) {
            return null; // Don't create cached version
        }
        // Find the DijkstraLlmClient specifically
        LlmClient dijkstraClient = dijkstraProvider.stream()
            .filter(client -> client instanceof DijkstraLlmClient)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("DijkstraLlmClient not found for caching"));

        JAIRouterProperties.Dijkstra.Cache cacheConfig = props.getDijkstra().getCache();

        log.info("Creating CachedDijkstraClient with maxSize: {}, ttl: {}ms",
                 cacheConfig.getMaxSize(), cacheConfig.getTtlMs());

        return new CachedDijkstraClient(
            dijkstraClient,
            cacheConfig.getMaxSize(),
            cacheConfig.getTtlMs()
        );
    }

    /**
     * Creates HybridLlmClient when hybrid routing is enabled.
     * <p>
     * Activated when {@code jai.router.llm-provider=hybrid}.
     * </p>
     *
     * @param providers provides all available LlmClients
     * @return configured HybridLlmClient
     */
    @Bean
    @ConditionalOnProperty(name = "jai.router.llm-provider", havingValue = "hybrid")
    @ConditionalOnMissingBean(name = "hybridLlmClient")
    public LlmClient hybridLlmClient(ObjectProvider<LlmClient> providers) {

        List<LlmClient> allClients = providers.stream().toList();

        // Get AI client (builtin or OpenAI)
        LlmClient aiClient = allClients.stream()
            .filter(client -> client instanceof BuiltinAiLlmClient || client instanceof OpenAiLlmClient)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No AI LlmClient found for hybrid routing"));

        // Get Dijkstra client (cached or uncached)
        LlmClient dijkstraClient = allClients.stream()
            .filter(client -> client instanceof CachedDijkstraClient || client instanceof DijkstraLlmClient)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No Dijkstra LlmClient found for hybrid routing"));

        log.info("Creating HybridLlmClient with AI: {} and Dijkstra: {}",
                 aiClient.getClass().getSimpleName(),
                 dijkstraClient.getClass().getSimpleName());

        return new HybridLlmClient(aiClient, dijkstraClient);
    }

    /**
     * Creates OpenAI LLM client when configured.
     * <p>
     * Activated when {@code jai.router.llm-provider=openai}.
     * Requires {@code jai.router.openai.api-key} to be set.
     * </p>
     *
     * @param meterProvider optional metrics registry
     * @return configured OpenAI client
     */
    @Bean
    @ConditionalOnProperty(name = "jai.router.llm-provider", havingValue = "openai")
    @ConditionalOnMissingBean(LlmClient.class)
    public LlmClient openAiLlmClient(ObjectProvider<MeterRegistry> meterProvider) {
        JAIRouterProperties.OpenAi openai = props.getOpenai();

        if (openai.getApiKey() == null || openai.getApiKey().isBlank()) {
            throw new IllegalStateException(
                    "OpenAI API key is required when using openai provider. " +
                    "Please set jai.router.openai.api-key property."
            );
        }

        MeterRegistry meterRegistry = meterProvider.getIfAvailable();

        log.info("Creating OpenAI LLM client with model: {}", openai.getModel());

        return new OpenAiLlmClient(
                openai.getApiKey(),
                openai.getModel(),
                openai.getTemperature(),
                openai.getMaxRetries(),
                openai.getTimeoutSeconds() * 1000,
                openai.getRetryBackoffMillis(),
                meterRegistry
        );
    }

    /**
     * Creates built-in AI LLM client (default, free option).
     * <p>
     * This client uses keyword matching for routing and doesn't require external API calls.
     * Activated when {@code jai.router.llm-provider=builtin-ai} or not specified.
     * </p>
     *
     * @param registryProvider optional service registry
     * @return configured built-in AI client
     */
    @Bean
    @ConditionalOnMissingBean(LlmClient.class)
    public LlmClient builtinLlmClient(ObjectProvider<ServiceRegistry> registryProvider) {
        double threshold = props.getConfidenceThreshold();
        Map<String, String> keywordMap = buildKeywordMap();
        ServiceRegistry registry = registryProvider.getIfAvailable();

        log.info("Creating BuiltinAi LLM client with {} keywords and confidence threshold: {}",
                keywordMap.size(), threshold);

        return new BuiltinAiLlmClient(keywordMap, threshold, registry);
    }

    /**
     * Creates health probe for monitoring router availability.
     *
     * @param clientProvider provides the configured LLM client
     * @return health probe instance
     */
    @Bean
    @ConditionalOnMissingBean
    public RouterHealthProbe routerHealthProbe(ObjectProvider<LlmClient> clientProvider) {
        return new RouterHealthProbe(clientProvider.getIfAvailable());
    }

    /**
     * Builds a keyword-to-service mapping from configured services.
     *
     * @return map of keywords to service IDs
     */
    private Map<String, String> buildKeywordMap() {
        Map<String, String> map = new HashMap<>();

        if (props.getServices() != null && !props.getServices().isEmpty()) {
            for (JAIRouterProperties.Service service : props.getServices()) {
                if (service.getId() != null && service.getKeywords() != null) {
                    for (String keyword : service.getKeywords()) {
                        if (keyword != null && !keyword.isBlank()) {
                            map.put(keyword.toLowerCase(), service.getId());
                        }
                    }
                }
            }
        }

        return map;
    }
}
