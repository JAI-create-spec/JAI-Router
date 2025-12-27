package io.jai.router.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LearningRouterTest {
    @Test
    void testLearningRouterTracksSuccessRates() {
        Router mockRouter = input -> new RoutingResult("serviceA", 0.9, "ok", 10L, java.time.Instant.now());
        LearningRouter learningRouter = new LearningRouter(mockRouter);
        for (int i = 0; i < 5; i++) {
            learningRouter.route("test");
        }
        assertEquals(1.0, learningRouter.getSuccessRate("serviceA"), 0.01);
    }

    @Test
    void testLearningRouterHandlesLowConfidence() {
        Router mockRouter = input -> new RoutingResult("serviceB", 0.5, "low", 10L, java.time.Instant.now());
        LearningRouter learningRouter = new LearningRouter(mockRouter);
        for (int i = 0; i < 5; i++) {
            learningRouter.route("test");
        }
        assertEquals(0.0, learningRouter.getSuccessRate("serviceB"), 0.01);
    }
}
