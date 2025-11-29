# JAI Router

A lightweight AI-assisted routing engine for Java and Spring Boot.

JAI Router classifies short natural-language requests and routes them to the most appropriate microservice. It provides a small core (framework-agnostic), optional Spring Boot auto-configuration and starter, and example apps to help you get started quickly.

Highlights
- Small, dependency-free core module suitable for use outside of Spring.
- Pluggable LLM provider interface and a built-in offline classifier.
- Spring Boot autoconfigure + starter for quick integration.
- Example application demonstrating REST endpoints and a minimal usage pattern.

Repository layout

```
jai-router/
├── jai-router-core/                    # Core logic (no Spring dependency)
├── jai-router-spring-boot-autoconfigure/ # Auto-configuration for Spring Boot
├── jai-router-spring-boot-starter/     # Starter module (aggregates dependencies)
├── jai-router-examples/                # Example applications
│   └── simple-routing-demo/
├── build.gradle                        # Root build file
├── settings.gradle
└── README.md
```

Quick start (build & test)
1. Clone the repository and run a build using the included Gradle wrapper (recommended):

```bash
git clone https://github.com/JAI-create-spec/JAI-Router.git
cd JAI-Router
# Make the wrapper executable once on macOS/Linux
chmod +x gradlew || true
./gradlew clean build
```

2. Run unit tests:

```bash
./gradlew test
```

Run the example application

The example app is a minimal Spring Boot demo located at `jai-router-examples/simple-routing-demo`.

From the repository root you can run:

```bash
./gradlew :jai-router-examples:simple-routing-demo:bootRun
```

Or run the built jar:

```bash
./gradlew :jai-router-examples:simple-routing-demo:bootJar
java -jar jai-router-examples/simple-routing-demo/build/libs/*.jar --server.port=8085
```

API example

Route a single request (adjust host/port if you changed them):

```bash
curl -X POST http://localhost:8085/api/router/route \
  -H "Content-Type: application/json" \
  -d '"Generate a quarterly KPI dashboard"'
```

The response is a `RoutingResult` JSON object describing the selected service, confidence and an explanation.

Configuration

The example and Spring auto-configuration read properties under the `jai.router` prefix. Example `application.yml` snippets:

```yaml
# Use built-in classifier
jai:
  router:
    llm:
      provider: builtin-ai
    confidence-threshold: 0.7

# Add custom services
jai:
  router:
    services:
      - id: payment-service
        displayName: Payment Service
        keywords: [payment, invoice, billing]
        endpoint: http://localhost:8083
        priority: HIGH
```

OpenAI provider (optional)

The project includes an optional OpenAI provider in the autoconfigure module. To enable it set `jai.router.llm.provider` to `openai` and provide an API key via environment variable or configuration.

```yaml
jai:
  router:
    llm:
      provider: openai
      # openai-specific configuration
      openai:
        api-key: ${OPENAI_API_KEY}    # set in env or CI secrets
        model: gpt-4o-mini
        temperature: 0.0
```

Notes:
- The autoconfiguration will create an OpenAI-backed `LlmClient` when `jai.router.llm.provider=openai` and a non-empty `jai.router.openai.api-key` is present.
- For predictable parsing and safety the client instructs the model to return machine-parseable JSON; however, you should always validate responses and provide fallbacks.
- Do NOT commit API keys into source code. Use environment variables, CI secrets, or a secrets manager in production.
- Prefer `temperature: 0.0` for deterministic routing decisions.

IDE import (IntelliJ, Eclipse, NetBeans, VS Code)

This project is a standard Gradle multi-module build and can be imported into major Java IDEs. Use the Gradle wrapper when importing.

- IntelliJ IDEA: Open the repository root, import as a Gradle project, and select "Use Gradle wrapper". The Gradle tool window lists modules and tasks (e.g. `:jai-router-examples:simple-routing-demo:bootRun`).
- Eclipse (Buildship): File → Import → Gradle → Existing Gradle Project → choose project root and use the Gradle wrapper.
- NetBeans: File → Open Project → select repository root; NetBeans recognizes Gradle projects.
- VS Code: Install Java Extension Pack and Gradle extensions, then open the folder and import the Gradle project. Use the Gradle tasks view or CLI to run `bootRun`.

Prerequisites
- Java 17+ (required for Spring Boot 3.x features used by the example). Ensure `JAVA_HOME` points to a compatible JDK.
- Gradle wrapper is included; prefer `./gradlew` so contributors use a consistent Gradle version.

Development notes
- Core (`jai-router-core`) is intentionally framework-agnostic. It performs basic validation and offers an LLM provider interface for custom routing strategies.
- The example uses constructor injection and `ObjectProvider<Router>` so the example runs even when auto-configuration is not present.

Contributing
- Please open issues or pull requests on GitHub. When contributing:
  - Add or update unit tests for new behavior.
  - Keep changes focused and documented.
  - Run `./gradlew clean build` before submitting a PR.

CI and code quality
- This repository can be scanned by Qodana / other linters; review CI workflow files under `.github/workflows` and `qodana.yaml` if you add/adjust checks.

License
- MIT — see `LICENSE` for details.

Contact & support
- Repository: https://github.com/JAI-create-spec/JAI-Router/tree/develop
- Issues: https://github.com/JAI-create-spec/JAI-Router/issues
- Email: rrezart.prebreza@gmail.com

Thank you for using JAI Router — contributions and feedback are welcome.
