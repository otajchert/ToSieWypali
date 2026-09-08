package com.tsw.controller;

import com.tsw.config.AuthenticatedClient;
import com.tsw.dto.OrderItemDto;
import com.tsw.dto.OrderRequest;
import com.tsw.dto.OrderResponse;
import com.tsw.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/me/orders")
public class CurrentClientOrderController {

    private final OrderService orderService;

    public CurrentClientOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public List<OrderResponse> getAll(@AuthenticationPrincipal AuthenticatedClient client) {
        return orderService.findByClient(client.id());
    }

    @GetMapping("/{orderId}")
    public OrderResponse getById(@AuthenticationPrincipal AuthenticatedClient client,
                                 @PathVariable UUID orderId) {
        return orderService.getByIdForClient(orderId, client.id());
    }

    @GetMapping("/{orderId}/items")
    public List<OrderItemDto> getItems(@AuthenticationPrincipal AuthenticatedClient client,
                                       @PathVariable UUID orderId) {
        return orderService.getOrderItemsForClient(orderId, client.id());
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@AuthenticationPrincipal AuthenticatedClient client,
                                                @Valid @RequestBody OrderRequest request) {
        return ResponseEntity.ok(orderService.create(client.id(), request));
    }
}
