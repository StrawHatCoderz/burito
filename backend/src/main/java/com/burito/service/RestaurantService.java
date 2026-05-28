package com.burito.service;

import com.burito.exceptions.InvalidRestaurantIdException;
import com.burito.exceptions.RestaurantNotFoundException;
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

  public Restaurant get(String restaurantId)
          throws RestaurantNotFoundException, InvalidRestaurantIdException {
    if (restaurantId.isEmpty()) {
      throw new InvalidRestaurantIdException(restaurantId);
    }
    Restaurant restaurant =
            restaurantRepo.findRestaurantByRestaurantId(restaurantId);

    if (restaurant == null) {
      throw new RestaurantNotFoundException(restaurantId);
    }

    return restaurant;
  }
}
