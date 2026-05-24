package com.burito.controller;

import com.burito.enums.CuisineType;
import com.burito.enums.ErrorCode;
import com.burito.exceptions.RestaurantNotFoundException;
import com.burito.repository.entities.Address;
import com.burito.repository.entities.Restaurant;
import com.burito.service.RestaurantService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RestaurantController.class)
class RestaurantControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private RestaurantService restaurantService;

  @Test
  void shouldReturnRestaurants() throws Exception {
    Address address = new Address(1L, "123 MG Road","Bangalore","Karnataka",
            "India","560001");

    Restaurant restaurant =
            new Restaurant("AK98",
                    "Spicy Hub",
                    CuisineType.INDIAN.toString(),
                    4.6,
                    20,
                    true,
                    address);

    when(restaurantService.list()).thenReturn(List.of(restaurant));

    mockMvc.perform(get("/api/restaurants/"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success")
                    .value(true))
            .andExpect(jsonPath("$.data[0].restaurantName")
                    .value("Spicy Hub"))
            .andExpect(jsonPath("$.data[0].rating")
                    .value(4.6));
  }

  @Test
  void shouldReturnRequestedRestaurant() throws Exception {
    Address address = new Address(1L, "123 MG Road","Bangalore","Karnataka",
            "India","560001");

    Restaurant restaurant =
            new Restaurant("AK98",
                    "Spicy Hub",
                    CuisineType.INDIAN.toString(),
                    4.6,
                    20,
                    true,
                    address);

    when(restaurantService.get(restaurant.getRestaurantId()))
            .thenReturn(restaurant);

    mockMvc.perform(get("/api/restaurants/AK98"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success")
                    .value(true))
            .andExpect(jsonPath("$.data.restaurantName")
                    .value("Spicy Hub"))
            .andExpect(jsonPath("$.data.rating")
                    .value(4.6))
            .andExpect(jsonPath("$.data.address.city").exists());
  }

  @Test
  void shouldReturnNotFoundWhenRequestedWithInvalidId() throws Exception {
    when(restaurantService.get(anyString()))
            .thenThrow(new RestaurantNotFoundException(anyString()));

    mockMvc.perform(get("/api/restaurants/invalid_id"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success")
                    .value(false))
            .andExpect(jsonPath("error.errorCode")
                    .value(ErrorCode.RESTAURANT_NOT_FOUND.toString()))
            .andExpect(jsonPath("$.error.message").exists());
  }
}