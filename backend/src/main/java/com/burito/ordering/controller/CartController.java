package com.burito.ordering.controller;
import com.burito.catalog.controller.views.*;
import com.burito.identity.controller.views.*;
import com.burito.ordering.controller.views.*;
import com.burito.core.controller.views.*;
import com.burito.core.controller.views.APIResponse;
import com.burito.core.controller.views.ApiError;

import com.burito.core.controller.views.APIResponse;
import com.burito.core.controller.views.ApiError;
import com.burito.ordering.controller.views.CartItemRequest;
import com.burito.ordering.controller.views.CartView;
import com.burito.identity.domain.User;
import com.burito.core.exceptions.APIException;
import com.burito.core.exceptions.UnauthorizedException;
import com.burito.identity.service.AuthService;
import com.burito.ordering.service.CartService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.HttpStatus;

import java.util.UUID;

@Tag(name = "Cart", description = "Cart management endpoints — authentication required")
@RestController
@RequestMapping("/api/cart")
public class CartController {

  private final CartService cartService;
  private final AuthService authService;

  public CartController(CartService cartService, AuthService authService) {
    this.cartService = cartService;
    this.authService = authService;
  }

  @Operation(summary = "Add an item to the cart", security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Item added successfully, returning updated cart"),
      @ApiResponse(responseCode = "400", description = "Menu item not found or unavailable",
          content = @Content(schema = @Schema(implementation = ApiError.class))),
      @ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @PostMapping("/items")
  public ResponseEntity<APIResponse<CartView>> addItem(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestHeader(value = "X-Guest-Id", required = false) UUID guestId,
      @RequestBody CartItemRequest request) throws APIException {

    UUID userId = null;
    if (userDetails != null) {
      User user = authService.getCurrentUser(userDetails.getUsername());
      if (user == null) {
        throw new UnauthorizedException("Unauthorized");
      }
      userId = user.getUserId();
    }

    if (userId == null && guestId == null) {
      throw new UnauthorizedException("Unauthorized");
    }

    CartView cartView = cartService.addItem(userId, guestId, request.menuItemId(), request.quantity());
    return ResponseEntity.ok(APIResponse.success(cartView));
  }

  @Operation(summary = "View current cart", security = @SecurityRequirement(name = "bearerAuth"))
  @GetMapping
  public ResponseEntity<APIResponse<CartView>> getCart(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestHeader(value = "X-Guest-Id", required = false) UUID guestId) throws APIException {

    UUID userId = null;
    if (userDetails != null) {
      User user = authService.getCurrentUser(userDetails.getUsername());
      if (user == null) {
        throw new UnauthorizedException("Unauthorized");
      }
      userId = user.getUserId();
    }

    if (userId == null && guestId == null) {
      throw new UnauthorizedException("Unauthorized");
    }

    CartView cartView = cartService.getCart(userId, guestId);
    return ResponseEntity.ok(APIResponse.success(cartView));
  }

  @Operation(summary = "Merge guest cart", security = @SecurityRequirement(name = "bearerAuth"))
  @PostMapping("/merge")
  public ResponseEntity<APIResponse<Void>> mergeCart(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestHeader(value = "X-Guest-Id", required = true) UUID guestId) throws APIException {

    if (userDetails == null) {
      throw new UnauthorizedException("Unauthorized");
    }
    User user = authService.getCurrentUser(userDetails.getUsername());
    if (user == null) {
      throw new UnauthorizedException("Unauthorized");
    }
    cartService.mergeCart(user.getUserId(), guestId);
    return ResponseEntity.ok(APIResponse.success(null));
  }

  @Operation(summary = "Remove an item from the cart", security = @SecurityRequirement(name = "bearerAuth"))
  @DeleteMapping("/items/{cartItemId}")
  public ResponseEntity<APIResponse<CartView>> removeItem(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestHeader(value = "X-Guest-Id", required = false) UUID guestId,
      @PathVariable UUID cartItemId) throws APIException {

    UUID userId = null;
    if (userDetails != null) {
      User user = authService.getCurrentUser(userDetails.getUsername());
      if (user == null) {
        throw new UnauthorizedException("Unauthorized");
      }
      userId = user.getUserId();
    }

    if (userId == null && guestId == null) {
      throw new UnauthorizedException("Unauthorized");
    }

    CartView cartView = cartService.removeItem(userId, guestId, cartItemId);
    return ResponseEntity.ok(APIResponse.success(cartView));
  }

  @Operation(summary = "Decrement item quantity", security = @SecurityRequirement(name = "bearerAuth"))
  @PutMapping("/items/{cartItemId}/decrement")
  public ResponseEntity<APIResponse<CartView>> decrementItem(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestHeader(value = "X-Guest-Id", required = false) UUID guestId,
      @PathVariable UUID cartItemId) throws APIException {

    UUID userId = null;
    if (userDetails != null) {
      User user = authService.getCurrentUser(userDetails.getUsername());
      if (user == null) {
        throw new UnauthorizedException("Unauthorized");
      }
      userId = user.getUserId();
    }

    if (userId == null && guestId == null) {
      throw new UnauthorizedException("Unauthorized");
    }

    CartView cartView = cartService.decrementItem(userId, guestId, cartItemId);
    return ResponseEntity.ok(APIResponse.success(cartView));
  }

  @Operation(summary = "Clear the entire cart", security = @SecurityRequirement(name = "bearerAuth"))
  @DeleteMapping
  public ResponseEntity<APIResponse<CartView>> clearCart(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestHeader(value = "X-Guest-Id", required = false) UUID guestId) throws APIException {

    UUID userId = null;
    if (userDetails != null) {
      User user = authService.getCurrentUser(userDetails.getUsername());
      if (user == null) {
        throw new UnauthorizedException("Unauthorized");
      }
      userId = user.getUserId();
    }

    if (userId == null && guestId == null) {
      throw new UnauthorizedException("Unauthorized");
    }

    CartView cartView = cartService.clearCart(userId, guestId);
    return ResponseEntity.ok(APIResponse.success(cartView));
  }
}
