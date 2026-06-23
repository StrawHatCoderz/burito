package com.burito.catalog.service;

import com.burito.catalog.controller.views.UpdateRestaurantRequest;
import com.burito.catalog.domain.Address;
import com.burito.catalog.domain.Restaurant;
import com.burito.catalog.enums.CuisineType;
import com.burito.catalog.exceptions.RestaurantNotFoundException;
import com.burito.catalog.repository.RestaurantRepo;
import com.burito.core.exceptions.APIException;
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

  @Mock
  private org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

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

    APIException ex = assertThrows(APIException.class, () -> 
        adminRestaurantService.updateRestaurant(restaurantId, "different-id", request));
    assertEquals(com.burito.core.enums.ErrorCode.FORBIDDEN, ex.getErrorCode());
    verify(restaurantRepo, never()).findById(any());
    verify(restaurantRepo, never()).save(any());
  }

  @Test
  void updateRestaurant_notFound_whenRestaurantDoesNotExist() {
    String tokenRestaurantId = restaurantId.toString();
    UpdateRestaurantRequest request = new UpdateRestaurantRequest("New Name", CuisineType.ITALIAN, 45, true, "http://image.url");

    when(restaurantRepo.findById(restaurantId)).thenReturn(Optional.empty());

    APIException ex = assertThrows(APIException.class, () -> 
        adminRestaurantService.updateRestaurant(restaurantId, tokenRestaurantId, request));
    assertEquals(com.burito.core.enums.ErrorCode.NOT_FOUND, ex.getErrorCode());
    verify(restaurantRepo, never()).save(any());
  }

  @Test
  void getRestaurant_success() {
    String tokenRestaurantId = restaurantId.toString();
    when(restaurantRepo.findById(restaurantId)).thenReturn(Optional.of(restaurant));

    Restaurant fetched = adminRestaurantService.getRestaurant(restaurantId, tokenRestaurantId);

    assertNotNull(fetched);
    assertEquals(restaurantId, fetched.getRestaurantId());
    verify(restaurantRepo).findById(restaurantId);
  }

  @Test
  void getRestaurant_forbidden_whenIdsMismatch() {
    String tokenRestaurantId = UUID.randomUUID().toString(); // different 
    APIException ex = assertThrows(APIException.class, () -> 
        adminRestaurantService.getRestaurant(restaurantId, "different-id"));
    assertEquals(com.burito.core.enums.ErrorCode.FORBIDDEN, ex.getErrorCode());
    verify(restaurantRepo, never()).findById(any());
  }

  @Test
  void getRestaurant_notFound_whenRestaurantDoesNotExist() {
    String tokenRestaurantId = restaurantId.toString();

    when(restaurantRepo.findById(restaurantId)).thenReturn(Optional.empty());

    APIException ex = assertThrows(APIException.class, () -> 
        adminRestaurantService.getRestaurant(restaurantId, tokenRestaurantId));
    assertEquals(com.burito.core.enums.ErrorCode.NOT_FOUND, ex.getErrorCode());
  }

  @Test
  void updateRestaurant_WithNullFields() {
    String tokenRestaurantId = restaurantId.toString();
    UpdateRestaurantRequest nullReq = new UpdateRestaurantRequest(null, null, 0, false, null);

    when(restaurantRepo.findById(restaurantId)).thenReturn(Optional.of(restaurant));
    when(restaurantRepo.save(any(Restaurant.class))).thenAnswer(i -> i.getArguments()[0]);

    Restaurant updated = adminRestaurantService.updateRestaurant(restaurantId, tokenRestaurantId, nullReq);

    assertEquals("Old Name", updated.getRestaurantName());
    assertEquals(CuisineType.AMERICAN, updated.getCuisineType());
  }
}
