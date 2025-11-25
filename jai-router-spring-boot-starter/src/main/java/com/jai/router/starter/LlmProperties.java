package com.jai.router.starter;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties to configure the LLM provider and model.
 *
 * <p>Defaults to the built-in offline provider. Users can override provider and
 * model via application.properties/yaml:
 *
 * jai.router.llm.provider = builtin-ai | openai | azure | ...
 * jai.router.llm.model = gpt-4 | gpt-4o | text-davinci-003 | ...
 */
@ConfigurationProperties(prefix = "jai.router.llm")
public class LlmProperties {

    /** LLM provider id. Defaults to builtin-ai (offline keyword matcher). */
    private String provider = "builtin-ai";

    /** Optional model identifier for remote providers. */
    private String model;

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}

