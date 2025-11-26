package io.jai.router.starter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.beans.factory.InitializingBean;

import java.util.Map;

/**
 * Configuration properties for the built-in AI router.
 * 
 * <p>Validates configuration at startup to fail-fast on invalid settings.
 */
//@Component  // removed: properties are registered via @EnableConfigurationProperties
@ConfigurationProperties(prefix = "jai.router.builtin")
public class BuiltinAiProperties implements InitializingBean {
    
    /** Map from keyword to service id */
    private Map<String, String> keywords;

    public Map<String, String> getKeywords() {
        return keywords;
    }

    public void setKeywords(Map<String, String> keywords) {
        this.keywords = keywords;
    }

    /**
     * Validate configuration at startup.
     * @throws IllegalArgumentException if configuration is invalid
     */
    @Override
    public void afterPropertiesSet() {
        if (keywords != null && !keywords.isEmpty()) {
            keywords.forEach((keyword, service) -> {
                if (keyword == null || keyword.isBlank()) {
                    throw new IllegalArgumentException(
                        "Keyword cannot be null or blank in jai.router.builtin.keywords"
                    );
                }
                if (service == null || service.isBlank()) {
                    throw new IllegalArgumentException(
                        String.format(
                            "Service name for keyword '%s' cannot be null or blank",
                            keyword
                        )
                    );
                }
            });
        }
    }
}
