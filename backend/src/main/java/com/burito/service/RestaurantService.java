package com.burito.service;

import com.burito.enums.CuisineType;
import com.burito.exceptions.RestaurantNotFoundException;
import com.burito.repository.RestaurantRepo;
import com.burito.domain.Restaurant;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class RestaurantService {

  private final RestaurantRepo restaurantRepo;

  public RestaurantService(RestaurantRepo restaurantRepo) {
    this.restaurantRepo = restaurantRepo;
  }

  public List<Restaurant> list() {
    return restaurantRepo.findAll();
  }

  public List<Restaurant> search(String name, CuisineType cuisine) {
    return restaurantRepo.search(name, cuisine);
  }

  public Restaurant get(UUID restaurantId) throws RestaurantNotFoundException {
    return restaurantRepo.findById(restaurantId)
            .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));
  }
}
