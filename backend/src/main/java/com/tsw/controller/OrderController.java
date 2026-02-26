package com.tsw.controller;

import com.tsw.dto.OrderRequest;
import com.tsw.model.ShopOrder;
import com.tsw.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/client/{clientId}")
    public List<ShopOrder> getByClient(@PathVariable UUID clientId) {
        return orderService.findByClient(clientId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShopOrder> getById(@PathVariable UUID id) {
        return orderService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // body: OrderRequest JSON
    @PostMapping("/client/{clientId}")
    public ResponseEntity<?> create(@PathVariable UUID clientId, @RequestBody OrderRequest req) {
        try {
            return ResponseEntity.ok(orderService.create(clientId, req));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // body: { "statusId": "uuid" }
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        try {
            UUID statusId = UUID.fromString(body.get("statusId"));
            return orderService.updateStatus(id, statusId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
