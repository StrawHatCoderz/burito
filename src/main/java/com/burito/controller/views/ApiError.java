package com.burito.controller.views;

import com.burito.enums.ErrorCodes;

public record ApiError(ErrorCodes errorCode, String message) {
}