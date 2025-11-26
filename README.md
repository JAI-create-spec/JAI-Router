# JAI Router

Lightweight AI-assisted routing engine for Java and Spring Boot.

JAI Router classifies short natural-language requests and routes them to the most appropriate microservice. It ships with an offline, explainable classifier and supports pluggable LLM providers for more advanced routing.

Key features
------------
- Offline built-in classifier (no cloud required)
- Pluggable LLM providers (OpenAI, Anthropic, local HTTP endpoints, custom)
- Small, dependency-free core module (can be used outside Spring)
- Spring Boot auto-configuration and starter for easy integration
- Example applications showing common usage and REST endpoints

Requirements
------------
- Java 17 (or Java 11 if your toolchain requires it) — ensure JAVA_HOME is set to a compatible JDK
- Gradle wrapper is included; use `./gradlew` (Unix/macOS: `chmod +x gradlew` if needed)

Quick start
-----------
Clone and build:

```bash
git clone https://github.com/JAI-create-spec/JAI-Router.git
cd jai-router
./gradlew clean build
# run unit tests
./gradlew test
```

Run the example application (module names may vary):

```bash
# If the example is included in settings.gradle
./gradlew :jai-router-examples:simple-routing-demo:bootRun

# Or run from the example folder
cd jai-router-examples/simple-routing-demo
../../gradlew bootRun

# To run on a different port (avoid conflicts):
# Using Gradle (example: port 8090)
./gradlew :jai-router-examples:simple-routing-demo:bootRun -Dserver.port=8090

# Or run the built jar on a different port:
# (build with ./gradlew :jai-router-examples:bootJar)
java -jar jai-router-examples/build/libs/jai-router-examples-0.5.0-SNAPSHOT.jar --server.port=8090
```

Send a routing request to the example (adjust host/port as needed):

```bash
# The example accepts a JSON body matching RouteRequest: {"payload":"..."}
curl -X POST http://localhost:8085/api/router/route \
  -H "Content-Type: application/json" \
  -d '{"payload":"Generate a quarterly KPI dashboard"}'
```

Expected response (example):

```json
{
  "service": "bi-service",
  "confidence": 0.91,
  "explanation": "Detected keywords: quarterly, kpi, dashboard",
  "processingTimeMs": 5,
  "timestamp": "<ISO-8601 timestamp>",
  "metadata": { "provider": "builtin-ai", "keywords": ["generate","quarterly","kpi","dashboard"] }
}
```

Project layout
--------------
This repository uses a multi-module Gradle layout. Important modules:

```
jai-router/
├── jai-router-core/                    # Core logic (no Spring dependency)
│   └── src/main/java/io/jai/router/...
├── jai-router-spring-boot-autoconfigure/ # Spring Boot auto-configuration
│   └── src/main/java/io/jai/router/spring/...
├── jai-router-spring-boot-starter/     # Starter module (aggregates dependencies)
├── jai-router-examples/                # Example applications
│   └── simple-routing-demo/
├── build.gradle                        # Root build file
├── settings.gradle
└── README.md
```

Package namespace & migration
-----------------------------
The canonical package namespace is `io.jai.router`. If your sources or tests still use `com.jai.router`, update them to `io.jai.router`.

Migration checklist:
1. Replace `com.jai.router` → `io.jai.router` across source and test files.
2. Update imports in resource files (JSON/YAML) referencing classes.
3. Refresh IDE project settings if necessary.
4. Rebuild: `./gradlew clean build` and fix any compilation issues.

Tips & commands
---------------
- Find occurrences of the old package quickly with:

```bash
# from repo root
git grep -n "com.jai.router" || true
```

- Recommended IDE workflow: use IntelliJ IDEA's Refactor → Rename on the package node, then commit the changes. This keeps references and imports consistent.

Build & run notes
-----------------
- If Gradle reports a missing project for an example (for example: "project 'simple-routing-demo' not found"), open `settings.gradle` and ensure nested example modules are included. Example include line:

```gradle
include ':jai-router-core', ':jai-router-spring-boot-autoconfigure', ':jai-router-examples:simple-routing-demo'
```

- To inspect available projects and tasks:

```bash
./gradlew projects
./gradlew tasks --all
```

- If your example refuses to start because the port is already in use, either run it on another port or stop the process currently listening on that port. Example commands (macOS/Linux):

```bash
# Find the process using port 8085
lsof -nP -iTCP:8085 -sTCP:LISTEN
# Kill the process (replace <PID> with the PID from lsof)
kill <PID>
# If necessary, force kill
kill -9 <PID>

# Or run the example on a different port to avoid killing processes:
./gradlew :jai-router-examples:simple-routing-demo:bootRun -Dserver.port=8090
# Or when running the jar:
java -jar jai-router-examples/build/libs/jai-router-examples-0.5.0-SNAPSHOT.jar --server.port=8090
```

Usage example (Spring Boot)
--------------------------
Example controller showing how to use the `Router` bean (package names use `io.jai.router`):

```java
package io.jai.router.examples;

import io.jai.router.core.Router;
import io.jai.router.core.RoutingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RouterController {

    @Autowired
    private Router router;

    @PostMapping("/api/router/route")
    public RoutingResult route(@RequestBody String request) {
        return router.route(request);
    }
}
```

Configuration
-------------
Simple `application.yml` snippet to enable the built-in provider:

```yaml
# Example 1: Basic (Default)
jai:
  router:
    llm:
      provider: builtin-ai

# Example 2: With OpenAI
jai:
  router:
    llm:
      provider: openai
      openai-api-key: ${OPENAI_API_KEY}
      openai-model: gpt-4o-mini

# Example 3: Custom Services
jai:
  router:
    services:
      - id: payment-service
        keywords: [payment, invoice, billing]
        endpoint: http://localhost:8083
        priority: HIGH
```

Troubleshooting
---------------
- "RouterEngine bean not found": ensure auto-configuration is on the classpath and package `io.jai.router.spring` contains `JAIRouterAutoConfiguration`. Verify `src/main/resources/META-INF/spring.factories` or Spring Boot `spring.factories`/`spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` entries.
- "No services registered": verify `JAIRouterAutoConfiguration` registers default services or add services in `application.yml`.
- Build failures: run with `--stacktrace` to get the detailed cause:

```bash
./gradlew clean build --stacktrace
```

Contributing
------------
Contributions are welcome. Please open issues or pull requests. When contributing:
- Add tests for new functionality
- Keep changes focused and documented
- Run `./gradlew clean build` locally before submitting a PR

License
-------
This project is licensed under the MIT License — see the `LICENSE` file for details.

Contact & support
-----------------
If you have questions, feature requests, or want to contribute, please open an issue on GitHub or reach out by email.

- GitHub repository: [jai-router](https://github.com/JAI-create-spec/JAI-Router/tree/develop)
- Report issues: [Create an issue](https://github.com/JAI-create-spec/JAI-Router/issues)
- Email: [rrezart.prebreza@gmail.com](mailto:rrezart.prebreza@gmail.com)
