package io.jai.router.core;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Router that performs A/B testing by routing a percentage of traffic to alternative services.
 * <p>
 * This implementation allows you to test new routing strategies or services by
 * directing a configurable percentage of traffic to alternative destinations.
 * This is useful for gradual rollouts, canary deployments, and comparing
 * routing strategies.
 * </p>
 *
 * <h2>Features</h2>
 * <ul>
 *   <li>Configurable traffic splitting percentages</li>
 *   <li>Multiple concurrent A/B tests</li>
 *   <li>Traffic distribution statistics</li>
 *   <li>Thread-safe test management</li>
 *   <li>Deterministic or random splitting</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * Router baseRouter = new RouterEngine(llmClient);
 * ABTestingRouter testRouter = new ABTestingRouter(baseRouter);
 *
 * // Route 20% of payment-service traffic to new-payment-service
 * testRouter.addTest("payment-service", "new-payment-service", 0.20);
 *
 * // Route 10% of analytics traffic to experimental-analytics
 * testRouter.addTest("analytics-service", "experimental-analytics", 0.10);
 *
 * // Check test results
 * ABTestStats stats = testRouter.getTestStats("payment-service");
 * System.out.println("Test traffic: " + stats.testTrafficPercentage() + "%");
 * }</pre>
 *
 * @author JAI Router Team
 * @since 1.0.0
 */
public class ABTestingRouter implements Router {

    private static final Logger log = LoggerFactory.getLogger(ABTestingRouter.class);

    private final Router delegate;
    private final Map<String, ABTest> activeTests;
    private final Random random;
    private final Map<String, TestMetrics> metrics;

    /**
     * Creates an A/B testing router with a new random instance.
     *
     * @param delegate the underlying router to wrap
     * @throws NullPointerException if delegate is null
     */
    public ABTestingRouter(@NotNull Router delegate) {
        this(delegate, new Random());
    }

    /**
     * Creates an A/B testing router with a specific random instance.
     * <p>
     * Using a seeded Random allows for deterministic testing.
     * </p>
     *
     * @param delegate the underlying router to wrap
     * @param random   random instance for traffic splitting
     * @throws NullPointerException if delegate or random is null
     */
    public ABTestingRouter(@NotNull Router delegate, @NotNull Random random) {
        this.delegate = Objects.requireNonNull(delegate, "Delegate router cannot be null");
        this.random = Objects.requireNonNull(random, "Random instance cannot be null");
        this.activeTests = new ConcurrentHashMap<>();
        this.metrics = new ConcurrentHashMap<>();
        log.info("ABTestingRouter initialized");
    }

    /**
     * Adds an A/B test for a specific service.
     *
     * @param originalService  the original service to test
     * @param testService      the alternative service to test
     * @param testPercentage   percentage of traffic to route to test service (0.0-1.0)
     * @return this router instance
     * @throws NullPointerException     if originalService or testService is null
     * @throws IllegalArgumentException if testPercentage is not between 0.0 and 1.0
     */
    @NotNull
    public ABTestingRouter addTest(
            @NotNull String originalService,
            @NotNull String testService,
            double testPercentage) {
        Objects.requireNonNull(originalService, "Original service cannot be null");
        Objects.requireNonNull(testService, "Test service cannot be null");

        if (testPercentage < 0.0 || testPercentage > 1.0) {
            throw new IllegalArgumentException(
                    String.format("Test percentage must be between 0.0 and 1.0, got: %.2f", testPercentage)
            );
        }

        ABTest test = new ABTest(testService, testPercentage);
        activeTests.put(originalService, test);
        metrics.put(originalService, new TestMetrics());

        log.info("A/B test added: {} -> {} ({}% traffic)",
                originalService, testService, testPercentage * 100);

        return this;
    }

    /**
     * Removes an A/B test for a specific service.
     *
     * @param originalService the service to stop testing
     * @return true if a test was removed, false if no test existed
     */
    public boolean removeTest(@NotNull String originalService) {
        Objects.requireNonNull(originalService, "Original service cannot be null");

        ABTest removed = activeTests.remove(originalService);
        if (removed != null) {
            log.info("A/B test removed: {} -> {}", originalService, removed.testService);
            return true;
        }
        return false;
    }

    /**
     * Removes all active A/B tests.
     */
    public void clearAllTests() {
        int count = activeTests.size();
        activeTests.clear();
        log.info("All A/B tests cleared ({} tests removed)", count);
    }

    /**
     * Routes the input, potentially redirecting to test service based on A/B test configuration.
     *
     * @param input the request content to route, must not be null
     * @return routing result (possibly redirected to test service)
     * @throws NullPointerException if input is null
     */
    @Override
    @NotNull
    public RoutingResult route(@NotNull String input) {
        Objects.requireNonNull(input, "Input cannot be null");

        // Get routing decision from delegate
        RoutingResult result = delegate.route(input);

        // Check if there's an active A/B test for this service
        ABTest test = activeTests.get(result.service());
        if (test == null) {
            return result;
        }

        // Get or create metrics for this test
        TestMetrics testMetrics = metrics.computeIfAbsent(result.service(), k -> new TestMetrics());
        testMetrics.totalRequests.incrementAndGet();

        // Determine if this request should go to the test service
        boolean useTestService = random.nextDouble() < test.testPercentage;

        if (useTestService) {
            testMetrics.testRequests.incrementAndGet();

            if (log.isDebugEnabled()) {
                log.debug("A/B test: routing to {} instead of {} (test traffic: {:.2f}%)",
                        test.testService, result.service(),
                        testMetrics.getTestTrafficPercentage() * 100);
            }

            return new RoutingResult(
                    test.testService,
                    result.confidence(),
                    String.format("A/B test variant (%.0f%% traffic): %s",
                            test.testPercentage * 100, result.explanation()),
                    result.processingTimeMs(),
                    result.timestamp()
            );
        } else {
            testMetrics.controlRequests.incrementAndGet();

            if (log.isTraceEnabled()) {
                log.trace("A/B test: routing to control service {} (control traffic: {:.2f}%)",
                        result.service(),
                        testMetrics.getControlTrafficPercentage() * 100);
            }

            return result;
        }
    }

