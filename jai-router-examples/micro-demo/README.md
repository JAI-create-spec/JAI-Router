# JAI Router - Microservices Demo

This demo showcases JAI Router in a microservices architecture with multiple services communicating through a gateway.

## Architecture

```
                    ┌──────────────┐
                    │   Gateway    │
                    │   (Port TBD) │
                    └──────┬───────┘
                           │
          ┌────────────────┼────────────────┐
          │                │                │
          ▼                ▼                ▼
    ┌──────────┐    ┌──────────┐    ┌──────────┐
    │  User    │    │  Order   │    │  Other   │
    │ Service  │    │ Service  │    │ Services │
    └──────────┘    └──────────┘    └──────────┘
```

## Services

### Gateway
- Entry point for all requests
- Routes requests to appropriate microservices using JAI Router
- Uses hybrid routing for optimal service selection

### User Service
- Manages user data and authentication
- Example endpoints for user operations

### Order Service
- Handles order processing
- Example endpoints for order management

## Running the Demo

### Prerequisites
- Java 17 or higher
- Gradle 8.x

### Start All Services

From the project root:

```bash
# Build all modules
./gradlew build

# Start services individually (in separate terminals)
./gradlew :jai-router-examples:micro-demo:gateway:bootRun
./gradlew :jai-router-examples:micro-demo:user-service:bootRun
./gradlew :jai-router-examples:micro-demo:order-service:bootRun
```

### Test the Demo

Once all services are running, test the routing:

```bash
# Example: Route to user service
curl -X POST http://localhost:8080/route \
  -H "Content-Type: application/json" \
  -d '{"request": "Get user profile"}'

# Example: Route to order service
curl -X POST http://localhost:8080/route \
  -H "Content-Type: application/json" \
  -d '{"request": "Place an order"}'
```

## Configuration

Each service can be configured independently via `application.yml` in their respective `src/main/resources` directories.

### Gateway Configuration Example

```yaml
jai:
  router:
    llm-provider: hybrid
    dijkstra:
      enabled: true
      edges:
        - from: gateway
          to: user-service
          latency: 15.0
        - from: gateway
          to: order-service
          latency: 20.0
```

## Development

To add a new service to the demo:

1. Create a new module under `micro-demo/`
2. Add service definition to gateway's `application.yml`
3. Configure routing edges if using Dijkstra
4. Update this README with service details

## Learn More

- [Main README](../../README.md) - Full documentation
- [Hybrid Routing Guide](../../docs/HYBRID_ROUTING.md) - Advanced routing
- [Technical Documentation](../../TECHNICAL.md) - Implementation details

