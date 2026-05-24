package com.burito.controller.views;

import com.burito.enums.ErrorCode;

public record ApiError(ErrorCode errorCode, String message) {
}