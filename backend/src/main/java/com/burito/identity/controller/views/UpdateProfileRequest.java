package com.burito.identity.controller.views;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request model to update user profile details")
public record UpdateProfileRequest(
    @Schema(description = "Full name of the user", example = "Jane Doe")
    String fullName,

    @Schema(description = "Phone number of the user", example = "+1234567890")
    String phoneNumber
) {
}
