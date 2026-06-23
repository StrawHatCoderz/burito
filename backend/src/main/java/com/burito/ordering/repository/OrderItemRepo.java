package com.burito.ordering.repository;

import com.burito.ordering.domain.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrderItemRepo extends JpaRepository<OrderItem, UUID> {
}
