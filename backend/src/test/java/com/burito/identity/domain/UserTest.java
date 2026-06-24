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

    @Test
    void testPhoneNumberAndAddressFields() {
        User user = new User();
        assertNull(user.getPhoneNumber());
        assertNull(user.getAddress());

        user.setPhoneNumber("1234567890");
        UserAddress address = new UserAddress(null, "123 St", "City", "State", "India", "12345");
        user.setAddress(address);

        assertEquals("1234567890", user.getPhoneNumber());
        assertNotNull(user.getAddress());
        assertEquals("123 St", user.getAddress().getStreet());
        assertEquals("City", user.getAddress().getCity());
        assertEquals("State", user.getAddress().getState());
        assertEquals("12345", user.getAddress().getZipcode());
        assertEquals("India", user.getAddress().getCountry());
    }
}
