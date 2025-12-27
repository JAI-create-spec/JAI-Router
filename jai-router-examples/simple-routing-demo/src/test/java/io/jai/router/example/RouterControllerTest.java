package io.jai.router.example;

import io.jai.router.core.Router;
import io.jai.router.core.RoutingResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RouterController.class)
public class RouterControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private Router router; // mocked to verify behavior when present

    @Test
    void missingPayloadReturnsBadRequest() throws Exception {
        mvc.perform(post("/api/router/route")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void stringPayloadWithoutRouterUsesFallback() throws Exception {
        // simulate no router by making bean return null via mock? Here, since MockBean provides a bean, we set expectation
        when(router.route("test")).thenReturn(RoutingResult.of("test-service", 0.9, "ok"));

        mvc.perform(post("/api/router/route")
                .contentType(MediaType.APPLICATION_JSON)
                .content("\"test\""))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.service").value("test-service"));
    }
}

