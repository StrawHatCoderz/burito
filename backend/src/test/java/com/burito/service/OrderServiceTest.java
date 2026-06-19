package com.burito.service;

import com.burito.controller.views.OrderView;
import com.burito.domain.Order;
import com.burito.domain.Restaurant;
import com.burito.domain.User;
import com.burito.enums.OrderStatus;
import com.burito.repository.CartItemRepo;
import com.burito.repository.CartRepo;
import com.burito.repository.OrderRepo;
import com.burito.repository.UserRepo;
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
    private UserRepo userRepo;
    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepo, cartRepo, cartItemRepo, paymentService, userRepo, messagingTemplate);
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

        when(orderRepo.findFirstByUser_UserIdAndStatusInOrderByCreatedAtDesc(
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

        when(orderRepo.findFirstByUser_UserIdAndStatusInOrderByCreatedAtDesc(
                eq(userId), 
                eq(Arrays.asList(OrderStatus.PENDING, OrderStatus.ACCEPTED))))
                .thenReturn(null);

        OrderView view = orderService.getActiveOrder(userId);

        assertNull(view);
    }
}
