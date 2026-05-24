package com.burito.service;

import com.burito.enums.CuisineType;
import com.burito.exceptions.RestaurantNotFoundException;
import com.burito.repository.RestaurantRepo;
import com.burito.repository.entities.Restaurant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

  @Mock
  private RestaurantRepo repo;

  @InjectMocks
  private RestaurantService service;

  @Test
  void shouldReturnRestaurants() {
    Restaurant restaurant =
            new Restaurant("AK98",
                    "Spicy Hub",
                    CuisineType.INDIAN.toString(),
                    4.6,
                    20,
                    true);

    when(repo.findAll())
            .thenReturn(List.of(restaurant));

    List<Restaurant> result = service.list();

    verify(repo).findAll();
    assertEquals(1, result.size());
  }

  @Test
  void shouldReturnARestaurantWithValidId()
          throws RestaurantNotFoundException {
    Restaurant restaurant =
            new Restaurant("AK98",
                    "Spicy Hub",
                    CuisineType.INDIAN.toString(),
                    4.6,
                    20,
                    true);

    when(repo.findRestaurantByRestaurantId(restaurant.getRestaurantId()))
            .thenReturn(restaurant);

    Restaurant result = service.get(restaurant.getRestaurantId());
    verify(repo).findRestaurantByRestaurantId(restaurant.getRestaurantId());

    assertEquals(result.toString(), restaurant.toString());
  }

  @Test
  void shouldThrowExceptionWithInvalidId() throws RestaurantNotFoundException {
    when(repo.findRestaurantByRestaurantId(anyString()))
            .thenReturn(null);

    assertThrows(RestaurantNotFoundException.class,
            () -> service.get("invalid_id"));

    verify(repo).findRestaurantByRestaurantId(anyString());
  }
}