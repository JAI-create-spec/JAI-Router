package io.jai.router.core;

import io.jai.router.core.RoutingDecision;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class RoutingDecisionTest {

    @Test
    void validDecision() {
        RoutingDecision r = RoutingDecision.of("svc", 0.5, "ok");
        assertThat(r.service()).isEqualTo("svc");
        assertThat(r.confidence()).isEqualTo(0.5);
    }

    @Test
    void ofValidatesServiceNotNullOrBlank() {
        assertThatThrownBy(() -> RoutingDecision.of(null, 0.5, "explain"))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> RoutingDecision.of("  ", 0.5, "explain"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void confidenceIsClamped() {
        RoutingDecision r1 = RoutingDecision.of("svc", 1.5, "e");
        assertThat(r1.confidence()).isEqualTo(1.0);
        RoutingDecision r2 = RoutingDecision.of("svc", -0.5, "e");
        assertThat(r2.confidence()).isEqualTo(0.0);
    }
}
