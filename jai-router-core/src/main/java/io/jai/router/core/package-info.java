/**
 * Core routing engine and interfaces for JAI Router.
 * <p>
 * This package provides the fundamental abstractions for intelligent request routing
 * in microservice architectures. It enables content-based routing decisions using
 * various strategies including keyword matching, machine learning models, and
 * external LLM services.
 * </p>
 *
 * <h2>Core Components</h2>
 * <ul>
 *   <li>{@link io.jai.router.core.Router} - Main API entry point for routing requests</li>
 *   <li>{@link io.jai.router.core.RouterEngine} - Default implementation with configurable retry logic</li>
 *   <li>{@link io.jai.router.core.LlmClient} - Strategy interface for routing decision logic</li>
 *   <li>{@link io.jai.router.core.RoutingResult} - Immutable result containing service, confidence, and metrics</li>
 *   <li>{@link io.jai.router.core.RoutingDecision} - Internal decision representation from LLM clients</li>
 *   <li>{@link io.jai.router.core.DecisionContext} - Validated input context for routing decisions</li>
 *   <li>{@link io.jai.router.core.KeywordMatcher} - Fast keyword-based routing strategy</li>
 *   <li>{@link io.jai.router.core.ScoringKeywordMatcher} - Configurable keyword matcher with scoring</li>
 * </ul>
 *
 * <h2>Quick Start Example</h2>
 * <pre>{@code
 * // Create a simple router with keyword-based routing
 * Map<String, String> keywords = Map.of(
 *     "payment", "payment-service",
 *     "invoice", "payment-service",
 *     "report", "analytics-service",
 *     "dashboard", "analytics-service"
 * );
 *
 * KeywordMatcher matcher = new ScoringKeywordMatcher(keywords, "default-service", 0.5);
 * LlmClient client = new BuiltinAiLlmClient(matcher);
 * Router router = new RouterEngine(client);
 *
 * // Route a request
 * RoutingResult result = router.route("Generate quarterly sales report");
 * System.out.println("Service: " + result.service());
 * System.out.println("Confidence: " + result.confidence());
 * System.out.println("Processing time: " + result.processingTimeMs() + "ms");
 * }</pre>
 *
 * <h2>Advanced Usage with Builder Pattern</h2>
 * <pre>{@code
 * // Create router with retry logic
 * Router router = RouterEngine.builder()
 *     .client(myLlmClient)
 *     .maxRetries(3)
 *     .enableRetry(true)
 *     .build();
 *
 * // Async routing
 * CompletableFuture<RoutingResult> future = router.routeAsync("Process payment");
 * future.thenAccept(result -> {
 *     System.out.println("Routed to: " + result.service());
 * });
 *
 * // Batch routing
 * List<String> requests = List.of(
 *     "Process payment",
 *     "Generate report",
 *     "Authenticate user"
 * );
 * List<RoutingResult> results = router.routeBatch(requests);
 * }</pre>
 *
 * <h2>Exception Handling</h2>
 * <p>
 * The core package defines a hierarchy of exceptions for different failure scenarios:
 * </p>
 * <ul>
 *   <li>{@link io.jai.router.core.LlmClientException} - LLM client failures</li>
 *   <li>{@link io.jai.router.core.exception.RoutingException} - General routing failures</li>
 *   <li>{@link io.jai.router.core.exception.InvalidInputException} - Invalid input validation</li>
 *   <li>{@link io.jai.router.core.exception.LowConfidenceException} - Low confidence routing</li>
 *   <li>{@link io.jai.router.core.exception.ServiceNotFoundException} - Service not found</li>
 *   <li>{@link io.jai.router.core.exception.LlmProviderException} - External LLM provider failures</li>
 * </ul>
 *
 * <h2>Validation</h2>
 * <p>
 * Input validation is provided through:
 * </p>
 * <ul>
 *   <li>{@link io.jai.router.core.DecisionContext} - Automatic validation of routing inputs</li>
 *   <li>{@link io.jai.router.core.validation.RoutingInputValidator} - Advanced validation with security checks</li>
 * </ul>
 *
 * <h2>Thread Safety</h2>
 * <p>
 * All core interfaces and implementations are designed to be thread-safe:
 * </p>
 * <ul>
 *   <li>{@link io.jai.router.core.Router} - Thread-safe routing operations</li>
 *   <li>{@link io.jai.router.core.RouterEngine} - Immutable configuration, thread-safe execution</li>
 *   <li>{@link io.jai.router.core.ScoringKeywordMatcher} - Synchronized registry binding</li>
 *   <li>{@link io.jai.router.core.RoutingResult} - Immutable result objects</li>
 *   <li>{@link io.jai.router.core.RoutingDecision} - Immutable decision objects</li>
 * </ul>
 *
 * <h2>Performance Considerations</h2>
 * <p>
 * The core package is optimized for high-throughput scenarios:
 * </p>
 * <ul>
 *   <li>Keyword-based routing: 12-35ms average latency</li>
 *   <li>Async routing support for non-blocking operations</li>
 *   <li>Batch routing for efficient multi-request processing</li>
 *   <li>Automatic retry with exponential backoff for resilience</li>
 *   <li>Processing time metrics included in results</li>
 * </ul>
 *
 * @author JAI Router Team
 * @version 1.0.0
 * @since 1.0.0
 */
package io.jai.router.core;
