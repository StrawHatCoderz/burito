package com.burito.controller.views;

import com.burito.enums.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Error details returned on a failed request")
public record ApiError(
    @Schema(description = "Machine-readable error code", example = "INVALID_CREDENTIALS")
    ErrorCode errorCode,

    @Schema(description = "Human-readable error message", example = "Invalid email or password")
    String message
) {
}
