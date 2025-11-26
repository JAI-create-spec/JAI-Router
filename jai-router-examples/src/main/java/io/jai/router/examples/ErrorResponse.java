package io.jai.router.examples;

/** Error response DTO. */
public record ErrorResponse(
    String code,
    String message,
    long timestamp
) {
    public ErrorResponse(String code, String message) {
        this(code, message, System.currentTimeMillis());
    }
}
