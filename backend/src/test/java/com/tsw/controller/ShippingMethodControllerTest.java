package com.tsw.controller;

import com.tsw.config.JwtUtil;
import com.tsw.model.Client;
import com.tsw.model.ShippingMethod;
import com.tsw.model.ShopOrder;
import com.tsw.repository.ClientRepository;
import com.tsw.repository.ShippingMethodRepository;
import com.tsw.repository.ShopOrderRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ShippingMethodControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ShippingMethodRepository shippingMethodRepository;

    @Autowired
    private ShopOrderRepository orderRepository;

    private final List<UUID> clientIds = new ArrayList<>();
    private final List<UUID> shippingMethodIds = new ArrayList<>();
    private final List<UUID> orderIds = new ArrayList<>();

    @AfterEach
    void cleanUp() {
        orderRepository.deleteAllById(orderIds);
        shippingMethodRepository.deleteAllById(shippingMethodIds);
        clientRepository.deleteAllById(clientIds);
    }

    @Test
    void shippingMethodUsedByAnOrderCanBeRetiredWithoutDamagingHistory() throws Exception {
        Client admin = createClient("ADMIN");
        Client customer = createClient("CLIENT");
        ShippingMethod shippingMethod = createShippingMethod("Testowa metoda");

        ShopOrder order = new ShopOrder();
        order.setClient(customer);
        order.setShippingMethod(shippingMethod);
        order.setOrderTotal(new BigDecimal("10.00"));
        order = orderRepository.saveAndFlush(order);
        orderIds.add(order.getId());

        mockMvc.perform(delete("/api/shipping-methods/{id}", shippingMethod.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(admin)))
                .andExpect(status().isNoContent());

        assertFalse(shippingMethodRepository.findById(shippingMethod.getId()).orElseThrow().isActive());
        assertEquals(
                shippingMethod.getId(),
                orderRepository.findById(order.getId()).orElseThrow().getShippingMethod().getId()
        );
    }

    private Client createClient(String role) {
        Client client = new Client();
        client.setEmail("shipping-method-test-" + UUID.randomUUID() + "@example.com");
        client.setPassword("test-password");
        client.setRole(role);
        client = clientRepository.saveAndFlush(client);
        clientIds.add(client.getId());
        return client;
    }

    private ShippingMethod createShippingMethod(String name) {
        ShippingMethod shippingMethod = new ShippingMethod();
        shippingMethod.setName(name);
        shippingMethod.setPrice(BigDecimal.ZERO);
        shippingMethod.setActive(true);
        shippingMethod = shippingMethodRepository.saveAndFlush(shippingMethod);
        shippingMethodIds.add(shippingMethod.getId());
        return shippingMethod;
    }

    private String bearer(Client client) {
        return "Bearer " + jwtUtil.generate(client.getId());
    }
}
