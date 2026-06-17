package com.burito.controller;

import com.burito.controller.views.UpdateRestaurantRequest;
import com.burito.domain.Restaurant;
import com.burito.service.AdminRestaurantService;
import com.burito.service.JWTService;
import com.burito.utils.Parser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/restaurants")
public class AdminRestaurantController {

  private final AdminRestaurantService adminRestaurantService;
  private final JWTService jwtService;

  public AdminRestaurantController(AdminRestaurantService adminRestaurantService, JWTService jwtService) {
    this.adminRestaurantService = adminRestaurantService;
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
}
