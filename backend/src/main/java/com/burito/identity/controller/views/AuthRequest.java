package com.burito.identity.controller.views;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Credentials for registration or login")
public record AuthRequest(
    @Schema(description = "Full name of the user (required for registration only)", example = "Jane Doe")
    String full_name,

    @Schema(description = "User email address", example = "jane@example.com")
    String email,

    @Schema(description = "Password (min 8 characters recommended)", example = "s3cr3tP@ss")
    String password
) {
}
