package io.jai.router.core;

/**
 * Runtime exception indicating an error communicating with or using an LLM client.
 * <p>
 * This exception is thrown when:
 * <ul>
 *   <li>LLM client initialization fails</li>
 *   <li>Communication with external LLM service fails</li>
 *   <li>LLM client configuration is invalid</li>
 *   <li>Required dependencies are missing</li>
 * </ul>
 * </p>
 *
 * @author JAI Router Team
 * @since 0.4.0
 */
public class LlmClientException extends RuntimeException {

    /**
     * Constructs a new LLM client exception with the specified detail message.
     *
     * @param message the detail message explaining the error
     */
    public LlmClientException(String message) {
        super(message);
    }

    /**
     * Constructs a new LLM client exception with the specified detail message and cause.
     *
     * @param message the detail message explaining the error
     * @param cause   the cause of this exception
     */
    public LlmClientException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new LLM client exception with the specified cause.
     *
     * @param cause the cause of this exception
     */
    public LlmClientException(Throwable cause) {
        super(cause);
    }
}

