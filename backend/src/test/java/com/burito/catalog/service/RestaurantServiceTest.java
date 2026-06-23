package com.burito.catalog.service;

import com.burito.catalog.enums.CuisineType;
import com.burito.catalog.exceptions.RestaurantNotFoundException;
import com.burito.catalog.repository.RestaurantRepo;
import com.burito.catalog.domain.Address;
import com.burito.catalog.domain.Restaurant;
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
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.eq;
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

  @Test
  void shouldReturnAllRestaurantsWhenSearchParamsAreNull() {
    when(repo.search(null, null)).thenReturn(List.of());
    service.search(null, null);
    verify(repo).search(null, null);
  }

  @Test
  void shouldSearchByNameOnly() {
    when(repo.search(eq("spicy"), isNull())).thenReturn(List.of());
    service.search("spicy", null);
    verify(repo).search("spicy", null);
  }

  @Test
  void shouldFilterByCuisineOnly() {
    when(repo.search(isNull(), eq(CuisineType.INDIAN))).thenReturn(List.of());
    service.search(null, CuisineType.INDIAN);
    verify(repo).search(null, CuisineType.INDIAN);
  }

  @Test
  void shouldSearchByNameAndCuisineCombined() {
    when(repo.search(eq("hub"), eq(CuisineType.INDIAN))).thenReturn(List.of());
    service.search("hub", CuisineType.INDIAN);
    verify(repo).search("hub", CuisineType.INDIAN);
  }

  @Test
  void shouldCreateRestaurantForAdmin() {
    UUID ownerId = UUID.randomUUID();
    Restaurant r = new Restaurant();
    r.setOwnerId(ownerId);
    when(repo.save(any(Restaurant.class))).thenReturn(r);

    Restaurant result = service.createRestaurantForAdmin(ownerId, "Name", CuisineType.ITALIAN, 45.0);

    assertNotNull(result);
    assertEquals(ownerId, result.getOwnerId());
    verify(repo).save(any(Restaurant.class));
  }

  @Test
  void shouldGetRestaurantIdByOwnerIdWhenExists() {
    UUID ownerId = UUID.randomUUID();
    Restaurant r = new Restaurant();
    r.setRestaurantId(UUID.randomUUID());
    when(repo.findByOwnerId(ownerId)).thenReturn(r);

    UUID resultId = service.getRestaurantIdByOwnerId(ownerId);

    assertEquals(r.getRestaurantId(), resultId);
  }

  @Test
  void shouldReturnNullWhenGetRestaurantIdByOwnerIdNotFound() {
    UUID ownerId = UUID.randomUUID();
    when(repo.findByOwnerId(ownerId)).thenReturn(null);

    UUID resultId = service.getRestaurantIdByOwnerId(ownerId);

    assertNull(resultId);
  }
}
