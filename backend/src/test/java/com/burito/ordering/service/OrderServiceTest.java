package com.burito.ordering.service;

import com.burito.ordering.controller.views.OrderView;
import com.burito.ordering.domain.Order;
import com.burito.catalog.domain.Restaurant;
import com.burito.identity.domain.User;
import com.burito.ordering.enums.OrderStatus;
import com.burito.ordering.repository.CartItemRepo;
import com.burito.ordering.repository.CartRepo;
import com.burito.ordering.repository.OrderRepo;
import com.burito.identity.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepo orderRepo;
    @Mock
    private CartRepo cartRepo;
    @Mock
    private CartItemRepo cartItemRepo;
    @Mock
    private PaymentService paymentService;
    @Mock
    private UserService userService;
    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepo, cartRepo, cartItemRepo, paymentService, userService, messagingTemplate);
    }

    @Test
    void getActiveOrder_returnsOrderView() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setUserId(userId);
        
        Restaurant restaurant = new Restaurant();
        restaurant.setRestaurantId(UUID.randomUUID());

        Order activeOrder = new Order(user, restaurant, 50.0);
        activeOrder.setId(UUID.randomUUID());
        activeOrder.setStatus(OrderStatus.ACCEPTED);

        when(orderRepo.findFirstByCustomer_UserIdAndStatusInOrderByCreatedAtDesc(
                eq(userId), 
                eq(Arrays.asList(OrderStatus.PENDING, OrderStatus.ACCEPTED))))
                .thenReturn(activeOrder);

        OrderView view = orderService.getActiveOrder(userId);

        assertNotNull(view);
        assertEquals(activeOrder.getId(), view.getId());
        assertEquals(OrderStatus.ACCEPTED, view.getStatus());
    }

    @Test
    void getActiveOrder_returnsNullWhenNotFound() {
        UUID userId = UUID.randomUUID();

        when(orderRepo.findFirstByCustomer_UserIdAndStatusInOrderByCreatedAtDesc(
                eq(userId), 
                eq(Arrays.asList(OrderStatus.PENDING, OrderStatus.ACCEPTED))))
                .thenReturn(null);

        OrderView view = orderService.getActiveOrder(userId);

        assertNull(view);
    }

    @Test
    void checkout_throwsExceptionWhenUserNotFound() {
        UUID userId = UUID.randomUUID();
        when(userService.findUserById(userId)).thenReturn(null);
        
        Exception exception = org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            orderService.checkout(userId);
        });
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void checkout_throwsExceptionWhenCartNotFound() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        when(userService.findUserById(userId)).thenReturn(user);
        when(cartRepo.findByUser_UserIdAndStatus(userId, com.burito.ordering.enums.CartStatus.PENDING)).thenReturn(java.util.Optional.empty());

        Exception exception = org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () -> {
            orderService.checkout(userId);
        });
        assertEquals("No active cart found", exception.getMessage());
    }

    @Test
    void checkout_throwsExceptionWhenCartEmpty() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        when(userService.findUserById(userId)).thenReturn(user);
        
        com.burito.ordering.domain.Cart cart = new com.burito.ordering.domain.Cart();
        cart.setCartId(UUID.randomUUID());
        when(cartRepo.findByUser_UserIdAndStatus(userId, com.burito.ordering.enums.CartStatus.PENDING)).thenReturn(java.util.Optional.of(cart));
        when(cartItemRepo.findByCart_CartId(cart.getCartId())).thenReturn(java.util.Collections.emptyList());

        Exception exception = org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () -> {
            orderService.checkout(userId);
        });
        assertEquals("Cannot checkout with an empty cart", exception.getMessage());
    }

    @Test
    void checkout_success() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        when(userService.findUserById(userId)).thenReturn(user);
        
        Restaurant restaurant = new Restaurant();
        restaurant.setRestaurantId(UUID.randomUUID());
        restaurant.setOpen(true);

        com.burito.ordering.domain.Cart cart = new com.burito.ordering.domain.Cart();
        cart.setCartId(UUID.randomUUID());
        cart.setRestaurant(restaurant);
        cart.setTotal(java.math.BigDecimal.valueOf(100.0));
        when(cartRepo.findByUser_UserIdAndStatus(userId, com.burito.ordering.enums.CartStatus.PENDING)).thenReturn(java.util.Optional.of(cart));

        com.burito.ordering.domain.CartItem cartItem = new com.burito.ordering.domain.CartItem();
        com.burito.catalog.domain.MenuItem menuItem = new com.burito.catalog.domain.MenuItem();
        menuItem.setMenuItemId(UUID.randomUUID());
        menuItem.setName("Taco");
        cartItem.setMenuItem(menuItem);
        cartItem.setUnitPrice(java.math.BigDecimal.valueOf(50.0));
        cartItem.setQuantity(2);

        when(cartItemRepo.findByCart_CartId(cart.getCartId())).thenReturn(java.util.List.of(cartItem));
        when(paymentService.processPayment(100.0)).thenReturn(true);
        when(orderRepo.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order order = orderService.checkout(userId);
        
        assertNotNull(order);
        assertEquals(100.0, order.getTotalAmount());
        assertEquals(1, order.getItems().size());
        org.mockito.Mockito.verify(cartItemRepo).deleteByCart_CartId(cart.getCartId());
        org.mockito.Mockito.verify(cartRepo).save(cart);
        assertEquals(com.burito.ordering.enums.CartStatus.EXPIRED, cart.getStatus());
    }

    @Test
    void getActiveOrders_success() {
        UUID restaurantId = UUID.randomUUID();
        Order order = new Order();
        when(orderRepo.findByRestaurant_RestaurantIdAndStatusInOrderByCreatedAtDesc(eq(restaurantId), any())).thenReturn(java.util.List.of(order));

        java.util.List<Order> orders = orderService.getActiveOrders(restaurantId);
        assertEquals(1, orders.size());
    }

    @Test
    void updateOrderStatus_throwsExceptionWhenOrderNotFound() {
        UUID orderId = UUID.randomUUID();
        when(orderRepo.findById(orderId)).thenReturn(java.util.Optional.empty());

        Exception exception = org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            orderService.updateOrderStatus(orderId, UUID.randomUUID(), OrderStatus.ACCEPTED);
        });
        assertEquals("Order not found", exception.getMessage());
    }

    @Test
    void updateOrderStatus_throwsExceptionWhenForbidden() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        Restaurant restaurant = new Restaurant();
        restaurant.setRestaurantId(UUID.randomUUID());
        order.setRestaurant(restaurant);
        when(orderRepo.findById(orderId)).thenReturn(java.util.Optional.of(order));

        Exception exception = org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () -> {
            orderService.updateOrderStatus(orderId, UUID.randomUUID(), OrderStatus.ACCEPTED);
        });
        assertEquals("Forbidden", exception.getMessage());
    }

    @Test
    void updateOrderStatus_success() {
        UUID orderId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();
        Order order = new Order();
        Restaurant restaurant = new Restaurant();
        restaurant.setRestaurantId(restaurantId);
        order.setRestaurant(restaurant);
        when(orderRepo.findById(orderId)).thenReturn(java.util.Optional.of(order));
        when(orderRepo.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        Order updated = orderService.updateOrderStatus(orderId, restaurantId, OrderStatus.DELIVERED);
        assertEquals(OrderStatus.DELIVERED, updated.getStatus());
    }

    @Test
    void checkout_shouldThrowExceptionWhenRestaurantIsClosed() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        when(userService.findUserById(userId)).thenReturn(user);

        Restaurant closedRestaurant = new Restaurant();
        closedRestaurant.setRestaurantId(UUID.randomUUID());
        closedRestaurant.setOpen(false);

        com.burito.ordering.domain.Cart cart = new com.burito.ordering.domain.Cart(user, closedRestaurant);
        cart.setCartId(UUID.randomUUID());
        cart.setTotal(java.math.BigDecimal.valueOf(100.0));

        when(cartRepo.findByUser_UserIdAndStatus(userId, com.burito.ordering.enums.CartStatus.PENDING)).thenReturn(java.util.Optional.of(cart));
        
        com.burito.catalog.domain.MenuItem menuItem = new com.burito.catalog.domain.MenuItem();
        menuItem.setMenuItemId(UUID.randomUUID());
        com.burito.ordering.domain.CartItem cartItem = new com.burito.ordering.domain.CartItem(cart, menuItem, 2, java.math.BigDecimal.valueOf(50.0));
        when(cartItemRepo.findByCart_CartId(cart.getCartId())).thenReturn(java.util.List.of(cartItem));

        IllegalStateException exception = org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () -> orderService.checkout(userId));
        assertEquals("Restaurant is currently closed", exception.getMessage());
    }

    @Test
    void checkout_shouldThrowExceptionWhenPaymentFails() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        when(userService.findUserById(userId)).thenReturn(user);

        Restaurant restaurant = new Restaurant();
        restaurant.setRestaurantId(UUID.randomUUID());
        restaurant.setOpen(true);

        com.burito.ordering.domain.Cart cart = new com.burito.ordering.domain.Cart(user, restaurant);
        cart.setCartId(UUID.randomUUID());
        cart.setTotal(java.math.BigDecimal.valueOf(100.0));

        when(cartRepo.findByUser_UserIdAndStatus(userId, com.burito.ordering.enums.CartStatus.PENDING)).thenReturn(java.util.Optional.of(cart));
        
        com.burito.catalog.domain.MenuItem menuItem = new com.burito.catalog.domain.MenuItem();
        menuItem.setMenuItemId(UUID.randomUUID());
        com.burito.ordering.domain.CartItem cartItem = new com.burito.ordering.domain.CartItem(cart, menuItem, 2, java.math.BigDecimal.valueOf(50.0));
        when(cartItemRepo.findByCart_CartId(cart.getCartId())).thenReturn(java.util.List.of(cartItem));
        
        when(paymentService.processPayment(100.0)).thenReturn(false);

        IllegalStateException exception = org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () -> orderService.checkout(userId));
        assertEquals("Payment failed", exception.getMessage());
    }
}
