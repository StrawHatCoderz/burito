package com.burito.ordering.controller;
import com.burito.catalog.controller.views.*;
import com.burito.identity.controller.views.*;
import com.burito.ordering.controller.views.*;
import com.burito.core.controller.views.*;
import com.burito.core.controller.views.APIResponse;
import com.burito.core.controller.views.ApiError;

import com.burito.ordering.domain.Order;
import com.burito.identity.domain.User;
import com.burito.identity.service.UserService;
import com.burito.ordering.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.burito.core.exceptions.UnauthorizedException;
import com.burito.core.exceptions.ResourceNotFoundException;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    public OrderController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            throw new UnauthorizedException("Unauthorized");
        }

        User user = userService.findUserByEmail(userDetails.getUsername());
        if (user == null) {
            throw new UnauthorizedException("Unauthorized");
        }

        Order order = orderService.checkout(user.getUserId());
        return ResponseEntity.ok(APIResponse.success(Map.of("orderId", order.getId(), "status", order.getStatus())));
    }

    @GetMapping("/active")
    public ResponseEntity<?> getActiveOrder(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            throw new UnauthorizedException("Unauthorized");
        }

        User user = userService.findUserByEmail(userDetails.getUsername());
        if (user == null) {
            throw new UnauthorizedException("Unauthorized");
        }

        var activeOrder = orderService.getActiveOrder(user.getUserId());
        if (activeOrder == null) {
            throw new ResourceNotFoundException("No active order found");
        }

        return ResponseEntity.ok(APIResponse.success(activeOrder));
    }
}
