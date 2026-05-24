package com.burito.controller;

import com.burito.controller.views.APIResponse;
import com.burito.controller.views.ApiError;
import com.burito.controller.views.UserRegistrationRequest;
import com.burito.domain.User;
import com.burito.exceptions.InvalidCredentialsException;
import com.burito.exceptions.UsernameAlreadyExistsException;
import com.burito.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  public ResponseEntity<APIResponse<User>> handleUserRegistration(
          @RequestBody UserRegistrationRequest payload) {
    try {
      User user = authService.register(payload.username(), payload.password());
      return ResponseEntity.status(HttpStatus.CREATED)
              .body(APIResponse.success(user));
    } catch (InvalidCredentialsException | UsernameAlreadyExistsException e) {
      return ResponseEntity.status(e.getHttpStatus())
              .body(APIResponse.error(new ApiError(
                      e.getErrorCode(),
                      e.getMessage()
              )));
    }
  }
}
