package com.tsw.service;

import com.tsw.dto.OrderItemDto;
import com.tsw.dto.OrderRequest;
import com.tsw.exception.ApiErrorCode;
import com.tsw.exception.InsufficientStockException;
import com.tsw.exception.InvalidOrderStatusTransitionException;
import com.tsw.exception.InvalidQuantityException;
import com.tsw.exception.OrderConfigurationException;
import com.tsw.exception.ResourceNotFoundException;
import com.tsw.model.Address;
import com.tsw.model.Client;
import com.tsw.model.ClientAddress;
import com.tsw.model.ClientAddressId;
import com.tsw.model.OrderProduct;
import com.tsw.model.OrderProductId;
import com.tsw.model.OrderStatus;
import com.tsw.model.Product;
import com.tsw.model.ShippingMethod;
import com.tsw.model.ShopOrder;
import com.tsw.repository.ClientAddressRepository;
import com.tsw.repository.ClientRepository;
import com.tsw.repository.OrderProductRepository;
import com.tsw.repository.OrderStatusRepository;
import com.tsw.repository.ProductRepository;
import com.tsw.repository.ShippingMethodRepository;
import com.tsw.repository.ShopOrderRepository;
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

    public ShopOrder getById(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(this::orderNotFound);
    }

    @Transactional(readOnly = true)
    public ShopOrder getByIdForClient(UUID orderId, UUID clientId) {
        return orderRepository.findByIdAndClientId(orderId, clientId)
                .orElseThrow(this::orderNotFound);
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
        getByIdForClient(orderId, clientId);
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
                .orElseThrow(() -> new ResourceNotFoundException(
                        ApiErrorCode.CLIENT_NOT_FOUND,
                        "Nie znaleziono klienta"
                ));

        Address address = req.addressId() != null
                ? clientAddressRepository.findById(new ClientAddressId(clientId, req.addressId()))
                        .map(ClientAddress::getAddress)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                ApiErrorCode.ADDRESS_NOT_FOUND,
                                "Nie znaleziono adresu"
                        ))
                : null;

        ShippingMethod shippingMethod = shippingMethodRepository.findById(req.shippingMethodId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        ApiErrorCode.SHIPPING_METHOD_NOT_FOUND,
                        "Nie znaleziono metody dostawy"
                ));

        BigDecimal total = shippingMethod.getPrice();
        for (var item : req.items()) {
            if (item.qty() == null || item.qty() <= 0) {
                throw new InvalidQuantityException("Ilość musi być większa od zera");
            }
            Product product = productRepository.findById(item.productId())
                    .orElseThrow(() -> productNotFound());
            if (product.getQtyInStock() < item.qty()) {
                throw new InsufficientStockException("Niewystarczająca ilość produktu: " + product.getName());
            }
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(item.qty())));
        }

        ShopOrder order = new ShopOrder();
        order.setClient(client);
        order.setShippingAddress(address);
        order.setShippingMethod(shippingMethod);
        order.setOrderTotal(total);
        OrderStatus initialStatus = orderStatusRepository.findById(PLACED_STATUS_ID)
                .orElseThrow(OrderConfigurationException::new);
        order.setOrderStatus(initialStatus);
        order = orderRepository.save(order);

        for (var itemRequest : req.items()) {
            Product product = productRepository.findById(itemRequest.productId())
                    .orElseThrow(() -> productNotFound());

            OrderProductId opId = new OrderProductId();
            opId.setOrderId(order.getId());
            opId.setProductId(product.getId());

            OrderProduct op = new OrderProduct();
            op.setId(opId);
            op.setOrder(order);
            op.setProduct(product);
            op.setQty(itemRequest.qty());
            op.setPrice(product.getPrice());
            orderProductRepository.save(op);

            product.setQtyInStock(product.getQtyInStock() - itemRequest.qty());
            productRepository.save(product);
        }

        return order;
    }

    @Transactional
    public ShopOrder updateStatus(UUID orderId, UUID statusId) {
        OrderStatus status = orderStatusRepository.findById(statusId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ApiErrorCode.ORDER_STATUS_NOT_FOUND,
                        "Nie znaleziono statusu zamówienia"
                ));
        ShopOrder order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(this::orderNotFound);
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
    }

    private ResourceNotFoundException orderNotFound() {
        return new ResourceNotFoundException(ApiErrorCode.ORDER_NOT_FOUND, "Nie znaleziono zamówienia");
    }

    private ResourceNotFoundException productNotFound() {
        return new ResourceNotFoundException(ApiErrorCode.PRODUCT_NOT_FOUND, "Nie znaleziono produktu");
    }
}
