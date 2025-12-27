package io.jai.router.core.test;

import io.jai.router.core.DecisionContext;
import io.jai.router.core.LlmClient;
import io.jai.router.core.RoutingDecision;
import io.jai.router.core.RoutingResult;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utility class for testing JAI Router components.
 * <p>
 * This class provides helper methods and mock implementations for testing
 * routing functionality without requiring external dependencies or complex setup.
 * </p>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * // Create test context
 * DecisionContext ctx = RouterTestUtils.createTestContext("test input");
 *
 * // Create test decision
 * RoutingDecision decision = RouterTestUtils.createTestDecision("payment-service", 0.85);
 *
 * // Create mock client
 * LlmClient mockClient = RouterTestUtils.createMockClient("default-service");
 *
 * // Create configurable mock client
 * LlmClient configurableMock = RouterTestUtils.createConfigurableMockClient(
 *     Map.of("payment", "payment-service", "report", "analytics-service")
 * );
 * }</pre>
 *
 * @author JAI Router Team
 * @since 1.0.0
 */
public final class RouterTestUtils {

    private RouterTestUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Creates a test DecisionContext with the given payload.
     *
     * @param payload the input text for the context
     * @return a new DecisionContext instance
     * @throws NullPointerException     if payload is null
     * @throws IllegalArgumentException if payload is invalid
     */
    @NotNull
    public static DecisionContext createTestContext(@NotNull String payload) {
        return DecisionContext.of(payload);
    }

    /**
     * Creates a test RoutingDecision with the given service and confidence.
     *
     * @param service    the target service identifier
     * @param confidence the confidence score (0.0-1.0)
     * @return a new RoutingDecision instance
     */
    @NotNull
    public static RoutingDecision createTestDecision(@NotNull String service, double confidence) {
        return RoutingDecision.of(service, confidence, "Test decision");
    }

    /**
     * Creates a test RoutingDecision with custom explanation.
     *
     * @param service     the target service identifier
     * @param confidence  the confidence score (0.0-1.0)
     * @param explanation the explanation text
     * @return a new RoutingDecision instance
     */
    @NotNull
    public static RoutingDecision createTestDecision(
            @NotNull String service,
            double confidence,
            @NotNull String explanation) {
        return RoutingDecision.of(service, confidence, explanation);
    }

    /**
     * Creates a test RoutingResult with the given parameters.
     *
     * @param service          the target service identifier
     * @param confidence       the confidence score (0.0-1.0)
     * @param explanation      the explanation text
     * @param processingTimeMs processing time in milliseconds
     * @return a new RoutingResult instance
     */
    @NotNull
    public static RoutingResult createTestResult(
            @NotNull String service,
            double confidence,
            @NotNull String explanation,
            long processingTimeMs) {
        return new RoutingResult(service, confidence, explanation, processingTimeMs, Instant.now());
    }

    /**
     * Creates a test RoutingResult with default values.
     *
     * @param service the target service identifier
     * @return a new RoutingResult instance
     */
    @NotNull
    public static RoutingResult createTestResult(@NotNull String service) {
        return createTestResult(service, 0.8, "Test result", 10L);
    }

    /**
     * Creates a simple mock LlmClient that always returns the same service.
     * <p>
     * This is useful for testing scenarios where you need a predictable
     * routing decision without complex logic.
     * </p>
     *
     * @param defaultService the service to always return
     * @return a mock LlmClient implementation
     */
    @NotNull
    public static LlmClient createMockClient(@NotNull String defaultService) {
        Objects.requireNonNull(defaultService, "defaultService cannot be null");
        return ctx -> RoutingDecision.of(defaultService, 0.8, "Mock decision");
    }

    /**
     * Creates a mock LlmClient with configurable service and confidence.
     *
     * @param service    the service to return
     * @param confidence the confidence score to return
     * @return a mock LlmClient implementation
     */
    @NotNull
    public static LlmClient createMockClient(@NotNull String service, double confidence) {
        Objects.requireNonNull(service, "service cannot be null");
        return ctx -> RoutingDecision.of(service, confidence, "Mock decision");
    }

    /**
     * Creates a configurable mock LlmClient that routes based on keyword matching.
     * <p>
     * This mock client checks if the input contains any of the configured keywords
     * and routes to the corresponding service. If no keywords match, it returns
     * a default service.
     * </p>
     *
     * @param keywordToServiceMap map of keywords to service identifiers
     * @return a configurable mock LlmClient implementation
     */
    @NotNull
    public static LlmClient createConfigurableMockClient(@NotNull Map<String, String> keywordToServiceMap) {
        Objects.requireNonNull(keywordToServiceMap, "keywordToServiceMap cannot be null");
        return ctx -> {
            String input = ctx.payload().toLowerCase();
            for (Map.Entry<String, String> entry : keywordToServiceMap.entrySet()) {
                if (input.contains(entry.getKey().toLowerCase())) {
                    return RoutingDecision.of(entry.getValue(), 0.85,
                            "Matched keyword: " + entry.getKey());
                }
            }
            return RoutingDecision.of("default-service", 0.5, "No keywords matched");
        };
    }