    /**
     * Returns statistics for a specific A/B test.
     *
     * @param originalService the service being tested
     * @return test statistics, or null if no test exists
     */
    @NotNull
    public ABTestStats getTestStats(@NotNull String originalService) {
        Objects.requireNonNull(originalService, "Original service cannot be null");

        ABTest test = activeTests.get(originalService);
        if (test == null) {
            throw new IllegalArgumentException("No A/B test found for service: " + originalService);
        }

        TestMetrics testMetrics = metrics.get(originalService);
        if (testMetrics == null) {
            testMetrics = new TestMetrics();
        }

        return new ABTestStats(
                originalService,
                test.testService,
                test.testPercentage,
                testMetrics.totalRequests.get(),
                testMetrics.controlRequests.get(),
                testMetrics.testRequests.get(),
                testMetrics.getControlTrafficPercentage(),
                testMetrics.getTestTrafficPercentage()
        );
    }

    /**
     * Returns statistics for all active A/B tests.
     *
     * @return map of service names to test statistics
     */
    @NotNull
    public Map<String, ABTestStats> getAllTestStats() {
        Map<String, ABTestStats> allStats = new ConcurrentHashMap<>();
        for (String service : activeTests.keySet()) {
            allStats.put(service, getTestStats(service));
        }
        return allStats;
    }

    /**
     * Resets metrics for a specific test.
     *
     * @param originalService the service to reset metrics for
     */
    public void resetTestMetrics(@NotNull String originalService) {
        Objects.requireNonNull(originalService, "Original service cannot be null");
        metrics.put(originalService, new TestMetrics());
        log.info("Metrics reset for A/B test: {}", originalService);
    }

    /**
     * Resets metrics for all tests.
     */
    public void resetAllMetrics() {
        metrics.clear();
        log.info("All A/B test metrics reset");
    }

    /**
     * Returns the number of active A/B tests.
     *
     * @return number of active tests
     */
    public int getActiveTestCount() {
        return activeTests.size();
    }

    /**
     * Internal record representing an A/B test configuration.
     */
    private record ABTest(
            @NotNull String testService,
            double testPercentage
    ) {
    }

    /**
     * Internal class for tracking test metrics.
     */
    private static class TestMetrics {
        final AtomicLong totalRequests = new AtomicLong(0);
        final AtomicLong controlRequests = new AtomicLong(0);
        final AtomicLong testRequests = new AtomicLong(0);

        double getControlTrafficPercentage() {
            long total = totalRequests.get();
            return total == 0 ? 0.0 : (double) controlRequests.get() / total;
        }

        double getTestTrafficPercentage() {
            long total = totalRequests.get();
            return total == 0 ? 0.0 : (double) testRequests.get() / total;
        }
    }

    /**
     * Immutable statistics for an A/B test.
     *
     * @param originalService         the original service being tested
     * @param testService             the alternative test service
     * @param configuredTestPercentage configured percentage for test traffic (0.0-1.0)
     * @param totalRequests           total number of requests
     * @param controlRequests         requests routed to control (original) service
     * @param testRequests            requests routed to test service
     * @param actualControlPercentage actual percentage of control traffic (0.0-1.0)
     * @param actualTestPercentage    actual percentage of test traffic (0.0-1.0)
     */
    public record ABTestStats(
            @NotNull String originalService,
            @NotNull String testService,
            double configuredTestPercentage,
            long totalRequests,
            long controlRequests,
            long testRequests,
            double actualControlPercentage,
            double actualTestPercentage
    ) {
        public ABTestStats {
            Objects.requireNonNull(originalService, "originalService cannot be null");
            Objects.requireNonNull(testService, "testService cannot be null");
        }

        @Override
        public String toString() {
            return String.format(
                    "ABTestStats[%s -> %s: configured=%.1f%%, actual=%.1f%%, " +
                            "total=%d, control=%d, test=%d]",
                    originalService, testService,
                    configuredTestPercentage * 100, actualTestPercentage * 100,
                    totalRequests, controlRequests, testRequests
            );
        }

        /**
         * Returns true if the actual test percentage is within tolerance of the configured percentage.
         *
         * @param tolerance acceptable deviation (e.g., 0.05 for 5%)
         * @return true if within tolerance
         */
        public boolean isWithinTolerance(double tolerance) {
            if (totalRequests < 100) {
                return true; // Not enough data to judge
            }
            double deviation = Math.abs(actualTestPercentage - configuredTestPercentage);
            return deviation <= tolerance;
        }
    }
}
