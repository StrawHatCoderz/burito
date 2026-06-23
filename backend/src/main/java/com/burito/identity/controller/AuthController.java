package com.burito.identity.controller;
import com.burito.catalog.controller.views.*;
import com.burito.identity.controller.views.*;
import com.burito.ordering.controller.views.*;
import com.burito.core.controller.views.*;
import com.burito.core.controller.views.APIResponse;
import com.burito.core.controller.views.ApiError;


import com.burito.core.exceptions.APIException;
import com.burito.identity.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "Registration, login, token refresh, and logout")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @Operation(summary = "Register a new user")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "User registered successfully",
          content = @Content(schema = @Schema(implementation = UserCreationView.class))),
      @ApiResponse(responseCode = "409", description = "Email already registered — errorCode: EMAIL_ALREADY_EXISTS",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @PostMapping("/register")
  public ResponseEntity<APIResponse<UserCreationView>> handleUserRegistration(@RequestBody AuthRequest payload) throws APIException {
    UserCreationView user = authService.register(payload.full_name(), payload.email(), payload.password());
    return ResponseEntity.status(HttpStatus.CREATED).body(APIResponse.success(user));
  }

  @Operation(summary = "Login with email and password")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Login successful — returns JWT access + refresh tokens",
          content = @Content(schema = @Schema(implementation = JWTToken.class))),
      @ApiResponse(responseCode = "401", description = "Wrong email or password — errorCode: INVALID_CREDENTIALS",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @PostMapping("/login")
  public ResponseEntity<APIResponse<JWTToken>> handleUserLogin(@RequestBody AuthRequest payload) throws APIException {
    JWTToken jwtToken = authService.login(payload.email(), payload.password());
    return ResponseEntity.ok(APIResponse.success(jwtToken));
  }

  @Operation(summary = "Refresh the access token using a valid refresh token")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Token refreshed — returns new JWT pair",
          content = @Content(schema = @Schema(implementation = JWTToken.class))),
      @ApiResponse(responseCode = "401", description = "Refresh token invalid or expired — errorCode: INVALID_REFRESH_TOKEN",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @PostMapping("/refresh")
  public ResponseEntity<APIResponse<JWTToken>> handleTokenRefresh(@RequestBody RefreshRequest payload) throws APIException {
    JWTToken jwtToken = authService.refresh(payload.refreshToken());
    return ResponseEntity.ok(APIResponse.success(jwtToken));
  }

  @Operation(summary = "Logout — invalidate the refresh token")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Logged out successfully")
  })
  @PostMapping("/logout")
  public ResponseEntity<APIResponse<Void>> handleLogout(@RequestBody RefreshRequest payload) {
    authService.logout(payload.refreshToken());
    return ResponseEntity.ok(APIResponse.success(null));
  }
}
