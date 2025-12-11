# JAI Router - Technical Documentation

## Architecture Overview

JAI Router is a modular, intelligent request routing library built with Spring Boot. It analyzes incoming requests and routes them to the most appropriate service based on content analysis.

### Core Components

```
┌─────────────────────────────────────────────────────────────┐
│                      Client Application                      │
└───────────────────────────┬─────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                         Router                               │
│  ┌────────────────┐  ┌──────────────┐  ┌─────────────────┐ │
│  │ RouterEngine   │─▶│  LlmClient   │─▶│ ServiceRegistry │ │
│  └────────────────┘  └──────────────┘  └─────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                            │
         ┌──────────────────┼──────────────────┐
         ▼                  ▼                  ▼
┌────────────────┐ ┌────────────────┐ ┌────────────────┐
│  Auth Service  │ │   BI Service   │ │ Crypto Service │
└────────────────┘ └────────────────┘ └────────────────┘
```

### Hybrid Routing Architecture (v0.6.0+)

```
┌─────────────────────────────────────────────────────────────┐
│                   HybridLlmClient                            │
│            (Intelligent Strategy Selection)                  │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌────────────────────┐         ┌───────────────────────┐  │
│  │  AI Classifier     │         │  Dijkstra Router      │  │
│  │  (Fast Path)       │         │  (Optimal Path)       │  │
│  │                    │         │                       │  │
│  │ • 50-200ms         │         │ • 3-16ms              │  │
│  │ • Single-hop       │         │ • Multi-hop           │  │
│  │ • Keyword-based    │         │ • Graph-based         │  │
│  │ • 90% of requests  │         │ • 10% of requests     │  │
│  └─────────┬──────────┘         └─────────┬─────────────┘  │
│            │                              │                 │
│            └──────────────┬───────────────┘                 │
│                           │                                 │
│              ┌────────────▼──────────────┐                  │
│              │  ComplexityAnalyzer       │                  │
│              │  (Pattern Detection)      │                  │
│              └───────────────────────────┘                  │
│                           │                                 │
│              ┌────────────▼──────────────┐                  │
│              │     ServiceGraph          │                  │
│              │  (Microservices Graph)    │                  │
│              └───────────────────────────┘                  │
└─────────────────────────────────────────────────────────────┘
                           │
        ┌──────────────────┼───────────────────┐
        ▼                  ▼                   ▼
  ┌──────────┐       ┌──────────┐       ┌──────────┐
  │  Auth    │──────▶│  User    │──────▶│ Billing  │
  │ Service  │       │ Service  │       │ Service  │
  └──────────┘       └──────────┘       └──────────┘
```

## Module Structure

### 1. jai-router-core

The core library with no external dependencies (except logging).

**Key Classes**:
- `Router` - Main routing interface
- `RouterEngine` - Default implementation
- `LlmClient` - AI/ML provider interface
- `RoutingDecision` - Decision result model
- `DecisionContext` - Input validation and context
- `ServiceRegistry` - Service catalog interface

**Graph Routing (v0.6.0+)**:
- `HybridLlmClient` - Intelligent strategy selection
- `DijkstraLlmClient` - Dijkstra's shortest path algorithm
- `ServiceGraph` - Microservices graph with weighted edges
- `CachedDijkstraClient` - Path caching for performance
- `ComplexityAnalyzer` - Request pattern detection
- `EdgeMetrics` - Latency, cost, reliability calculations
- `RoutingPath` - Optimal path representation

**Features**:
- Pure Java (no Spring dependency)
- Thread-safe implementations
- Comprehensive validation
- Null-safety annotations
- Hybrid routing (AI + Dijkstra)
- Sub-millisecond caching

### 2. jai-router-spring-boot-autoconfigure

Spring Boot auto-configuration module.

**Key Classes**:
- `JAIRouterAutoConfiguration` - Main config
- `JAIRouterProperties` - Configuration properties
- `OpenAiLlmClient` - OpenAI integration
- `RouterHealthProbe` - Health monitoring

**Features**:
- Zero-configuration setup
- Conditional bean creation
- Metrics integration
- Health checks

### 3. jai-router-spring-boot-starter

Dependency aggregator (empty module).

**Purpose**:
- Convenient single dependency
- Pulls in core + autoconfigure
- Standard Spring Boot starter pattern

### 4. jai-router-examples

Example applications demonstrating usage.

**Examples**:
- `simple-routing-demo` - Basic REST API
- Integration test examples

