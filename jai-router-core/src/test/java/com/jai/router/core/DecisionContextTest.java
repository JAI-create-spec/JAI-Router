package com.jai.router.core;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

public class DecisionContextTest {

    @Test
    void nullPayloadShouldThrow() {
        assertThatThrownBy(() -> DecisionContext.of(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Payload cannot be null or empty");
    }

    @Test
    void blankPayloadShouldThrow() {
        assertThatThrownBy(() -> DecisionContext.of("   "))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void payloadIsTrimmed() {
        DecisionContext ctx = DecisionContext.of("  hello world  ");
        assertThat(ctx.payload()).isEqualTo("hello world");
    }
}

