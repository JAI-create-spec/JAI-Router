package com.jai.router.examples;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
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
        ResponseEntity<String> resp = restTemplate.postForEntity(url, "Please generate KPI report", String.class);
        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        String body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body).contains("bi-service");
    }
}