## Design Patterns

### 1. Strategy Pattern
Different `LlmClient` implementations provide routing strategies:
- `BuiltinAiLlmClient` - Keyword-based (fast)
- `OpenAiLlmClient` - AI-powered (semantic)
- `DijkstraLlmClient` - Graph-based (optimal paths)
- `HybridLlmClient` - Intelligent strategy selection
- Custom implementations

**Hybrid Strategy Selection**:
```java
// Automatically chooses best strategy based on request complexity
HybridLlmClient hybrid = new HybridLlmClient(aiClient, dijkstraClient);

// Simple request → AI Classifier
hybrid.decide(DecisionContext.of("Show user dashboard"));

// Complex request → Dijkstra
hybrid.decide(DecisionContext.of("Auth user and then fetch billing"));
```

### 2. Builder Pattern
Fluent object creation:
```java
RoutingResult result = RoutingResult.of(
    "auth-service",
    0.95,
    "Login keyword matched"
);
```

### 3. Factory Pattern
Static factory methods:
```java
DecisionContext ctx = DecisionContext.of(input);
ServiceDefinition service = ServiceDefinition.of(id, name, keywords);
```

### 4. Decorator Pattern
Resilience4j decorators for fault tolerance:
```java
Callable<RoutingDecision> decorated = 
    CircuitBreaker.decorateCallable(circuitBreaker, call);
decorated = Retry.decorateCallable(retry, decorated);
```

### 5. Registry Pattern
`ServiceRegistry` maintains service definitions:
```java
registry.registerService(service);
registry.findServiceById("auth-service");
registry.listServices();
```

## Thread Safety

### Thread-Safe Components

1. **InMemoryServiceRegistry**
   - Uses `CopyOnWriteArrayList`
   - Safe for concurrent reads/writes
   - Immutable service definitions

2. **RouterEngine**
   - Stateless design
   - Thread-safe LlmClient implementations
   - No shared mutable state

3. **OpenAiLlmClient**
   - Stateless HTTP client
   - Thread-safe resilience components
   - Thread-safe metrics

4. **ServiceGraph (v0.6.0+)**
   - Uses `ConcurrentHashMap` for nodes
   - Uses `CopyOnWriteArrayList` for edges
   - Thread-safe for concurrent reads after initialization
   - Safe dynamic updates with `updateServiceReliability()`

5. **CachedDijkstraClient (v0.6.0+)**
   - Thread-safe cache using `ConcurrentHashMap`
   - Atomic operations for cache statistics
   - Safe concurrent access to cached paths

### Best Practices

```java
// Good - Immutable configuration
public class Config {
    private final String apiKey;
    private final int timeout;
    
    public Config(String apiKey, int timeout) {
        this.apiKey = apiKey;
        this.timeout = timeout;
    }
}

//  Bad - Mutable shared state
public class BadClient {
    private String lastResult; // Shared mutable state
    
    public void decide() {
        lastResult = "..."; // Race condition
    }
}
```

## Error Handling

### Exception Hierarchy

```
RuntimeException
  └── LlmClientException
       ├── OpenAiException
       └── RetryableOpenAiException
```

### Error Handling Strategy

1. **Validation Errors**: Throw `IllegalArgumentException`
2. **Null Checks**: Use `Objects.requireNonNull()` with descriptive messages
3. **Business Logic Errors**: Throw `LlmClientException` or subclasses
4. **External API Errors**: Wrap in custom exceptions with context

### Example

```java
public RoutingDecision decide(DecisionContext ctx) {
    // 1. Validate input
    Objects.requireNonNull(ctx, "DecisionContext cannot be null");
    
    try {
        // 2. Business logic
        return performRouting(ctx);
    } catch (IOException e) {
        // 3. Wrap external errors
        throw new LlmClientException("Routing failed", e);
    }
}
```

## Performance Considerations

### 1. Caching Strategy

**Path Caching (v0.6.0+)**:
```java
// Implemented in CachedDijkstraClient
CachedDijkstraClient cached = new CachedDijkstraClient(
    dijkstraClient,
    1000,      // max cache size
    300_000    // 5 minute TTL
);

// First call: ~11ms (cache miss)
// Subsequent calls: <1ms (cache hit)
CacheStats stats = cached.getStats();
// CacheStats[size=42, hits=850, misses=150, hitRate=85.00%]
```

**Performance Characteristics**:

