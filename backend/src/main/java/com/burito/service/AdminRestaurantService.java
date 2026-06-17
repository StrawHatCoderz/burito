package com.burito.service;

import com.burito.controller.views.UpdateRestaurantRequest;
import com.burito.domain.Restaurant;
import com.burito.exceptions.RestaurantNotFoundException;
import com.burito.repository.RestaurantRepo;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AdminRestaurantService {

  private final RestaurantRepo restaurantRepo;

  public AdminRestaurantService(RestaurantRepo restaurantRepo) {
    this.restaurantRepo = restaurantRepo;
  }

  public Restaurant updateRestaurant(UUID restaurantId, String tokenRestaurantId, UpdateRestaurantRequest request) {
    if (tokenRestaurantId == null || !tokenRestaurantId.equals(restaurantId.toString())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied. You can only update your own restaurant.");
    }

    Restaurant restaurant = restaurantRepo.findById(restaurantId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant not found"));

    if (request.getRestaurantName() != null) {
      restaurant.setRestaurantName(request.getRestaurantName());
    }
    if (request.getCuisineType() != null) {
      restaurant.setCuisineType(request.getCuisineType());
    }
    restaurant.setEstDeliveryMinutes(request.getEstDeliveryMinutes());
    restaurant.setOpen(request.isOpen());
    
    if (request.getImageUrl() != null) {
      restaurant.setImageUrl(request.getImageUrl());
    }

    return restaurantRepo.save(restaurant);
  }

  public Restaurant getRestaurant(UUID restaurantId, String tokenRestaurantId) {
    if (tokenRestaurantId == null || !tokenRestaurantId.equals(restaurantId.toString())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied. You can only view your own restaurant.");
    }

    return restaurantRepo.findById(restaurantId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant not found"));
  }
}
