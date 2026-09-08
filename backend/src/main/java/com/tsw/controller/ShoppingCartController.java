package com.tsw.controller;

import com.tsw.config.AuthenticatedClient;
import com.tsw.dto.AddCartItemRequest;
import com.tsw.dto.CartItemResponse;
import com.tsw.dto.UpdateCartItemQuantityRequest;
import com.tsw.service.ShoppingCartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
public class ShoppingCartController {

    private final ShoppingCartService cartService;

    public ShoppingCartController(ShoppingCartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public List<CartItemResponse> getItems(@AuthenticationPrincipal AuthenticatedClient client) {
        return cartService.getItems(client.id());
    }

    @PostMapping("/items")
    public ResponseEntity<CartItemResponse> addItem(@AuthenticationPrincipal AuthenticatedClient client,
                                                    @Valid @RequestBody AddCartItemRequest request) {
        CartItemResponse item = cartService.addItem(client.id(), request.productId(), request.qty());
        return ResponseEntity.ok(item);
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeItem(@AuthenticationPrincipal AuthenticatedClient client,
                                           @PathVariable UUID itemId) {
        cartService.removeItem(client.id(), itemId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartItemResponse> updateQty(@AuthenticationPrincipal AuthenticatedClient client,
                                                      @PathVariable UUID itemId,
                                                      @Valid @RequestBody UpdateCartItemQuantityRequest request) {
        CartItemResponse item = cartService.updateItemQty(client.id(), itemId, request.qty());
        return ResponseEntity.ok(item);
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal AuthenticatedClient client) {
        cartService.clearCart(client.id());
        return ResponseEntity.noContent().build();
    }
}
