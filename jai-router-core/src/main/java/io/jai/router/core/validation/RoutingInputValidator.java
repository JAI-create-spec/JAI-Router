package io.jai.router.core.validation;

import io.jai.router.core.exception.InvalidInputException;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Validator for routing input with security checks and sanitization.
 * <p>
 * Validates input for:
 * <ul>
 *   <li>Null or empty values</li>
 *   <li>Length constraints (1-10,000 characters)</li>
 *   <li>Dangerous patterns (XSS, script injection)</li>
 * </ul>
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>
 * RoutingInputValidator validator = new RoutingInputValidator();
 * validator.validate(userInput);
 * String cleaned = validator.sanitize(userInput);
 * </pre>
 *
 * @author JAI Router Team
 * @since 0.5.0
 */
public class RoutingInputValidator {

    /**
     * Maximum allowed input length in characters.
     */
    public static final int MAX_INPUT_LENGTH = 10_000;

    /**
     * Minimum allowed input length in characters.
     */
    public static final int MIN_INPUT_LENGTH = 1;

    private static final Pattern DANGEROUS_PATTERN = Pattern.compile(
            "<script|javascript:|on\\w+\\s*=",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Validates the input against security and length constraints.
     *
     * @param input the input string to validate
     * @throws InvalidInputException if validation fails
     */
    public void validate(@NotNull String input) throws InvalidInputException {
        if (input == null) {
            throw new InvalidInputException("Input cannot be null");
        }

        String trimmed = input.trim();

        if (trimmed.isEmpty()) {
            throw new InvalidInputException(
                    "Input too short (min: " + MIN_INPUT_LENGTH + " character)"
            );
        }

        if (trimmed.length() > MAX_INPUT_LENGTH) {
            throw new InvalidInputException(
                    String.format("Input too long (max: %d characters, got: %d)",
                            MAX_INPUT_LENGTH, trimmed.length())
            );
        }

        if (DANGEROUS_PATTERN.matcher(trimmed).find()) {
            throw new InvalidInputException(
                    "Input contains dangerous patterns (potential XSS or script injection)"
            );
        }
    }

    /**
     * Sanitizes the input by removing dangerous characters and normalizing whitespace.
     * <p>
     * Sanitization steps:
     * <ul>
     *   <li>Trim leading/trailing whitespace</li>
     *   <li>Normalize multiple spaces to single space</li>
     *   <li>Remove angle brackets (&lt; and &gt;)</li>
     * </ul>
     * </p>
     *
     * @param input the input string to sanitize
     * @return sanitized string
     * @throws NullPointerException if input is null
     */
    @NotNull
    public String sanitize(@NotNull String input) {
        Objects.requireNonNull(input, "Input cannot be null");
        return input.trim()
                .replaceAll("\\s+", " ")
                .replaceAll("[<>]", "");
    }
}
