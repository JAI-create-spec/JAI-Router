
package com.jai.router.starter;

import com.jai.router.core.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JaiRouterAutoConfiguration {

    @Bean
    @ConditionalOnProperty(name = "jai.router.llm.provider", havingValue = "builtin-ai", matchIfMissing = true)
    public LlmClient builtinAiClient() {
        return new BuiltinAiLlmClient();
    }
}
