package com.burito.ordering.controller;

import com.burito.ordering.controller.views.OrderView;
import com.burito.identity.domain.User;
import com.burito.identity.service.UserService;
import com.burito.ordering.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private UserService userService;

    private OrderController orderController;

    @BeforeEach
    void setUp() {
        orderController = new OrderController(orderService, userService);
    }

    @Test
    void getActiveOrder_returnsActiveOrder() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("test@test.com");

        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setEmail("test@test.com");

        when(userService.findUserByEmail("test@test.com")).thenReturn(user);

        OrderView view = new OrderView();
        view.setId(UUID.randomUUID());
        
        when(orderService.getActiveOrder(user.getUserId())).thenReturn(view);

        ResponseEntity<?> response = orderController.getActiveOrder(userDetails);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(view, response.getBody());
    }

    @Test
    void getActiveOrder_returns404WhenNotFound() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("test@test.com");

        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setEmail("test@test.com");

        when(userService.findUserByEmail("test@test.com")).thenReturn(user);
        when(orderService.getActiveOrder(user.getUserId())).thenReturn(null);

        ResponseEntity<?> response = orderController.getActiveOrder(userDetails);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
