package com.burito.repository;

import com.burito.domain.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

import com.burito.enums.CartStatus;

@Repository
public interface CartRepo extends JpaRepository<Cart, UUID> {
    Optional<Cart> findByUser_UserIdAndStatus(UUID userId, CartStatus status);
    Optional<Cart> findByGuestIdAndStatus(UUID guestId, CartStatus status);
}
