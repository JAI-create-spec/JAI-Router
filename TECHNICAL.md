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

**Features**:
- Pure Java (no Spring dependency)
- Thread-safe implementations
- Comprehensive validation
- Null-safety annotations

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
- `BuiltinAiLlmClient` - Keyword-based
- `OpenAiLlmClient` - AI-powered
- Custom implementations

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

```java
// Future implementation
@Cacheable(value = "routing-cache", key = "#ctx.payload")
public RoutingDecision decide(DecisionContext ctx) {
    // Expensive operation
}
```

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
- **GitHub Issues**: https://github.com/JAI-create-spec/JAI-Router/issues
- **Discussions**: https://github.com/JAI-create-spec/JAI-Router/discussions

