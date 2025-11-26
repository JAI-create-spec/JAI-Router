package com.jai.router.starter.config;

import com.jai.router.core.registry.InMemoryServiceRegistry;
import com.jai.router.core.registry.ServiceRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceRegistryAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ServiceRegistry.class)
    public ServiceRegistry serviceRegistry() {
        return new InMemoryServiceRegistry();
    }
}

