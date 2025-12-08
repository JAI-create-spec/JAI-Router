package io.jai.examples.gateway.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class GatewayController {
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${order.service.url:http://localhost:8081}")
    private String orderServiceUrl;

    @Value("${user.service.url:http://localhost:8082}")
    private String userServiceUrl;

    @GetMapping("/orders")
    public ResponseEntity<?> orders() {
        String url = orderServiceUrl + "/orders";
        ResponseEntity<Object> resp = restTemplate.getForEntity(url, Object.class);
        return ResponseEntity.status(resp.getStatusCode()).body(resp.getBody());
    }

    @GetMapping("/users")
    public ResponseEntity<?> users() {
        String url = userServiceUrl + "/users";
        ResponseEntity<Object> resp = restTemplate.getForEntity(url, Object.class);
        return ResponseEntity.status(resp.getStatusCode()).body(resp.getBody());
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "component", "gateway");
    }
}

