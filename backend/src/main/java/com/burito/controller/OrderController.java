package com.burito.controller;

import com.burito.domain.Order;
import com.burito.domain.User;
import com.burito.repository.UserRepo;
import com.burito.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserRepo userRepo;

    public OrderController(OrderService orderService, UserRepo userRepo) {
        this.orderService = orderService;
        this.userRepo = userRepo;
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        try {
            User user = userRepo.findUserByEmail(userDetails.getUsername());
            if (user == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }

            Order order = orderService.checkout(user.getUserId());
            return ResponseEntity.ok(Map.of("orderId", order.getId(), "status", order.getStatus()));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
