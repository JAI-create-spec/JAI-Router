package com.jai.router.examples;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ExampleAppIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void postRouteReturnsDecision() {
        String url = "http://localhost:" + port + "/api/router/route";
        RouteRequest request = new RouteRequest("Please generate KPI report");
        ResponseEntity<RouteResponse> resp = restTemplate.postForEntity(url, request, RouteResponse.class);
        
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().service()).isEqualTo("bi-service");
        assertThat(resp.getBody().confidence()).isGreaterThan(0.5);
        assertThat(resp.getBody().processingTimeMs()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void postRouteWithEncryptKeyword() {
        String url = "http://localhost:" + port + "/api/router/route";
        RouteRequest request = new RouteRequest("Please encrypt this data");
        ResponseEntity<RouteResponse> resp = restTemplate.postForEntity(url, request, RouteResponse.class);
        
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().service()).isEqualTo("cryptography-service");
    }

    @Test
    void postRouteWithEmptyPayloadReturnsBadRequest() {
        String url = "http://localhost:" + port + "/api/router/route";
        RouteRequest request = new RouteRequest("");
        ResponseEntity<ErrorResponse> resp = restTemplate.postForEntity(url, request, ErrorResponse.class);
        
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().code()).isEqualTo("INVALID_ARGUMENT");
    }

    @Test
    void postRouteWithUnknownKeywordReturnsDefaultService() {
        String url = "http://localhost:" + port + "/api/router/route";
        RouteRequest request = new RouteRequest("hello world");
        ResponseEntity<RouteResponse> resp = restTemplate.postForEntity(url, request, RouteResponse.class);
        
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().service()).isEqualTo("default-service");
        assertThat(resp.getBody().confidence()).isEqualTo(0.5);
    }
}
