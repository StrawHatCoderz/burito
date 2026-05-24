package com.burito.controller;

import com.burito.controller.views.APIResponse;
import com.burito.repository.entities.Restaurant;
import com.burito.service.RestaurantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RestaurantController {
  private final RestaurantService restaurantService;

  public RestaurantController(RestaurantService restaurantService) {
    this.restaurantService = restaurantService;
  }

  @GetMapping("/restaurants/")
  public ResponseEntity<APIResponse<?>> serveAllRestaurant() {
    List<Restaurant> restaurantList = restaurantService.list();
    return ResponseEntity.ok(APIResponse.success(restaurantList));
  }
}
