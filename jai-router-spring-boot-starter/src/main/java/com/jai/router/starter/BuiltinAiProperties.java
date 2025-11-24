package com.jai.router.starter;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "jai.router.builtin")
public class BuiltinAiProperties {
    /** Map from keyword to service id */
    private Map<String, String> keywords;

    public Map<String, String> getKeywords() { return keywords; }
    public void setKeywords(Map<String, String> keywords) { this.keywords = keywords; }
}

