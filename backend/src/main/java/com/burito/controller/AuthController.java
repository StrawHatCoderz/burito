package com.burito.controller;

import com.burito.controller.views.*;
import com.burito.exceptions.APIException;
import com.burito.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  public ResponseEntity<APIResponse<UserCreationView>> handleUserRegistration(@RequestBody AuthRequest payload) throws APIException {
    UserCreationView user = authService.register(payload.full_name(), payload.email(), payload.password());
    return ResponseEntity.status(HttpStatus.CREATED).body(APIResponse.success(user));
  }

  @PostMapping("/login")
  public ResponseEntity<APIResponse<JWTToken>> handleUserLogin(@RequestBody AuthRequest payload) throws APIException {
    JWTToken jwtToken = authService.login(payload.email(), payload.password());
    return ResponseEntity.ok(APIResponse.success(jwtToken));
  }

  @PostMapping("/refresh")
  public ResponseEntity<APIResponse<JWTToken>> handleTokenRefresh(@RequestBody RefreshRequest payload) throws APIException {
    JWTToken jwtToken = authService.refresh(payload.refreshToken());
    return ResponseEntity.ok(APIResponse.success(jwtToken));
  }

  @PostMapping("/logout")
  public ResponseEntity<APIResponse<Void>> handleLogout(@RequestBody RefreshRequest payload) {
    authService.logout(payload.refreshToken());
    return ResponseEntity.ok(APIResponse.success(null));
  }
}
