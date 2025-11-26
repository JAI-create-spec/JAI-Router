
package io.jai.router.spring;

import io.jai.router.core.LlmClient;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class JAIRouterHealthIndicator implements HealthIndicator {

    private final LlmClient client;

    public JAIRouterHealthIndicator(LlmClient client) {
        this.client = client;
    }

    @Override
    public Health health() {
        try {
            // Basic check: call decideNullable with a short payload
            var opt = client.decideNullable(io.jai.router.core.DecisionContext.of("health check"));
            return opt.isPresent() ? Health.up().withDetail("provider", "available").build()
                                     : Health.down().withDetail("provider", "no-decision").build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}

