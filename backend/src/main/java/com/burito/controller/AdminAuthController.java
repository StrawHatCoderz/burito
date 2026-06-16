package com.burito.controller;

import com.burito.controller.views.JWTToken;
import com.burito.controller.views.UserCreationView;
import com.burito.exceptions.EmailAlreadyExistsException;
import com.burito.exceptions.InvalidCredentialsException;
import com.burito.service.AuthService;
import com.burito.enums.CuisineType;
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
  public ResponseEntity<UserCreationView> register(@RequestBody AdminRegisterRequest request) {
    try {
      UserCreationView view = authService.registerAdmin(
          request.fullName(), request.email(), request.password(),
          request.restaurantName(), request.cuisineType(), request.estDeliveryMinutes()
      );
      return new ResponseEntity<>(view, HttpStatus.CREATED);
    } catch (EmailAlreadyExistsException e) {
      return new ResponseEntity<>(HttpStatus.CONFLICT);
    } catch (InvalidCredentialsException e) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
  }

  public record AdminLoginRequest(String email, String password) {}

  @PostMapping("/login")
  @Operation(summary = "Login an existing restaurant admin")
  public ResponseEntity<JWTToken> login(@RequestBody AdminLoginRequest request) {
    try {
      JWTToken token = authService.login(request.email(), request.password());
      return new ResponseEntity<>(token, HttpStatus.OK);
    } catch (InvalidCredentialsException e) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
  }
}
