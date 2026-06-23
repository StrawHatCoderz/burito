package com.burito.catalog.service;

import com.burito.catalog.controller.views.MenuEvent;
import com.burito.catalog.controller.views.MenuItemRequest;
import com.burito.catalog.domain.MenuItem;
import com.burito.catalog.domain.Restaurant;
import com.burito.catalog.repository.MenuItemRepo;
import com.burito.catalog.repository.RestaurantRepo;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class AdminMenuService {

    private final MenuItemRepo menuItemRepo;
    private final RestaurantRepo restaurantRepo;
    private final SimpMessagingTemplate messagingTemplate;

    public AdminMenuService(MenuItemRepo menuItemRepo, RestaurantRepo restaurantRepo, SimpMessagingTemplate messagingTemplate) {
        this.menuItemRepo = menuItemRepo;
        this.restaurantRepo = restaurantRepo;
        this.messagingTemplate = messagingTemplate;
    }

    private void validateOwnership(UUID restaurantId, String tokenRestaurantId) {
        if (tokenRestaurantId == null || !tokenRestaurantId.equals(restaurantId.toString())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied. You can only manage menu items for your own restaurant.");
        }
    }

    private void broadcast(String restaurantId, MenuEvent event) {
        messagingTemplate.convertAndSend("/topic/restaurant/" + restaurantId + "/menu", event);
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

        MenuItem saved = menuItemRepo.save(item);
        broadcast(restaurantId.toString(), MenuEvent.added(restaurantId.toString(), saved));
        return saved;
    }

    public MenuItem updateMenuItem(UUID restaurantId, UUID itemId, String tokenRestaurantId, MenuItemRequest request) {
        validateOwnership(restaurantId, tokenRestaurantId);

        MenuItem item = menuItemRepo.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Menu item not found"));

        if (!item.getRestaurant().getRestaurantId().equals(restaurantId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Menu item does not belong to the specified restaurant");
        }

        boolean availabilityChanged = item.isAvailable() != request.isAvailable();

        item.setName(request.name());
        item.setDescription(request.description());
        item.setPrice(request.price());
        item.setCategory(request.category());
        item.setAvailable(request.isAvailable());
        item.setImageUrl(request.imageUrl());

        MenuItem saved = menuItemRepo.save(item);

        // Use a more specific event type when only availability toggled
        MenuEvent event = availabilityChanged
                ? MenuEvent.availabilityChanged(restaurantId.toString(), saved)
                : MenuEvent.updated(restaurantId.toString(), saved);
        broadcast(restaurantId.toString(), event);

        return saved;
    }

    public void deleteMenuItem(UUID restaurantId, UUID itemId, String tokenRestaurantId) {
        validateOwnership(restaurantId, tokenRestaurantId);

        MenuItem item = menuItemRepo.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Menu item not found"));

        if (!item.getRestaurant().getRestaurantId().equals(restaurantId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Menu item does not belong to the specified restaurant");
        }

        menuItemRepo.delete(item);
        broadcast(restaurantId.toString(), MenuEvent.deleted(restaurantId.toString(), itemId.toString()));
    }
}

