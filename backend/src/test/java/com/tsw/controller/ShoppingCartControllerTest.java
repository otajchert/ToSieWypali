package com.tsw.controller;

import tools.jackson.databind.ObjectMapper;
import com.tsw.config.JwtUtil;
import com.tsw.dto.AddCartItemRequest;
import com.tsw.dto.UpdateCartItemQuantityRequest;
import com.tsw.model.CartItem;
import com.tsw.model.Client;
import com.tsw.model.Product;
import com.tsw.model.ShoppingCart;
import com.tsw.repository.CartItemRepository;
import com.tsw.repository.ClientRepository;
import com.tsw.repository.ProductRepository;
import com.tsw.repository.ShoppingCartRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ShoppingCartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ShoppingCartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    private final List<UUID> clientIds = new ArrayList<>();
    private final List<UUID> productIds = new ArrayList<>();

    @AfterEach
    void cleanUp() {
        for (UUID clientId : clientIds) {
            cartRepository.findByClientId(clientId).ifPresent(cart -> {
                cartItemRepository.deleteAll(cartItemRepository.findByCartId(cart.getId()));
                cartRepository.deleteById(cart.getId());
            });
        }
        productRepository.deleteAllById(productIds);
        clientRepository.deleteAllById(clientIds);
    }

    @Test
    void addingSameProductTwiceUpdatesOneCartLine() throws Exception {
        Client client = createClient();
        Product product = createProduct(10);

        mockMvc.perform(post("/api/cart/items")
                        .header(HttpHeaders.AUTHORIZATION, bearer(client))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new AddCartItemRequest(product.getId(), 2))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qty").value(2));

        mockMvc.perform(post("/api/cart/items")
                        .header(HttpHeaders.AUTHORIZATION, bearer(client))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new AddCartItemRequest(product.getId(), 3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qty").value(5));

        mockMvc.perform(get("/api/cart")
                        .header(HttpHeaders.AUTHORIZATION, bearer(client)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].qty").value(5))
                .andExpect(jsonPath("$[0].product.id").value(product.getId().toString()));

        ShoppingCart cart = cartRepository.findByClientId(client.getId()).orElseThrow();
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());

        assertEquals(1, items.size());
        assertEquals(5, items.getFirst().getQty());
        assertEquals(product.getId(), items.getFirst().getProduct().getId());
    }

    @Test
    void clientCannotUpdateAnotherClientsCartItem() throws Exception {
        Client owner = createClient();
        Client otherClient = createClient();
        Product product = createProduct(10);

        mockMvc.perform(post("/api/cart/items")
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new AddCartItemRequest(product.getId(), 2))))
                .andExpect(status().isOk());

        ShoppingCart ownerCart = cartRepository.findByClientId(owner.getId()).orElseThrow();
        CartItem ownerItem = cartItemRepository.findByCartId(ownerCart.getId()).getFirst();

        mockMvc.perform(put("/api/cart/items/{itemId}", ownerItem.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(otherClient))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new UpdateCartItemQuantityRequest(4))))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("CART_ITEM_NOT_FOUND"));

        assertEquals(2, cartItemRepository.findById(ownerItem.getId()).orElseThrow().getQty());
        assertTrue(cartRepository.findByClientId(otherClient.getId()).isEmpty());
    }

    @Test
    void invalidOrUnavailableQuantityDoesNotChangeCart() throws Exception {
        Client client = createClient();
        Product product = createProduct(3);

        mockMvc.perform(post("/api/cart/items")
                        .header(HttpHeaders.AUTHORIZATION, bearer(client))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new AddCartItemRequest(product.getId(), 0))))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors.qty[0]").value("QUANTITY_MUST_BE_POSITIVE"));

        mockMvc.perform(post("/api/cart/items")
                        .header(HttpHeaders.AUTHORIZATION, bearer(client))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new AddCartItemRequest(product.getId(), 4))))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_STOCK"));

        assertTrue(cartRepository.findByClientId(client.getId()).isEmpty());
    }

    private Client createClient() {
        Client client = new Client();
        client.setEmail("cart-test-" + UUID.randomUUID() + "@example.com");
        client.setPassword("test-password");
        client = clientRepository.saveAndFlush(client);
        clientIds.add(client.getId());
        return client;
    }

    private Product createProduct(int stock) {
        Product product = new Product();
        product.setName("Test product");
        product.setPrice(new BigDecimal("40.00"));
        product.setQtyInStock(stock);
        product.setSku("TEST-" + UUID.randomUUID());
        product = productRepository.saveAndFlush(product);
        productIds.add(product.getId());
        return product;
    }

    private String bearer(Client client) {
        return "Bearer " + jwtUtil.generate(client.getId());
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
