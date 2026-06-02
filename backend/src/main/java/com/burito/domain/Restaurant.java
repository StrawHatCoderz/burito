package com.burito.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;
import java.util.UUID;

@Entity
@Table(name = "restaurant")
@Getter
@Setter
@NoArgsConstructor
public class Restaurant {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID restaurantId;

  private String restaurantName;
  private String description;
  private String cuisineType;
  private double rating;
  private double estDeliveryMinutes;
  private boolean isOpen;
  private Date createdAt;

  @ManyToOne
  @JoinColumn(name = "address_id")
  private Address address;

  public Restaurant(UUID restaurantId, String restaurantName,
                    String cuisineType,
                    double rating,
                    double estDeliveryMinutes,
                    boolean isOpen, Address address) {
    this.restaurantId = restaurantId;
    this.restaurantName = restaurantName;
    this.cuisineType = cuisineType;
    this.rating = rating;
    this.estDeliveryMinutes = estDeliveryMinutes;
    this.isOpen = isOpen;
    this.address = address;
  }
}