package com.burito.identity.controller.views;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;
import com.burito.identity.domain.UserAddress;

@Schema(description = "Profile of the currently authenticated user")
public record UserProfileView(
    @Schema(description = "Unique identifier of the user", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    UUID id,

    @Schema(description = "Email address", example = "jane@example.com")
    String email,

    @Schema(description = "Full name", example = "Jane Doe")
    String name,

    @Schema(description = "Phone number", example = "1234567890")
    String phoneNumber,

    @Schema(description = "Delivery address")
    UserAddress address,

    @Schema(description = "Account creation timestamp", example = "2025-01-15T10:30:00")
    LocalDateTime createdAt
) {
}
