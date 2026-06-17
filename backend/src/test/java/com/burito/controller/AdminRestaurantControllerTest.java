package com.burito.controller;

import com.burito.controller.views.UpdateRestaurantRequest;
import com.burito.domain.Address;
import com.burito.domain.Restaurant;
import com.burito.enums.CuisineType;
import com.burito.service.AdminRestaurantService;
import com.burito.service.JWTService;
import com.burito.service.UserService;
import com.burito.service.AdminMenuService;
import com.burito.controller.views.MenuItemRequest;
import com.burito.domain.MenuItem;
import com.burito.enums.MenuCategory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.burito.config.Security;
import org.springframework.context.annotation.Import;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@WebMvcTest(AdminRestaurantController.class)
@Import(Security.class)
public class AdminRestaurantControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private AdminRestaurantService adminRestaurantService;

  @MockitoBean
  private JWTService jwtService;

  @MockitoBean
  private AdminMenuService adminMenuService;

  @MockitoBean
  private UserService userService;

  private ObjectMapper objectMapper = new ObjectMapper();

  private UUID restaurantId;
  private UpdateRestaurantRequest request;
  private Restaurant updatedRestaurant;

  private UUID itemId;
  private MenuItemRequest menuItemRequest;
  private MenuItem menuItem;

  @BeforeEach
  void setUp() {
    restaurantId = UUID.randomUUID();
    request = new UpdateRestaurantRequest("New Name", CuisineType.ITALIAN, 45, true, "http://image.url");
    updatedRestaurant = new Restaurant(restaurantId, "New Name", CuisineType.ITALIAN, 4.0, 45, true, new Address(), UUID.randomUUID());
    updatedRestaurant.setImageUrl("http://image.url");

    itemId = UUID.randomUUID();
    menuItemRequest = new MenuItemRequest("Tacos", "Delicious", java.math.BigDecimal.valueOf(10.99), MenuCategory.MAINS, true, "http://image.url/taco");
    menuItem = new MenuItem();
    menuItem.setMenuItemId(itemId);
    menuItem.setName("Tacos");
    menuItem.setDescription("Delicious");
    menuItem.setPrice(java.math.BigDecimal.valueOf(10.99));
    menuItem.setCategory(MenuCategory.MAINS);
    menuItem.setAvailable(true);
    menuItem.setImageUrl("http://image.url/taco");
    menuItem.setRestaurant(updatedRestaurant);
  }

  @Test
  @WithMockUser(roles = "RESTAURANT_ADMIN")
  void updateRestaurant_success() throws Exception {
    when(jwtService.extractRestaurantId(any())).thenReturn(restaurantId.toString());
    when(adminRestaurantService.updateRestaurant(eq(restaurantId), eq(restaurantId.toString()), any(UpdateRestaurantRequest.class)))
            .thenReturn(updatedRestaurant);

    mockMvc.perform(put("/api/admin/restaurants/" + restaurantId)
            .header("Authorization", "Bearer fake-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.restaurantName").value("New Name"))
            .andExpect(jsonPath("$.imageUrl").value("http://image.url"));
  }

  @Test
  @WithMockUser(roles = "RESTAURANT_ADMIN")
  void updateRestaurant_forbidden() throws Exception {
    when(jwtService.extractRestaurantId(any())).thenReturn("some-other-id");
    when(adminRestaurantService.updateRestaurant(eq(restaurantId), eq("some-other-id"), any(UpdateRestaurantRequest.class)))
            .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN));

    mockMvc.perform(put("/api/admin/restaurants/" + restaurantId)
            .header("Authorization", "Bearer fake-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "RESTAURANT_ADMIN")
  void getRestaurant_success() throws Exception {
    when(jwtService.extractRestaurantId(any())).thenReturn(restaurantId.toString());
    when(adminRestaurantService.getRestaurant(eq(restaurantId), eq(restaurantId.toString())))
            .thenReturn(updatedRestaurant);

    mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/admin/restaurants/" + restaurantId)
            .header("Authorization", "Bearer fake-token")
            .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.restaurantName").value("New Name"))
            .andExpect(jsonPath("$.imageUrl").value("http://image.url"));
  }

  @Test
  @WithMockUser(roles = "RESTAURANT_ADMIN")
  void getRestaurant_forbidden() throws Exception {
    when(jwtService.extractRestaurantId(any())).thenReturn("some-other-id");
    when(adminRestaurantService.getRestaurant(eq(restaurantId), eq("some-other-id")))
            .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN));

    mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/admin/restaurants/" + restaurantId)
            .header("Authorization", "Bearer fake-token")
            .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "RESTAURANT_ADMIN")
  void addMenuItem_success() throws Exception {
    when(jwtService.extractRestaurantId(any())).thenReturn(restaurantId.toString());
    when(adminMenuService.createMenuItem(eq(restaurantId), eq(restaurantId.toString()), any(MenuItemRequest.class)))
            .thenReturn(menuItem);

    mockMvc.perform(post("/api/admin/restaurants/" + restaurantId + "/menu")
            .header("Authorization", "Bearer fake-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(menuItemRequest)))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Tacos"))
            .andExpect(jsonPath("$.imageUrl").value("http://image.url/taco"));
  }

  @Test
  @WithMockUser(roles = "RESTAURANT_ADMIN")
  void updateMenuItem_success() throws Exception {
    when(jwtService.extractRestaurantId(any())).thenReturn(restaurantId.toString());
    when(adminMenuService.updateMenuItem(eq(restaurantId), eq(itemId), eq(restaurantId.toString()), any(MenuItemRequest.class)))
            .thenReturn(menuItem);

    mockMvc.perform(put("/api/admin/restaurants/" + restaurantId + "/menu/" + itemId)
            .header("Authorization", "Bearer fake-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(menuItemRequest)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Tacos"));
  }

  @Test
  @WithMockUser(roles = "RESTAURANT_ADMIN")
  void deleteMenuItem_success() throws Exception {
    when(jwtService.extractRestaurantId(any())).thenReturn(restaurantId.toString());

    mockMvc.perform(delete("/api/admin/restaurants/" + restaurantId + "/menu/" + itemId)
            .header("Authorization", "Bearer fake-token"))
            .andDo(print())
            .andExpect(status().isNoContent());
  }
}
