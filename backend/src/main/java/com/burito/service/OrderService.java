package com.burito.service;

import com.burito.domain.Cart;
import com.burito.domain.CartItem;
import com.burito.domain.Order;
import com.burito.domain.OrderItem;
import com.burito.domain.User;
import com.burito.enums.CartStatus;
import com.burito.repository.CartItemRepo;
import com.burito.repository.CartRepo;
import com.burito.repository.OrderRepo;
import com.burito.repository.UserRepo;
import com.burito.controller.AdminOrderController;
import com.burito.controller.views.OrderView;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.Arrays;
import com.burito.enums.OrderStatus;

@Service
public class OrderService {

    private final OrderRepo orderRepo;
    private final CartRepo cartRepo;
    private final CartItemRepo cartItemRepo;
    private final PaymentService paymentService;
    private final UserRepo userRepo;
    private final SimpMessagingTemplate messagingTemplate;

    public OrderService(OrderRepo orderRepo, CartRepo cartRepo, CartItemRepo cartItemRepo, PaymentService paymentService, UserRepo userRepo, SimpMessagingTemplate messagingTemplate) {
        this.orderRepo = orderRepo;
        this.cartRepo = cartRepo;
        this.cartItemRepo = cartItemRepo;
        this.paymentService = paymentService;
        this.userRepo = userRepo;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public Order checkout(UUID userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Cart cart = cartRepo.findByUser_UserIdAndStatus(userId, CartStatus.PENDING)
                .orElseThrow(() -> new IllegalStateException("No active cart found"));

        List<CartItem> cartItems = cartItemRepo.findByCart_CartId(cart.getCartId());
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("Cannot checkout with an empty cart");
        }

        if (!cart.getRestaurant().isOpen()) {
            throw new IllegalStateException("Restaurant is currently closed");
        }

        Double totalAmount = cart.getTotal().doubleValue();

        boolean paymentSuccess = paymentService.processPayment(totalAmount);
        if (!paymentSuccess) {
            throw new IllegalStateException("Payment failed");
        }

        Order order = new Order(user, cart.getRestaurant(), totalAmount);

        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem(
                    cartItem.getMenuItem().getMenuItemId(),
                    cartItem.getMenuItem().getName(),
                    cartItem.getUnitPrice().doubleValue(),
                    cartItem.getQuantity()
            );
            order.addItem(orderItem);
        }

        Order savedOrder = orderRepo.save(order);

        // Clear cart
        cartItemRepo.deleteByCart_CartId(cart.getCartId());
        cart.setStatus(CartStatus.EXPIRED);
        cartRepo.save(cart);

        OrderView view = AdminOrderController.mapToView(savedOrder);
        messagingTemplate.convertAndSend("/topic/restaurant/" + cart.getRestaurant().getRestaurantId() + "/orders", view);

        return savedOrder;
    }

    @Transactional(readOnly = true)
    public OrderView getActiveOrder(UUID userId) {
        Order activeOrder = orderRepo.findFirstByCustomer_UserIdAndStatusInOrderByCreatedAtDesc(
                userId, 
                Arrays.asList(OrderStatus.PENDING, OrderStatus.ACCEPTED)
        );
        
        if (activeOrder == null) {
            return null;
        }
        
        return AdminOrderController.mapToView(activeOrder);
    }
}
