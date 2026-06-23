package com.burito.catalog.controller;
import com.burito.catalog.controller.views.*;
import com.burito.identity.controller.views.*;
import com.burito.ordering.controller.views.*;
import com.burito.core.controller.views.*;
import com.burito.core.controller.views.APIResponse;
import com.burito.core.controller.views.ApiError;

import com.burito.catalog.controller.views.MenuItemRequest;
import com.burito.catalog.controller.views.UpdateRestaurantRequest;
import com.burito.catalog.domain.MenuItem;
import com.burito.catalog.domain.Restaurant;
import com.burito.catalog.service.AdminMenuService;
import com.burito.catalog.service.AdminRestaurantService;
import com.burito.identity.service.JWTService;
import com.burito.core.utils.Parser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/restaurants")
public class AdminRestaurantController {

  private final AdminRestaurantService adminRestaurantService;
  private final AdminMenuService adminMenuService;
  private final JWTService jwtService;

  public AdminRestaurantController(AdminRestaurantService adminRestaurantService, AdminMenuService adminMenuService, JWTService jwtService) {
    this.adminRestaurantService = adminRestaurantService;
    this.adminMenuService = adminMenuService;
    this.jwtService = jwtService;
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('RESTAURANT_ADMIN')")
  public ResponseEntity<APIResponse<Restaurant>> updateRestaurant(
          @PathVariable UUID id,
          @RequestBody UpdateRestaurantRequest request,
          HttpServletRequest httpRequest) {
          
    String authHeader = httpRequest.getHeader("Authorization");
    String token = Parser.parseJwtToken(authHeader);
    String restaurantId = jwtService.extractRestaurantId(token);

    Restaurant updated = adminRestaurantService.updateRestaurant(id, restaurantId, request);
    return ResponseEntity.ok(APIResponse.success(updated));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('RESTAURANT_ADMIN')")
  public ResponseEntity<APIResponse<Restaurant>> getRestaurant(
          @PathVariable UUID id,
          HttpServletRequest httpRequest) {
          
    String authHeader = httpRequest.getHeader("Authorization");
    String token = Parser.parseJwtToken(authHeader);
    String restaurantId = jwtService.extractRestaurantId(token);

    Restaurant restaurant = adminRestaurantService.getRestaurant(id, restaurantId);
    return ResponseEntity.ok(APIResponse.success(restaurant));
  }

  @PostMapping("/{id}/menu")
  @PreAuthorize("hasRole('RESTAURANT_ADMIN')")
  public ResponseEntity<APIResponse<MenuItem>> addMenuItem(
          @PathVariable UUID id,
          @RequestBody MenuItemRequest request,
          HttpServletRequest httpRequest) {
          
    String authHeader = httpRequest.getHeader("Authorization");
    String token = Parser.parseJwtToken(authHeader);
    String restaurantId = jwtService.extractRestaurantId(token);

    MenuItem created = adminMenuService.createMenuItem(id, restaurantId, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(APIResponse.success(created));
  }

  @PutMapping("/{id}/menu/{itemId}")
  @PreAuthorize("hasRole('RESTAURANT_ADMIN')")
  public ResponseEntity<APIResponse<MenuItem>> updateMenuItem(
          @PathVariable UUID id,
          @PathVariable UUID itemId,
          @RequestBody MenuItemRequest request,
          HttpServletRequest httpRequest) {
          
    String authHeader = httpRequest.getHeader("Authorization");
    String token = Parser.parseJwtToken(authHeader);
    String restaurantId = jwtService.extractRestaurantId(token);

    MenuItem updated = adminMenuService.updateMenuItem(id, itemId, restaurantId, request);
    return ResponseEntity.ok(APIResponse.success(updated));
  }

  @DeleteMapping("/{id}/menu/{itemId}")
  @PreAuthorize("hasRole('RESTAURANT_ADMIN')")
  public ResponseEntity<APIResponse<Void>> deleteMenuItem(
          @PathVariable UUID id,
          @PathVariable UUID itemId,
          HttpServletRequest httpRequest) {
          
    String authHeader = httpRequest.getHeader("Authorization");
    String token = Parser.parseJwtToken(authHeader);
    String restaurantId = jwtService.extractRestaurantId(token);

    adminMenuService.deleteMenuItem(id, itemId, restaurantId);
    return ResponseEntity.ok(APIResponse.success(null));
  }
}
