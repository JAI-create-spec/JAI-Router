package io.jai.router.spring;

import io.jai.router.core.LlmClient;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class JAIRouterHealthIndicator implements HealthIndicator {

    private final RouterHealthProbe probe;

    public JAIRouterHealthIndicator(RouterHealthProbe probe) {
        this.probe = probe;
    }

    @Override
    public Health health() {
        if (probe == null) {
            return Health.down().withDetail("provider", "none").build();
        }
        boolean ok = probe.isHealthy();
        return ok ? Health.up().withDetail("provider", "available").withDetail("lastChecked", probe.getLastChecked()).build()
                  : Health.down().withDetail("provider", "no-decision").withDetail("lastChecked", probe.getLastChecked()).build();
    }
}
