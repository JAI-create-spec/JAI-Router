package com.jai.router.starter;

import com.jai.router.core.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({BuiltinAiProperties.class, LlmProperties.class})
public class JaiRouterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(LlmClient.class)
    @ConditionalOnProperty(name = "jai.router.llm.provider", havingValue = "builtin-ai", matchIfMissing = true)
    public LlmClient builtinAiLlmClient(BuiltinAiProperties properties) {
        Map<String, String> keywords = properties.getKeywords();
        if (keywords == null || keywords.isEmpty()) {
            return new BuiltinAiLlmClient();
        }
        return new BuiltinAiLlmClient(keywords);
    }
}
