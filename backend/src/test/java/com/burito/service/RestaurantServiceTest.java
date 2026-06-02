package com.burito.service;

import com.burito.enums.CuisineType;
import com.burito.exceptions.RestaurantNotFoundException;
import com.burito.repository.RestaurantRepo;
import com.burito.domain.Address;
import com.burito.domain.Restaurant;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
    Address address = new Address(1L, "123 MG Road","Bangalore","Karnataka",
            "India","560001");

    Restaurant restaurant =
            new Restaurant(UUID.randomUUID(),
                    "Spicy Hub",
                    CuisineType.INDIAN,
                    4.6,
                    20,
                    true,
                    address);

    when(repo.findAll())
            .thenReturn(List.of(restaurant));

    List<Restaurant> result = service.list();

    verify(repo).findAll();
    assertEquals(1, result.size());
  }

  @SneakyThrows
  @Test
  void shouldReturnARestaurantWithValidId() {

    Address address = new Address(1L, "123 MG Road","Bangalore","Karnataka",
            "India","560001");

    Restaurant restaurant =
            new Restaurant(UUID.randomUUID(),
                    "Spicy Hub",
                    CuisineType.INDIAN,
                    4.6,
                    20,
                    true,
                    address);

    when(repo.findById(restaurant.getRestaurantId()))
            .thenReturn(Optional.of(restaurant));

    Restaurant result = service.get(restaurant.getRestaurantId());
    verify(repo).findById(restaurant.getRestaurantId());

    assertEquals(result.toString(), restaurant.toString());
  }

  @Test
  void shouldThrowNotFoundWhenRestaurantDoesNotExist() {
    when(repo.findById(any(UUID.class)))
            .thenReturn(Optional.empty());

    assertThrows(RestaurantNotFoundException.class,
            () -> service.get(UUID.randomUUID()));

    verify(repo).findById(any(UUID.class));
  }
}
