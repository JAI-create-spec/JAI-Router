package io.jai.router.graph;

import io.jai.router.core.DecisionContext;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Analyzes request complexity to determine optimal routing strategy.
 * <p>
 * Determines whether a request should use fast AI classification (single-hop)
 * or slower Dijkstra pathfinding (multi-hop orchestration).
 * </p>
 *
 * <p><strong>Decision Criteria:</strong></p>
 * <ul>
 *   <li><strong>SIMPLE:</strong> Single service call, use AI classifier</li>
 *   <li><strong>MULTI_HOP:</strong> Service chains, use Dijkstra</li>
 *   <li><strong>COST_SENSITIVE:</strong> Cost optimization needed, use Dijkstra</li>
 *   <li><strong>FAILOVER:</strong> Failover scenario, use Dijkstra</li>
 * </ul>
 *
 * @author JAI Router Team
 * @since 0.6.0
 */
public class ComplexityAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(ComplexityAnalyzer.class);

    // Patterns for detecting multi-hop requests
    private static final Pattern MULTI_HOP_PATTERN = Pattern.compile(
            "\\b(and then|after|followed by|before|chain|orchestrate|workflow)\\b",
            Pattern.CASE_INSENSITIVE
    );

    // Patterns for cost-sensitive requests
    private static final Pattern COST_PATTERN = Pattern.compile(
            "\\b(cheap|cheapest|expensive|cost|budget|optimize|minimiz)\\b",
            Pattern.CASE_INSENSITIVE
    );

    // Patterns for failover scenarios
    private static final Pattern FAILOVER_PATTERN = Pattern.compile(
            "\\b(failover|backup|alternative|fallback|retry)\\b",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Analyzes a decision context to determine request complexity.
     *
     * @param ctx decision context
     * @return request complexity level
     * @throws NullPointerException if ctx is null
     */
    @NotNull
    public RequestComplexity analyze(@NotNull DecisionContext ctx) {
        Objects.requireNonNull(ctx, "ctx cannot be null");

        String payload = ctx.payload().toLowerCase();

        // Check for explicit TARGET: prefix (indicates explicit routing)
        if (payload.startsWith("target:")) {
            // If it has multi-hop indicators, use Dijkstra
            if (MULTI_HOP_PATTERN.matcher(payload).find()) {
                log.debug("Detected MULTI_HOP request");
                return RequestComplexity.MULTI_HOP;
            }
            // Otherwise simple
            log.debug("Detected SIMPLE request (explicit target)");
            return RequestComplexity.SIMPLE;
        }

        // Check for cost optimization requests FIRST (higher priority)
        if (COST_PATTERN.matcher(payload).find()) {
            log.debug("Detected COST_SENSITIVE request");
            return RequestComplexity.COST_SENSITIVE;
        }

        // Check for multi-hop indicators
        if (MULTI_HOP_PATTERN.matcher(payload).find()) {
            log.debug("Detected MULTI_HOP request from keywords");
            return RequestComplexity.MULTI_HOP;
        }


        // Check for failover scenarios
        if (FAILOVER_PATTERN.matcher(payload).find()) {
            log.debug("Detected FAILOVER request");
            return RequestComplexity.FAILOVER;
        }

        // Default: simple single-hop
        log.debug("Detected SIMPLE request (default)");
        return RequestComplexity.SIMPLE;
    }

    /**
     * Request complexity levels.
     */
    public enum RequestComplexity {
        /**
         * Simple single-hop request, use fast AI classifier.
         */
        SIMPLE,

        /**
         * Multi-hop orchestration needed, use Dijkstra.
         */
        MULTI_HOP,

        /**
         * Cost optimization required, use Dijkstra.
         */
        COST_SENSITIVE,

        /**
         * Failover scenario, use Dijkstra for path recalculation.
         */
        FAILOVER
    }
}

