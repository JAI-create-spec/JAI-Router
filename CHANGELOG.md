# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.6.0] - 2024-12-11

### 🎉 Major Features Added

#### Hybrid Routing System
- **NEW**: Intelligent hybrid routing combining AI classification with Dijkstra pathfinding
- **NEW**: `HybridLlmClient` - Automatically chooses optimal routing strategy based on request complexity
- **NEW**: `DijkstraLlmClient` - Implements Dijkstra's algorithm for multi-hop service orchestration
- **NEW**: `ServiceGraph` - Thread-safe service graph modeling with weighted edges
- **NEW**: `CachedDijkstraClient` - Path caching for sub-millisecond routing of repeated workflows
- **NEW**: `ComplexityAnalyzer` - Intelligent request pattern detection

#### Core Components
- Added `ServiceGraph` class for modeling microservices as a weighted graph
- Added `EdgeMetrics` record for latency, cost, and reliability calculations
- Added `RoutingPath` record representing optimal paths through service dependencies
- Added `ComplexityAnalyzer` for automatic routing strategy selection

#### Performance Improvements
- Multi-hop routing: 3-16ms (Dijkstra pathfinding)
- Cached routing: <1ms (for repeated patterns)
- Intelligent fallback: 90% fast AI, 10% optimal Dijkstra

### Added
- `io.jai.router.graph` package with complete hybrid routing implementation
- 33 comprehensive unit tests for all graph routing components:
  - `ServiceGraphTest.java` - Graph operations (8 tests)
  - `DijkstraLlmClientTest.java` - Pathfinding (9 tests)
  - `ComplexityAnalyzerTest.java` - Request analysis (12 tests)
  - `HybridLlmClientTest.java` - Hybrid routing (4 tests)
- `HybridRoutingExample.java` - Complete working demonstration
- `docs/HYBRID_ROUTING.md` - Comprehensive hybrid routing guide
- `docs/IMPLEMENTATION_SUMMARY.md` - Implementation summary and technical details
- Package-level documentation in `package-info.java`

### Enhanced
- Updated README.md with hybrid routing documentation and examples
- Improved project structure documentation
- Added performance benchmarks and comparisons
- Enhanced features table with new capabilities

### Technical Details
- **Algorithm**: Dijkstra's shortest path (O((V + E) log V))
- **Thread Safety**: All graph operations are thread-safe using concurrent collections
- **Caching**: LRU cache with configurable size and TTL
- **Strategy Pattern**: Pluggable routing strategies for extensibility
- **Null Safety**: Comprehensive @NotNull/@Nullable annotations

### Use Cases
- ✅ Multi-hop service orchestration (Auth → User → Billing → Notification)
- ✅ Cost-optimized routing across microservices
- ✅ Dynamic failover with path recalculation
- ✅ Service dependency management
- ✅ Latency-optimized request routing

### Documentation
- Complete API documentation with Javadoc
- Step-by-step hybrid routing guide
- Working code examples and demos
- Performance analysis and best practices
- Migration guide from simple to hybrid routing

### Breaking Changes
None - All changes are backward compatible

---

## [0.5.0] - 2024-11-25

### Added
- Core routing engine with `Router` and `RouterEngine`
- Built-in AI provider with keyword-based classification
- OpenAI GPT integration for semantic routing
- Anthropic Claude integration
- Service registry with `InMemoryServiceRegistry`
- Spring Boot auto-configuration
- REST API endpoints for routing
- Health check integration
- Comprehensive validation and error handling

### Features
- AI-powered semantic analysis of requests
- Multiple LLM provider support
- Spring Boot zero-config integration
- Production-grade null safety and validation
- Service registration and discovery
- Confidence scoring
- Framework-agnostic core module

---

## Links

- [GitHub Repository](https://github.com/JAI-create-spec/JAI-Router)
- [Documentation](docs/)
- [Issues](https://github.com/JAI-create-spec/JAI-Router/issues)

