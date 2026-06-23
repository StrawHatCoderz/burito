package com.burito.catalog.domain;

import com.burito.catalog.enums.CuisineType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
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
  @Enumerated(EnumType.STRING)
  private CuisineType cuisineType;
  private double rating;
  private double estDeliveryMinutes;
  private boolean isOpen;
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  private LocalDate createdAt;

  @ManyToOne
  @JoinColumn(name = "address_id")
  private Address address;

  @Column(name = "owner_id")
  private UUID ownerId;

  private String imageUrl;

  public Restaurant(UUID restaurantId, String restaurantName,
                    CuisineType cuisineType,
                    double rating,
                    double estDeliveryMinutes,
                    boolean isOpen, Address address) {
    this(restaurantId, restaurantName, cuisineType, rating, estDeliveryMinutes, isOpen, address, null);
  }

  public Restaurant(UUID restaurantId, String restaurantName,
                    CuisineType cuisineType,
                    double rating,
                    double estDeliveryMinutes,
                    boolean isOpen, Address address, UUID ownerId) {
    this.restaurantId = restaurantId;
    this.restaurantName = restaurantName;
    this.cuisineType = cuisineType;
    this.rating = rating;
    this.estDeliveryMinutes = estDeliveryMinutes;
    this.isOpen = isOpen;
    this.address = address;
    this.ownerId = ownerId;
  }
}