| Strategy | Scenario | Latency | Use Case |
|----------|----------|---------|----------|
| AI Classifier | Simple single-hop | 50-200ms | 90% of requests |
| Dijkstra | Complex multi-hop (miss) | 3-16ms | First time complex |
| Cached Dijkstra | Complex multi-hop (hit) | <1ms | Repeated patterns |
| Hybrid (overall) | Mixed workload | ~52ms avg | Production |

**Algorithm Complexity**:
- **Dijkstra**: O((V + E) log V) where V = services, E = edges
- **Typical**: 3-16ms for graphs with 20-50 services
- **Large graphs**: May increase to 50ms for 100+ services

### 2. Connection Pooling

```java
// OpenAiLlmClient uses HttpClient connection pooling
HttpClient client = HttpClient.newBuilder()
    .connectTimeout(Duration.ofSeconds(30))
    .build(); // Reuses connections
```

### 3. Async Processing

```java
// Future improvement
CompletableFuture<RoutingResult> routeAsync(String input);
```

## Configuration

### Application Properties

```yaml
jai:
  router:
    llm-provider: openai  # or builtin-ai
    confidence-threshold: 0.7
    
    # OpenAI Configuration
    openai:
      api-key: ${OPENAI_API_KEY}
      model: gpt-4o-mini
      temperature: 0.0
      max-retries: 2
      timeout-seconds: 30
      retry-backoff-millis: 500
    
    # Service Definitions
    services:
      - id: auth-service
        display-name: Authentication
        keywords: [login, token, auth, password]
      
      - id: bi-service
        display-name: Business Intelligence
        keywords: [report, dashboard, analytics, kpi]
```

### Programmatic Configuration

```java
@Configuration
public class CustomRouterConfig {
    
    @Bean
    public ServiceRegistry customRegistry() {
        return new InMemoryServiceRegistry(List.of(
            ServiceDefinition.of("custom-service", "Custom", List.of("custom"))
        ));
    }
    
    @Bean
    public LlmClient customLlmClient() {
        return new MyCustomLlmClient();
    }
}
```

### Spring Boot Auto-Configuration

JAI Router provides automatic Spring Boot integration through `JAIRouterAutoConfiguration`.

**Auto-Configuration Registration**:

File: `jai-router-spring-boot-autoconfigure/src/main/resources/META-INF/spring.factories`
```properties
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
  io.jai.router.spring.JAIRouterAutoConfiguration
```

**Beans Created Automatically**:

1. **ServiceRegistry** - Populated from `jai.router.services` configuration
2. **LlmClient** - Based on `jai.router.llm-provider`:
   - `builtin-ai` creates `BuiltinAiLlmClient`
   - `openai` creates `OpenAiLlmClient`
   - `hybrid` creates `HybridLlmClient`
3. **ServiceGraph** - When `jai.router.dijkstra.enabled=true`
4. **DijkstraLlmClient** - For Dijkstra pathfinding
5. **CachedDijkstraClient** - Wraps Dijkstra with caching
6. **RouterHealthProbe** - For health monitoring

**Configuration Properties**:

Defined in `JAIRouterProperties` with prefix `jai.router`:
- `llm-provider` - LLM client selection (builtin-ai, openai, hybrid)
- `confidence-threshold` - Routing confidence threshold (0.0-1.0)
- `services[]` - Service definitions with keywords
- `hybrid.enabled` - Enable hybrid routing
- `dijkstra.enabled` - Enable Dijkstra pathfinding
- `dijkstra.cache.*` - Cache configuration (size, TTL)
- `dijkstra.edges[]` - Service graph edges with metrics

**Conditional Bean Creation**:

```java
@Bean
@ConditionalOnProperty(name = "jai.router.llm-provider", havingValue = "hybrid")
@ConditionalOnMissingBean(name = "hybridLlmClient")
public LlmClient hybridLlmClient(...) {
    // Creates HybridLlmClient only when llm-provider=hybrid
}
```

**Example Configuration**:

```yaml
jai:
  router:
    llm-provider: hybrid
    confidence-threshold: 0.7
    
    dijkstra:
      enabled: true
      source-service: gateway
      cache:
        enabled: true
        max-size: 1000
        ttl-ms: 300000
      edges:
        - from: gateway
          to: auth-service
          latency: 10.0
          cost: 0.0
          reliability: 0.999
    
    services:
      - id: gateway
        keywords: []
      - id: auth-service
        keywords: [login, auth, token]
```

