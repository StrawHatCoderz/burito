package com.burito.controller.views;

import com.burito.enums.CuisineType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRestaurantRequest {
  private String restaurantName;
  private CuisineType cuisineType;
  private double estDeliveryMinutes;
  private boolean open;
  private String imageUrl;
}
