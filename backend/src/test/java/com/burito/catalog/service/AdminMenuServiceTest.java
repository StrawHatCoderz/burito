package com.burito.catalog.service;

import com.burito.catalog.controller.views.MenuItemRequest;
import com.burito.catalog.domain.MenuItem;
import com.burito.catalog.domain.Restaurant;
import com.burito.catalog.enums.MenuCategory;
import com.burito.catalog.repository.MenuItemRepo;
import com.burito.catalog.repository.RestaurantRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import com.burito.core.exceptions.ForbiddenException;
import com.burito.core.exceptions.ResourceNotFoundException;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminMenuServiceTest {

    @Mock
    private MenuItemRepo menuItemRepo;

    @Mock
    private RestaurantRepo restaurantRepo;

    @Mock
    private org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private AdminMenuService adminMenuService;

    private UUID restaurantId;
    private UUID itemId;
    private String tokenRestaurantId;
    private Restaurant restaurant;
    private MenuItemRequest request;

    @BeforeEach
    void setUp() {
        restaurantId = UUID.randomUUID();
        itemId = UUID.randomUUID();
        tokenRestaurantId = restaurantId.toString();

        restaurant = new Restaurant();
        restaurant.setRestaurantId(restaurantId);

        request = new MenuItemRequest("Tacos", "Delicious tacos", BigDecimal.valueOf(10.99), MenuCategory.MAINS, true, "http://image.url");
    }

    @Test
    void createMenuItem_Success() {
        when(restaurantRepo.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(menuItemRepo.save(any(MenuItem.class))).thenAnswer(i -> {
            MenuItem saved = (MenuItem) i.getArguments()[0];
            saved.setMenuItemId(itemId);
            return saved;
        });

        MenuItem result = adminMenuService.createMenuItem(restaurantId, tokenRestaurantId, request);

        assertNotNull(result);
        assertEquals("Tacos", result.getName());
        assertEquals("http://image.url", result.getImageUrl());
        assertEquals(restaurant, result.getRestaurant());
        verify(menuItemRepo, times(1)).save(any(MenuItem.class));
    }

    @Test
    void createMenuItem_Forbidden() {
        assertThrows(ForbiddenException.class, () -> 
            adminMenuService.createMenuItem(restaurantId, "different-id", request));
            
        verify(menuItemRepo, never()).save(any());
    }

    @Test
    void updateMenuItem_Success() {
        MenuItem existingItem = new MenuItem();
        existingItem.setMenuItemId(itemId);
        existingItem.setRestaurant(restaurant);

        when(menuItemRepo.findById(itemId)).thenReturn(Optional.of(existingItem));
        when(menuItemRepo.save(any(MenuItem.class))).thenAnswer(i -> i.getArguments()[0]);

        MenuItem result = adminMenuService.updateMenuItem(restaurantId, itemId, tokenRestaurantId, request);

        assertNotNull(result);
        assertEquals("Tacos", result.getName());
        assertEquals("http://image.url", result.getImageUrl());
        verify(menuItemRepo, times(1)).save(any(MenuItem.class));
    }

    @Test
    void deleteMenuItem_Success() {
        MenuItem existingItem = new MenuItem();
        existingItem.setMenuItemId(itemId);
        existingItem.setRestaurant(restaurant);

        when(menuItemRepo.findById(itemId)).thenReturn(Optional.of(existingItem));

        adminMenuService.deleteMenuItem(restaurantId, itemId, tokenRestaurantId);

        verify(menuItemRepo, times(1)).delete(existingItem);
    }
}
