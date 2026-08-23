package com.tsw.controller;

import com.tsw.dto.OrderItemDto;
import com.tsw.dto.UpdateOrderStatusRequest;
import com.tsw.model.ShopOrder;
import com.tsw.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public List<ShopOrder> getAll() {
        return orderService.findAll();
    }

    @GetMapping("/{id}")
    public ShopOrder getById(@PathVariable UUID id) {
        return orderService.getById(id);
    }

    @GetMapping("/{id}/items")
    public List<OrderItemDto> getOrderItems(@PathVariable UUID id) {
        return orderService.getOrderItems(id);
    }

    @PutMapping("/{id}/status")
    public ShopOrder updateStatus(@PathVariable UUID id,
                                  @Valid @RequestBody UpdateOrderStatusRequest request) {
        return orderService.updateStatus(id, request.statusId());
    }
}
