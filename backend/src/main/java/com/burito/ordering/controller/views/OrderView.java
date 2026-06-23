package com.burito.ordering.controller.views;

import com.burito.ordering.enums.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderView {
    private UUID id;
    private UUID customerId;
    private String customerName;
    private UUID restaurantId;
    private OrderStatus status;
    private Double totalAmount;
    private LocalDateTime createdAt;
    private List<OrderItemView> items;
}
