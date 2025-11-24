package com.jai.router.core;

/**
 * Runtime exception used for LLM client related errors.
 */
public class LlmClientException extends RuntimeException {
    public LlmClientException(String message) { super(message); }
    public LlmClientException(String message, Throwable cause) { super(message, cause); }
    public LlmClientException(Throwable cause) { super(cause); }
}

