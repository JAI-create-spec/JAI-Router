package io.jai.router.core;

/**
 * Runtime exception indicating an error communicating with or using an LLM client.
 */
public class LlmClientException extends RuntimeException {

    public LlmClientException(String message) {
        super(message);
    }

    public LlmClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public LlmClientException(Throwable cause) {
        super(cause);
    }
}

