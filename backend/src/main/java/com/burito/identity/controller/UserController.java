package com.burito.identity.controller;

import com.burito.identity.controller.views.*;
import com.burito.core.controller.views.*;
import com.burito.identity.domain.User;
import com.burito.identity.domain.UserAddress;
import com.burito.identity.service.AuthService;
import com.burito.identity.service.UserService;
import com.burito.core.exceptions.APIException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User", description = "Authenticated user profile")
@RestController
@RequestMapping("/api")
public class UserController {
  private final AuthService authService;
  private final UserService userService;

  public UserController(AuthService authService, UserService userService) {
    this.authService = authService;
    this.userService = userService;
  }

  @Operation(summary = "Get the current user's profile", security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Profile returned successfully"),
      @ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/me")
  public ResponseEntity<APIResponse<UserProfileView>> getCurrentUser(
          @AuthenticationPrincipal UserDetails userDetails) {
    User user = authService.getCurrentUser(userDetails.getUsername());
    if (user == null) {
      throw APIException.unauthorized();
    }
    return ResponseEntity.ok(APIResponse.success(
            new UserProfileView(
                    user.getUserId(),
                    user.getEmail(),
                    user.getFullName(),
                    user.getPhoneNumber(),
                    user.getAddress(),
                    user.getCreatedAt()
            )
    ));
  }

  @Operation(summary = "Update the current user's profile details", security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid phone number format or empty name",
          content = @Content(schema = @Schema(implementation = ApiError.class))),
      @ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @PutMapping("/me")
  public ResponseEntity<APIResponse<UserProfileView>> updateProfile(
          @AuthenticationPrincipal UserDetails userDetails,
          @RequestBody UpdateProfileRequest request) {
    User user = authService.getCurrentUser(userDetails.getUsername());
    if (user == null) {
      throw APIException.unauthorized();
    }
    User updatedUser = userService.updateProfile(user.getUserId(), request.fullName(), request.phoneNumber());
    return ResponseEntity.ok(APIResponse.success(
            new UserProfileView(
                    updatedUser.getUserId(),
                    updatedUser.getEmail(),
                    updatedUser.getFullName(),
                    updatedUser.getPhoneNumber(),
                    updatedUser.getAddress(),
                    updatedUser.getCreatedAt()
            )
    ));
  }

  @Operation(summary = "Update the current user's delivery address", security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Address updated successfully"),
      @ApiResponse(responseCode = "400", description = "Missing required address fields",
          content = @Content(schema = @Schema(implementation = ApiError.class))),
      @ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @PutMapping("/me/address")
  public ResponseEntity<APIResponse<UserProfileView>> updateAddress(
          @AuthenticationPrincipal UserDetails userDetails,
          @RequestBody UpdateAddressRequest request) {
    User user = authService.getCurrentUser(userDetails.getUsername());
    if (user == null) {
      throw APIException.unauthorized();
    }
    
    UserAddress newAddress = new UserAddress();
    newAddress.setStreet(request.street());
    newAddress.setCity(request.city());
    newAddress.setState(request.state());
    newAddress.setZipcode(request.zipcode());
    newAddress.setCountry(request.country());

    User updatedUser = userService.updateAddress(user.getUserId(), newAddress);
    return ResponseEntity.ok(APIResponse.success(
            new UserProfileView(
                    updatedUser.getUserId(),
                    updatedUser.getEmail(),
                    updatedUser.getFullName(),
                    updatedUser.getPhoneNumber(),
                    updatedUser.getAddress(),
                    updatedUser.getCreatedAt()
            )
    ));
  }
}
