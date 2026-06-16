package com.burito.service;

import com.burito.controller.views.UpdateRestaurantRequest;
import com.burito.domain.Address;
import com.burito.domain.Restaurant;
import com.burito.enums.CuisineType;
import com.burito.exceptions.RestaurantNotFoundException;
import com.burito.repository.RestaurantRepo;
import org.springframework.web.server.ResponseStatusException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminRestaurantServiceTest {

  @Mock
  private RestaurantRepo restaurantRepo;

  @InjectMocks
  private AdminRestaurantService adminRestaurantService;

  private UUID restaurantId;
  private Restaurant restaurant;

  @BeforeEach
  void setUp() {
    restaurantId = UUID.randomUUID();
    Address address = new Address();
    restaurant = new Restaurant(restaurantId, "Old Name", CuisineType.AMERICAN, 4.0, 30, false, address, UUID.randomUUID());
  }

  @Test
  void updateRestaurant_success() {
    String tokenRestaurantId = restaurantId.toString();
    UpdateRestaurantRequest request = new UpdateRestaurantRequest("New Name", CuisineType.ITALIAN, 45, true, "http://image.url");

    when(restaurantRepo.findById(restaurantId)).thenReturn(Optional.of(restaurant));
    when(restaurantRepo.save(any(Restaurant.class))).thenReturn(restaurant);

    Restaurant updated = adminRestaurantService.updateRestaurant(restaurantId, tokenRestaurantId, request);

    assertEquals("New Name", updated.getRestaurantName());
    assertEquals(CuisineType.ITALIAN, updated.getCuisineType());
    assertEquals(45, updated.getEstDeliveryMinutes());
    assertTrue(updated.isOpen());
    assertEquals("http://image.url", updated.getImageUrl());

    verify(restaurantRepo).save(restaurant);
  }

  @Test
  void updateRestaurant_forbidden_whenIdsMismatch() {
    String tokenRestaurantId = UUID.randomUUID().toString(); // different id
    UpdateRestaurantRequest request = new UpdateRestaurantRequest("New Name", CuisineType.ITALIAN, 45, true, "http://image.url");

    ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> 
      adminRestaurantService.updateRestaurant(restaurantId, tokenRestaurantId, request)
    );

    assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    verify(restaurantRepo, never()).findById(any());
    verify(restaurantRepo, never()).save(any());
  }

  @Test
  void updateRestaurant_notFound_whenRestaurantDoesNotExist() {
    String tokenRestaurantId = restaurantId.toString();
    UpdateRestaurantRequest request = new UpdateRestaurantRequest("New Name", CuisineType.ITALIAN, 45, true, "http://image.url");

    when(restaurantRepo.findById(restaurantId)).thenReturn(Optional.empty());

    ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> 
      adminRestaurantService.updateRestaurant(restaurantId, tokenRestaurantId, request)
    );

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    verify(restaurantRepo, never()).save(any());
  }
}
