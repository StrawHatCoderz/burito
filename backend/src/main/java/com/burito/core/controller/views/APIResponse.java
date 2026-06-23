package com.burito.core.controller.views;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Standard API envelope. On success, data is populated and error is null. On failure, error is populated and data is null.")
public record APIResponse<T>(
    @Schema(description = "true when the request succeeded, false on error")
    boolean success,

    @Schema(description = "Response payload, null on error")
    T data,

    @Schema(description = "Error details, null on success")
    ApiError error
) {

  public static <T> APIResponse<T> success(T data) {
    return new APIResponse<>(true, data, null);
  }

  public static <T> APIResponse<T> error(ApiError error) {
    return new APIResponse<>(false, null, error);
  }
}
