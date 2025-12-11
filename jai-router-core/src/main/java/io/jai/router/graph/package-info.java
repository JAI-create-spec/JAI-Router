/**
 * Hybrid routing system combining AI classification with Dijkstra pathfinding.
 * <p>
 * This package provides intelligent routing for microservices architectures:
 * </p>
 *
 * <h2>Key Components</h2>
 * <ul>
 *   <li>{@link io.jai.router.graph.HybridLlmClient} - Combines fast AI classifier with Dijkstra router</li>
 *   <li>{@link io.jai.router.graph.DijkstraLlmClient} - Finds optimal paths through service graphs</li>
 *   <li>{@link io.jai.router.graph.ServiceGraph} - Models microservices as a weighted graph</li>
 *   <li>{@link io.jai.router.graph.CachedDijkstraClient} - Caches paths for performance</li>
 *   <li>{@link io.jai.router.graph.ComplexityAnalyzer} - Determines routing strategy</li>
 * </ul>
 *
 * <h2>When to Use</h2>
 * <table border="1">
 *   <tr>
 *     <th>Scenario</th>
 *     <th>Strategy</th>
 *     <th>Performance</th>
 *   </tr>
 *   <tr>
 *     <td>Single service call</td>
 *     <td>AI Classifier</td>
 *     <td>50-200ms</td>
 *   </tr>
 *   <tr>
 *     <td>Multi-hop workflow</td>
 *     <td>Dijkstra</td>
 *     <td>3-16ms + AI time</td>
 *   </tr>
 *   <tr>
 *     <td>Cost optimization</td>
 *     <td>Dijkstra</td>
 *     <td>3-16ms</td>
 *   </tr>
 *   <tr>
 *     <td>Repeated workflows</td>
 *     <td>Cached Dijkstra</td>
 *     <td>&lt;1ms (cache hit)</td>
 *   </tr>
 * </table>
 *
 * <h2>Quick Start</h2>
 * <pre>
 * // 1. Create service graph
 * ServiceGraph graph = new ServiceGraph();
 * graph.addService("gateway", Map.of("type", "entry-point"));
 * graph.addService("auth-service", Map.of("endpoint", "http://auth:8080"));
 * graph.addEdge("gateway", "auth-service", new EdgeMetrics(10.0, 0.0, 0.999));
 *
 * // 2. Create AI classifier for simple requests
 * LlmClient aiClient = new BuiltinAiLlmClient(keywords, 0.7);
 *
 * // 3. Create Dijkstra client for complex requests
 * LlmClient dijkstraClient = new DijkstraLlmClient(graph, "gateway");
 *
 * // 4. Wrap with caching
 * LlmClient cachedDijkstra = new CachedDijkstraClient(dijkstraClient);
 *
 * // 5. Create hybrid client
 * LlmClient hybridClient = new HybridLlmClient(aiClient, cachedDijkstra);
 *
 * // 6. Route requests
 * RoutingDecision decision = hybridClient.decide(DecisionContext.of("Show user profile"));
 * // → Uses AI classifier (fast)
 *
 * decision = hybridClient.decide(DecisionContext.of("Auth and then fetch billing"));
 * // → Uses Dijkstra (optimal path)
 * </pre>
 *
 * <h2>Performance Characteristics</h2>
 * <h3>Time Complexity</h3>
 * <ul>
 *   <li>Dijkstra: O((V + E) log V) where V = services, E = edges</li>
 *   <li>Typical overhead: 3-16ms for medium graphs (20-50 services)</li>
 * </ul>
 *
 * <h3>Space Complexity</h3>
 * <ul>
 *   <li>Graph: O(V + E)</li>
 *   <li>Cache: ~100 bytes per cached entry</li>
 * </ul>
 *
 * <h2>Examples</h2>
 * <p>
 * See {@link io.jai.router.examples.HybridRoutingExample} for a complete demonstration.
 * </p>
 *
 * @since 0.6.0
 * @author JAI Router Team
 */
package io.jai.router.graph;

