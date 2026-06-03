package com.burito.controller;

import com.burito.enums.CuisineType;
import com.burito.enums.ErrorCode;
import com.burito.enums.MenuCategory;
import com.burito.exceptions.RestaurantNotFoundException;
import com.burito.domain.Address;
import com.burito.domain.MenuItem;
import com.burito.domain.Restaurant;
import com.burito.service.JWTService;
import com.burito.service.MenuService;
import com.burito.service.RestaurantService;
import com.burito.service.UserService;
import com.burito.config.Security;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RestaurantController.class)
@Import(Security.class)
@WithMockUser
class RestaurantControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private RestaurantService restaurantService;
  @MockitoBean
  private MenuService menuService;
  @MockitoBean
  private JWTService jwtService;
  @MockitoBean
  private UserService userService;

  @BeforeEach
  void setUp() {
    when(jwtService.extractUsername(anyString()))
            .thenReturn("testuser");
    when(jwtService.isValidToken(anyString(), any()))
            .thenReturn(true);
  }

  @Test
  void shouldReturnRestaurants() throws Exception {
    Address address = new Address(1L, "123 MG Road", "Bangalore", "Karnataka",
            "India", "560001");

    Restaurant restaurant =
            new Restaurant(UUID.randomUUID(),
                    "Spicy Hub",
                    CuisineType.INDIAN,
                    4.6,
                    20,
                    true,
                    address);
    restaurant.setCreatedAt(LocalDate.of(2024, 1, 15));

    when(restaurantService.list()).thenReturn(List.of(restaurant));

    mockMvc.perform(get("/api/restaurants/"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].restaurantName").value("Spicy Hub"))
            .andExpect(jsonPath("$.data[0].rating").value(4.6))
            .andExpect(jsonPath("$.data[0].createdAt").value("2024-01-15"));
  }

  @Test
  void shouldReturnRequestedRestaurant() throws Exception {
    Address address = new Address(1L, "123 MG Road", "Bangalore", "Karnataka",
            "India", "560001");

    UUID restaurantId = UUID.randomUUID();

    Restaurant restaurant =
            new Restaurant(restaurantId,
                    "Spicy Hub",
                    CuisineType.INDIAN,
                    4.6,
                    20,
                    true,
                    address);
    restaurant.setCreatedAt(LocalDate.of(2024, 1, 15));

    when(restaurantService.get(restaurant.getRestaurantId()))
            .thenReturn(restaurant);

    mockMvc.perform(get(String.format("/api/restaurants/%s", restaurantId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.restaurantName").value("Spicy Hub"))
            .andExpect(jsonPath("$.data.rating").value(4.6))
            .andExpect(jsonPath("$.data.createdAt").value("2024-01-15"))
            .andExpect(jsonPath("$.data.address.city").exists());
  }

  @Test
  @WithAnonymousUser
  void shouldAllowAnonymousAccessToRestaurantList() throws Exception {
    when(restaurantService.list()).thenReturn(List.of());
    mockMvc.perform(get("/api/restaurants/"))
            .andExpect(status().isOk());
  }

  @Test
  void shouldReturnBadRequestWhenIdIsNotValidUUID() throws Exception {
    mockMvc.perform(get("/api/restaurants/not-a-valid-uuid"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.errorCode")
                    .value(ErrorCode.INVALID_RESTAURANT_ID.toString()))
            .andExpect(jsonPath("$.error.message").exists());
  }

  @Test
  void shouldReturnNotFoundWhenRequestedWithInvalidId() throws Exception {
    UUID nonExistentId = UUID.randomUUID();
    when(restaurantService.get(any(UUID.class)))
            .thenThrow(new RestaurantNotFoundException(nonExistentId));

    mockMvc.perform(get("/api/restaurants/" + nonExistentId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success")
                    .value(false))
            .andExpect(jsonPath("error.errorCode")
                    .value(ErrorCode.RESTAURANT_NOT_FOUND.toString()))
            .andExpect(jsonPath("$.error.message").exists());
  }

  // --- menu endpoint ---

  @Test
  @WithAnonymousUser
  void shouldReturnMenuItemsWithoutAuthentication() throws Exception {
    UUID restaurantId = UUID.randomUUID();
    MenuItem item = buildMenuItem(restaurantId, "Samosa", MenuCategory.STARTERS, "79.00");
    when(menuService.getMenuForRestaurant(restaurantId)).thenReturn(List.of(item));

    mockMvc.perform(get("/api/restaurants/" + restaurantId + "/menu"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].name").value("Samosa"))
            .andExpect(jsonPath("$.data[0].price").value(79.00))
            .andExpect(jsonPath("$.data[0].category").value("STARTERS"));
  }

  @Test
  void shouldReturnEmptyListWhenRestaurantHasNoMenuItems() throws Exception {
    UUID restaurantId = UUID.randomUUID();
    when(menuService.getMenuForRestaurant(restaurantId)).thenReturn(List.of());

    mockMvc.perform(get("/api/restaurants/" + restaurantId + "/menu"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data").isEmpty());
  }

  @Test
  void shouldReturn404WhenMenuRequestedForNonExistentRestaurant() throws Exception {
    UUID nonExistentId = UUID.randomUUID();
    when(menuService.getMenuForRestaurant(nonExistentId))
            .thenThrow(new RestaurantNotFoundException(nonExistentId));

    mockMvc.perform(get("/api/restaurants/" + nonExistentId + "/menu"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.errorCode")
                    .value(ErrorCode.RESTAURANT_NOT_FOUND.toString()));
  }

  @Test
  void shouldReturn400WhenMenuRequestedWithMalformedRestaurantId() throws Exception {
    mockMvc.perform(get("/api/restaurants/not-a-uuid/menu"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.errorCode")
                    .value(ErrorCode.INVALID_RESTAURANT_ID.toString()));
  }

  private MenuItem buildMenuItem(UUID restaurantId, String name,
                                 MenuCategory category, String price) {
    Address address = new Address(1L, "1 Main St", "City", "State", "India", "000001");
    Restaurant restaurant = new Restaurant(restaurantId, "Test Restaurant",
            CuisineType.INDIAN, 4.0, 30, true, address);
    MenuItem item = new MenuItem();
    item.setName(name);
    item.setDescription("A delicious item");
    item.setPrice(new BigDecimal(price));
    item.setCategory(category);
    item.setAvailable(true);
    item.setRestaurant(restaurant);
    return item;
  }
}