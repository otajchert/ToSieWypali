package com.tsw.service;

import com.tsw.dto.OrderItemDto;
import com.tsw.dto.OrderRequest;
import com.tsw.exception.InsufficientStockException;
import com.tsw.exception.InvalidOrderStatusTransitionException;
import com.tsw.exception.InvalidQuantityException;
import com.tsw.exception.ResourceNotFoundException;
import com.tsw.model.*;
import com.tsw.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class OrderService {

    private static final UUID PLACED_STATUS_ID = UUID.fromString("00000000-0000-0000-0000-000000000021");
    private static final UUID PROCESSING_STATUS_ID = UUID.fromString("00000000-0000-0000-0000-000000000022");
    private static final UUID SHIPPED_STATUS_ID = UUID.fromString("00000000-0000-0000-0000-000000000023");
    private static final UUID DELIVERED_STATUS_ID = UUID.fromString("00000000-0000-0000-0000-000000000024");
    private static final UUID CANCELLED_STATUS_ID = UUID.fromString("00000000-0000-0000-0000-000000000025");

    private static final Map<UUID, Set<UUID>> ALLOWED_STATUS_TRANSITIONS = Map.of(
            PLACED_STATUS_ID, Set.of(PROCESSING_STATUS_ID, CANCELLED_STATUS_ID),
            PROCESSING_STATUS_ID, Set.of(SHIPPED_STATUS_ID, CANCELLED_STATUS_ID),
            SHIPPED_STATUS_ID, Set.of(DELIVERED_STATUS_ID),
            DELIVERED_STATUS_ID, Set.of(),
            CANCELLED_STATUS_ID, Set.of()
    );

    private final ShopOrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final ProductRepository productRepository;
    private final ClientRepository clientRepository;
    private final ClientAddressRepository clientAddressRepository;
    private final ShippingMethodRepository shippingMethodRepository;
    private final OrderStatusRepository orderStatusRepository;

    public OrderService(ShopOrderRepository orderRepository,
                        OrderProductRepository orderProductRepository,
                        ProductRepository productRepository,
                        ClientRepository clientRepository,
                        ClientAddressRepository clientAddressRepository,
                        ShippingMethodRepository shippingMethodRepository,
                        OrderStatusRepository orderStatusRepository) {
        this.orderRepository = orderRepository;
        this.orderProductRepository = orderProductRepository;
        this.productRepository = productRepository;
        this.clientRepository = clientRepository;
        this.clientAddressRepository = clientAddressRepository;
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
    public Optional<ShopOrder> findByIdForClient(UUID orderId, UUID clientId) {
        return orderRepository.findByIdAndClientId(orderId, clientId);
    }

    @Transactional(readOnly = true)
    public List<OrderItemDto> getOrderItems(UUID orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw orderNotFound();
        }

        return findOrderItems(orderId);
    }

    @Transactional(readOnly = true)
    public List<OrderItemDto> getOrderItemsForClient(UUID orderId, UUID clientId) {
        findByIdForClient(orderId, clientId)
                .orElseThrow(this::orderNotFound);
        return findOrderItems(orderId);
    }

    private List<OrderItemDto> findOrderItems(UUID orderId) {
        return orderProductRepository.findByIdOrderId(orderId).stream()
                .map(op -> new OrderItemDto(
                        op.getProduct().getId(),
                        op.getProduct().getName(),
                        op.getProduct().getPhoto(),
                        op.getQty(),
                        op.getPrice()))
                .toList();
    }

    @Transactional
    public ShopOrder create(UUID clientId, OrderRequest req) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono klienta"));

        Address address = req.getAddressId() != null
                ? clientAddressRepository.findById(new ClientAddressId(clientId, req.getAddressId()))
                        .map(ClientAddress::getAddress)
                        .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono adresu"))
                : null;

        ShippingMethod shippingMethod = shippingMethodRepository.findById(req.getShippingMethodId())
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono metody dostawy"));

        BigDecimal total = shippingMethod.getPrice();
        for (var item : req.getItems()) {
            if (item.getQty() <= 0) {
                throw new InvalidQuantityException("Ilość musi być większa od zera");
            }
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono produktu"));
            if (product.getQtyInStock() < item.getQty()) {
                throw new InsufficientStockException("Niewystarczająca ilość produktu: " + product.getName());
            }
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(item.getQty())));
        }

        ShopOrder order = new ShopOrder();
        order.setClient(client);
        order.setShippingAddress(address);
        order.setShippingMethod(shippingMethod);
        order.setOrderTotal(total);
        OrderStatus initialStatus = orderStatusRepository.findById(PLACED_STATUS_ID)
                .orElseThrow(() -> new IllegalStateException("Brak początkowego statusu zamówienia"));
        order.setOrderStatus(initialStatus);
        order = orderRepository.save(order);

        for (var itemReq : req.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono produktu"));

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

    @Transactional
    public Optional<ShopOrder> updateStatus(UUID orderId, UUID statusId) {
        OrderStatus status = orderStatusRepository.findById(statusId)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono statusu zamówienia"));

        return orderRepository.findByIdForUpdate(orderId).map(order -> {
            UUID currentStatusId = Optional.ofNullable(order.getOrderStatus())
                    .map(OrderStatus::getId)
                    .orElse(null);
            if (statusId.equals(currentStatusId)) {
                return order;
            }
            Set<UUID> allowedStatuses = currentStatusId == null
                    ? Set.of(PLACED_STATUS_ID)
                    : ALLOWED_STATUS_TRANSITIONS.getOrDefault(currentStatusId, Set.of());
            if (!allowedStatuses.contains(statusId)) {
                throw new InvalidOrderStatusTransitionException("Ta zmiana statusu zamówienia nie jest dozwolona");
            }

            order.setOrderStatus(status);
            return orderRepository.save(order);
        });
    }

    private ResourceNotFoundException orderNotFound() {
        return new ResourceNotFoundException("Nie znaleziono zamówienia");
    }
}
