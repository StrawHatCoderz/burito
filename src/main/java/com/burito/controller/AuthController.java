package com.burito.controller;

import com.burito.controller.views.*;
import com.burito.enums.ErrorCode;
import com.burito.exceptions.InvalidCredentialsException;
import com.burito.exceptions.EmailAlreadyExistsException;
import com.burito.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
  public ResponseEntity<APIResponse<UserCreationView>> handleUserRegistration(
          @RequestBody AuthRequest payload) {
    try {
      UserCreationView user = authService.register(
              payload.email(),
              payload.password()
      );

      return ResponseEntity.status(HttpStatus.CREATED)
              .body(APIResponse.success(user));
    } catch (InvalidCredentialsException | EmailAlreadyExistsException e) {
      return ResponseEntity.status(e.getHttpStatus())
              .body(APIResponse.error(new ApiError(
                      e.getErrorCode(),
                      e.getMessage()
              )));
    }
  }

  @PostMapping("/login")
  public ResponseEntity<APIResponse<JWTToken>> handleUserLogin(
          @RequestBody AuthRequest payload) {
    try {
      JWTToken jwtToken = authService.login(payload.email(), payload.password());
      return ResponseEntity.ok(APIResponse.success(jwtToken));
    } catch (InvalidCredentialsException e) {
      return ResponseEntity.status(e.getHttpStatus())
              .body(APIResponse.error(new ApiError(
                      e.getErrorCode(),
                      e.getMessage()
              )));
    } catch (UsernameNotFoundException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body(APIResponse.error(new ApiError(
                      ErrorCode.USERNAME_NOT_FOUND,
                      e.getMessage()
                      )));
    }
  }


}
