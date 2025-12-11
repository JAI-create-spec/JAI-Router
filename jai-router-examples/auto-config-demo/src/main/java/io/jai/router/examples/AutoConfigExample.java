package io.jai.router.examples;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;

import io.jai.router.core.Router;
import io.jai.router.core.RoutingResult;

/**
 * Example Spring Boot application demonstrating auto-configuration.
 * <p>
 * This app shows how JAI Router auto-configuration works:
 * <ul>
 *   <li>Zero manual configuration needed</li>
 *   <li>Router bean automatically created</li>
 *   <li>Services configured via application.yml</li>
 *   <li>Hybrid routing support (when enabled)</li>
 * </ul>
 * </p>
 *
 * @author JAI Router Team
 * @since 0.6.0
 */
@SpringBootApplication
public class AutoConfigExample {

    public static void main(String[] args) {
        SpringApplication.run(AutoConfigExample.class, args);
        System.out.println("🚀 JAI Router Auto-Config Example is running!");
        System.out.println("   Try: curl -X POST http://localhost:8080/route -d '\"Show me analytics\"' -H 'Content-Type: application/json'");
    }
}

@RestController
class AutoConfigController {

    @Autowired
    private Router router; // Auto-configured by JAI Router

    @PostMapping("/route")
    public RoutingResult route(@RequestBody String request) {
        return router.route(request);
    }

    @GetMapping("/")
    public String home() {
        return """
            <h1>JAI Router Auto-Configuration Demo</h1>
            <p>Router is automatically configured!</p>
            <h3>Try these requests:</h3>
            <pre>
            curl -X POST http://localhost:8080/route \\
              -H "Content-Type: application/json" \\
              -d '"Show me analytics dashboard"'
              
            curl -X POST http://localhost:8080/route \\
              -H "Content-Type: application/json" \\
              -d '"Process user login"'
            </pre>
            """;
    }
}
