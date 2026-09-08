package com.tsw.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tsw.config.JwtUtil;
import com.tsw.model.Client;
import com.tsw.repository.ClientRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
class CatalogDtoContractTest {

    private static final Set<String> CATEGORY_FIELDS = Set.of("id", "categoryName");
    private static final Set<String> PRODUCT_FIELDS = Set.of(
            "id", "name", "description", "photo", "qtyInStock", "sku", "price",
            "weight", "height", "width", "productLength", "categories", "images"
    );
    private static final Set<String> PRODUCT_IMAGE_FIELDS = Set.of("id", "imageUrl", "sortOrder");
    private static final Set<String> SHIPPING_METHOD_FIELDS = Set.of("id", "name", "price");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EntityManager entityManager;

    @Test
    void categoryEndpointsKeepCategoryContract() throws Exception {
        String authorization = bearer(createAdmin());

        JsonNode parent = responseBody(mockMvc.perform(post("/api/categories")
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("categoryName", "Ceramika")))));
        UUID parentId = UUID.fromString(parent.path("id").asText());
        assertCategoryResponse(parent, parentId, "Ceramika");

        JsonNode created = responseBody(mockMvc.perform(post("/api/categories")
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                        "categoryName", "Kubki",
                        "parentCategoryId", parentId.toString()
                )))));
        UUID categoryId = UUID.fromString(created.path("id").asText());
        assertCategoryResponse(created, categoryId, "Kubki");

        JsonNode categories = responseBody(mockMvc.perform(get("/api/categories")));
        assertTrue(categories.isArray());
        categories.forEach(this::assertCategoryShape);
        assertEquals(created, findById(categories, categoryId));

        JsonNode details = responseBody(mockMvc.perform(get("/api/categories/{id}", categoryId)));
        assertEquals(created, details);

        JsonNode updated = responseBody(mockMvc.perform(put("/api/categories/{id}", categoryId)
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                        "categoryName", "Kubki ręcznie robione",
                        "parentCategoryId", parentId.toString()
                )))));
        assertCategoryResponse(updated, categoryId, "Kubki ręcznie robione");
    }

    @Test
    void productEndpointsKeepProductAndImageContracts() throws Exception {
        String authorization = bearer(createAdmin());

        JsonNode category = responseBody(mockMvc.perform(post("/api/categories")
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("categoryName", "Misy")))));
        UUID categoryId = UUID.fromString(category.path("id").asText());

        Map<String, Object> createRequest = productRequest(
                "Misa testowa",
                "Opis misy",
                new BigDecimal("49.90"),
                7,
                "1 kg",
                "10 cm",
                "20 cm",
                "20 cm",
                categoryId
        );
        JsonNode created = responseBody(mockMvc.perform(post("/api/products")
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(createRequest))));
        UUID productId = UUID.fromString(created.path("id").asText());
        String sku = created.path("sku").asText();
        assertProductShape(created);
        assertProductValues(
                created,
                productId,
                "Misa testowa",
                "Opis misy",
                new BigDecimal("49.90"),
                7,
                "1 kg",
                "10 cm",
                "20 cm",
                "20 cm",
                sku
        );
        assertTrue(created.path("photo").isNull());
        assertEquals(1, created.path("categories").size());
        assertCategoryResponse(created.path("categories").get(0), categoryId, "Misy");
        assertTrue(created.path("images").isEmpty());

        JsonNode products = responseBody(mockMvc.perform(get("/api/products")));
        assertTrue(products.isArray());
        products.forEach(this::assertProductShape);
        assertEquals(created, findById(products, productId));

        JsonNode details = responseBody(mockMvc.perform(get("/api/products/{id}", productId)));
        assertEquals(created, details);

        Map<String, Object> updateRequest = productRequest(
                "Misa zaktualizowana",
                null,
                new BigDecimal("59.50"),
                5,
                "1,2 kg",
                "11 cm",
                "21 cm",
                "21 cm",
                categoryId
        );
        JsonNode updated = responseBody(mockMvc.perform(put("/api/products/{id}", productId)
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(updateRequest))));
        assertProductShape(updated);
        assertProductValues(
                updated,
                productId,
                "Misa zaktualizowana",
                null,
                new BigDecimal("59.50"),
                5,
                "1,2 kg",
                "11 cm",
                "21 cm",
                "21 cm",
                sku
        );

        String galleryUrl = "https://example.com/gallery.jpg";
        JsonNode image = responseBody(mockMvc.perform(post("/api/products/{id}/image-url", productId)
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("url", galleryUrl)))));
        UUID imageId = UUID.fromString(image.path("id").asText());
        assertProductImageResponse(image, imageId, galleryUrl, 0);

        entityManager.flush();
        entityManager.clear();

        JsonNode productWithImage = responseBody(mockMvc.perform(get("/api/products/{id}", productId)));
        assertProductShape(productWithImage);
        assertEquals(1, productWithImage.path("images").size());
        assertProductImageResponse(productWithImage.path("images").get(0), imageId, galleryUrl, 0);

        String mainPhotoUrl = "https://example.com/main.jpg";
        JsonNode productWithMainPhoto = responseBody(mockMvc.perform(post("/api/products/{id}/photo-url", productId)
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("url", mainPhotoUrl)))));
        assertProductShape(productWithMainPhoto);
        assertEquals(mainPhotoUrl, productWithMainPhoto.path("photo").asText());

        JsonNode imageSetAsMain = responseBody(mockMvc.perform(put(
                        "/api/products/{id}/images/{imageId}/set-main",
                        productId,
                        imageId
                )
                .header(HttpHeaders.AUTHORIZATION, authorization)));
        assertProductShape(imageSetAsMain);
        assertEquals(galleryUrl, imageSetAsMain.path("photo").asText());
    }

    @Test
    void shippingMethodEndpointsKeepShippingMethodContract() throws Exception {
        String authorization = bearer(createAdmin());

        JsonNode created = responseBody(mockMvc.perform(post("/api/shipping-methods")
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                        "name", "  Kurier DTO  ",
                        "price", new BigDecimal("12.50")
                )))));
        UUID shippingMethodId = UUID.fromString(created.path("id").asText());
        assertShippingMethodResponse(created, shippingMethodId, "Kurier DTO", new BigDecimal("12.50"));

        JsonNode shippingMethods = responseBody(mockMvc.perform(get("/api/shipping-methods")));
        assertTrue(shippingMethods.isArray());
        shippingMethods.forEach(this::assertShippingMethodShape);
        assertEquals(created, findById(shippingMethods, shippingMethodId));
    }

    private Map<String, Object> productRequest(String name,
                                               String description,
                                               BigDecimal price,
                                               int qtyInStock,
                                               String weight,
                                               String height,
                                               String width,
                                               String productLength,
                                               UUID categoryId) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("name", name);
        request.put("description", description);
        request.put("price", price);
        request.put("qtyInStock", qtyInStock);
        request.put("weight", weight);
        request.put("height", height);
        request.put("width", width);
        request.put("productLength", productLength);
        request.put("categoryIds", List.of(categoryId.toString()));
        return request;
    }

    private Client createAdmin() {
        Client admin = new Client();
        admin.setEmail("catalog-dto-admin-" + UUID.randomUUID() + "@example.com");
        admin.setPassword("test-password");
        admin.setRole("ADMIN");
        return clientRepository.saveAndFlush(admin);
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

    private void assertCategoryResponse(JsonNode response, UUID id, String categoryName) {
        assertCategoryShape(response);
        assertEquals(id.toString(), response.path("id").asText());
        assertEquals(categoryName, response.path("categoryName").asText());
        assertFalse(response.has("parentCategory"));
    }

    private void assertCategoryShape(JsonNode response) {
        assertEquals(CATEGORY_FIELDS, fieldNames(response));
    }

    private void assertProductShape(JsonNode response) {
        assertEquals(PRODUCT_FIELDS, fieldNames(response));
        assertTrue(response.path("categories").isArray());
        response.path("categories").forEach(this::assertCategoryShape);
        assertTrue(response.path("images").isArray());
        response.path("images").forEach(this::assertProductImageShape);
    }

    private void assertProductValues(JsonNode response,
                                     UUID id,
                                     String name,
                                     String description,
                                     BigDecimal price,
                                     int qtyInStock,
                                     String weight,
                                     String height,
                                     String width,
                                     String productLength,
                                     String sku) {
        assertEquals(id.toString(), response.path("id").asText());
        assertEquals(name, response.path("name").asText());
        if (description == null) {
            assertTrue(response.path("description").isNull());
        } else {
            assertEquals(description, response.path("description").asText());
        }
        assertDecimalEquals(price, response.path("price"));
        assertEquals(qtyInStock, response.path("qtyInStock").asInt());
        assertEquals(weight, response.path("weight").asText());
        assertEquals(height, response.path("height").asText());
        assertEquals(width, response.path("width").asText());
        assertEquals(productLength, response.path("productLength").asText());
        assertEquals(sku, response.path("sku").asText());
        assertFalse(sku.isBlank());
    }

    private void assertProductImageResponse(JsonNode response,
                                            UUID id,
                                            String imageUrl,
                                            int sortOrder) {
        assertProductImageShape(response);
        assertEquals(id.toString(), response.path("id").asText());
        assertEquals(imageUrl, response.path("imageUrl").asText());
        assertEquals(sortOrder, response.path("sortOrder").asInt());
        assertFalse(response.has("product"));
    }

    private void assertProductImageShape(JsonNode response) {
        assertEquals(PRODUCT_IMAGE_FIELDS, fieldNames(response));
    }

    private void assertShippingMethodResponse(JsonNode response,
                                              UUID id,
                                              String name,
                                              BigDecimal price) {
        assertShippingMethodShape(response);
        assertEquals(id.toString(), response.path("id").asText());
        assertEquals(name, response.path("name").asText());
        assertDecimalEquals(price, response.path("price"));
        assertFalse(response.has("active"));
    }

    private void assertShippingMethodShape(JsonNode response) {
        assertEquals(SHIPPING_METHOD_FIELDS, fieldNames(response));
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
        node.fieldNames().forEachRemaining(names::add);
        return names;
    }
}
