package com.jai.router.starter;

import com.jai.router.core.LlmClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(OpenAiProperties.class)
public class OpenAiAutoConfiguration {

    @Bean
    @ConditionalOnProperty(name = "jai.router.llm.provider", havingValue = "openai")
    public LlmClient openAiLlmClient(OpenAiProperties openAiProperties, LlmProperties llmProperties) {
        return new OpenAiLlmClient(openAiProperties, llmProperties);
    }
}

