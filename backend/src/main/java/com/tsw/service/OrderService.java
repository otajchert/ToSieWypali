package com.tsw.service;

import com.tsw.dto.OrderItemDto;
import com.tsw.dto.OrderRequest;
import com.tsw.model.*;
import com.tsw.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final ShopOrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final ProductRepository productRepository;
    private final ClientRepository clientRepository;
    private final AddressRepository addressRepository;
    private final ShippingMethodRepository shippingMethodRepository;
    private final OrderStatusRepository orderStatusRepository;

    public OrderService(ShopOrderRepository orderRepository,
                        OrderProductRepository orderProductRepository,
                        ProductRepository productRepository,
                        ClientRepository clientRepository,
                        AddressRepository addressRepository,
                        ShippingMethodRepository shippingMethodRepository,
                        OrderStatusRepository orderStatusRepository) {
        this.orderRepository = orderRepository;
        this.orderProductRepository = orderProductRepository;
        this.productRepository = productRepository;
        this.clientRepository = clientRepository;
        this.addressRepository = addressRepository;
        this.shippingMethodRepository = shippingMethodRepository;
        this.orderStatusRepository = orderStatusRepository;
    }

    public List<ShopOrder> findAll() {
        return orderRepository.findAll();
    }

    public List<ShopOrder> findByClient(UUID clientId) {
        return orderRepository.findByClientId(clientId);
    }

    public Optional<ShopOrder> findById(UUID id) {
        return orderRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<OrderItemDto> getOrderItems(UUID orderId) {
        return orderProductRepository.findByIdOrderId(orderId).stream()
                .map(op -> new OrderItemDto(
                        op.getProduct().getId(),
                        op.getProduct().getName(),
                        op.getProduct().getPhoto(),
                        op.getQty(),
                        op.getPrice()))
                .collect(Collectors.toList());
    }

    public ShopOrder create(UUID clientId, OrderRequest req) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));

        Address address = req.getAddressId() != null
                ? addressRepository.findById(req.getAddressId()).orElseThrow(() -> new IllegalArgumentException("Address not found"))
                : null;

        ShippingMethod shippingMethod = shippingMethodRepository.findById(req.getShippingMethodId())
                .orElseThrow(() -> new IllegalArgumentException("Shipping method not found"));

        // validate stock and calculate total before creating the order
        BigDecimal total = shippingMethod.getPrice();
        for (var item : req.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + item.getProductId()));
            if (product.getQtyInStock() < item.getQty()) {
                throw new IllegalStateException("Not enough stock for: " + product.getName());
            }
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(item.getQty())));
        }

        ShopOrder order = new ShopOrder();
        order.setClient(client);
        order.setShippingAddress(address);
        order.setShippingMethod(shippingMethod);
        order.setOrderTotal(total);
        orderStatusRepository.findById(UUID.fromString("00000000-0000-0000-0000-000000000021"))
                .ifPresent(order::setOrderStatus);
        order = orderRepository.save(order);

        // create order lines and deduct stock
        for (var itemReq : req.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId()).get();

            OrderProductId opId = new OrderProductId();
            opId.setOrderId(order.getId());
            opId.setProductId(product.getId());

            OrderProduct op = new OrderProduct();
            op.setId(opId);
            op.setOrder(order);
            op.setProduct(product);
            op.setQty(itemReq.getQty());
            op.setPrice(product.getPrice());
            orderProductRepository.save(op);

            product.setQtyInStock(product.getQtyInStock() - itemReq.getQty());
            productRepository.save(product);
        }

        return order;
    }

    public Optional<ShopOrder> updateStatus(UUID orderId, UUID statusId) {
        OrderStatus status = orderStatusRepository.findById(statusId)
                .orElseThrow(() -> new IllegalArgumentException("Status not found"));

        return orderRepository.findById(orderId).map(order -> {
            order.setOrderStatus(status);
            return orderRepository.save(order);
        });
    }
}
