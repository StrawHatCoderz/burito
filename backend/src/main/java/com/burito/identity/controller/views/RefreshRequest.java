package com.burito.identity.controller.views;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Refresh token payload")
public record RefreshRequest(
    @Schema(description = "The refresh token issued at login", example = "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4...")
    String refreshToken
) {
}
