package com.burito.identity.controller.views;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "JWT token pair returned after successful authentication")
public record JWTToken(
    @Schema(description = "Short-lived access token", example = "eyJhbGciOiJIUzI1NiJ9...")
    String accessToken,

    @Schema(description = "Long-lived refresh token used to obtain a new access token", example = "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4...")
    String refreshToken,

    @Schema(description = "Access token validity in minutes", example = "60.0")
    double expiresInMins
) {
}
