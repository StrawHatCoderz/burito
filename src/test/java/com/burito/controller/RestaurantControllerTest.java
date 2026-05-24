package com.burito.controller;

import com.burito.enums.CuisineType;
import com.burito.repository.entities.Restaurant;
import com.burito.service.RestaurantService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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
    Restaurant restaurant =
            new Restaurant("Spicy Hub",
                    CuisineType.INDIAN.toString(),
                    4.6,
                    20,
                    true);

    when(restaurantService.list()).thenReturn(List.of(restaurant));

    mockMvc.perform(get("/api/restaurants/"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("success")
                    .value(true))
            .andExpect(jsonPath("data[0].restaurantName")
                    .value("Spicy Hub"))
            .andExpect(jsonPath("data[0].rating")
                    .value(4.6));
  }
}