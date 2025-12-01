package io.jai.router.registry;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link ServiceDefinition}.
 */
@DisplayName("ServiceDefinition Tests")
class ServiceDefinitionTest {

    @Test
    @DisplayName("Should create valid service definition")
    void shouldCreateValidServiceDefinition() {
        ServiceDefinition service = new ServiceDefinition(
                "test-service",
                "Test Service",
                List.of("test", "keyword")
        );

        assertThat(service.id()).isEqualTo("test-service");
        assertThat(service.displayName()).isEqualTo("Test Service");
        assertThat(service.keywords()).containsExactly("test", "keyword");
    }

    @Test
    @DisplayName("Should create service using factory method")
    void shouldCreateServiceUsingFactoryMethod() {
        ServiceDefinition service = ServiceDefinition.of(
                "auth-service",
                "Auth Service",
                List.of("login", "token")
        );

        assertThat(service).isNotNull();
        assertThat(service.id()).isEqualTo("auth-service");
    }

    @Test
    @DisplayName("Should throw NPE when ID is null")
    void shouldThrowNPEWhenIdIsNull() {
        assertThatThrownBy(() -> new ServiceDefinition(
                null,
                "Display Name",
                List.of("keyword")
        )).isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Service ID cannot be null");
    }

    @Test
    @DisplayName("Should throw NPE when display name is null")
    void shouldThrowNPEWhenDisplayNameIsNull() {
        assertThatThrownBy(() -> new ServiceDefinition(
                "test-id",
                null,
                List.of("keyword")
        )).isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Display name cannot be null");
    }

    @Test
    @DisplayName("Should throw NPE when keywords are null")
    void shouldThrowNPEWhenKeywordsAreNull() {
        assertThatThrownBy(() -> new ServiceDefinition(
                "test-id",
                "Display Name",
                null
        )).isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Keywords list cannot be null");
    }

    @Test
    @DisplayName("Should throw exception when ID is blank")
    void shouldThrowExceptionWhenIdIsBlank() {
        assertThatThrownBy(() -> new ServiceDefinition(
                "  ",
                "Display Name",
                List.of("keyword")
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Service ID cannot be blank");
    }

    @Test
    @DisplayName("Should make keywords immutable")
    void shouldMakeKeywordsImmutable() {
        List<String> mutableList = new ArrayList<>();
        mutableList.add("keyword1");
        mutableList.add("keyword2");

        ServiceDefinition service = new ServiceDefinition(
                "test-service",
                "Test Service",
                mutableList
        );

        // Modify original list
        mutableList.add("keyword3");

        // Service keywords should remain unchanged
        assertThat(service.keywords()).hasSize(2);
        assertThat(service.keywords()).doesNotContain("keyword3");
    }

    @Test
    @DisplayName("Should return immutable keywords list")
    void shouldReturnImmutableKeywordsList() {
        ServiceDefinition service = new ServiceDefinition(
                "test-service",
                "Test Service",
                List.of("keyword1")
        );

        List<String> keywords = service.keywords();

        assertThatThrownBy(() -> keywords.add("new-keyword"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("Should support empty keywords list")
    void shouldSupportEmptyKeywordsList() {
        ServiceDefinition service = new ServiceDefinition(
                "test-service",
                "Test Service",
                List.of()
        );

        assertThat(service.keywords()).isEmpty();
    }

    @Test
    @DisplayName("Should implement equals correctly")
    void shouldImplementEqualsCorrectly() {
        ServiceDefinition service1 = new ServiceDefinition(
                "test-id",
                "Test Service",
                List.of("keyword")
        );

        ServiceDefinition service2 = new ServiceDefinition(
                "test-id",
                "Test Service",
                List.of("keyword")
        );

        ServiceDefinition service3 = new ServiceDefinition(
                "different-id",
                "Test Service",
                List.of("keyword")
        );

        assertThat(service1).isEqualTo(service2);
        assertThat(service1).isNotEqualTo(service3);
    }

    @Test
    @DisplayName("Should implement hashCode correctly")
    void shouldImplementHashCodeCorrectly() {
        ServiceDefinition service1 = new ServiceDefinition(
                "test-id",
                "Test Service",
                List.of("keyword")
        );

        ServiceDefinition service2 = new ServiceDefinition(
                "test-id",
                "Test Service",
                List.of("keyword")
        );

        assertThat(service1.hashCode()).isEqualTo(service2.hashCode());
    }
}

