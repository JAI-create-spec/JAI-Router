package io.jai.router.spring;

import io.jai.router.core.LlmClient;
import io.jai.router.llm.BuiltinAiLlmClient;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

@Configuration
@EnableConfigurationProperties(JAIRouterProperties.class)
public class JAIRouterAutoConfiguration {

    private final JAIRouterProperties props;

    public JAIRouterAutoConfiguration(JAIRouterProperties props) {
        this.props = props;
    }

    @Bean
    @ConditionalOnMissingBean
    public LlmClient llmClient() {
        if (props.getServices() != null && !props.getServices().isEmpty()) {
            var map = new HashMap<String, String>();
            for (var s : props.getServices()) {
                if (s.getKeywords() != null) {
                    for (var kw : s.getKeywords()) {
                        map.put(kw, s.getId());
                    }
                }
            }
            return new BuiltinAiLlmClient(map);
        }
        return new BuiltinAiLlmClient();
    }
}
