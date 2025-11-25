package com.jai.router.examples;

import jakarta.validation.constraints.NotBlank;

/** Request DTO for routing. */
public record RouteRequest(
    @NotBlank(message = "Payload cannot be blank")
    String payload
) {}

