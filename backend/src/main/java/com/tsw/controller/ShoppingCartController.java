package com.tsw.controller;

import com.tsw.model.CartItem;
import com.tsw.service.ShoppingCartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/cart/{clientId}")
public class ShoppingCartController {

    private final ShoppingCartService cartService;

    public ShoppingCartController(ShoppingCartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public List<CartItem> getItems(@PathVariable UUID clientId) {
        return cartService.getItems(clientId);
    }

    // body: { "productId": "uuid", "qty": 2 }
    @PostMapping("/items")
    public ResponseEntity<?> addItem(@PathVariable UUID clientId, @RequestBody Map<String, Object> body) {
        try {
            UUID productId = UUID.fromString((String) body.get("productId"));
            int qty = (int) body.get("qty");
            return ResponseEntity.ok(cartService.addItem(clientId, productId, qty));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeItem(@PathVariable UUID clientId, @PathVariable UUID itemId) {
        return cartService.removeItem(clientId, itemId)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    // body: { "qty": 3 }
    @PutMapping("/items/{itemId}")
    public ResponseEntity<?> updateQty(@PathVariable UUID clientId,
                                       @PathVariable UUID itemId,
                                       @RequestBody Map<String, Integer> body) {
        int qty = body.get("qty");
        return cartService.updateItemQty(clientId, itemId, qty)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(@PathVariable UUID clientId) {
        cartService.clearCart(clientId);
        return ResponseEntity.noContent().build();
    }
}
