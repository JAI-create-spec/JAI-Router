JAI Router – README
Lightweight AI Routing Engine for Java + Spring Boot
Free • Offline • Zero Dependencies • Built-In AI Engine Included

🧩 What is JAI Router?
JAI Router is a Java + Spring Boot library that provides intelligent request routing inside backend systems.
It helps microservices decide “which service should handle this request?” using a combination of:
• Lightweight built-in AI
• Simple rules
• Explainability
• Confidence scoring

Designed for teams who want AI-assisted routing without depending on external cloud LLMs.

🧠 What Problem Does It Solve?
Microservice platforms often receive free-text or semi-structured requests such as:
• “generate KPI dashboard”
• “encrypt this payload”
• “verify this token”
• “create monthly report”

Traditionally, developers must write many if/else or regex rules. This becomes hard to maintain.
JAI Router solves this by providing a built-in AI classifier to automatically detect intent and route accordingly.

🚦 How JAI Router Works
1. User sends a request (text)
2. JAI Router analyzes the text using built-in AI
3. Determines the user’s intent
4. Selects the appropriate microservice
5. Returns service, confidence, explanation

Example:
{ "service": "bi-service", "confidence": 0.92, "explanation": "Detected BI terms: 'KPI', 'dashboard'" }

🌟 Who Is This Library For?
• Backend developers
• Teams with microservice architectures
• Companies building automation workflows
• Developers wanting AI features without OpenAI cost
• Anyone needing a “smart routing switchboard”

🎯 Why It’s Useful
• Reduces routing complexity
• Removes many if/else blocks
• Works completely offline
• AI-like reasoning at zero cost
• Supports optional external LLMs (OpenAI, local-http)
• Gives transparent explanations
• Easy Spring Boot integration
• Works out-of-the-box

🧩 Example Use Cases
✔ BI Automation – “Generate monthly dashboard” → bi-service
✔ Security & Cryptography – “encrypt my password” → cryptography-service
✔ Authentication – “validate this token” → auth-service
✔ Fallback – Unknown inputs → default-service

🚀 Simple Example
curl -X POST http://localhost:8080/api/router/route \
-H "Content-Type: application/json" \
-d "Create quarterly KPI report"

Output:
{ "service": "bi-service", "confidence": 0.90, "explanation": "Matched BI keywords: 'KPI', 'report'" }

🔧 Configuration (application.yml)
Default (Built-In AI):
jai:
router:
llm:
provider: builtin-ai

Local LLM:
provider: local-http
local-endpoint-url: http://localhost:11434/api/route

OpenAI:
provider: openai
openai-api-key: YOUR_KEY
openai-model: gpt-4o-mini

One-sentence Summary
JAI Router is an AI-powered request router for Java/Spring systems using a built-in, offline classifier to map natural-language input to microservice endpoints.

Project Structure
jai-router/
├── jai-router-core
├── jai-router-spring-boot-starter
└── jai-router-examples

License
MIT License – free for commercial and personal use.

