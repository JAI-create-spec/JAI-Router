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
     * LLM provider to use (builtin-ai, openai, or hybrid).
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

    /**
     * Hybrid routing configuration.
     */
    private Hybrid hybrid = new Hybrid();

    /**
     * Dijkstra routing configuration.
     */
    private Dijkstra dijkstra = new Dijkstra();

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

    public Hybrid getHybrid() {
        return hybrid;
    }

    public void setHybrid(Hybrid hybrid) {
        this.hybrid = hybrid != null ? hybrid : new Hybrid();
    }

    public Dijkstra getDijkstra() {
        return dijkstra;
    }

    public void setDijkstra(Dijkstra dijkstra) {
        this.dijkstra = dijkstra != null ? dijkstra : new Dijkstra();
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

    /**
     * Hybrid routing configuration.
     */
    public static class Hybrid {

        /**
         * Whether to enable hybrid routing (AI + Dijkstra).
         * Default: false
         */
        private boolean enabled = false;

        /**
         * Strategy selection mode (auto, ai-only, dijkstra-only).
         * Default: auto
         */
        private String mode = "auto";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }
    }

    /**
     * Dijkstra routing configuration.
     */
    public static class Dijkstra {

        /**
         * Whether to enable Dijkstra routing.
         * Default: false
         */
        private boolean enabled = false;

        /**
         * Source service ID for pathfinding.
         * Default: gateway
         */
        private String sourceService = "gateway";

        /**
         * Cache configuration.
         */
        private Cache cache = new Cache();

        /**
         * Edge weight configuration.
         */
        private Weights weights = new Weights();

        /**
         * Service edges configuration.
         */
        private List<Edge> edges = new ArrayList<>();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getSourceService() {
            return sourceService;
        }

        public void setSourceService(String sourceService) {
            this.sourceService = sourceService;
        }

        public Cache getCache() {
            return cache;
        }

        public void setCache(Cache cache) {
            this.cache = cache != null ? cache : new Cache();
        }

        public Weights getWeights() {
            return weights;
        }

        public void setWeights(Weights weights) {
            this.weights = weights != null ? weights : new Weights();
        }

        public List<Edge> getEdges() {
            return edges;
        }

        public void setEdges(List<Edge> edges) {
            this.edges = edges != null ? edges : new ArrayList<>();
        }

        /**
         * Cache configuration for Dijkstra paths.
         */
        public static class Cache {

            /**
             * Whether to enable path caching.
             * Default: true
             */
            private boolean enabled = true;

            /**
             * Maximum number of cached paths.
             * Default: 1000
             */
            private int maxSize = 1000;

            /**
             * Time-to-live for cached paths (in milliseconds).
             * Default: 300000 (5 minutes)
             */
            private long ttlMs = 300_000;

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public int getMaxSize() {
                return maxSize;
            }

            public void setMaxSize(int maxSize) {
                this.maxSize = maxSize;
            }

            public long getTtlMs() {
                return ttlMs;
            }

            public void setTtlMs(long ttlMs) {
                this.ttlMs = ttlMs;
            }
        }

        /**
         * Weight factors for edge calculation.
         */
        public static class Weights {

            /**
             * Weight factor for latency (0.0 - 1.0).
             * Default: 0.5
             */
            private double latency = 0.5;

            /**
             * Weight factor for cost (0.0 - 1.0).
             * Default: 0.3
             */
            private double cost = 0.3;

            /**
             * Weight factor for reliability (0.0 - 1.0).
             * Default: 0.2
             */
            private double reliability = 0.2;

            public double getLatency() {
                return latency;
            }

            public void setLatency(double latency) {
                this.latency = latency;
            }

            public double getCost() {
                return cost;
            }

            public void setCost(double cost) {
                this.cost = cost;
            }

            public double getReliability() {
                return reliability;
            }

            public void setReliability(double reliability) {
                this.reliability = reliability;
            }
        }

        /**
         * Service edge configuration.
         */
        public static class Edge {

            /**
             * Source service ID.
             */
            private String from;

            /**
             * Target service ID.
             */
            private String to;

            /**
             * Edge latency in milliseconds.
             */
            private double latency;

            /**
             * Edge cost (arbitrary units).
             */
            private double cost;

            /**
             * Edge reliability (0.0 - 1.0).
             */
            private double reliability = 0.99;

            public String getFrom() {
                return from;
            }

            public void setFrom(String from) {
                this.from = from;
            }

            public String getTo() {
                return to;
            }

            public void setTo(String to) {
                this.to = to;
            }

            public double getLatency() {
                return latency;
            }

            public void setLatency(double latency) {
                this.latency = latency;
            }

            public double getCost() {
                return cost;
            }

            public void setCost(double cost) {
                this.cost = cost;
            }

            public double getReliability() {
                return reliability;
            }

            public void setReliability(double reliability) {
                this.reliability = reliability;
            }
        }
    }
}
