package com.burito.service;

import com.burito.repository.RestaurantRepo;
import com.burito.repository.entities.Restaurant;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RestaurantService {

  private final RestaurantRepo restaurantRepo;

  public RestaurantService(RestaurantRepo restaurantRepo) {
    this.restaurantRepo = restaurantRepo;
  }

  public List<Restaurant> list() {
    return restaurantRepo.findAll();
  }
}
