package com.burito.service;

import com.burito.controller.views.CartView;
import com.burito.domain.*;
import com.burito.enums.CuisineType;
import com.burito.enums.MenuCategory;
import com.burito.exceptions.MenuItemNotFoundException;
import com.burito.exceptions.MenuItemUnavailableException;
import com.burito.repository.CartItemRepo;
import com.burito.repository.CartRepo;
import com.burito.repository.MenuItemRepo;
import com.burito.repository.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepo cartRepo;
    @Mock
    private CartItemRepo cartItemRepo;
    @Mock
    private MenuItemRepo menuItemRepo;
    @Mock
    private UserRepo userRepo;

    @InjectMocks
    private CartService cartService;

    private User user;
    private Restaurant restaurantA;
    private Restaurant restaurantB;
    private MenuItem menuItemA;
    private MenuItem menuItemB;
    private UUID userId;
    private UUID menuItemIdA;
    private UUID menuItemIdB;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        menuItemIdA = UUID.randomUUID();
        menuItemIdB = UUID.randomUUID();

        user = new User("John Doe", "john@example.com", "password");
        user.setUserId(userId);

        restaurantA = new Restaurant(UUID.randomUUID(), "Restaurant A", CuisineType.INDIAN, 4.0, 30, true, null);
        restaurantB = new Restaurant(UUID.randomUUID(), "Restaurant B", CuisineType.ITALIAN, 4.5, 25, true, null);

        menuItemA = new MenuItem();
        menuItemA.setMenuItemId(menuItemIdA);
        menuItemA.setName("Item A");
        menuItemA.setPrice(new BigDecimal("10.00"));
        menuItemA.setCategory(MenuCategory.MAINS);
        menuItemA.setAvailable(true);
        menuItemA.setRestaurant(restaurantA);

        menuItemB = new MenuItem();
        menuItemB.setMenuItemId(menuItemIdB);
        menuItemB.setName("Item B");
        menuItemB.setPrice(new BigDecimal("15.00"));
        menuItemB.setCategory(MenuCategory.MAINS);
        menuItemB.setAvailable(true);
        menuItemB.setRestaurant(restaurantB);
    }

    @Test
    void shouldAddItemToNewCart() throws Exception {
        when(menuItemRepo.findById(menuItemIdA)).thenReturn(Optional.of(menuItemA));
        when(userRepo.findById(userId)).thenReturn(Optional.of(user));
        when(cartRepo.findByUser_UserId(userId)).thenReturn(Optional.empty());

        Cart expectedCart = new Cart(user, restaurantA);
        expectedCart.setCartId(UUID.randomUUID());
        when(cartRepo.save(any(Cart.class))).thenReturn(expectedCart);

        CartItem expectedItem = new CartItem(expectedCart, menuItemA, 2, menuItemA.getPrice());
        when(cartItemRepo.findByCart_CartId(expectedCart.getCartId())).thenReturn(List.of(expectedItem));

        CartView result = cartService.addItem(userId, menuItemIdA, 2);

        assertNotNull(result);
        assertEquals(expectedCart.getCartId(), result.cartId());
        assertEquals(restaurantA.getRestaurantId(), result.restaurantId());
        assertEquals(1, result.items().size());
        assertEquals("Item A", result.items().get(0).name());
        assertEquals(2, result.items().get(0).quantity());
        assertEquals(new BigDecimal("20.00"), result.total());

        verify(cartRepo, atLeastOnce()).save(any(Cart.class));
        verify(cartItemRepo).save(any(CartItem.class));
    }

    @Test
    void shouldIncrementQuantityWhenAddingSameItemToExistingCart() throws Exception {
        Cart cart = new Cart(user, restaurantA);
        cart.setCartId(UUID.randomUUID());

        CartItem existingItem = new CartItem(cart, menuItemA, 2, menuItemA.getPrice());

        when(menuItemRepo.findById(menuItemIdA)).thenReturn(Optional.of(menuItemA));
        when(userRepo.findById(userId)).thenReturn(Optional.of(user));
        when(cartRepo.findByUser_UserId(userId)).thenReturn(Optional.of(cart));
        when(cartItemRepo.findByCartAndMenuItem(cart, menuItemA)).thenReturn(Optional.of(existingItem));

        when(cartRepo.save(any(Cart.class))).thenReturn(cart);
        when(cartItemRepo.findByCart_CartId(cart.getCartId())).thenReturn(List.of(existingItem));

        CartView result = cartService.addItem(userId, menuItemIdA, 3);

        assertNotNull(result);
        assertEquals(5, existingItem.getQuantity());
        assertEquals(new BigDecimal("50.00"), result.total());

        verify(cartItemRepo).save(existingItem);
    }

    @Test
    void shouldAddNewItemToExistingCartWhenSameRestaurant() throws Exception {
        Cart cart = new Cart(user, restaurantA);
        cart.setCartId(UUID.randomUUID());

        when(menuItemRepo.findById(menuItemIdA)).thenReturn(Optional.of(menuItemA));
        when(userRepo.findById(userId)).thenReturn(Optional.of(user));
        when(cartRepo.findByUser_UserId(userId)).thenReturn(Optional.of(cart));
        when(cartItemRepo.findByCartAndMenuItem(cart, menuItemA)).thenReturn(Optional.empty());

        when(cartRepo.save(any(Cart.class))).thenReturn(cart);
        CartItem newItem = new CartItem(cart, menuItemA, 1, menuItemA.getPrice());
        when(cartItemRepo.findByCart_CartId(cart.getCartId())).thenReturn(List.of(newItem));

        CartView result = cartService.addItem(userId, menuItemIdA, 1);

        assertNotNull(result);
        assertEquals(1, result.items().size());
        assertEquals(new BigDecimal("10.00"), result.total());

        verify(cartItemRepo).save(any(CartItem.class));
    }

    @Test
    void shouldResetCartAndAddNewItemWhenDifferentRestaurant() throws Exception {
        Cart cart = new Cart(user, restaurantA);
        cart.setCartId(UUID.randomUUID());

        when(menuItemRepo.findById(menuItemIdB)).thenReturn(Optional.of(menuItemB));
        when(userRepo.findById(userId)).thenReturn(Optional.of(user));
        when(cartRepo.findByUser_UserId(userId)).thenReturn(Optional.of(cart));
        when(cartRepo.save(any(Cart.class))).thenReturn(cart);

        CartItem newItem = new CartItem(cart, menuItemB, 1, menuItemB.getPrice());
        when(cartItemRepo.findByCart_CartId(cart.getCartId())).thenReturn(List.of(newItem));

        CartView result = cartService.addItem(userId, menuItemIdB, 1);

        assertNotNull(result);
        assertEquals(restaurantB.getRestaurantId(), result.restaurantId());
        assertEquals(1, result.items().size());
        assertEquals("Item B", result.items().get(0).name());
        assertEquals(new BigDecimal("15.00"), result.total());

        verify(cartItemRepo).deleteByCart_CartId(cart.getCartId());
        verify(cartItemRepo).save(any(CartItem.class));
    }

    @Test
    void shouldThrowMenuItemNotFoundExceptionWhenMenuItemDoesNotExist() {
        when(menuItemRepo.findById(menuItemIdA)).thenReturn(Optional.empty());

        assertThrows(MenuItemNotFoundException.class, () -> cartService.addItem(userId, menuItemIdA, 1));
    }

    @Test
    void shouldThrowMenuItemUnavailableExceptionWhenMenuItemNotAvailable() {
        menuItemA.setAvailable(false);
        when(menuItemRepo.findById(menuItemIdA)).thenReturn(Optional.of(menuItemA));

        assertThrows(MenuItemUnavailableException.class, () -> cartService.addItem(userId, menuItemIdA, 1));
    }
}
