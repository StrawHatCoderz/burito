package com.burito.identity.controller;
import com.burito.catalog.controller.views.*;
import com.burito.identity.controller.views.*;
import com.burito.ordering.controller.views.*;
import com.burito.core.controller.views.*;
import com.burito.core.controller.views.APIResponse;
import com.burito.core.controller.views.ApiError;

import com.burito.identity.controller.views.JWTToken;
import com.burito.identity.controller.views.UserCreationView;
import com.burito.identity.exceptions.EmailAlreadyExistsException;
import com.burito.identity.exceptions.InvalidCredentialsException;
import com.burito.identity.service.AuthService;
import com.burito.catalog.enums.CuisineType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/auth")
@Tag(name = "Admin Auth", description = "Admin Authentication API")
public class AdminAuthController {
  private final AuthService authService;

  public AdminAuthController(AuthService authService) {
    this.authService = authService;
  }

  public record AdminRegisterRequest(
      String fullName,
      String email,
      String password,
      String restaurantName,
      CuisineType cuisineType,
      double estDeliveryMinutes
  ) {}

  @PostMapping("/register")
  @Operation(summary = "Register a new restaurant admin")
  public ResponseEntity<APIResponse<UserCreationView>> register(@RequestBody AdminRegisterRequest request) {
    try {
      UserCreationView view = authService.registerAdmin(
          request.fullName(), request.email(), request.password(),
          request.restaurantName(), request.cuisineType(), request.estDeliveryMinutes()
      );
      return new ResponseEntity<>(APIResponse.success(view), HttpStatus.CREATED);
    } catch (EmailAlreadyExistsException e) {
      throw com.burito.core.exceptions.APIException.badRequest("Email already exists");
    } catch (InvalidCredentialsException e) {
      throw com.burito.core.exceptions.APIException.badRequest("Invalid credentials");
    }
  }

  public record AdminLoginRequest(String email, String password) {}

  @PostMapping("/login")
  @Operation(summary = "Login an existing restaurant admin")
  public ResponseEntity<APIResponse<JWTToken>> login(@RequestBody AdminLoginRequest request) {
    try {
      JWTToken token = authService.login(request.email(), request.password());
      return new ResponseEntity<>(APIResponse.success(token), HttpStatus.OK);
    } catch (InvalidCredentialsException e) {
      throw com.burito.core.exceptions.APIException.unauthorized();
    }
  }
}
