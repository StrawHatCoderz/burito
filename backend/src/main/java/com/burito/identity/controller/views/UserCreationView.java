package com.burito.identity.controller.views;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "User details returned after successful registration")
public record UserCreationView(
    @Schema(description = "Unique identifier of the created user", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    UUID userId,

    @Schema(description = "Email address of the created user", example = "jane@example.com")
    String email
) {
}
