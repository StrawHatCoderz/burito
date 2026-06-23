package com.burito.identity.controller;
import com.burito.catalog.controller.views.*;
import com.burito.identity.controller.views.*;
import com.burito.ordering.controller.views.*;
import com.burito.core.controller.views.*;
import com.burito.core.controller.views.APIResponse;
import com.burito.core.controller.views.ApiError;

import com.burito.core.controller.views.APIResponse;
import com.burito.core.controller.views.ApiError;
import com.burito.identity.controller.views.UserProfileView;
import com.burito.identity.domain.User;
import com.burito.identity.service.AuthService;
import com.burito.core.exceptions.UnauthorizedException;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User", description = "Authenticated user profile")
@RestController
@RequestMapping("/api")
public class UserController {
  private final AuthService authService;

  public UserController(AuthService authService) {
    this.authService = authService;
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
      throw new UnauthorizedException("User not found or token invalid");
    }
    return ResponseEntity.ok(APIResponse.success(
            new UserProfileView(user.getUserId(), user.getEmail(), user.getFullName(), user.getCreatedAt())
    ));
  }
}
