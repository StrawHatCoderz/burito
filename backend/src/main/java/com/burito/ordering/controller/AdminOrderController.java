package com.burito.ordering.controller;
import com.burito.catalog.controller.views.*;
import com.burito.identity.controller.views.*;
import com.burito.ordering.controller.views.*;
import com.burito.core.controller.views.*;
import com.burito.core.controller.views.APIResponse;
import com.burito.core.controller.views.ApiError;

import com.burito.ordering.controller.views.OrderItemView;
import com.burito.ordering.controller.views.OrderStatusEvent;
import com.burito.ordering.controller.views.OrderView;
import com.burito.ordering.domain.Order;
import com.burito.catalog.domain.Restaurant;
import com.burito.identity.domain.User;
import com.burito.ordering.enums.OrderStatus;
import com.burito.ordering.repository.OrderRepo;
import com.burito.catalog.service.RestaurantService;
import com.burito.identity.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/orders")
@PreAuthorize("hasRole('RESTAURANT_ADMIN')")
public class AdminOrderController {

    private final OrderRepo orderRepo;
    private final RestaurantService restaurantService;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    public AdminOrderController(OrderRepo orderRepo, RestaurantService restaurantService, UserService userService, SimpMessagingTemplate messagingTemplate) {
        this.orderRepo = orderRepo;
        this.restaurantService = restaurantService;
        this.userService = userService;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping
    public ResponseEntity<?> getActiveOrders(@AuthenticationPrincipal UserDetails userDetails) {
        User admin = userService.findUserByEmail(userDetails.getUsername());
        if (admin == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        UUID restaurantId = restaurantService.getRestaurantIdByOwnerId(admin.getUserId());
        if (restaurantId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Restaurant not found for admin"));
        }

        List<Order> orders = orderRepo.findByRestaurant_RestaurantIdAndStatusInOrderByCreatedAtDesc(
                restaurantId,
                List.of(OrderStatus.PENDING, OrderStatus.ACCEPTED)
        );

        List<OrderView> orderViews = orders.stream().map(AdminOrderController::mapToView).collect(Collectors.toList());
        return ResponseEntity.ok(orderViews);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable UUID orderId,
            @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal UserDetails userDetails) {

        User admin = userService.findUserByEmail(userDetails.getUsername());
        if (admin == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        UUID adminRestaurantId = restaurantService.getRestaurantIdByOwnerId(admin.getUserId());

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (adminRestaurantId == null || !order.getRestaurant().getRestaurantId().equals(adminRestaurantId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }

        try {
            OrderStatus newStatus = OrderStatus.valueOf(payload.get("status").toUpperCase());
            order.setStatus(newStatus);
            Order savedOrder = orderRepo.save(order);

            String customerId = savedOrder.getCustomer().getUserId().toString();
            String restaurantId = savedOrder.getRestaurant().getRestaurantId().toString();

            OrderStatusEvent statusEvent = new OrderStatusEvent(
                    savedOrder.getId().toString(),
                    customerId,
                    newStatus,
                    restaurantId
            );

            // Notify all admin sessions for this restaurant (multi-tab sync)
            messagingTemplate.convertAndSend(
                    "/topic/restaurant/" + restaurantId + "/order-status", statusEvent);

            // Notify the specific customer who placed the order
            String customerEmail = savedOrder.getCustomer().getEmail();
            messagingTemplate.convertAndSendToUser(
                    customerEmail, "/queue/orders", statusEvent);

            return ResponseEntity.ok(mapToView(savedOrder));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid status"));
        }
    }

    public static OrderView mapToView(Order order) {
        List<OrderItemView> itemViews = order.getItems().stream()
                .map(item -> new OrderItemView(
                        item.getId(),
                        item.getMenuItemId(),
                        item.getName(),
                        item.getPriceAtCheckout(),
                        item.getQuantity()))
                .collect(Collectors.toList());

        return new OrderView(
                order.getId(),
                order.getCustomer().getUserId(),
                order.getCustomer().getFullName() != null ? order.getCustomer().getFullName() : order.getCustomer().getEmail(),
                order.getRestaurant().getRestaurantId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                itemViews
        );
    }
}

