package io.jai.router.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link RouterEngine}.
 */
@DisplayName("RouterEngine Tests")
class RouterEngineTest {

    private LlmClient mockClient;
    private RouterEngine router;

    @BeforeEach
    void setUp() {
        mockClient = mock(LlmClient.class);
        router = new RouterEngine(mockClient);
    }

    @Test
    @DisplayName("Should throw NPE when client is null")
    void shouldThrowNPEWhenClientIsNull() {
        assertThatThrownBy(() -> new RouterEngine(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("LlmClient cannot be null");
    }

    @Test
    @DisplayName("Should route successfully")
    void shouldRouteSuccessfully() {
        // Given
        String input = "Login to the system";
        DecisionContext expectedCtx = DecisionContext.of(input);
        RoutingDecision decision = RoutingDecision.of("auth-service", 0.95, "Login keyword matched");

        when(mockClient.decide(any(DecisionContext.class))).thenReturn(decision);

        // When
        RoutingResult result = router.route(input);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.service()).isEqualTo("auth-service");
        assertThat(result.confidence()).isEqualTo(0.95);
        assertThat(result.explanation()).isEqualTo("Login keyword matched");

        verify(mockClient, times(1)).decide(any(DecisionContext.class));
    }

    @Test
    @DisplayName("Should throw NPE when input is null")
    void shouldThrowNPEWhenInputIsNull() {
        assertThatThrownBy(() -> router.route(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Input cannot be null");
    }

    @Test
    @DisplayName("Should throw exception when LLM client returns null")
    void shouldThrowExceptionWhenClientReturnsNull() {
        // Given
        when(mockClient.decide(any(DecisionContext.class))).thenReturn(null);

        // When/Then
        assertThatThrownBy(() -> router.route("test input"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("LLM client returned no decision");
    }

    @Test
    @DisplayName("Should propagate LLM client exceptions")
    void shouldPropagateLlmClientExceptions() {
        // Given
        when(mockClient.decide(any(DecisionContext.class)))
                .thenThrow(new LlmClientException("LLM service unavailable"));

        // When/Then
        assertThatThrownBy(() -> router.route("test input"))
                .isInstanceOf(LlmClientException.class)
                .hasMessageContaining("LLM service unavailable");
    }

    @Test
    @DisplayName("Should handle empty input appropriately")
    void shouldHandleEmptyInput() {
        // Empty input should be caught by DecisionContext validation
        assertThatThrownBy(() -> router.route("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Payload cannot be empty");
    }

    @Test
    @DisplayName("Should trim input before routing")
    void shouldTrimInputBeforeRouting() {
        // Given
        String inputWithWhitespace = "   test input   ";
        RoutingDecision decision = RoutingDecision.of("test-service", 0.8, "matched");

        when(mockClient.decide(any(DecisionContext.class))).thenReturn(decision);

        // When
        RoutingResult result = router.route(inputWithWhitespace);

        // Then
        assertThat(result).isNotNull();
        verify(mockClient, times(1)).decide(argThat(ctx ->
                ctx.payload().equals("test input")
        ));
    }
}

