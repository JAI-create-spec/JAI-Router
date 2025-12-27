package io.jai.router.core.exception;

/**
 * Exception thrown when an external LLM provider fails.
 * <p>
 * This exception is thrown when communication with external LLM services
 * (such as OpenAI, Anthropic, etc.) fails due to network issues, API errors,
 * or service unavailability.
 * </p>
 *
 * @author JAI Router Team
 * @since 1.0.0
 */
public class LlmProviderException extends RoutingException {

    /**
     * Constructs a new LLM provider exception with the specified provider name and cause.
     *
     * @param provider the name of the LLM provider that failed
     * @param cause    the underlying cause of the failure
     */
    public LlmProviderException(String provider, Throwable cause) {
        super("LLM provider failed: " + provider, cause);
    }
}

