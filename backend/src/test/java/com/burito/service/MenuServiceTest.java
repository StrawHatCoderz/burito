package com.burito.service;

import com.burito.domain.MenuItem;
import com.burito.domain.Restaurant;
import com.burito.enums.CuisineType;
import com.burito.enums.MenuCategory;
import com.burito.exceptions.RestaurantNotFoundException;
import com.burito.repository.MenuItemRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

  @Mock private RestaurantService restaurantService;
  @Mock private MenuItemRepo menuItemRepo;
  @InjectMocks private MenuService menuService;

  @Test
  void shouldReturnMenuItemsForValidRestaurant() throws Exception {
    UUID restaurantId = UUID.randomUUID();
    Restaurant restaurant = new Restaurant(restaurantId, "Spice Garden",
            CuisineType.INDIAN, 4.5, 30, true, null);

    MenuItem item = buildMenuItem("Samosa", MenuCategory.STARTERS, new BigDecimal("79.00"), restaurant);

    when(restaurantService.get(restaurantId)).thenReturn(restaurant);
    when(menuItemRepo.findByRestaurant_RestaurantIdOrderByCategoryAscNameAsc(restaurantId))
            .thenReturn(List.of(item));

    List<MenuItem> result = menuService.getMenuForRestaurant(restaurantId);

    assertEquals(1, result.size());
    assertEquals("Samosa", result.get(0).getName());
    verify(restaurantService).get(restaurantId);
    verify(menuItemRepo).findByRestaurant_RestaurantIdOrderByCategoryAscNameAsc(restaurantId);
  }

  @Test
  void shouldReturnEmptyListWhenRestaurantHasNoItems() throws Exception {
    UUID restaurantId = UUID.randomUUID();
    Restaurant restaurant = new Restaurant(restaurantId, "New Place",
            CuisineType.INDIAN, 4.0, 20, true, null);

    when(restaurantService.get(restaurantId)).thenReturn(restaurant);
    when(menuItemRepo.findByRestaurant_RestaurantIdOrderByCategoryAscNameAsc(restaurantId))
            .thenReturn(List.of());

    List<MenuItem> result = menuService.getMenuForRestaurant(restaurantId);

    assertTrue(result.isEmpty());
  }

  @Test
  void shouldThrowRestaurantNotFoundWhenRestaurantDoesNotExist() throws Exception {
    UUID restaurantId = UUID.randomUUID();
    when(restaurantService.get(restaurantId))
            .thenThrow(new RestaurantNotFoundException(restaurantId));

    assertThrows(RestaurantNotFoundException.class,
            () -> menuService.getMenuForRestaurant(restaurantId));

    verify(menuItemRepo, never()).findByRestaurant_RestaurantIdOrderByCategoryAscNameAsc(any());
  }

  private MenuItem buildMenuItem(String name, MenuCategory category,
                                 BigDecimal price, Restaurant restaurant) {
    MenuItem item = new MenuItem();
    item.setName(name);
    item.setCategory(category);
    item.setPrice(price);
    item.setAvailable(true);
    item.setRestaurant(restaurant);
    return item;
  }
}
