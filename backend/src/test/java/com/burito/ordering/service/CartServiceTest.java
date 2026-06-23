package com.burito.ordering.service;
import com.burito.identity.domain.User;
import com.burito.catalog.domain.Restaurant;
import com.burito.catalog.domain.MenuItem;

import com.burito.ordering.controller.views.CartView;
import com.burito.ordering.domain.*;
import com.burito.catalog.enums.CuisineType;
import com.burito.catalog.enums.MenuCategory;
import com.burito.ordering.enums.CartStatus;
import com.burito.catalog.exceptions.MenuItemNotFoundException;
import com.burito.catalog.exceptions.MenuItemUnavailableException;
import com.burito.ordering.exceptions.CartItemNotFoundException;
import com.burito.ordering.repository.CartItemRepo;
import com.burito.ordering.repository.CartRepo;
import com.burito.catalog.repository.MenuItemRepo;
import com.burito.identity.repository.UserRepo;
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
        when(cartRepo.findByUser_UserIdAndStatus(userId, CartStatus.PENDING)).thenReturn(Optional.empty());

        Cart expectedCart = new Cart(user, restaurantA);
        expectedCart.setCartId(UUID.randomUUID());
        when(cartRepo.save(any(Cart.class))).thenReturn(expectedCart);

        CartItem expectedItem = new CartItem(expectedCart, menuItemA, 2, menuItemA.getPrice());
        when(cartItemRepo.findByCart_CartId(expectedCart.getCartId())).thenReturn(List.of(expectedItem));

        CartView result = cartService.addItem(userId, null, menuItemIdA, 2);

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
        when(cartRepo.findByUser_UserIdAndStatus(userId, CartStatus.PENDING)).thenReturn(Optional.of(cart));
        when(cartItemRepo.findByCartAndMenuItem(cart, menuItemA)).thenReturn(Optional.of(existingItem));

        when(cartRepo.save(any(Cart.class))).thenReturn(cart);
        when(cartItemRepo.findByCart_CartId(cart.getCartId())).thenReturn(List.of(existingItem));

        CartView result = cartService.addItem(userId, null, menuItemIdA, 3);

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
        when(cartRepo.findByUser_UserIdAndStatus(userId, CartStatus.PENDING)).thenReturn(Optional.of(cart));
        when(cartItemRepo.findByCartAndMenuItem(cart, menuItemA)).thenReturn(Optional.empty());

        when(cartRepo.save(any(Cart.class))).thenReturn(cart);
        CartItem newItem = new CartItem(cart, menuItemA, 1, menuItemA.getPrice());
        when(cartItemRepo.findByCart_CartId(cart.getCartId())).thenReturn(List.of(newItem));

        CartView result = cartService.addItem(userId, null, menuItemIdA, 1);

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
        when(cartRepo.findByUser_UserIdAndStatus(userId, CartStatus.PENDING)).thenReturn(Optional.of(cart));
        when(cartRepo.save(any(Cart.class))).thenReturn(cart);

        CartItem newItem = new CartItem(cart, menuItemB, 1, menuItemB.getPrice());
        when(cartItemRepo.findByCart_CartId(cart.getCartId())).thenReturn(List.of(newItem));

        CartView result = cartService.addItem(userId, null, menuItemIdB, 1);

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

        assertThrows(MenuItemNotFoundException.class, () -> cartService.addItem(userId, null, menuItemIdA, 1));
    }

    @Test
    void shouldThrowMenuItemUnavailableExceptionWhenMenuItemNotAvailable() {
        menuItemA.setAvailable(false);
        when(menuItemRepo.findById(menuItemIdA)).thenReturn(Optional.of(menuItemA));

        assertThrows(MenuItemUnavailableException.class, () -> cartService.addItem(userId, null, menuItemIdA, 1));
    }

    @Test
    void shouldRemoveItemAndRecomputeTotal() throws Exception {
        Cart cart = new Cart(user, restaurantA);
        cart.setCartId(UUID.randomUUID());
        
        CartItem itemA = new CartItem(cart, menuItemA, 2, menuItemA.getPrice());
        itemA.setCartItemId(UUID.randomUUID());
        
        CartItem itemB = new CartItem(cart, menuItemB, 1, menuItemB.getPrice());
        itemB.setCartItemId(UUID.randomUUID());
        
        when(cartRepo.findByUser_UserIdAndStatus(userId, CartStatus.PENDING)).thenReturn(Optional.of(cart));
        when(cartItemRepo.findById(itemA.getCartItemId())).thenReturn(Optional.of(itemA));
        when(cartItemRepo.findByCart_CartId(cart.getCartId())).thenReturn(List.of(itemB));
        when(cartRepo.save(any(Cart.class))).thenReturn(cart);

        CartView result = cartService.removeItem(userId, null, itemA.getCartItemId());
        
        assertNotNull(result);
        assertEquals(1, result.items().size());
        assertEquals("Item B", result.items().get(0).name());
        assertEquals(new BigDecimal("15.00"), result.total());
        
        verify(cartItemRepo).delete(itemA);
        verify(cartRepo).save(cart);
    }

    @Test
    void shouldRemoveLastItemAndMarkCartExpired() throws Exception {
        Cart cart = new Cart(user, restaurantA);
        cart.setCartId(UUID.randomUUID());
        
        CartItem itemA = new CartItem(cart, menuItemA, 2, menuItemA.getPrice());
        itemA.setCartItemId(UUID.randomUUID());
        
        when(cartRepo.findByUser_UserIdAndStatus(userId, CartStatus.PENDING)).thenReturn(Optional.of(cart));
        when(cartItemRepo.findById(itemA.getCartItemId())).thenReturn(Optional.of(itemA));
        when(cartItemRepo.findByCart_CartId(cart.getCartId())).thenReturn(List.of()); // No remaining items
        
        CartView result = cartService.removeItem(userId, null, itemA.getCartItemId());
        
        assertNotNull(result);
        assertTrue(result.items().isEmpty());
        assertEquals(BigDecimal.ZERO, result.total());
        assertEquals(CartStatus.EXPIRED, cart.getStatus());
        
        verify(cartItemRepo).delete(itemA);
        verify(cartRepo).save(cart);
    }

    @Test
    void shouldThrowCartItemNotFoundWhenCartDoesNotExist() {
        when(cartRepo.findByUser_UserIdAndStatus(userId, CartStatus.PENDING)).thenReturn(Optional.empty());
        assertThrows(CartItemNotFoundException.class, () -> cartService.removeItem(userId, null, UUID.randomUUID()));
    }

    @Test
    void shouldThrowCartItemNotFoundWhenItemNotBelongToCart() {
        Cart cart = new Cart(user, restaurantA);
        cart.setCartId(UUID.randomUUID());

        Cart otherCart = new Cart(user, restaurantB);
        otherCart.setCartId(UUID.randomUUID());

        CartItem itemA = new CartItem(otherCart, menuItemA, 2, menuItemA.getPrice());
        itemA.setCartItemId(UUID.randomUUID());
        
        when(cartRepo.findByUser_UserIdAndStatus(userId, CartStatus.PENDING)).thenReturn(Optional.of(cart));
        when(cartItemRepo.findById(itemA.getCartItemId())).thenReturn(Optional.of(itemA));

        assertThrows(CartItemNotFoundException.class, () -> cartService.removeItem(userId, null, itemA.getCartItemId()));
    }

    @Test
    void shouldClearCart() {
        Cart cart = new Cart(user, restaurantA);
        cart.setCartId(UUID.randomUUID());
        
        when(cartRepo.findByUser_UserIdAndStatus(userId, CartStatus.PENDING)).thenReturn(Optional.of(cart));
        
        CartView result = cartService.clearCart(userId, null);
        
        assertNotNull(result);
        assertTrue(result.items().isEmpty());
        assertEquals(BigDecimal.ZERO, result.total());
        assertEquals(CartStatus.EXPIRED, cart.getStatus());
        
        verify(cartItemRepo).deleteByCart_CartId(cart.getCartId());
        verify(cartRepo).save(cart);
    }

    @Test
    void getCart_shouldReturnCart() {
        Cart cart = new Cart(user, restaurantA);
        cart.setCartId(UUID.randomUUID());
        when(cartRepo.findByUser_UserIdAndStatus(userId, CartStatus.PENDING)).thenReturn(Optional.of(cart));
        CartItem itemA = new CartItem(cart, menuItemA, 2, menuItemA.getPrice());
        when(cartItemRepo.findByCart_CartId(cart.getCartId())).thenReturn(List.of(itemA));

        CartView result = cartService.getCart(userId, null);
        assertNotNull(result);
        assertEquals(cart.getCartId(), result.cartId());
        assertEquals(1, result.items().size());
    }

    @Test
    void getCart_shouldReturnEmptyCartWhenNotFound() {
        when(cartRepo.findByUser_UserIdAndStatus(userId, CartStatus.PENDING)).thenReturn(Optional.empty());

        CartView result = cartService.getCart(userId, null);
        assertNotNull(result);
        assertNull(result.cartId());
        assertTrue(result.items().isEmpty());
    }

    @Test
    void decrementItem_shouldDecrementQuantity() throws Exception {
        Cart cart = new Cart(user, restaurantA);
        cart.setCartId(UUID.randomUUID());
        CartItem itemA = new CartItem(cart, menuItemA, 2, menuItemA.getPrice());
        itemA.setCartItemId(UUID.randomUUID());

        when(cartRepo.findByUser_UserIdAndStatus(userId, CartStatus.PENDING)).thenReturn(Optional.of(cart));
        when(cartItemRepo.findById(itemA.getCartItemId())).thenReturn(Optional.of(itemA));
        when(cartRepo.save(any(Cart.class))).thenReturn(cart);

        CartView result = cartService.decrementItem(userId, null, itemA.getCartItemId());
        assertEquals(1, itemA.getQuantity());
        verify(cartItemRepo).save(itemA);
    }

    @Test
    void decrementItem_shouldRemoveItemWhenQuantityIsOne() throws Exception {
        Cart cart = new Cart(user, restaurantA);
        cart.setCartId(UUID.randomUUID());
        CartItem itemA = new CartItem(cart, menuItemA, 1, menuItemA.getPrice());
        itemA.setCartItemId(UUID.randomUUID());

        when(cartRepo.findByUser_UserIdAndStatus(userId, CartStatus.PENDING)).thenReturn(Optional.of(cart));
        when(cartItemRepo.findById(itemA.getCartItemId())).thenReturn(Optional.of(itemA));

        CartView result = cartService.decrementItem(userId, null, itemA.getCartItemId());
        verify(cartItemRepo).delete(itemA);
    }

    @Test
    void decrementItem_shouldThrowExceptionWhenCartNotFound() {
        when(cartRepo.findByUser_UserIdAndStatus(userId, CartStatus.PENDING)).thenReturn(Optional.empty());
        assertThrows(CartItemNotFoundException.class, () -> cartService.decrementItem(userId, null, UUID.randomUUID()));
    }

    @Test
    void mergeCart_shouldDiscardGuestCartIfUserCartExists() {
        UUID guestId = UUID.randomUUID();
        Cart guestCart = new Cart(guestId, restaurantB);
        guestCart.setCartId(UUID.randomUUID());
        Cart userCart = new Cart(user, restaurantA);
        userCart.setCartId(UUID.randomUUID());

        when(cartRepo.findByGuestIdAndStatus(guestId, CartStatus.PENDING)).thenReturn(Optional.of(guestCart));
        when(cartRepo.findByUser_UserIdAndStatus(userId, CartStatus.PENDING)).thenReturn(Optional.of(userCart));

        cartService.mergeCart(userId, guestId);

        assertEquals(CartStatus.EXPIRED, guestCart.getStatus());
        verify(cartRepo).save(guestCart);
    }

    @Test
    void mergeCart_shouldAssignGuestCartToUser() {
        UUID guestId = UUID.randomUUID();
        Cart guestCart = new Cart(guestId, restaurantB);
        guestCart.setCartId(UUID.randomUUID());

        when(cartRepo.findByGuestIdAndStatus(guestId, CartStatus.PENDING)).thenReturn(Optional.of(guestCart));
        when(cartRepo.findByUser_UserIdAndStatus(userId, CartStatus.PENDING)).thenReturn(Optional.empty());
        when(userRepo.findById(userId)).thenReturn(Optional.of(user));

        cartService.mergeCart(userId, guestId);

        assertEquals(user, guestCart.getUser());
        assertNull(guestCart.getGuestId());
        verify(cartRepo).save(guestCart);
    }

    @Test
    void mergeCart_shouldDoNothingIfIdsNull() {
        cartService.mergeCart(null, UUID.randomUUID());
        verify(cartRepo, never()).findByGuestIdAndStatus(any(), any());
    }

    @Test
    void getCartEntity_shouldThrowExceptionIfBothIdsNull() {
        assertThrows(IllegalArgumentException.class, () -> cartService.getCart(null, null));
    }

    @Test
    void decrementItem_whenItemDoesNotBelongToCart_throwsException() {
        Cart cart = new Cart(user, restaurantA);
        cart.setCartId(UUID.randomUUID());

        Cart otherCart = new Cart(user, restaurantB);
        otherCart.setCartId(UUID.randomUUID());

        CartItem itemA = new CartItem(otherCart, menuItemA, 2, menuItemA.getPrice());
        itemA.setCartItemId(UUID.randomUUID());
        
        when(cartRepo.findByUser_UserIdAndStatus(userId, CartStatus.PENDING)).thenReturn(Optional.of(cart));
        when(cartItemRepo.findById(itemA.getCartItemId())).thenReturn(Optional.of(itemA));

        assertThrows(CartItemNotFoundException.class, () -> cartService.decrementItem(userId, null, itemA.getCartItemId()));
    }

    @Test
    void clearCart_whenCartNull_returnsEmptyCartView() {
        when(cartRepo.findByUser_UserIdAndStatus(userId, CartStatus.PENDING)).thenReturn(Optional.empty());
        CartView result = cartService.clearCart(userId, null);
        assertNull(result.cartId());
        assertTrue(result.items().isEmpty());
    }

    @Test
    void addItem_whenUserNotFound_throwsException() throws Exception {
        when(menuItemRepo.findById(menuItemIdA)).thenReturn(Optional.of(menuItemA));
        when(userRepo.findById(userId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> cartService.addItem(userId, null, menuItemIdA, 2));
    }

    @Test
    void addItem_whenUserNull_createsCartWithGuestId() throws Exception {
        UUID guestId = UUID.randomUUID();
        when(menuItemRepo.findById(menuItemIdA)).thenReturn(Optional.of(menuItemA));
        when(cartRepo.findByGuestIdAndStatus(guestId, CartStatus.PENDING)).thenReturn(Optional.empty());

        Cart expectedCart = new Cart(guestId, restaurantA);
        expectedCart.setCartId(UUID.randomUUID());
        when(cartRepo.save(any(Cart.class))).thenReturn(expectedCart);

        CartItem expectedItem = new CartItem(expectedCart, menuItemA, 2, menuItemA.getPrice());
        when(cartItemRepo.findByCart_CartId(expectedCart.getCartId())).thenReturn(List.of(expectedItem));

        CartView result = cartService.addItem(null, guestId, menuItemIdA, 2);

        assertNotNull(result);
        assertEquals(expectedCart.getCartId(), result.cartId());
        verify(cartRepo, atLeastOnce()).save(any(Cart.class));
    }
}
