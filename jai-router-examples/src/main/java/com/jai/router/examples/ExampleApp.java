package com.jai.router.examples;

import com.jai.router.core.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication(scanBasePackages = "com.jai.router")
@RestController
@RequestMapping("/api/router")
public class ExampleApp {

    private static final Logger logger = LoggerFactory.getLogger(ExampleApp.class);
    private final LlmClient client;

    public ExampleApp(LlmClient client){
        this.client = client;
    }

    @PostMapping("/route")
    public ResponseEntity<RoutingDecision> route(@RequestBody String payload){
        DecisionContext ctx = DecisionContext.of(payload);
        RoutingDecision decision = client.decide(ctx);
        logger.info("Routing decision service='{}' confidence={}", decision.service(), decision.confidence());
        return ResponseEntity.ok(decision);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<RoutingDecision> handleBadRequest(IllegalArgumentException ex) {
        logger.warn("Bad request: {}", ex.getMessage());
        RoutingDecision error = new RoutingDecision("error", 0.0, ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(LlmClientException.class)
    public ResponseEntity<RoutingDecision> handleLlmError(LlmClientException ex) {
        logger.error("LLM client error: {}", ex.getMessage());
        RoutingDecision error = new RoutingDecision("error", 0.0, ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(error);
    }

    public static void main(String[] args){
        SpringApplication.run(ExampleApp.class, args);
    }
}
