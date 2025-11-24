package com.jai.router.core;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

public class RoutingDecisionTest {

    @Test
    void invalidConfidenceLowThrows() {
        assertThatThrownBy(() -> RoutingDecision.of("svc", -0.1, "x"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void invalidConfidenceHighThrows() {
        assertThatThrownBy(() -> RoutingDecision.of("svc", 1.1, "x"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nullServiceThrows() {
        assertThatThrownBy(() -> RoutingDecision.of(null, 0.5, "x"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void validDecision() {
        RoutingDecision r = RoutingDecision.of("svc", 0.5, "ok");
        assertThat(r.service()).isEqualTo("svc");
        assertThat(r.confidence()).isEqualTo(0.5);
    }
}

