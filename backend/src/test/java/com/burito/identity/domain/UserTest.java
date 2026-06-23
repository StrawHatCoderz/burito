package com.burito.identity.domain;

import com.burito.identity.enums.Role;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class UserTest {

    @Test
    void testNoArgsConstructor() {
        User user = new User();
        assertNull(user.getUserId());
        assertNull(user.getEmail());
        assertNull(user.getHashPassword());
        assertNull(user.getFullName());
        assertEquals(Role.USER, user.getRole());
        assertNull(user.getCreatedAt());
        
        UUID id = UUID.randomUUID();
        user.setUserId(id);
        user.setEmail("test@test.com");
        user.setHashPassword("hash");
        user.setFullName("Test");
        user.setRole(Role.RESTAURANT_ADMIN);
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);

        assertEquals(id, user.getUserId());
        assertEquals("test@test.com", user.getEmail());
        assertEquals("hash", user.getHashPassword());
        assertEquals("Test", user.getFullName());
        assertEquals(Role.RESTAURANT_ADMIN, user.getRole());
        assertEquals(now, user.getCreatedAt());
    }

    @Test
    void testThreeArgsConstructor() {
        User user = new User("Wade Wilson", "wade@test.com", "hash");
        assertEquals("Wade Wilson", user.getFullName());
        assertEquals("wade@test.com", user.getEmail());
        assertEquals("hash", user.getHashPassword());
        assertEquals(Role.USER, user.getRole());
    }

    @Test
    void testFourArgsConstructor() {
        User user = new User("Admin", "admin@test.com", "hash", null);
        assertEquals(Role.USER, user.getRole());
    }
}
