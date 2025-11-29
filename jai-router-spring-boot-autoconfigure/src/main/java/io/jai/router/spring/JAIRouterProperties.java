package io.jai.router.spring;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jai.router")
public class JAIRouterProperties {
    private String llmProvider = "builtin-ai";
    private double confidenceThreshold = 0.7;
    private List<Service> services;
    private OpenAi openai = new OpenAi();

    public String getLlmProvider() { return llmProvider; }
    public void setLlmProvider(String llmProvider) { this.llmProvider = llmProvider; }

    public double getConfidenceThreshold() { return confidenceThreshold; }
    public void setConfidenceThreshold(double confidenceThreshold) { this.confidenceThreshold = confidenceThreshold; }

    public List<Service> getServices() { return services; }
    public void setServices(List<Service> services) { this.services = services; }

    public OpenAi getOpenai() { return openai; }
    public void setOpenai(OpenAi openai) { this.openai = openai; }

    public static class Service {
        private String id;
        private String displayName;
        private List<String> keywords;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
        public List<String> getKeywords() { return keywords; }
        public void setKeywords(List<String> keywords) { this.keywords = keywords; }
    }

    public static class OpenAi {
        private String apiKey;
        private String model = "gpt-4o-mini";
        private double temperature = 0.0;
        private int maxRetries = 2;
        private int timeoutSeconds = 30;
        private int retryBackoffMillis = 500;

        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }

        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }

        public double getTemperature() { return temperature; }
        public void setTemperature(double temperature) { this.temperature = temperature; }

        public int getMaxRetries() { return maxRetries; }
        public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }

        public int getTimeoutSeconds() { return timeoutSeconds; }
        public void setTimeoutSeconds(int timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }

        public int getRetryBackoffMillis() { return retryBackoffMillis; }
        public void setRetryBackoffMillis(int retryBackoffMillis) { this.retryBackoffMillis = retryBackoffMillis; }
    }
}
