package com.burito.controller;

import com.burito.controller.views.MenuItemRequest;
import com.burito.controller.views.UpdateRestaurantRequest;
import com.burito.domain.MenuItem;
import com.burito.domain.Restaurant;
import com.burito.service.AdminMenuService;
import com.burito.service.AdminRestaurantService;
import com.burito.service.JWTService;
import com.burito.utils.Parser;
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
  public ResponseEntity<Restaurant> updateRestaurant(
          @PathVariable UUID id,
          @RequestBody UpdateRestaurantRequest request,
          HttpServletRequest httpRequest) {
          
    String authHeader = httpRequest.getHeader("Authorization");
    String token = Parser.parseJwtToken(authHeader);
    String restaurantId = jwtService.extractRestaurantId(token);

    Restaurant updated = adminRestaurantService.updateRestaurant(id, restaurantId, request);
    return ResponseEntity.ok(updated);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('RESTAURANT_ADMIN')")
  public ResponseEntity<Restaurant> getRestaurant(
          @PathVariable UUID id,
          HttpServletRequest httpRequest) {
          
    String authHeader = httpRequest.getHeader("Authorization");
    String token = Parser.parseJwtToken(authHeader);
    String restaurantId = jwtService.extractRestaurantId(token);

    Restaurant restaurant = adminRestaurantService.getRestaurant(id, restaurantId);
    return ResponseEntity.ok(restaurant);
  }

  @PostMapping("/{id}/menu")
  @PreAuthorize("hasRole('RESTAURANT_ADMIN')")
  public ResponseEntity<MenuItem> addMenuItem(
          @PathVariable UUID id,
          @RequestBody MenuItemRequest request,
          HttpServletRequest httpRequest) {
          
    String authHeader = httpRequest.getHeader("Authorization");
    String token = Parser.parseJwtToken(authHeader);
    String restaurantId = jwtService.extractRestaurantId(token);

    MenuItem created = adminMenuService.createMenuItem(id, restaurantId, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @PutMapping("/{id}/menu/{itemId}")
  @PreAuthorize("hasRole('RESTAURANT_ADMIN')")
  public ResponseEntity<MenuItem> updateMenuItem(
          @PathVariable UUID id,
          @PathVariable UUID itemId,
          @RequestBody MenuItemRequest request,
          HttpServletRequest httpRequest) {
          
    String authHeader = httpRequest.getHeader("Authorization");
    String token = Parser.parseJwtToken(authHeader);
    String restaurantId = jwtService.extractRestaurantId(token);

    MenuItem updated = adminMenuService.updateMenuItem(id, itemId, restaurantId, request);
    return ResponseEntity.ok(updated);
  }

  @DeleteMapping("/{id}/menu/{itemId}")
  @PreAuthorize("hasRole('RESTAURANT_ADMIN')")
  public ResponseEntity<Void> deleteMenuItem(
          @PathVariable UUID id,
          @PathVariable UUID itemId,
          HttpServletRequest httpRequest) {
          
    String authHeader = httpRequest.getHeader("Authorization");
    String token = Parser.parseJwtToken(authHeader);
    String restaurantId = jwtService.extractRestaurantId(token);

    adminMenuService.deleteMenuItem(id, itemId, restaurantId);
    return ResponseEntity.noContent().build();
  }
}