**Testing Auto-Configuration**:

```java
@SpringBootTest
class AutoConfigTest {
    
    @Autowired
    private LlmClient client;
    
    @Test
    void clientIsAutoConfigured() {
        assertThat(client).isNotNull();
        assertThat(client).isInstanceOf(BuiltinAiLlmClient.class);
    }
}
```

## Testing

### Unit Testing

```java
@Test
@DisplayName("Should route to correct service")
void shouldRouteCorrectly() {
    // Arrange
    LlmClient mockClient = mock(LlmClient.class);
    Router router = new RouterEngine(mockClient);
    
    when(mockClient.decide(any()))
        .thenReturn(RoutingDecision.of("auth-service", 0.95, "matched"));
    
    // Act
    RoutingResult result = router.route("login request");
    
    // Assert
    assertThat(result.service()).isEqualTo("auth-service");
    assertThat(result.confidence()).isEqualTo(0.95);
}
```

### Integration Testing

```java
@SpringBootTest
@AutoConfigureMockMvc
class RouterIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void shouldRouteViaRestApi() throws Exception {
        mockMvc.perform(post("/api/router/route")
                .contentType(MediaType.APPLICATION_JSON)
                .content("\"Generate report\""))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.service").value("bi-service"));
    }
}
```

## Monitoring & Metrics

### Available Metrics

```
# Request count
jai_router_llm_requests_total

# Request latency
jai_router_llm_request_latency_ms{quantile="0.5"}
jai_router_llm_request_latency_ms{quantile="0.95"}

# Failure count
jai_router_llm_failures_total

# Circuit breaker state
resilience4j_circuitbreaker_state{name="openai-cb"}
```

### Health Checks

```bash
# Health endpoint
curl http://localhost:8080/actuator/health

# Response
{
  "status": "UP",
  "components": {
    "router": {
      "status": "UP",
      "details": {
        "provider": "openai",
        "available": true
      }
    }
  }
}
```

## Security

### API Key Management

```yaml
#  Never commit API keys
jai:
  router:
    openai:
      api-key: sk-1234567890abcdef

#  Use environment variables
jai:
  router:
    openai:
      api-key: ${OPENAI_API_KEY}
```

### Input Validation

All inputs are validated:
- Max length: 10,000 characters
- Non-null checks
- Trimming and sanitization

### Rate Limiting

```java
// Future implementation
@RateLimiter(name = "router", fallbackMethod = "routeFallback")
public RoutingResult route(String input) {
    // ...
}
```

## Dijkstra Algorithm Implementation (v0.6.0+)

### Overview

JAI Router implements Dijkstra's shortest path algorithm for optimal multi-hop routing through microservices.

### Graph Model

**Nodes**: Microservices
**Edges**: Service dependencies with weighted metrics

```java
// Edge weight calculation
weight = α*latency + β*cost + γ*(1 - reliability)*1000

// Default weights:
α (latency) = 0.5
β (cost) = 0.3
γ (reliability) = 0.2
```

### Algorithm Steps

1. **Initialization**
   - Set source distance to 0
   - Set all other distances to infinity
   - Create empty predecessors map
   - Initialize priority queue

2. **Main Loop**
   - Extract node with minimum distance
   - If target reached, reconstruct path
   - For each neighbor:
     - Calculate new distance through current node
     - If better than existing, update distance and predecessor

3. **Path Reconstruction**
   - Follow predecessors from target back to source
   - Reverse to get forward path

### Code Example

```java
// Create service graph
ServiceGraph graph = new ServiceGraph();
graph.addService("gateway", Map.of("type", "entry"));
graph.addService("auth-service", Map.of("endpoint", "http://auth:8080"));
graph.addEdge("gateway", "auth-service", 
    new EdgeMetrics(10.0, 0.0, 0.999));

// Find optimal path
DijkstraLlmClient dijkstra = new DijkstraLlmClient(graph, "gateway");
RoutingDecision decision = dijkstra.decide(
    DecisionContext.of("TARGET:auth-service")
);

// Result includes optimal path with metrics
String explanation = decision.explanation();
// "Optimal path: gateway → auth-service (hops: 1, latency: 5.2ms, cost: 0.0003)"
```

### Complexity Analysis

- **Time**: O((V + E) log V)
  - V = number of services (nodes)
  - E = number of edges
  - log V from priority queue operations

