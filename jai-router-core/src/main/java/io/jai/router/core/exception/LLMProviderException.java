package io.jai.router.core.exception;

public class LLMProviderException extends RoutingException {
    public LLMProviderException(String provider, Throwable cause) {
        super("LLM provider failed: " + provider, cause);
    }
}

