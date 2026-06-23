package com.burito.controller;

import com.burito.controller.views.OrderItemView;
import com.burito.controller.views.OrderStatusEvent;
import com.burito.controller.views.OrderView;
import com.burito.domain.Order;
import com.burito.domain.Restaurant;
import com.burito.domain.User;
import com.burito.enums.OrderStatus;
import com.burito.repository.OrderRepo;
import com.burito.repository.RestaurantRepo;
import com.burito.repository.UserRepo;
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
    private final RestaurantRepo restaurantRepo;
    private final UserRepo userRepo;
    private final SimpMessagingTemplate messagingTemplate;

    public AdminOrderController(OrderRepo orderRepo, RestaurantRepo restaurantRepo, UserRepo userRepo, SimpMessagingTemplate messagingTemplate) {
        this.orderRepo = orderRepo;
        this.restaurantRepo = restaurantRepo;
        this.userRepo = userRepo;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping
    public ResponseEntity<?> getActiveOrders(@AuthenticationPrincipal UserDetails userDetails) {
        User admin = userRepo.findUserByEmail(userDetails.getUsername());
        if (admin == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        Restaurant restaurant = restaurantRepo.findByOwnerId(admin.getUserId());
        if (restaurant == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Restaurant not found for admin"));
        }

        List<Order> orders = orderRepo.findByRestaurant_RestaurantIdAndStatusInOrderByCreatedAtDesc(
                restaurant.getRestaurantId(),
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

        User admin = userRepo.findUserByEmail(userDetails.getUsername());
        if (admin == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        Restaurant restaurant = restaurantRepo.findByOwnerId(admin.getUserId());

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (!order.getRestaurant().getRestaurantId().equals(restaurant.getRestaurantId())) {
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

