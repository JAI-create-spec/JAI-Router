package io.jai.router.core.validation;

import io.jai.router.core.exception.InvalidInputException;

import java.util.regex.Pattern;
import java.util.Objects;

public class RoutingInputValidator {

    private static final int MAX_INPUT_LENGTH = 10_000;
    private static final int MIN_INPUT_LENGTH = 1;
    private static final Pattern DANGEROUS_PATTERN = Pattern.compile("<script|javascript:|on\\w+\\s*=", Pattern.CASE_INSENSITIVE);

    public void validate(String input) throws InvalidInputException {
        if (input == null) throw new InvalidInputException("Input cannot be null");
        String trimmed = input.trim();
        if (trimmed.isEmpty()) {
            throw new InvalidInputException("Input too short (min: " + MIN_INPUT_LENGTH + ")");
        }
        if (trimmed.length() > MAX_INPUT_LENGTH) {
            throw new InvalidInputException("Input too long (max: " + MAX_INPUT_LENGTH + ")");
        }
        if (DANGEROUS_PATTERN.matcher(trimmed).find()) {
            throw new InvalidInputException("Input contains dangerous patterns");
        }
    }

    public String sanitize(String input) {
        Objects.requireNonNull(input, "input cannot be null");
        return input.trim()
                .replaceAll("\\s+", " ")
                .replaceAll("[<>]", "");
    }
}
