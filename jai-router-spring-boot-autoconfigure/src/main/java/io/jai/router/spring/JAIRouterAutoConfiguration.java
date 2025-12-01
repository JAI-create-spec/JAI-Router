package io.jai.router.spring;

import io.jai.router.core.LlmClient;
import io.jai.router.core.Router;
import io.jai.router.core.RouterEngine;
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
