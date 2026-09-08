package com.tsw.controller;

import tools.jackson.databind.ObjectMapper;
import com.tsw.config.JwtUtil;
import com.tsw.dto.OrderItemRequest;
import com.tsw.dto.OrderRequest;
import com.tsw.model.Client;
import com.tsw.model.Product;
import com.tsw.model.ShippingMethod;
import com.tsw.repository.ClientRepository;
import com.tsw.repository.ProductRepository;
import com.tsw.repository.ShippingMethodRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class OrderControllerTest {

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
    private ShippingMethodRepository shippingMethodRepository;

    @Test
    void orderDecrementsStockAndRejectsWhenStockIsInsufficient() throws Exception {
        Client client = createClient();
        Product product = createProduct(5);
        ShippingMethod shippingMethod = createShippingMethod();

        mockMvc.perform(post("/api/me/orders")
                        .header(HttpHeaders.AUTHORIZATION, bearer(client))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(order(shippingMethod, new OrderItemRequest(product.getId(), 3)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderStatus.id").exists());

        assertEquals(2, stockOf(product));

        mockMvc.perform(post("/api/me/orders")
                        .header(HttpHeaders.AUTHORIZATION, bearer(client))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(order(shippingMethod, new OrderItemRequest(product.getId(), 3)))))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_STOCK"));

        assertEquals(2, stockOf(product));
    }

    @Test
    void orderWithRepeatedProductIsRejectedWithoutTouchingStock() throws Exception {
        Client client = createClient();
        Product product = createProduct(5);
        ShippingMethod shippingMethod = createShippingMethod();

        mockMvc.perform(post("/api/me/orders")
                        .header(HttpHeaders.AUTHORIZATION, bearer(client))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(order(
                                shippingMethod,
                                new OrderItemRequest(product.getId(), 2),
                                new OrderItemRequest(product.getId(), 2)
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("DUPLICATE_ORDER_ITEM"));

        assertEquals(5, stockOf(product));
    }

    private OrderRequest order(ShippingMethod shippingMethod, OrderItemRequest... items) {
        return new OrderRequest(List.of(items), shippingMethod.getId(), null);
    }

    private int stockOf(Product product) {
        return productRepository.findById(product.getId()).orElseThrow().getQtyInStock();
    }

    private Client createClient() {
        Client client = new Client();
        client.setEmail("order-test-" + UUID.randomUUID() + "@example.com");
        client.setPassword("test-password");
        client.setRole("CLIENT");
        return clientRepository.saveAndFlush(client);
    }

    private Product createProduct(int stock) {
        Product product = new Product();
        product.setName("Produkt testowy");
        product.setPrice(new BigDecimal("40.00"));
        product.setQtyInStock(stock);
        product.setSku("TEST-" + UUID.randomUUID());
        return productRepository.saveAndFlush(product);
    }

    private ShippingMethod createShippingMethod() {
        ShippingMethod shippingMethod = new ShippingMethod();
        shippingMethod.setName("Kurier testowy");
        shippingMethod.setPrice(BigDecimal.ZERO);
        shippingMethod.setActive(true);
        return shippingMethodRepository.saveAndFlush(shippingMethod);
    }

    private String bearer(Client client) {
        return "Bearer " + jwtUtil.generate(client.getId());
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
