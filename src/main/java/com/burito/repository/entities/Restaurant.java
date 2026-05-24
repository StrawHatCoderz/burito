package com.burito.repository.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "restaurant")
@Getter
@Setter
@NoArgsConstructor
public class Restaurant {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String restaurantId;

  private String restaurantName;
  private String cuisineType;
  private double rating;
  private double estimatedDeliveryTime;
  private boolean isOpen;

  public Restaurant(String restaurantId, String restaurantName,
                    String cuisineType,
                    double rating,
                    double estimatedDeliveryTime,
                    boolean isOpen) {
    this.restaurantId = restaurantId;
    this.restaurantName = restaurantName;
    this.cuisineType = cuisineType;
    this.rating = rating;
    this.estimatedDeliveryTime = estimatedDeliveryTime;
    this.isOpen = isOpen;
  }
}