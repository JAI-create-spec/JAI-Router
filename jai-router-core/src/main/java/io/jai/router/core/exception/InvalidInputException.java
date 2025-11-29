package io.jai.router.core.exception;

public class InvalidInputException extends RoutingException {
    public InvalidInputException(String message) {
        super("Invalid input: " + message);
    }
}

