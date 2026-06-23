package com.burito.catalog.repository;

import com.burito.catalog.domain.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MenuItemRepo extends JpaRepository<MenuItem, UUID> {
  List<MenuItem> findByRestaurant_RestaurantIdOrderByCategoryAscNameAsc(UUID restaurantId);
}
