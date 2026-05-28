package com.burito.controller.views;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserProfileView(UUID id, String email, String name, LocalDateTime createdAt) {
}
