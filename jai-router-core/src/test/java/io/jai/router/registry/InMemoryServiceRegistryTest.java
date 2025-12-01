package io.jai.router.registry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link InMemoryServiceRegistry}.
 */
@DisplayName("InMemoryServiceRegistry Tests")
class InMemoryServiceRegistryTest {

    private InMemoryServiceRegistry registry;
    private List<ServiceDefinition> initialServices;

    @BeforeEach
    void setUp() {
        initialServices = List.of(
                new ServiceDefinition("auth-service", "Authentication Service", List.of("login", "token", "auth")),
                new ServiceDefinition("bi-service", "Business Intelligence", List.of("report", "dashboard", "analytics"))
        );
        registry = new InMemoryServiceRegistry(new ArrayList<>(initialServices));
    }

    @Test
    @DisplayName("Should create registry with initial services")
    void shouldCreateRegistryWithInitialServices() {
        assertThat(registry.listServices()).hasSize(2);
        assertThat(registry.size()).isEqualTo(2);
        assertThat(registry.isEmpty()).isFalse();
    }

    @Test
    @DisplayName("Should throw NPE when services list is null")
    void shouldThrowNPEWhenServicesNull() {
        assertThatThrownBy(() -> new InMemoryServiceRegistry(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Services list cannot be null");
    }

    @Test
    @DisplayName("Should return immutable copy of services")
    void shouldReturnImmutableCopyOfServices() {
        List<ServiceDefinition> services = registry.listServices();

        assertThatThrownBy(() -> services.add(
                new ServiceDefinition("new-service", "New Service", List.of("test"))
        )).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("Should find service by ID")
    void shouldFindServiceById() {
        Optional<ServiceDefinition> found = registry.findServiceById("auth-service");

        assertThat(found).isPresent();
        assertThat(found.get().id()).isEqualTo("auth-service");
        assertThat(found.get().displayName()).isEqualTo("Authentication Service");
    }

    @Test
    @DisplayName("Should return empty when service not found")
    void shouldReturnEmptyWhenServiceNotFound() {
        Optional<ServiceDefinition> found = registry.findServiceById("nonexistent");

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should register new service successfully")
    void shouldRegisterNewService() {
        ServiceDefinition newService = new ServiceDefinition(
                "crypto-service",
                "Cryptography Service",
                List.of("encrypt", "decrypt")
        );

        boolean registered = registry.registerService(newService);

        assertThat(registered).isTrue();
        assertThat(registry.size()).isEqualTo(3);
        assertThat(registry.findServiceById("crypto-service")).isPresent();
    }

    @Test
    @DisplayName("Should not register duplicate service")
    void shouldNotRegisterDuplicateService() {
        ServiceDefinition duplicate = new ServiceDefinition(
                "auth-service",
                "Duplicate Auth",
                List.of("login")
        );

        boolean registered = registry.registerService(duplicate);

        assertThat(registered).isFalse();
        assertThat(registry.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should deregister service successfully")
    void shouldDeregisterService() {
        boolean deregistered = registry.deregisterService("auth-service");

        assertThat(deregistered).isTrue();
        assertThat(registry.size()).isEqualTo(1);
        assertThat(registry.findServiceById("auth-service")).isEmpty();
    }

    @Test
    @DisplayName("Should return false when deregistering nonexistent service")
    void shouldReturnFalseWhenDeregisteringNonexistent() {
        boolean deregistered = registry.deregisterService("nonexistent");

        assertThat(deregistered).isFalse();
        assertThat(registry.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should clear all services")
    void shouldClearAllServices() {
        registry.clear();

        assertThat(registry.isEmpty()).isTrue();
        assertThat(registry.size()).isZero();
        assertThat(registry.listServices()).isEmpty();
    }

    @Test
    @DisplayName("Should be thread-safe for concurrent operations")
    void shouldBeThreadSafe() throws InterruptedException {
        // Test concurrent registration
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                registry.registerService(new ServiceDefinition(
                        "service-" + i,
                        "Service " + i,
                        List.of("keyword" + i)
                ));
            }
        });

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                registry.listServices();
            }
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        // Should complete without errors
        assertThat(registry.size()).isGreaterThan(2);
    }
}

