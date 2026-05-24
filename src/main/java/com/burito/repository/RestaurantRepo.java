package com.burito.repository;

import com.burito.repository.entities.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantRepo extends JpaRepository<Restaurant, String> {
  Restaurant findRestaurantByRestaurantId(String restaurantId);
}
