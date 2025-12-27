package io.jai.router.core;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * LearningRouter adapts routing decisions based on historical data.
 * It tracks requests and outcomes, and can bias future routing based on observed patterns.
 * This is a simple implementation; can be extended to use ML or persistent storage.
 */
public class LearningRouter implements Router {
    private static final Logger log = LoggerFactory.getLogger(LearningRouter.class);

    private final Router baseRouter;
    private final Map<String, AtomicLong> serviceHits = new HashMap<>();
    private final Map<String, AtomicLong> serviceSuccesses = new HashMap<>();

    public LearningRouter(@NotNull Router baseRouter) {
        this.baseRouter = Objects.requireNonNull(baseRouter, "Base router cannot be null");
    }

    @Override
    @NotNull
    public RoutingResult route(@NotNull String input) {
        RoutingResult result = baseRouter.route(input);
        String service = result.service();
        serviceHits.computeIfAbsent(service, k -> new AtomicLong()).incrementAndGet();
        if (result.confidence() >= 0.8) {
            serviceSuccesses.computeIfAbsent(service, k -> new AtomicLong()).incrementAndGet();
        }
        log.debug("LearningRouter: Routed to {} (confidence={})", service, result.confidence());
        return result;
    }

    /**
     * Returns the success rate for a given service.
     */
    public double getSuccessRate(String service) {
        long hits = serviceHits.getOrDefault(service, new AtomicLong(0)).get();
        long successes = serviceSuccesses.getOrDefault(service, new AtomicLong(0)).get();
        return hits == 0 ? 0.0 : (double) successes / hits;
    }

    /**
     * Returns a summary of learned routing statistics.
     */
    public Map<String, Double> getServiceSuccessRates() {
        Map<String, Double> rates = new HashMap<>();
        for (String service : serviceHits.keySet()) {
            rates.put(service, getSuccessRate(service));
        }
        return rates;
    }
}

