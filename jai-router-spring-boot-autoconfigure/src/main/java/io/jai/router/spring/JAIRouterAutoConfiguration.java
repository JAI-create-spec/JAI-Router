package io.jai.router.spring;

import io.jai.router.core.LlmClient;
import io.jai.router.llm.BuiltinAiLlmClient;
import io.jai.router.registry.ServiceRegistry;
import io.jai.router.spring.openai.OpenAiLlmClient;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import io.micrometer.core.instrument.MeterRegistry;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableScheduling
@EnableConfigurationProperties(JAIRouterProperties.class)
public class JAIRouterAutoConfiguration {

    private final JAIRouterProperties props;

    public JAIRouterAutoConfiguration(JAIRouterProperties props) {
        this.props = props;
    }

    @Bean
    @ConditionalOnProperty(name = "jai.router.llm-provider", havingValue = "openai")
    @ConditionalOnMissingBean
    public LlmClient openAiLlmClient(ObjectProvider<MeterRegistry> meterProvider) {
        var o = props.getOpenai();
        MeterRegistry mr = meterProvider.getIfAvailable();
        return new OpenAiLlmClient(o.getApiKey(), o.getModel(), o.getTemperature(), o.getMaxRetries(), o.getTimeoutSeconds() * 1000, o.getRetryBackoffMillis(), mr);
    }

    @Bean
    @ConditionalOnMissingBean
    public LlmClient builtinLlmClient(ObjectProvider<ServiceRegistry> registryProvider) {
        double threshold = props.getConfidenceThreshold();
        Map<String, String> map = Map.of();
        if (props.getServices() != null && !props.getServices().isEmpty()) {
            var m = new HashMap<String, String>();
            for (var s : props.getServices()) {
                if (s.getKeywords() != null) {
                    for (var kw : s.getKeywords()) {
                        m.put(kw, s.getId());
                    }
                }
            }
            map = m;
        }
        ServiceRegistry registry = registryProvider.getIfAvailable();
        return new BuiltinAiLlmClient(map, threshold, registry);
    }

    @Bean
    public RouterHealthProbe routerHealthProbe(ObjectProvider<LlmClient> clientProvider) {
        return new RouterHealthProbe(clientProvider.getIfAvailable());
    }
}
