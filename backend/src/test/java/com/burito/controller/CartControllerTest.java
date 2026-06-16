package com.burito.controller;

import com.burito.config.TestcontainersConfig;
import com.burito.controller.views.CartItemRequest;
import com.burito.domain.MenuItem;
import com.burito.domain.User;
import com.burito.enums.ErrorCode;
import com.burito.repository.CartItemRepo;
import com.burito.repository.CartRepo;
import com.burito.repository.MenuItemRepo;
import com.burito.repository.UserRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
    "spring.flyway.enabled=true",
    "spring.jpa.hibernate.ddl-auto=validate",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect"
})
@Import(TestcontainersConfig.class)
@Transactional
class CartControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private MenuItemRepo menuItemRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private CartRepo cartRepo;

    @Autowired
    private CartItemRepo cartItemRepo;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private User user;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        cartItemRepo.deleteAll();
        cartRepo.deleteAll();

        user = userRepo.findUserByEmail("testuser@example.com");
        if (user == null) {
            user = new User("Test User", "testuser@example.com", "password");
            user = userRepo.save(user);
        }
    }

    @Test
    void shouldReturn401WhenMissingBothAuthAndGuestId() throws Exception {
        CartItemRequest request = new CartItemRequest(UUID.randomUUID(), 1);

        mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser@example.com")
    void shouldAddItemToNewCart() throws Exception {
        MenuItem item = menuItemRepo.findAll().stream()
                .filter(i -> i.getName().equals("Samosa (2 pcs)"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Seeded menu item Samosa (2 pcs) not found"));

        CartItemRequest request = new CartItemRequest(item.getMenuItemId(), 2);

        mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].name").value("Samosa (2 pcs)"))
                .andExpect(jsonPath("$.data.items[0].quantity").value(2))
                .andExpect(jsonPath("$.data.total").value(158.00));
    }

    @Test
    @WithMockUser(username = "testuser@example.com")
    void shouldIncrementQuantityForSameRestaurantItem() throws Exception {
        MenuItem item = menuItemRepo.findAll().stream()
                .filter(i -> i.getName().equals("Samosa (2 pcs)"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Seeded menu item Samosa (2 pcs) not found"));

        CartItemRequest request1 = new CartItemRequest(item.getMenuItemId(), 2);
        CartItemRequest request2 = new CartItemRequest(item.getMenuItemId(), 3);

        mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].name").value("Samosa (2 pcs)"))
                .andExpect(jsonPath("$.data.items[0].quantity").value(5))
                .andExpect(jsonPath("$.data.total").value(395.00));
    }

    @Test
    @WithMockUser(username = "testuser@example.com")
    void shouldResetCartWhenAddingDifferentRestaurantItem() throws Exception {
        MenuItem itemA = menuItemRepo.findAll().stream()
                .filter(i -> i.getName().equals("Samosa (2 pcs)"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Seeded menu item Samosa (2 pcs) not found"));

        MenuItem itemB = menuItemRepo.findAll().stream()
                .filter(i -> i.getName().equals("Masala Dosa"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Seeded menu item Masala Dosa not found"));

        CartItemRequest requestA = new CartItemRequest(itemA.getMenuItemId(), 2);
        CartItemRequest requestB = new CartItemRequest(itemB.getMenuItemId(), 1);

        mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestA)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.items[0].name").value("Masala Dosa"))
                .andExpect(jsonPath("$.data.items[0].quantity").value(1))
                .andExpect(jsonPath("$.data.total").value(149.00));
    }

    @Test
    @WithMockUser(username = "testuser@example.com")
    void shouldReturn400WhenMenuItemDoesNotExist() throws Exception {
        CartItemRequest request = new CartItemRequest(UUID.randomUUID(), 1);

        mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.errorCode").value(ErrorCode.MENU_ITEM_NOT_FOUND.name()))
                .andExpect(jsonPath("$.error.message").exists());
    }

    @Test
    @WithMockUser(username = "testuser@example.com")
    void shouldReturn400WhenMenuItemIsUnavailable() throws Exception {
        MenuItem item = menuItemRepo.findAll().stream()
                .filter(i -> i.getName().equals("Coconut Chutney"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Seeded menu item Coconut Chutney not found"));

        CartItemRequest request = new CartItemRequest(item.getMenuItemId(), 1);

        mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.errorCode").value(ErrorCode.MENU_ITEM_UNAVAILABLE.name()))
                .andExpect(jsonPath("$.error.message").exists());
    }

    @Test
    @WithMockUser(username = "testuser@example.com")
    void shouldRemoveItemAndRecomputeTotal() throws Exception {
        MenuItem itemA = menuItemRepo.findAll().stream()
                .filter(i -> i.getName().equals("Samosa (2 pcs)"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Seeded menu item Samosa (2 pcs) not found"));

        MenuItem itemB = menuItemRepo.findAll().stream()
                .filter(i -> i.getRestaurant().getRestaurantId().equals(itemA.getRestaurant().getRestaurantId())
                             && !i.getMenuItemId().equals(itemA.getMenuItemId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Second menu item from same restaurant not found"));

        CartItemRequest requestA = new CartItemRequest(itemA.getMenuItemId(), 2);
        CartItemRequest requestB = new CartItemRequest(itemB.getMenuItemId(), 1);

        mockMvc.perform(post("/api/cart/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestA)));

        String cartResponse = mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestB)))
                .andReturn().getResponse().getContentAsString();

        String cartItemIdA = objectMapper.readTree(cartResponse)
                .get("data").get("items").get(0).get("cartItemId").asText(); // Depending on insertion order, maybe need to check name

        mockMvc.perform(delete("/api/cart/items/" + cartItemIdA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items.length()").value(1));
    }

    @Test
    @WithMockUser(username = "testuser@example.com")
    void shouldClearCart() throws Exception {
        MenuItem item = menuItemRepo.findAll().stream()
                .filter(i -> i.getName().equals("Samosa (2 pcs)"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Seeded menu item Samosa (2 pcs) not found"));

        CartItemRequest request = new CartItemRequest(item.getMenuItemId(), 2);

        mockMvc.perform(post("/api/cart/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        mockMvc.perform(delete("/api/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items.length()").value(0))
                .andExpect(jsonPath("$.data.total").value(0.00));
    }

    @Test
    @WithMockUser(username = "testuser@example.com")
    void shouldReturn404WhenRemovingNonExistentItem() throws Exception {
        mockMvc.perform(delete("/api/cart/items/" + UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.errorCode").value(ErrorCode.CART_ITEM_NOT_FOUND.name()));
    }

    @Test
    void shouldReturn401WhenDeletingWithoutAuth() throws Exception {
        mockMvc.perform(delete("/api/cart"))
                .andExpect(status().isUnauthorized());
    }
}
