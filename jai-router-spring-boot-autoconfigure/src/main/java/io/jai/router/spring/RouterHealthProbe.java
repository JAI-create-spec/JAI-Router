package io.jai.router.spring;

import io.jai.router.core.LlmClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class RouterHealthProbe {
    private static final Logger log = LoggerFactory.getLogger(RouterHealthProbe.class);
    private final LlmClient client;
    private volatile boolean healthy = false;
    private volatile Instant lastChecked = Instant.EPOCH;

    public RouterHealthProbe(LlmClient client) {
        this.client = client;
    }

    @Scheduled(fixedDelayString = "${jai.router.health.probe-interval-ms:15000}")
    public void probe() {
        if (client == null) {
            healthy = false;
            lastChecked = Instant.now();
            return;
        }
        try {
            CompletableFuture<Boolean> f = CompletableFuture.supplyAsync(() -> {
                try {
                    var opt = client.decideNullable(io.jai.router.core.DecisionContext.of("health check"));
                    return opt != null && opt.isPresent();
                } catch (Exception e) {
                    return false;
                }
            });
            healthy = f.get(2, TimeUnit.SECONDS);
            lastChecked = Instant.now();
        } catch (Exception e) {
            healthy = false;
            lastChecked = Instant.now();
            log.debug("Router health probe failed", e);
        }
    }

    public boolean isHealthy() { return healthy; }
    public Instant getLastChecked() { return lastChecked; }
}
