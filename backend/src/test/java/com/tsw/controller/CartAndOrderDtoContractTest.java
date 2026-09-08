package com.tsw.controller;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.tsw.config.JwtUtil;
import com.tsw.dto.AddCartItemRequest;
import com.tsw.dto.AddressRequest;
import com.tsw.dto.OrderItemRequest;
import com.tsw.dto.OrderRequest;
import com.tsw.dto.UpdateCartItemQuantityRequest;
import com.tsw.dto.UpdateOrderStatusRequest;
import com.tsw.model.Client;
import com.tsw.model.OrderStatus;
import com.tsw.model.Product;
import com.tsw.model.ShippingMethod;
import com.tsw.repository.ClientRepository;
import com.tsw.repository.OrderStatusRepository;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CartAndOrderDtoContractTest {

    private static final UUID PLACED_STATUS_ID = UUID.fromString("00000000-0000-0000-0000-000000000021");
    private static final UUID PROCESSING_STATUS_ID = UUID.fromString("00000000-0000-0000-0000-000000000022");

    private static final Set<String> CART_ITEM_FIELDS = Set.of("id", "product", "qty");
    private static final Set<String> PRODUCT_FIELDS = Set.of(
            "id", "name", "description", "photo", "qtyInStock", "sku", "price",
            "weight", "height", "width", "productLength", "categories", "images"
    );
    private static final Set<String> ORDER_FIELDS = Set.of(
            "id", "client", "shippingAddress", "shippingMethod", "orderStatus", "orderDate", "orderTotal"
    );
    private static final Set<String> CLIENT_FIELDS = Set.of(
            "id", "email", "firstName", "lastName", "phoneNumber", "role"
    );
    private static final Set<String> ADDRESS_FIELDS = Set.of(
            "id", "city", "region", "postalCode", "streetNumber", "flat"
    );
    private static final Set<String> SHIPPING_METHOD_FIELDS = Set.of("id", "name", "price");
    private static final Set<String> ORDER_STATUS_FIELDS = Set.of("id", "name");
    private static final Set<String> ORDER_ITEM_FIELDS = Set.of(
            "productId", "productName", "productPhoto", "qty", "price"
    );

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ShippingMethodRepository shippingMethodRepository;

    @Autowired
    private OrderStatusRepository orderStatusRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void cartEndpointsKeepCartItemContract() throws Exception {
        Client client = createClient("CLIENT");
        Product product = createProduct(10);

        JsonNode added = responseBody(mockMvc.perform(post("/api/cart/items")
                .header(HttpHeaders.AUTHORIZATION, bearer(client))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(new AddCartItemRequest(product.getId(), 2)))));
        UUID itemId = UUID.fromString(added.path("id").asText());
        assertCartItemResponse(added, itemId, product, 2);

        JsonNode items = responseBody(mockMvc.perform(get("/api/cart")
                .header(HttpHeaders.AUTHORIZATION, bearer(client))));
        assertTrue(items.isArray());
        assertEquals(1, items.size());
        assertEquals(added, items.get(0));

        JsonNode updated = responseBody(mockMvc.perform(put("/api/cart/items/{itemId}", itemId)
                .header(HttpHeaders.AUTHORIZATION, bearer(client))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(new UpdateCartItemQuantityRequest(5)))));
        assertCartItemResponse(updated, itemId, product, 5);
    }

    @Test
    void orderEndpointsKeepOrderContract() throws Exception {
        Client client = createClient("CLIENT");
        Client admin = createClient("ADMIN");
        Product product = createProduct(10);
        ShippingMethod shippingMethod = createShippingMethod(new BigDecimal("12.50"));
        OrderStatus placed = orderStatusRepository.findById(PLACED_STATUS_ID).orElseThrow();
        OrderStatus processing = orderStatusRepository.findById(PROCESSING_STATUS_ID).orElseThrow();

        AddressRequest addressRequest = new AddressRequest(
                "Dom",
                "Warszawa",
                "mazowieckie",
                "00-001",
                "Dluga 1",
                "2",
                true
        );
        JsonNode address = responseBody(mockMvc.perform(post("/api/me/addresses")
                .header(HttpHeaders.AUTHORIZATION, bearer(client))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(addressRequest))));
        UUID addressId = UUID.fromString(address.path("address").path("id").asText());

        OrderRequest orderRequest = new OrderRequest(
                List.of(new OrderItemRequest(product.getId(), 3)),
                shippingMethod.getId(),
                addressId
        );
        JsonNode created = responseBody(mockMvc.perform(post("/api/me/orders")
                .header(HttpHeaders.AUTHORIZATION, bearer(client))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(orderRequest))));
        UUID orderId = UUID.fromString(created.path("id").asText());
        BigDecimal expectedTotal = product.getPrice().multiply(BigDecimal.valueOf(3)).add(shippingMethod.getPrice());
        assertOrderResponse(created, orderId, client, shippingMethod, placed, expectedTotal);
        assertAddressResponse(created.path("shippingAddress"), addressId, addressRequest);

        JsonNode clientOrders = responseBody(mockMvc.perform(get("/api/me/orders")
                .header(HttpHeaders.AUTHORIZATION, bearer(client))));
        assertTrue(clientOrders.isArray());
        clientOrders.forEach(this::assertOrderShape);
        assertEquals(created, findById(clientOrders, orderId));

        JsonNode clientDetails = responseBody(mockMvc.perform(get("/api/me/orders/{orderId}", orderId)
                .header(HttpHeaders.AUTHORIZATION, bearer(client))));
        assertEquals(created, clientDetails);

        JsonNode clientItems = responseBody(mockMvc.perform(get("/api/me/orders/{orderId}/items", orderId)
                .header(HttpHeaders.AUTHORIZATION, bearer(client))));
        assertOrderItems(clientItems, product, 3);

        JsonNode adminOrders = responseBody(mockMvc.perform(get("/api/orders")
                .header(HttpHeaders.AUTHORIZATION, bearer(admin))));
        assertTrue(adminOrders.isArray());
        adminOrders.forEach(this::assertOrderShape);
        assertEquals(created, findById(adminOrders, orderId));

        JsonNode adminDetails = responseBody(mockMvc.perform(get("/api/orders/{id}", orderId)
                .header(HttpHeaders.AUTHORIZATION, bearer(admin))));
        assertEquals(created, adminDetails);

        JsonNode adminItems = responseBody(mockMvc.perform(get("/api/orders/{id}/items", orderId)
                .header(HttpHeaders.AUTHORIZATION, bearer(admin))));
        assertOrderItems(adminItems, product, 3);

        JsonNode updated = responseBody(mockMvc.perform(put("/api/orders/{id}/status", orderId)
                .header(HttpHeaders.AUTHORIZATION, bearer(admin))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(new UpdateOrderStatusRequest(PROCESSING_STATUS_ID)))));
        assertOrderResponse(updated, orderId, client, shippingMethod, processing, expectedTotal);
        assertAddressResponse(updated.path("shippingAddress"), addressId, addressRequest);
    }

    @Test
    void orderWithoutAddressKeepsNullShippingAddress() throws Exception {
        Client client = createClient("CLIENT");
        Product product = createProduct(4);
        ShippingMethod shippingMethod = createShippingMethod(BigDecimal.ZERO);
        OrderStatus placed = orderStatusRepository.findById(PLACED_STATUS_ID).orElseThrow();

        OrderRequest orderRequest = new OrderRequest(
                List.of(new OrderItemRequest(product.getId(), 1)),
                shippingMethod.getId(),
                null
        );
        JsonNode created = responseBody(mockMvc.perform(post("/api/me/orders")
                .header(HttpHeaders.AUTHORIZATION, bearer(client))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(orderRequest))));
        UUID orderId = UUID.fromString(created.path("id").asText());
        assertOrderResponse(created, orderId, client, shippingMethod, placed, product.getPrice());
        assertTrue(created.path("shippingAddress").isNull());
    }

    private Client createClient(String role) {
        Client client = new Client();
        client.setEmail("cart-order-dto-" + UUID.randomUUID() + "@example.com");
        client.setPassword("test-password");
        client.setFirstName("Anna");
        client.setLastName("Nowak");
        client.setPhoneNumber("123456789");
        client.setRole(role);
        return clientRepository.saveAndFlush(client);
    }

    private Product createProduct(int stock) {
        Product product = new Product();
        product.setName("Produkt testowy");
        product.setDescription("Opis produktu");
        product.setPrice(new BigDecimal("40.00"));
        product.setQtyInStock(stock);
        product.setSku("TEST-" + UUID.randomUUID());
        return productRepository.saveAndFlush(product);
    }

    private ShippingMethod createShippingMethod(BigDecimal price) {
        ShippingMethod shippingMethod = new ShippingMethod();
        shippingMethod.setName("Kurier testowy");
        shippingMethod.setPrice(price);
        shippingMethod.setActive(true);
        return shippingMethodRepository.saveAndFlush(shippingMethod);
    }

    private String bearer(Client client) {
        return "Bearer " + jwtUtil.generate(client.getId());
    }

    private JsonNode responseBody(ResultActions resultActions) throws Exception {
        String body = resultActions
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(body);
    }

    private void assertCartItemResponse(JsonNode response, UUID itemId, Product product, int qty) {
        assertEquals(CART_ITEM_FIELDS, fieldNames(response));
        assertEquals(itemId.toString(), response.path("id").asText());
        assertEquals(qty, response.path("qty").asInt());
        assertFalse(response.has("cart"));

        JsonNode productNode = response.path("product");
        assertEquals(PRODUCT_FIELDS, fieldNames(productNode));
        assertEquals(product.getId().toString(), productNode.path("id").asText());
        assertEquals(product.getName(), productNode.path("name").asText());
        assertEquals(product.getSku(), productNode.path("sku").asText());
        assertEquals(product.getQtyInStock(), productNode.path("qtyInStock").asInt());
        assertDecimalEquals(product.getPrice(), productNode.path("price"));
        assertTrue(productNode.path("categories").isArray());
        assertTrue(productNode.path("images").isArray());
    }

    private void assertOrderResponse(JsonNode response,
                                     UUID orderId,
                                     Client client,
                                     ShippingMethod shippingMethod,
                                     OrderStatus status,
                                     BigDecimal total) {
        assertOrderShape(response);
        assertEquals(orderId.toString(), response.path("id").asText());
        assertFalse(response.path("orderDate").asText().isBlank());
        assertDecimalEquals(total, response.path("orderTotal"));

        JsonNode clientNode = response.path("client");
        assertEquals(client.getId().toString(), clientNode.path("id").asText());
        assertEquals(client.getEmail(), clientNode.path("email").asText());
        assertEquals(client.getFirstName(), clientNode.path("firstName").asText());
        assertEquals(client.getLastName(), clientNode.path("lastName").asText());
        assertEquals(client.getPhoneNumber(), clientNode.path("phoneNumber").asText());
        assertEquals(client.getRole(), clientNode.path("role").asText());

        JsonNode shippingMethodNode = response.path("shippingMethod");
        assertEquals(shippingMethod.getId().toString(), shippingMethodNode.path("id").asText());
        assertEquals(shippingMethod.getName(), shippingMethodNode.path("name").asText());
        assertDecimalEquals(shippingMethod.getPrice(), shippingMethodNode.path("price"));

        JsonNode statusNode = response.path("orderStatus");
        assertEquals(status.getId().toString(), statusNode.path("id").asText());
        assertEquals(status.getName(), statusNode.path("name").asText());
    }

    private void assertOrderShape(JsonNode response) {
        assertEquals(ORDER_FIELDS, fieldNames(response));
        assertEquals(CLIENT_FIELDS, fieldNames(response.path("client")));
        assertFalse(response.path("client").has("password"));
        if (!response.path("shippingAddress").isNull()) {
            assertEquals(ADDRESS_FIELDS, fieldNames(response.path("shippingAddress")));
        }
        assertEquals(SHIPPING_METHOD_FIELDS, fieldNames(response.path("shippingMethod")));
        assertFalse(response.path("shippingMethod").has("active"));
        assertEquals(ORDER_STATUS_FIELDS, fieldNames(response.path("orderStatus")));
    }

    private void assertAddressResponse(JsonNode response, UUID addressId, AddressRequest request) {
        assertEquals(ADDRESS_FIELDS, fieldNames(response));
        assertEquals(addressId.toString(), response.path("id").asText());
        assertEquals(request.city(), response.path("city").asText());
        assertEquals(request.region(), response.path("region").asText());
        assertEquals(request.postalCode(), response.path("postalCode").asText());
        assertEquals(request.streetNumber(), response.path("streetNumber").asText());
        assertEquals(request.flat(), response.path("flat").asText());
    }

    private void assertOrderItems(JsonNode items, Product product, int qty) {
        assertTrue(items.isArray());
        assertEquals(1, items.size());
        JsonNode item = items.get(0);
        assertEquals(ORDER_ITEM_FIELDS, fieldNames(item));
        assertEquals(product.getId().toString(), item.path("productId").asText());
        assertEquals(product.getName(), item.path("productName").asText());
        assertEquals(qty, item.path("qty").asInt());
        assertDecimalEquals(product.getPrice(), item.path("price"));
    }

    private void assertDecimalEquals(BigDecimal expected, JsonNode actual) {
        assertTrue(actual.isNumber());
        assertEquals(0, expected.compareTo(actual.decimalValue()));
    }

    private JsonNode findById(JsonNode collection, UUID id) {
        for (JsonNode element : collection) {
            if (id.toString().equals(element.path("id").asText())) {
                return element;
            }
        }
        return fail("Nie znaleziono elementu w odpowiedzi");
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private Set<String> fieldNames(JsonNode node) {
        Set<String> names = new HashSet<>();
        names.addAll(node.propertyNames());
        return names;
    }
}
