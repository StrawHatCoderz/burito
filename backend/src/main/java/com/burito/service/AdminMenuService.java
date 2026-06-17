package com.burito.service;

import com.burito.controller.views.MenuItemRequest;
import com.burito.domain.MenuItem;
import com.burito.domain.Restaurant;
import com.burito.repository.MenuItemRepo;
import com.burito.repository.RestaurantRepo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class AdminMenuService {

    private final MenuItemRepo menuItemRepo;
    private final RestaurantRepo restaurantRepo;

    public AdminMenuService(MenuItemRepo menuItemRepo, RestaurantRepo restaurantRepo) {
        this.menuItemRepo = menuItemRepo;
        this.restaurantRepo = restaurantRepo;
    }

    private void validateOwnership(UUID restaurantId, String tokenRestaurantId) {
        if (tokenRestaurantId == null || !tokenRestaurantId.equals(restaurantId.toString())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied. You can only manage menu items for your own restaurant.");
        }
    }

    public MenuItem createMenuItem(UUID restaurantId, String tokenRestaurantId, MenuItemRequest request) {
        validateOwnership(restaurantId, tokenRestaurantId);

        Restaurant restaurant = restaurantRepo.findById(restaurantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant not found"));

        MenuItem item = new MenuItem();
        item.setName(request.name());
        item.setDescription(request.description());
        item.setPrice(request.price());
        item.setCategory(request.category());
        item.setAvailable(request.isAvailable());
        item.setImageUrl(request.imageUrl());
        item.setRestaurant(restaurant);

        return menuItemRepo.save(item);
    }

    public MenuItem updateMenuItem(UUID restaurantId, UUID itemId, String tokenRestaurantId, MenuItemRequest request) {
        validateOwnership(restaurantId, tokenRestaurantId);

        MenuItem item = menuItemRepo.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Menu item not found"));

        if (!item.getRestaurant().getRestaurantId().equals(restaurantId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Menu item does not belong to the specified restaurant");
        }

        item.setName(request.name());
        item.setDescription(request.description());
        item.setPrice(request.price());
        item.setCategory(request.category());
        item.setAvailable(request.isAvailable());
        item.setImageUrl(request.imageUrl());

        return menuItemRepo.save(item);
    }

    public void deleteMenuItem(UUID restaurantId, UUID itemId, String tokenRestaurantId) {
        validateOwnership(restaurantId, tokenRestaurantId);

        MenuItem item = menuItemRepo.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Menu item not found"));

        if (!item.getRestaurant().getRestaurantId().equals(restaurantId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Menu item does not belong to the specified restaurant");
        }

        menuItemRepo.delete(item);
    }
}
