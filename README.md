# JAI Router

A Java library for intelligent request dispatch in conversational and natural language interfaces. Routes free-form text requests to appropriate services based on semantic content understanding.

---

## What This Is

JAI Router helps backends that receive **free-form, unstructured text requests** and need to route them based on **meaning, not URL patterns**.

**Example:** A chatbot backend where users describe problems in their own words, not structured API calls.

---

## What This Is NOT

- ❌ A general-purpose microservice router (use Kong or Nginx)
- ❌ For standard REST API routing (use API Gateway)
- ❌ For sub-50ms latency requirements
- ❌ A replacement for traditional routing rules

---

## When to Use JAI Router

✅ **Chatbot backends** routing to specialists  
✅ **Customer support portals** where users describe problems  
✅ **Voice assistant backends** routing spoken requests  
✅ **Internal chat interfaces** with natural language input  
✅ **Support ticket systems** where intent matters  

## When NOT to Use JAI Router

❌ Standard REST API routing  
❌ Microservice request dispatch  
❌ Latency-critical systems (< 100ms)  
❌ When routing rules are known upfront  
❌ When predictability is more important than semantic understanding  

---

## Table of Contents

- [What This Is](#what-this-is)
- [When to Use](#when-to-use-jai-router)
- [Quick Start](#quick-start)
- [Installation](#installation)
- [Configuration](#configuration)
- [Usage Examples](#usage-examples)
- [Performance & Expectations](#performance--expectations)
- [LLM Providers](#llm-providers)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)
- [License](#license)
- [Contact](#contact)

---

## Overview

JAI Router analyzes request content to determine which service should handle it. It's designed for scenarios where requests are **conversational** and routing logic is based on **understanding intent**, not parsing URL patterns.

**Key difference:** Traditional routing looks at request structure. JAI Router looks at request meaning.

### Core Components

| Component | Purpose | Requirement |
|-----------|---------|-------------|
| **jai-router-core** | Core routing engine and provider interfaces | Java 17+ |
| **jai-router-spring-boot-autoconfigure** | Spring Boot auto-configuration | Spring Boot 3.0+ |
| **jai-router-spring-boot-starter** | Convenient starter POM | Spring Boot 3.0+ |
| **jai-router-examples** | Reference implementations and working examples | Spring Boot 3.0+ |

**Note:** JAI Router is designed for **Spring Boot 3.0+**. While the core library has zero Spring dependencies, all practical usage assumes Spring Boot integration.

### Core Concepts

| Concept | Description |
|---------|-------------|
| **Router** | Main API entry point that accepts text requests and returns routing decisions |
| **RoutingResult** | Immutable result containing target service, confidence score, and explanation |
| **ServiceRegistry** | Registry for managing available services and their metadata |
| **LLMProvider** | Strategy interface for routing logic (keyword matching or external LLMs) |
| **ServiceDefinition** | Metadata about a service (name, keywords, endpoint, etc.)

## Key Features

### Core Capabilities

| Feature | Description | Status |
|---------|-------------|--------|
| **Keyword-based Routing** | Fast local classification using keyword matching (12-35ms) | Stable |
| **OpenAI Integration** | GPT-4o-mini for semantic understanding (100-300ms) | Stable |
| **Anthropic Integration** | Claude for routing intent classification | Stable |
| **Spring Boot Auto-config** | Zero-code configuration via application.yml | Stable |
| **Confidence Scoring** | Numerical confidence with explanations | Stable |
| **Health Endpoints** | Spring Actuator integration for monitoring | Stable |
| **Service Discovery** | Dynamic service registration and lookup | Stable |

### Performance Expectations

| Provider | Latency | Cost | Use Case |
|----------|---------|------|----------|
| **Built-in (Keywords)** | 12-35ms | Free | Simple intent matching |
| **OpenAI (GPT-4o-mini)** | 100-300ms | $0.00001/token | Complex semantic understanding |
| **Anthropic (Claude)** | 120-280ms | $0.00030/token | High-accuracy classification |

## Architecture

### Simple Request Flow

```
User Request (text)
        ↓
    RouterEngine
        ↓
    LLMProvider (choose based on complexity)
        ├─ Built-in (fast, free, simple)
        ├─ OpenAI (accurate, paid, slow)
        └─ Anthropic (accurate, paid, slow)
        ↓
    ServiceRegistry (lookup matching services)
        ↓
    RoutingResult (service + confidence + explanation)
```

### Module Breakdown

| Module | Package | Responsibility |
|--------|---------|----------------|
| **core** | `io.jai.router.core` | Core routing API and engine |
| **llm** | `io.jai.router.llm` | LLM provider implementations |
| **registry** | `io.jai.router.registry` | Service discovery and management |
| **spring** | `io.jai.router.spring` | Spring Boot auto-configuration |

### How It Works

1. **Input:** User sends text request (e.g., "I need to dispute a charge")
2. **Analysis:** RouterEngine analyzes content using LLMProvider
3. **Matching:** ServiceRegistry finds candidate services
4. **Decision:** Router determines best service based on analysis
5. **Output:** RoutingResult with service, confidence, explanation

---

## Quick Start

### Prerequisites

| Requirement | Version | Purpose |
|-------------|---------|---------|
| Java | 17+ | Runtime environment |
| Gradle | 8.x | Build tool (wrapper included) |
| Spring Boot | 3.0+ | For Spring integration (optional) |

### 5-Minute Setup

**Step 1: Clone and Build**
```bash
git clone https://github.com/JAI-create-spec/JAI-Router.git
cd JAI-Router
./gradlew clean build
```

**Step 2: Run Example**
```bash
./gradlew :jai-router-examples:simple-routing-demo:bootRun
```

**Step 3: Test Routing**
```bash
curl -X POST http://localhost:8085/api/router/route \
  -H "Content-Type: application/json" \
  -d '"Generate a quarterly KPI dashboard"'
```

**Expected Response:**
```json
{
  "service": "analytics-service",
  "confidence": 0.91,
  "explanation": "Detected keywords: quarterly, kpi, dashboard",
  "processingTimeMs": 12,
  "timestamp": "2025-12-11T10:30:00Z"
}
```

---

## Installation

### Build from Source (recommended)

```bash
# Clone repository
git clone https://github.com/JAI-create-spec/JAI-Router.git
cd JAI-Router

# Build all modules
./gradlew clean build

# Install to local Maven repository (for local testing)
./gradlew publishToMavenLocal
```

> Note: This library is not published to Maven Central yet. Use `publishToMavenLocal` to consume locally until an official release is published.

### Add to Your Project

#### Maven
```xml
<repositories>
  <repository>
    <id>mavenLocal</id>
    <url>file://${user.home}/.m2/repository</url>
  </repository>
</repositories>

<dependency>
    <groupId>io.jai</groupId>
    <artifactId>jai-router-spring-boot-starter</artifactId>
    <version>0.6.0</version>
</dependency>
```

#### Gradle
```gradle
repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation 'io.jai:jai-router-spring-boot-starter:0.6.0'
}
```

#### Git Submodule (Advanced)
```bash
# Add as submodule
git submodule add https://github.com/JAI-create-spec/JAI-Router.git jai-router

# Include in settings.gradle
includeBuild 'jai-router'
```

---

## Configuration

JAI Router requires minimal setup. At minimum, define your services and choose a provider.

### Basic Setup (Built-in Keyword Matching)

```yaml
jai:
  router:
    llm-provider: builtin-ai
    confidence-threshold: 0.7
    
    services:
      - id: support-general
        display-name: "General Support"
        keywords: [help, support, question, issue, problem]
        
      - id: billing
        display-name: "Billing"
        keywords: [invoice, billing, charge, payment, refund]
        
      - id: technical
        display-name: "Technical Support"
        keywords: [bug, error, crash, broken, timeout]
```

### Using OpenAI (Recommended for Better Accuracy)

```yaml
jai:
  router:
    llm-provider: openai
    
    openai:
      api-key: ${OPENAI_API_KEY}  # Set environment variable
      model: gpt-4o-mini  # Fast and affordable
    
    services:
      - id: support-general
        keywords: [help, support]
      - id: billing
        keywords: [invoice, billing, payment]
```

### Using Anthropic (Claude)

```yaml
jai:
  router:
    llm-provider: anthropic
    
    anthropic:
      api-key: ${ANTHROPIC_API_KEY}
      model: claude-3-5-haiku
    
    services:
      - id: support-general
        keywords: [help, support]
      - id: billing
        keywords: [invoice, billing]
```

### Configuration Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `jai.router.llm-provider` | String | `builtin-ai` | `builtin-ai`, `openai`, or `anthropic` |
| `jai.router.confidence-threshold` | Double | `0.7` | Minimum confidence (0.0-1.0) |
| `jai.router.openai.api-key` | String | - | Your OpenAI API key |
| `jai.router.openai.model` | String | `gpt-4o-mini` | Model to use |
| `jai.router.anthropic.api-key` | String | - | Your Anthropic API key |
| `jai.router.anthropic.model` | String | `claude-3-5-haiku` | Model to use |

---

---

## Usage Examples

### Example 1: Chatbot Support Router

**Setup in `application.yml`:**

```yaml
jai:
  router:
    llm-provider: openai  # Or use builtin-ai
    confidence-threshold: 0.7
    
    services:
      - id: billing-support
        display-name: "Billing Support"
        keywords: [invoice, payment, refund, billing, charge, cost]
        
      - id: technical-support
        display-name: "Technical Support"
        keywords: [bug, error, crash, timeout, failed, broken]
        
      - id: general-support
        display-name: "General Support"
        keywords: [help, support, question, how, can you]
```

**Controller:**

```java
@RestController
@RequestMapping("/support")
public class SupportRouter {
    
    private final Router router;
    
    public SupportRouter(Router router) {
        this.router = router;
    }
    
    @PostMapping("/ticket")
    public ResponseEntity<Map<String, Object>> createTicket(
            @RequestBody String userMessage) {
        
        // Route based on user's description
        RoutingResult routing = router.route(userMessage);
        
        return ResponseEntity.ok(Map.of(
            "service", routing.getService(),
            "confidence", routing.getConfidence(),
            "reason", routing.getExplanation(),
            "message", userMessage
        ));
    }
}
```

### Example 2: Voice Assistant Routing

```java
@Component
public class VoiceAssistantRouter {
    
    private final Router router;
    
    public VoiceAssistantRouter(Router router) {
        this.router = router;
    }
    
    public String handleVoiceCommand(String spokenText) {
        RoutingResult result = router.route(spokenText);
        
        if (result.getConfidence() < 0.7) {
            return "Sorry, I didn't understand. Can you rephrase?";
        }
        
        return callService(result.getService(), spokenText);
    }
}
```

### Example 3: Core Library (No Spring)

If you don't want Spring Boot, you can use the core library directly:

```java
import io.jai.router.core.*;
import io.jai.router.llm.BuiltInAIProvider;
import io.jai.router.registry.*;

public class SimpleRouter {
    
    public static void main(String[] args) {
        // Create service registry
        ServiceRegistry registry = new InMemoryServiceRegistry();
        
        registry.register(ServiceDefinition.builder()
            .id("support-billing")
            .keywords(List.of("invoice", "payment", "billing"))
            .build());
        
        registry.register(ServiceDefinition.builder()
            .id("support-technical")
            .keywords(List.of("error", "bug", "broken"))
            .build());
        
        // Create router
        Router router = new RouterEngine(
            new BuiltInAIProvider(), 
            registry
        );
        
        // Route request
        RoutingResult result = router.route("I got an error");
        System.out.println("Route to: " + result.getService());
    }
}
```

---

## Performance & Expectations

### Latency by Provider

| Provider | Latency | Use Case |
|----------|---------|----------|
| **Built-in** | 12-35ms | Fast path for simple intents |
| **OpenAI** | 100-300ms | Complex semantic understanding |
| **Anthropic** | 120-280ms | High accuracy classification |

### Cost Comparison

| Provider | Cost per 1K Requests | Suitable For |
|----------|---------------------|--------------|
| **Built-in** | $0.00 | Offline, simple routing |
| **OpenAI** | $0.15 | Production systems |
| **Anthropic** | $0.30 | High-accuracy requirements |

### Important: Speed vs Accuracy Tradeoff

- **Use Built-in provider** if you need < 50ms latency
- **Use OpenAI/Anthropic** if accuracy matters more than latency
- **Don't use LLMs** if you need sub-100ms latency for routing

---

## LLM Providers

### Quick Comparison

| Provider | Setup | Latency | Cost | Accuracy | Best For |
|----------|-------|---------|------|----------|----------|
| **Built-in** | ✅ Zero config | 12-35ms | $0 | 84% | Offline, fast, simple |
| **OpenAI** | 🔑 API key | 100-300ms | $0.15/1K | 95% | Production, complex intents |
| **Anthropic** | 🔑 API key | 120-280ms | $0.30/1K | 95% | Safety-critical, accurate |

### Built-in Provider (Keyword Matching)

**Best for:** Offline routing, simple intent classification, high throughput

```yaml
jai:
  router:
    llm-provider: builtin-ai
    services:
      - id: support-billing
        keywords: [billing, invoice, payment, refund]
```

### OpenAI Provider (GPT-4o-mini)

**Best for:** Complex semantic understanding, production systems

**Requirements:**
- OpenAI API key
- $0.00015 per request (typical)
- Internet connectivity

```yaml
jai:
  router:
    llm-provider: openai
    openai:
      api-key: ${OPENAI_API_KEY}
      model: gpt-4o-mini  # Fast and cheap
```

### Anthropic Provider (Claude)

**Best for:** Safety-critical applications, high accuracy needed

**Requirements:**
- Anthropic API key
- $0.0008 per request (typical)
- Internet connectivity

```yaml
jai:
  router:
    llm-provider: anthropic
    anthropic:
      api-key: ${ANTHROPIC_API_KEY}
      model: claude-3-5-haiku
```

---

## Troubleshooting

### Common Issues and Solutions

| Issue | Cause | Solution |
|-------|-------|----------|
| **RouterEngine bean not found** | Missing auto-configuration | Ensure `spring.factories` exists in `META-INF/` |
| **No services registered** | Empty service list | Add services to `jai.router.services` in application.yml |
| **Low confidence scores** | Insufficient keywords | Add more specific keywords to ServiceDefinition |
| **High latency with OpenAI** | Network/API delays | Enable caching, use built-in provider for simple cases |
| **Build fails** | Java version mismatch | Upgrade to Java 17+ |
| **Tests fail** | Classpath issues | Run `./gradlew clean test` |

### Debugging Tips

#### Enable Debug Logging
```yaml
logging:
  level:
    io.jai.router: DEBUG
```

#### Check Router Configuration
```java
@Autowired
private Router router;

@PostConstruct
public void logRouterInfo() {
    logger.info("Router class: {}", router.getClass().getName());
    logger.info("Provider: {}", ((RouterEngine) router).getProvider().getName());
}
```

#### Validate Service Registry
```java
@Autowired
private ServiceRegistry registry;

@PostConstruct
public void validateServices() {
    List<ServiceDefinition> services = registry.getAllServices();
    logger.info("Registered services: {}", services.size());
    services.forEach(s -> logger.info("  - {}: {}", s.getId(), s.getKeywords()));
}
```

---

## Contributing

We welcome contributions! Here's how to get started:

### Development Requirements

| Tool | Version | Purpose |
|------|---------|---------|
| Java | 17+ | Runtime and compilation |
| Gradle | 8.x | Build tool (wrapper included) |
| Git | Latest | Version control |
| IDE | IntelliJ IDEA / VS Code | Development environment |

### Setup

```bash
# Fork and clone
git clone https://github.com/YOUR_USERNAME/JAI-Router.git
cd JAI-Router

# Create feature branch
git checkout -b feature/amazing-feature

# Build and test
./gradlew clean build test
```

### Code Standards

| Aspect | Standard | Tool/Reference |
|--------|----------|----------------|
| **Style** | Google Java Style Guide | Checkstyle (configured) |
| **Formatting** | 4-space indent, 120 char line | IDE formatter |
| **Commits** | Conventional Commits | `feat:`, `fix:`, `docs:`, etc. |
| **Tests** | 80%+ coverage target | JUnit 5 + Mockito |
| **Nullability** | `@Nonnull`, `@Nullable` annotations | JSR-305 |
| **Javadoc** | Public APIs must have docs | Standard Javadoc |

### Pull Request Process

1. **Update tests**: Add/modify tests for your changes
2. **Run checks**: `./gradlew clean build test`
3. **Update docs**: Modify README/TECHNICAL if needed
4. **Commit**: Use conventional commits format
5. **Push**: Push to your fork
6. **PR**: Open PR against `develop` branch
7. **Review**: Address review comments
8. **Merge**: Maintainer will merge after approval

### Commit Message Format

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types**: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`

**Examples**:
```
feat(core): add support for custom confidence threshold per service
fix(openai): handle timeout gracefully with exponential backoff
docs(readme): update configuration examples for hybrid mode
test(registry): add integration tests for service registration
```

---

## License

This project is licensed under the **MIT License**.

### MIT License Summary

```
MIT License

Copyright (c) 2025 JAI Router Contributors

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

See [LICENSE](LICENSE) for full text.

---

## Contact

For questions or to contribute, contact: rrezart.prebreza@gmail.com

### Get Help

| Channel | Link | Purpose |
|---------|------|---------|
| **GitHub Issues** | [Create Issue](https://github.com/JAI-create-spec/JAI-Router/issues/new) | Bug reports, feature requests |
| **GitHub Discussions** | [Join Discussion](https://github.com/JAI-create-spec/JAI-Router/discussions) | Questions, ideas, community |
| **Email** | rrezart.prebreza@gmail.com | Direct contact |
| **Documentation** | [Technical Docs](TECHNICAL.md) | Deep-dive technical reference |

### Useful Links

| Resource | URL |
|----------|-----|
| **Repository** | https://github.com/JAI-create-spec/JAI-Router |
| **Development Branch** | https://github.com/JAI-create-spec/JAI-Router/tree/develop |
| **Release Notes** | https://github.com/JAI-create-spec/JAI-Router/releases |
| **Issue Tracker** | https://github.com/JAI-create-spec/JAI-Router/issues |
| **CI/CD Pipeline** | https://github.com/JAI-create-spec/JAI-Router/actions |

---

## Acknowledgments

Built with:
- Java 17+
- Spring Boot 3.x
- Gradle 8.x

Inspired by modern microservice patterns and AI-assisted development practices.
