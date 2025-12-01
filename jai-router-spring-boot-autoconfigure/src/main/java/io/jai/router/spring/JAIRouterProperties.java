package io.jai.router.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for JAI Router.
 * <p>
 * Configures LLM providers, routing confidence thresholds, and service definitions.
 * </p>
 *
 * <p>Example configuration:</p>
 * <pre>
 * jai:
 *   router:
 *     llm-provider: openai
 *     confidence-threshold: 0.7
 *     openai:
 *       api-key: ${OPENAI_API_KEY}
 *       model: gpt-4o-mini
 *     services:
 *       - id: auth-service
 *         display-name: Authentication
 *         keywords: [login, token, auth]
 * </pre>
 *
 * @author JAI Router Team
 * @since 0.5.0
 */
@ConfigurationProperties(prefix = "jai.router")
public class JAIRouterProperties {

    /**
     * LLM provider to use (builtin-ai or openai).
     * Default: builtin-ai
     */
    private String llmProvider = "builtin-ai";

    /**
     * Minimum confidence threshold for routing decisions (0.0 - 1.0).
     * Default: 0.7
     */
    private double confidenceThreshold = 0.7;

    /**
     * List of service definitions for routing.
     */
    private List<Service> services = new ArrayList<>();

    /**
     * OpenAI-specific configuration.
     */
    private OpenAi openai = new OpenAi();

    public String getLlmProvider() {
        return llmProvider;
    }

    public void setLlmProvider(String llmProvider) {
        this.llmProvider = llmProvider;
    }

    public double getConfidenceThreshold() {
        return confidenceThreshold;
    }

    public void setConfidenceThreshold(double confidenceThreshold) {
        this.confidenceThreshold = confidenceThreshold;
    }

    public List<Service> getServices() {
        return services;
    }

    public void setServices(List<Service> services) {
        this.services = services != null ? services : new ArrayList<>();
    }

    public OpenAi getOpenai() {
        return openai;
    }

    public void setOpenai(OpenAi openai) {
        this.openai = openai != null ? openai : new OpenAi();
    }

    /**
     * Service definition configuration.
     */
    public static class Service {

        /**
         * Unique service identifier (required).
         */
        private String id;

        /**
         * Human-readable service name (required).
         */
        private String displayName;

        /**
         * Keywords associated with this service.
         */
        private List<String> keywords = new ArrayList<>();

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public List<String> getKeywords() {
            return keywords;
        }

        public void setKeywords(List<String> keywords) {
            this.keywords = keywords != null ? keywords : new ArrayList<>();
        }
    }

    /**
     * OpenAI LLM provider configuration.
     */
    public static class OpenAi {

        /**
         * OpenAI API key (required when using openai provider).
         */
        private String apiKey;

        /**
         * OpenAI model to use (e.g., gpt-4o-mini, gpt-4, gpt-3.5-turbo).
         * Default: gpt-4o-mini
         */
        private String model = "gpt-4o-mini";

        /**
         * Temperature for response generation (0.0 - 2.0).
         * Lower values make output more deterministic.
         * Default: 0.0
         */
        private double temperature = 0.0;

        /**
         * Maximum number of retry attempts for failed requests.
         * Default: 2
         */
        private int maxRetries = 2;

        /**
         * Request timeout in seconds.
         * Default: 30
         */
        private int timeoutSeconds = 30;

        /**
         * Initial backoff delay in milliseconds for retries.
         * Default: 500
         */
        private int retryBackoffMillis = 500;

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public double getTemperature() {
            return temperature;
        }

        public void setTemperature(double temperature) {
            this.temperature = temperature;
        }

        public int getMaxRetries() {
            return maxRetries;
        }

        public void setMaxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
        }

        public int getTimeoutSeconds() {
            return timeoutSeconds;
        }

        public void setTimeoutSeconds(int timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
        }

        public int getRetryBackoffMillis() {
            return retryBackoffMillis;
        }

        public void setRetryBackoffMillis(int retryBackoffMillis) {
            this.retryBackoffMillis = retryBackoffMillis;
        }
    }
}
