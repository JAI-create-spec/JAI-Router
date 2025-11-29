package io.jai.router.core.exception;

public class ServiceNotFoundException extends RoutingException {
    public ServiceNotFoundException(String serviceId) {
        super("Service not found: " + serviceId, serviceId, 0.0);
    }
}

