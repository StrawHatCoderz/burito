package com.burito.catalog.service;

import com.burito.catalog.controller.views.AvailabilityEvent;
import com.burito.catalog.controller.views.UpdateRestaurantRequest;
import com.burito.catalog.domain.Restaurant;
import com.burito.catalog.exceptions.RestaurantNotFoundException;
import com.burito.catalog.repository.RestaurantRepo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.burito.core.exceptions.APIException;

import java.util.UUID;

@Service
public class AdminRestaurantService {

  private final RestaurantRepo restaurantRepo;
  private final SimpMessagingTemplate messagingTemplate;

  public AdminRestaurantService(RestaurantRepo restaurantRepo, SimpMessagingTemplate messagingTemplate) {
    this.restaurantRepo = restaurantRepo;
    this.messagingTemplate = messagingTemplate;
  }

  public Restaurant updateRestaurant(UUID restaurantId, String tokenRestaurantId, UpdateRestaurantRequest request) {
    if (tokenRestaurantId == null || !tokenRestaurantId.equals(restaurantId.toString())) {
      throw APIException.forbidden("Access denied. You can only update your own restaurant.");
    }

    Restaurant restaurant = restaurantRepo.findById(restaurantId)
            .orElseThrow(() -> APIException.notFound("Restaurant not found"));

    boolean openChanged = restaurant.isOpen() != request.isOpen();

    if (request.getRestaurantName() != null) {
      restaurant.setRestaurantName(request.getRestaurantName());
    }
    if (request.getCuisineType() != null) {
      restaurant.setCuisineType(request.getCuisineType());
    }
    restaurant.setEstDeliveryMinutes(request.getEstDeliveryMinutes());
    restaurant.setOpen(request.isOpen());

    if (request.getImageUrl() != null) {
      restaurant.setImageUrl(request.getImageUrl());
    }

    Restaurant saved = restaurantRepo.save(restaurant);

    // Broadcast open/closed change to customers on the detail page and in the cart
    if (openChanged) {
      AvailabilityEvent event = new AvailabilityEvent(
              saved.getRestaurantId().toString(),
              saved.isOpen(),
              saved.getRestaurantName()
      );
      // Subscribers on RestaurantDetailPage / CartDrawer
      messagingTemplate.convertAndSend(
              "/topic/restaurant/" + saved.getRestaurantId() + "/availability", event);
      // Subscribers on RestaurantsPage list
      messagingTemplate.convertAndSend("/topic/restaurants", event);
    }

    return saved;
  }

  public Restaurant getRestaurant(UUID restaurantId, String tokenRestaurantId) {
    if (tokenRestaurantId == null || !tokenRestaurantId.equals(restaurantId.toString())) {
      throw APIException.forbidden("Access denied. You can only view your own restaurant.");
    }

    return restaurantRepo.findById(restaurantId)
            .orElseThrow(() -> APIException.notFound("Restaurant not found"));
  }
}

