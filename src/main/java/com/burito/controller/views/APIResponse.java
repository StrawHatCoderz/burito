package com.burito.controller.views;

public record APIResponse<T>(
        boolean success,
        T data,
        ApiError error
) {

  public static <T> APIResponse<T> success(T data) {
    return new APIResponse<>(true, data, null);
  }

  public static <T>APIResponse<T> error(ApiError error) {
    return new APIResponse<>(false, null, error);
  }
}