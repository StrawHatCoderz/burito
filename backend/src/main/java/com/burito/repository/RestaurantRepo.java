package com.burito.repository;

import com.burito.domain.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RestaurantRepo extends JpaRepository<Restaurant, UUID> {
}
