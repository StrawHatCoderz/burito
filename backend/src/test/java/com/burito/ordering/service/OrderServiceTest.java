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
}