    /**
     * Creates a mock LlmClient that fails after a specified number of attempts.
     * <p>
     * This is useful for testing retry logic and error handling.
     * </p>
     *
     * @param failuresBeforeSuccess number of failures before returning success
     * @param successService        the service to return on success
     * @return a failing mock LlmClient implementation
     */
    @NotNull
    public static LlmClient createFailingMockClient(int failuresBeforeSuccess, @NotNull String successService) {
        Objects.requireNonNull(successService, "successService cannot be null");
        AtomicInteger attemptCount = new AtomicInteger(0);

        return ctx -> {
            int attempt = attemptCount.incrementAndGet();
            if (attempt <= failuresBeforeSuccess) {
                throw new RuntimeException("Mock failure #" + attempt);
            }
            return RoutingDecision.of(successService, 0.9, "Success after " + failuresBeforeSuccess + " failures");
        };
    }

    /**
     * Creates a mock LlmClient that always throws an exception.
     * <p>
     * This is useful for testing error handling and fallback scenarios.
     * </p>
     *
     * @param errorMessage the error message to include in the exception
     * @return a failing mock LlmClient implementation
     */
    @NotNull
    public static LlmClient createAlwaysFailingMockClient(@NotNull String errorMessage) {
        Objects.requireNonNull(errorMessage, "errorMessage cannot be null");
        return ctx -> {
            throw new RuntimeException(errorMessage);
        };
    }

    /**
     * Creates a mock LlmClient with simulated latency.
     * <p>
     * This is useful for testing timeout handling and performance scenarios.
     * </p>
     *
     * @param delayMs the delay in milliseconds before returning a decision
     * @param service the service to return
     * @return a delayed mock LlmClient implementation
     */
    @NotNull
    public static LlmClient createDelayedMockClient(long delayMs, @NotNull String service) {
        Objects.requireNonNull(service, "service cannot be null");
        return ctx -> {
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted during delay", e);
            }
            return RoutingDecision.of(service, 0.8, "Delayed decision (" + delayMs + "ms)");
        };
    }

    /**
     * Creates a mock LlmClient that returns different services based on call count.
     * <p>
     * This is useful for testing scenarios where routing decisions change over time.
     * </p>
     *
     * @param services array of services to return in sequence (cycles after reaching the end)
     * @return a cycling mock LlmClient implementation
     */
    @NotNull
    public static LlmClient createCyclingMockClient(@NotNull String... services) {
        Objects.requireNonNull(services, "services cannot be null");
        if (services.length == 0) {
            throw new IllegalArgumentException("At least one service must be provided");
        }

        AtomicInteger callCount = new AtomicInteger(0);

        return ctx -> {
            int index = callCount.getAndIncrement() % services.length;
            String service = services[index];
            return RoutingDecision.of(service, 0.8, "Cycling decision #" + (index + 1));
        };
    }

    /**
     * Validates that a RoutingResult has expected values.
     *
     * @param result            the result to validate
     * @param expectedService   the expected service identifier
     * @param minConfidence     minimum expected confidence
     * @param maxConfidence     maximum expected confidence
     * @throws AssertionError if validation fails
     */
    public static void assertRoutingResult(
            @NotNull RoutingResult result,
            @NotNull String expectedService,
            double minConfidence,
            double maxConfidence) {
        Objects.requireNonNull(result, "result cannot be null");
        Objects.requireNonNull(expectedService, "expectedService cannot be null");

        if (!expectedService.equals(result.service())) {
            throw new AssertionError(
                    String.format("Expected service '%s' but got '%s'", expectedService, result.service())
            );
        }

        if (result.confidence() < minConfidence || result.confidence() > maxConfidence) {
            throw new AssertionError(
                    String.format("Confidence %.2f is not in range [%.2f, %.2f]",
                            result.confidence(), minConfidence, maxConfidence)
            );
        }
    }

    /**
     * Validates that a RoutingDecision has expected values.
     *
     * @param decision          the decision to validate
     * @param expectedService   the expected service identifier
     * @param minConfidence     minimum expected confidence
     * @throws AssertionError if validation fails
     */
    public static void assertRoutingDecision(
            @NotNull RoutingDecision decision,
            @NotNull String expectedService,
            double minConfidence) {
        Objects.requireNonNull(decision, "decision cannot be null");
        Objects.requireNonNull(expectedService, "expectedService cannot be null");

        if (!expectedService.equals(decision.service())) {
            throw new AssertionError(
                    String.format("Expected service '%s' but got '%s'", expectedService, decision.service())
            );
        }

        if (decision.confidence() < minConfidence) {
            throw new AssertionError(
                    String.format("Confidence %.2f is below minimum %.2f",
                            decision.confidence(), minConfidence)
            );
        }
    }
}
