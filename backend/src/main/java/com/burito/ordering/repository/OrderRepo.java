package com.burito.ordering.repository;

import com.burito.ordering.domain.Order;
import com.burito.ordering.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepo extends JpaRepository<Order, UUID> {
    List<Order> findByRestaurant_RestaurantIdAndStatusInOrderByCreatedAtDesc(UUID restaurantId, List<OrderStatus> statuses);

    Order findFirstByCustomer_UserIdAndStatusInOrderByCreatedAtDesc(UUID userId, List<OrderStatus> statuses);
}
