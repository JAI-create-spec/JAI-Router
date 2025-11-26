package io.jai.router.starter;

/**
 * OpenAI provider configuration. API key must be provided by the user when enabling OpenAI.
 */
public class OpenAiProperties {
    /** The API key for OpenAI (or compatible service). */
    private String apiKey;

    /** Base URL for the OpenAI API (without trailing slash). Optional override. */
    private String endpoint = "https://api.openai.com/v1";

    /** Timeout in seconds for network calls. */
    private int timeoutSeconds = 10;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }
}