- **Space**: O(V + E)
  - Graph storage: O(V + E)
  - Distance map: O(V)
  - Predecessor map: O(V)
  - Priority queue: O(V)

### Performance Optimization

**1. Early Termination**
```java
if (current.serviceId().equals(target)) {
    return reconstructPath(...); // Stop when target found
}
```

**2. Priority Queue**
```java
PriorityQueue<NodeDistance> pq = new PriorityQueue<>(
    Comparator.comparingDouble(NodeDistance::distance)
);
// O(log V) insertions instead of O(V) linear search
```

**3. Path Caching**
```java
// Cache frequently used paths
CachedDijkstraClient cached = new CachedDijkstraClient(dijkstra);
// Reduces 10ms → <1ms for repeated requests
```

### Request Complexity Detection

The `ComplexityAnalyzer` determines when to use Dijkstra:

**Multi-Hop Patterns**:
- "and then", "followed by", "after"
- "chain", "orchestrate", "workflow"

**Cost-Sensitive Patterns**:
- "cheap", "cheapest", "cost", "budget"
- "optimize", "minimize"

**Failover Patterns**:
- "failover", "backup", "alternative"
- "fallback", "retry"

**Example**:
```java
ComplexityAnalyzer analyzer = new ComplexityAnalyzer();

// Simple → Uses AI Classifier
analyzer.analyze(DecisionContext.of("Show dashboard"));
// Returns: RequestComplexity.SIMPLE

// Complex → Uses Dijkstra
analyzer.analyze(DecisionContext.of("Auth and then fetch billing"));
// Returns: RequestComplexity.MULTI_HOP
```

### Edge Metrics Configuration

```java
// Fast but expensive
new EdgeMetrics(
    10.0,   // 10ms latency
    0.01,   // $0.01 per call
    0.99    // 99% reliability
);

// Slow but cheap
new EdgeMetrics(
    100.0,  // 100ms latency
    0.001,  // $0.001 per call
    0.95    // 95% reliability
);

// Dijkstra will choose based on weighted formula
```

## Troubleshooting

### Common Issues

1. **"LLM client returned no decision"**
   - Check LlmClient implementation
   - Verify service registry is populated
   - Check logs for underlying errors

2. **"Payload exceeds maximum size"**
   - Input > 10,000 characters
   - Reduce input size or increase `MAX_PAYLOAD_LENGTH`

3. **"OpenAI request failed: 429"**
   - Rate limit exceeded
   - Retry mechanism will handle (configured retries)
   - Check OpenAI quota

4. **Circuit breaker open**
   - Too many failures detected
   - Wait for cool-down period (30s default)
   - Check service health

## Best Practices

### 1. Service Design

```java
//  Good - Single responsibility
ServiceDefinition.of(
    "auth-service",
    "Authentication",
    List.of("login", "token", "authenticate")
);

//  Bad - Too broad
ServiceDefinition.of(
    "all-service",
    "Everything",
    List.of("login", "report", "data", "...") // Too many keywords
);
```

### 2. Keyword Selection

- Use specific, relevant keywords
- Avoid overlapping keywords between services
- Include common variations
- Test with real-world data

### 3. Error Handling

```java
//  Good - Specific handling
try {
    return router.route(input);
} catch (LlmClientException e) {
    log.error("Routing failed: {}", e.getMessage());
    return fallbackRouting(input);
}

//  Bad - Catching everything
try {
    return router.route(input);
} catch (Exception e) {
    // Too broad
}
```

## Future Enhancements

### Planned Features

1. **Caching Layer**
   - Redis integration
   - Configurable TTL
   - Cache invalidation

2. **Additional LLM Providers**
   - Anthropic Claude
   - Google Gemini
   - Azure OpenAI

3. **Advanced Routing**
   - Machine learning models
   - A/B testing
   - Load-based routing

4. **Observability**
   - Distributed tracing (OpenTelemetry)
   - Enhanced metrics
   - Real-time dashboards

## Resources

- [Main README](README.md)
- [API JavaDocs](https://javadoc.io/doc/io.jai/jai-router-core)
- [Examples](jai-router-examples/)

## Support

For questions or issues:
- **Email**: rrezart.prebreza@gmail.com
- **GitHub Issues**: [JAI-Router Issues](https://github.com/JAI-create-spec/JAI-Router/issues)
- **Discussions**: [JAI-Router Discussions](https://github.com/JAI-create-spec/JAI-Router/discussions)

