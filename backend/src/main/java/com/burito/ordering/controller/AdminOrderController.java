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
import com.burito.ordering.utils.OrderMapper;
import com.burito.ordering.domain.Order;
import com.burito.catalog.domain.Restaurant;
import com.burito.identity.domain.User;
import com.burito.ordering.enums.OrderStatus;
import com.burito.ordering.service.OrderService;
import com.burito.catalog.service.RestaurantService;
import com.burito.identity.service.UserService;
import com.burito.core.exceptions.UnauthorizedException;
import com.burito.core.exceptions.ForbiddenException;
import com.burito.core.exceptions.BadRequestException;
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

    private final OrderService orderService;
    private final RestaurantService restaurantService;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    public AdminOrderController(OrderService orderService, RestaurantService restaurantService, UserService userService, SimpMessagingTemplate messagingTemplate) {
        this.orderService = orderService;
        this.restaurantService = restaurantService;
        this.userService = userService;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping
    public ResponseEntity<?> getActiveOrders(@AuthenticationPrincipal UserDetails userDetails) {
        User admin = userService.findUserByEmail(userDetails.getUsername());
        if (admin == null) {
            throw new UnauthorizedException("Unauthorized");
        }
        UUID restaurantId = restaurantService.getRestaurantIdByOwnerId(admin.getUserId());
        if (restaurantId == null) {
            throw new BadRequestException("Restaurant not found for admin");
        }

        List<Order> orders = orderService.getActiveOrders(restaurantId);

        List<OrderView> orderViews = orders.stream().map(OrderMapper::mapToView).collect(Collectors.toList());
        return ResponseEntity.ok(orderViews);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable UUID orderId,
            @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal UserDetails userDetails) {

        User admin = userService.findUserByEmail(userDetails.getUsername());
        if (admin == null) {
            throw new UnauthorizedException("Unauthorized");
        }
        UUID adminRestaurantId = restaurantService.getRestaurantIdByOwnerId(admin.getUserId());

        OrderStatus newStatus = OrderStatus.valueOf(payload.get("status").toUpperCase());
        Order savedOrder = orderService.updateOrderStatus(orderId, adminRestaurantId, newStatus);

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

        return ResponseEntity.ok(OrderMapper.mapToView(savedOrder));
    }

}

