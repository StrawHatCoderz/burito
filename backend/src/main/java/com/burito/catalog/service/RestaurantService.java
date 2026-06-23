package com.burito.catalog.service;

import com.burito.catalog.enums.CuisineType;
import com.burito.catalog.exceptions.RestaurantNotFoundException;
import com.burito.catalog.repository.RestaurantRepo;
import com.burito.catalog.domain.Restaurant;
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

  public Restaurant createRestaurantForAdmin(UUID ownerId, String restaurantName, CuisineType cuisineType, double estDeliveryMinutes) {
    Restaurant r = new Restaurant();
    r.setOwnerId(ownerId);
    r.setRestaurantName(restaurantName != null ? restaurantName : "New Restaurant");
    r.setCuisineType(cuisineType != null ? cuisineType : CuisineType.AMERICAN); // Default
    r.setRating(0.0);
    r.setEstDeliveryMinutes(estDeliveryMinutes > 0 ? estDeliveryMinutes : 30);
    r.setOpen(false);
    return restaurantRepo.save(r);
  }

  public UUID getRestaurantIdByOwnerId(UUID ownerId) {
    Restaurant restaurant = restaurantRepo.findByOwnerId(ownerId);
    if (restaurant != null && restaurant.getRestaurantId() != null) {
      return restaurant.getRestaurantId();
    }
    return null;
  }
}
