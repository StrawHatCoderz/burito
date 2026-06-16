package com.burito.repository;

import com.burito.domain.Restaurant;
import com.burito.enums.CuisineType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RestaurantRepo extends JpaRepository<Restaurant, UUID> {

  @Query("SELECT r FROM Restaurant r WHERE " +
         "(:name IS NULL OR LOWER(r.restaurantName) LIKE LOWER(CONCAT('%', CAST(:name AS string), '%'))) AND " +
         "(:cuisine IS NULL OR r.cuisineType = :cuisine)")
  List<Restaurant> search(@Param("name") String name, @Param("cuisine") CuisineType cuisine);

  Restaurant findByOwnerId(UUID ownerId);
}
