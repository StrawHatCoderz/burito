package com.burito.ordering.controller;

import com.burito.catalog.domain.Restaurant;
import com.burito.catalog.service.RestaurantService;
import com.burito.core.config.Security;
import com.burito.identity.domain.User;
import com.burito.identity.service.UserService;
import com.burito.ordering.domain.Order;
import com.burito.ordering.enums.OrderStatus;
import com.burito.ordering.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminOrderController.class)
@Import(Security.class)
public class AdminOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private RestaurantService restaurantService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private com.burito.identity.service.JWTService jwtService;

    @MockitoBean
    private SimpMessagingTemplate messagingTemplate;

    private ObjectMapper objectMapper = new ObjectMapper();

    private User adminUser;
    private UUID restaurantId;
    private Order mockOrder;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setUserId(UUID.randomUUID());
        adminUser.setEmail("admin@test.com");

        restaurantId = UUID.randomUUID();

        User customer = new User();
        customer.setUserId(UUID.randomUUID());
        customer.setEmail("customer@test.com");
        customer.setFullName("Customer Name");

        Restaurant restaurant = new Restaurant();
        restaurant.setRestaurantId(restaurantId);

        orderId = UUID.randomUUID();
        mockOrder = new Order(customer, restaurant, 100.0);
        mockOrder.setId(orderId);
        mockOrder.setStatus(OrderStatus.PENDING);
        mockOrder.setCreatedAt(LocalDateTime.now());
        mockOrder.setTotalAmount(100.0);
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "RESTAURANT_ADMIN")
    void getActiveOrders_success() throws Exception {
        when(userService.findUserByEmail("admin@test.com")).thenReturn(adminUser);
        when(restaurantService.getRestaurantIdByOwnerId(adminUser.getUserId())).thenReturn(restaurantId);
        when(orderService.getActiveOrders(restaurantId)).thenReturn(List.of(mockOrder));

        mockMvc.perform(get("/api/admin/orders")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(orderId.toString()))
                .andExpect(jsonPath("$.data[0].status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "RESTAURANT_ADMIN")
    void getActiveOrders_unauthorizedWhenUserNotFound() throws Exception {
        when(userService.findUserByEmail("admin@test.com")).thenReturn(null);

        mockMvc.perform(get("/api/admin/orders")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "RESTAURANT_ADMIN")
    void getActiveOrders_badRequestWhenRestaurantNotFound() throws Exception {
        when(userService.findUserByEmail("admin@test.com")).thenReturn(adminUser);
        when(restaurantService.getRestaurantIdByOwnerId(adminUser.getUserId())).thenReturn(null);

        mockMvc.perform(get("/api/admin/orders")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "RESTAURANT_ADMIN")
    void updateOrderStatus_success() throws Exception {
        when(userService.findUserByEmail("admin@test.com")).thenReturn(adminUser);
        when(restaurantService.getRestaurantIdByOwnerId(adminUser.getUserId())).thenReturn(restaurantId);

        mockOrder.setStatus(OrderStatus.ACCEPTED);
        when(orderService.updateOrderStatus(orderId, restaurantId, OrderStatus.ACCEPTED))
                .thenReturn(mockOrder);

        mockMvc.perform(put("/api/admin/orders/" + orderId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("status", "ACCEPTED"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ACCEPTED"));

        verify(messagingTemplate).convertAndSend(eq("/topic/restaurant/" + restaurantId + "/order-status"), any(com.burito.ordering.controller.views.OrderStatusEvent.class));
        verify(messagingTemplate).convertAndSendToUser(eq("customer@test.com"), eq("/queue/orders"), any(com.burito.ordering.controller.views.OrderStatusEvent.class));
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "RESTAURANT_ADMIN")
    void updateOrderStatus_unauthorizedWhenUserNotFound() throws Exception {
        when(userService.findUserByEmail("admin@test.com")).thenReturn(null);

        mockMvc.perform(put("/api/admin/orders/" + orderId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("status", "ACCEPTED"))))
                .andExpect(status().isUnauthorized());
    }
}
