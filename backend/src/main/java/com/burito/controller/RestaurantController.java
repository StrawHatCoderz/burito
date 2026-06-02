package com.burito.controller;

import com.burito.controller.views.APIResponse;
import com.burito.domain.Restaurant;
import com.burito.exceptions.APIException;
import com.burito.service.RestaurantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {
  private final RestaurantService restaurantService;

  public RestaurantController(RestaurantService restaurantService) {
    this.restaurantService = restaurantService;
  }

  @GetMapping("/")
  public ResponseEntity<APIResponse<List<Restaurant>>> serveAllRestaurant() {
    List<Restaurant> restaurantList = restaurantService.list();
    return ResponseEntity.ok(APIResponse.success(restaurantList));
  }

  @GetMapping("/{restaurantId}")
  public ResponseEntity<APIResponse<Restaurant>> serveRestaurant(@PathVariable UUID restaurantId) throws APIException {
    Restaurant restaurant = restaurantService.get(restaurantId);
    return ResponseEntity.ok(APIResponse.success(restaurant));
  }
}
