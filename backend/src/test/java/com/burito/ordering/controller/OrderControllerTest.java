package com.burito.ordering.controller;

import com.burito.ordering.controller.views.OrderView;
import com.burito.identity.domain.User;
import com.burito.identity.service.UserService;
import com.burito.ordering.service.OrderService;
import com.burito.ordering.domain.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import com.burito.core.controller.views.APIResponse;
import com.burito.core.exceptions.APIException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        assertEquals(view, ((APIResponse<?>) response.getBody()).data());
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

        APIException ex = assertThrows(APIException.class, () -> orderController.getActiveOrder(userDetails));
        assertEquals(com.burito.core.enums.ErrorCode.NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void getActiveOrder_throwsUnauthorizedWhenUserDetailsNull() {
        APIException ex = assertThrows(APIException.class, () -> orderController.getActiveOrder(null));
        assertEquals(com.burito.core.enums.ErrorCode.UNAUTHORIZED, ex.getErrorCode());
    }

    @Test
    void getActiveOrder_throwsUnauthorizedWhenUserNotFound() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("test@test.com");
        when(userService.findUserByEmail("test@test.com")).thenReturn(null);

        APIException ex = assertThrows(APIException.class, () -> orderController.getActiveOrder(userDetails));
        assertEquals(com.burito.core.enums.ErrorCode.UNAUTHORIZED, ex.getErrorCode());
    }

    @Test
    void checkout_success() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("test@test.com");

        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setEmail("test@test.com");
        when(userService.findUserByEmail("test@test.com")).thenReturn(user);

        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setStatus(com.burito.ordering.enums.OrderStatus.PENDING);
        when(orderService.checkout(user.getUserId())).thenReturn(order);

        ResponseEntity<?> response = orderController.checkout(userDetails);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        java.util.Map<String, Object> data = (java.util.Map<String, Object>) ((APIResponse<?>) response.getBody()).data();
        assertEquals(order.getId(), data.get("orderId"));
        assertEquals(com.burito.ordering.enums.OrderStatus.PENDING, data.get("status"));
    }

    @Test
    void checkout_throwsUnauthorizedWhenUserDetailsNull() {
        APIException ex = assertThrows(APIException.class, () -> orderController.checkout(null));
        assertEquals(com.burito.core.enums.ErrorCode.UNAUTHORIZED, ex.getErrorCode());
    }

    @Test
    void checkout_throwsUnauthorizedWhenUserNotFound() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("test@test.com");
        when(userService.findUserByEmail("test@test.com")).thenReturn(null);

        APIException ex = assertThrows(APIException.class, () -> orderController.checkout(userDetails));
        assertEquals(com.burito.core.enums.ErrorCode.UNAUTHORIZED, ex.getErrorCode());
    }
}
