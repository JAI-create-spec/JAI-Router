
package com.jai.router.examples;

import com.jai.router.core.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RestController
@RequestMapping("/api/router")
public class ExampleApp {

    private final LlmClient client;

    public ExampleApp(LlmClient client){
        this.client = client;
    }

    @PostMapping("/route")
    public RoutingDecision route(@RequestBody String payload){
        return client.infer(new DecisionContext(payload));
    }

    public static void main(String[] args){
        SpringApplication.run(ExampleApp.class, args);
    }
}
