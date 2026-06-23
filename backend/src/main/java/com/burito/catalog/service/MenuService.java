package com.burito.catalog.service;

import com.burito.catalog.domain.MenuItem;
import com.burito.catalog.exceptions.RestaurantNotFoundException;
import com.burito.catalog.repository.MenuItemRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class MenuService {

  private final RestaurantService restaurantService;
  private final MenuItemRepo menuItemRepo;

  public MenuService(RestaurantService restaurantService, MenuItemRepo menuItemRepo) {
    this.restaurantService = restaurantService;
    this.menuItemRepo = menuItemRepo;
  }

  public List<MenuItem> getMenuForRestaurant(UUID restaurantId) throws RestaurantNotFoundException {
    restaurantService.get(restaurantId);
    return menuItemRepo.findByRestaurant_RestaurantIdOrderByCategoryAscNameAsc(restaurantId);
  }
}
