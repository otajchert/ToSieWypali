package com.tsw.service;

import com.tsw.dto.AddressResponse;
import com.tsw.dto.ClientResponse;
import com.tsw.dto.OrderItemDto;
import com.tsw.dto.OrderItemRequest;
import com.tsw.dto.OrderRequest;
import com.tsw.dto.OrderResponse;
import com.tsw.dto.OrderStatusResponse;
import com.tsw.dto.ShippingMethodResponse;
import com.tsw.exception.ApiErrorCode;
import com.tsw.exception.DuplicateOrderItemException;
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
import com.tsw.repository.CartItemRepository;
import com.tsw.repository.ClientAddressRepository;
import com.tsw.repository.ClientRepository;
import com.tsw.repository.OrderProductRepository;
import com.tsw.repository.OrderStatusRepository;
import com.tsw.repository.ProductRepository;
import com.tsw.repository.ShippingMethodRepository;
import com.tsw.repository.ShopOrderRepository;
import com.tsw.repository.ShoppingCartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
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
    private final ShoppingCartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    public OrderService(ShopOrderRepository orderRepository,
                        OrderProductRepository orderProductRepository,
                        ProductRepository productRepository,
                        ClientRepository clientRepository,
                        ClientAddressRepository clientAddressRepository,
                        ShippingMethodRepository shippingMethodRepository,
                        OrderStatusRepository orderStatusRepository,
                        ShoppingCartRepository cartRepository,
                        CartItemRepository cartItemRepository) {
        this.orderRepository = orderRepository;
        this.orderProductRepository = orderProductRepository;
        this.productRepository = productRepository;
        this.clientRepository = clientRepository;
        this.clientAddressRepository = clientAddressRepository;
        this.shippingMethodRepository = shippingMethodRepository;
        this.orderStatusRepository = orderStatusRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findAll() {
        return orderRepository.findAllByOrderByOrderDateDescIdDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findByClient(UUID clientId) {
        return orderRepository.findByClientId(clientId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getById(UUID id) {
        return toResponse(getOrder(id));
    }

    @Transactional(readOnly = true)
    public OrderResponse getByIdForClient(UUID orderId, UUID clientId) {
        return toResponse(getOrderForClient(orderId, clientId));
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
        getOrderForClient(orderId, clientId);
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
    public OrderResponse create(UUID clientId, OrderRequest req, String idempotencyKey) {
        String key = idempotencyKey == null || idempotencyKey.isBlank() ? null : idempotencyKey;
        if (key != null) {
            Optional<ShopOrder> existing = orderRepository.findByClientIdAndIdempotencyKey(clientId, key);
            if (existing.isPresent()) {
                return toResponse(existing.get());
            }
        }

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

        ShippingMethod shippingMethod = shippingMethodRepository.findByIdAndActiveTrue(req.shippingMethodId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        ApiErrorCode.SHIPPING_METHOD_NOT_FOUND,
                        "Nie znaleziono metody dostawy"
                ));

        List<OrderItemRequest> items = sortedUniqueItems(req.items());

        Map<UUID, Product> products = new HashMap<>();
        BigDecimal total = shippingMethod.getPrice();
        for (OrderItemRequest item : items) {
            if (item.qty() == null || item.qty() <= 0) {
                throw new InvalidQuantityException("Ilość musi być większa od zera");
            }
            Product product = productRepository.findByIdForUpdate(item.productId())
                    .orElseThrow(this::productNotFound);
            if (product.getQtyInStock() < item.qty()) {
                throw new InsufficientStockException("Niewystarczająca ilość produktu: " + product.getName());
            }
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(item.qty())));
            products.put(product.getId(), product);
        }

        ShopOrder order = new ShopOrder();
        order.setClient(client);
        order.setShippingAddress(address);
        order.setShippingMethod(shippingMethod);
        order.setOrderTotal(total);
        order.setIdempotencyKey(key);
        OrderStatus initialStatus = orderStatusRepository.findById(PLACED_STATUS_ID)
                .orElseThrow(OrderConfigurationException::new);
        order.setOrderStatus(initialStatus);
        order = orderRepository.save(order);

        for (OrderItemRequest item : items) {
            Product product = products.get(item.productId());

            OrderProductId opId = new OrderProductId();
            opId.setOrderId(order.getId());
            opId.setProductId(product.getId());

            OrderProduct op = new OrderProduct();
            op.setId(opId);
            op.setOrder(order);
            op.setProduct(product);
            op.setQty(item.qty());
            op.setPrice(product.getPrice());
            orderProductRepository.save(op);

            product.setQtyInStock(product.getQtyInStock() - item.qty());
            productRepository.save(product);
        }

        cartRepository.findByClientId(clientId)
                .ifPresent(cart -> cartItemRepository.deleteAllByCartId(cart.getId()));

        return toResponse(order);
    }

    @Transactional
    public OrderResponse updateStatus(UUID orderId, UUID statusId) {
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
            return toResponse(order);
        }
        Set<UUID> allowedStatuses = currentStatusId == null
                ? Set.of(PLACED_STATUS_ID)
                : ALLOWED_STATUS_TRANSITIONS.getOrDefault(currentStatusId, Set.of());
        if (!allowedStatuses.contains(statusId)) {
            throw new InvalidOrderStatusTransitionException("Ta zmiana statusu zamówienia nie jest dozwolona");
        }

        order.setOrderStatus(status);
        return toResponse(orderRepository.save(order));
    }

    private List<OrderItemRequest> sortedUniqueItems(List<OrderItemRequest> items) {
        Set<UUID> productIds = new HashSet<>();
        for (OrderItemRequest item : items) {
            if (!productIds.add(item.productId())) {
                throw new DuplicateOrderItemException();
            }
        }
        return items.stream()
                .sorted(Comparator.comparing(OrderItemRequest::productId))
                .toList();
    }

    private ShopOrder getOrder(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(this::orderNotFound);
    }

    private ShopOrder getOrderForClient(UUID orderId, UUID clientId) {
        return orderRepository.findByIdAndClientId(orderId, clientId)
                .orElseThrow(this::orderNotFound);
    }

    private OrderResponse toResponse(ShopOrder order) {
        return new OrderResponse(
                order.getId(),
                toResponse(order.getClient()),
                order.getShippingAddress() == null ? null : toResponse(order.getShippingAddress()),
                order.getShippingMethod() == null ? null : toResponse(order.getShippingMethod()),
                order.getOrderStatus() == null ? null : toResponse(order.getOrderStatus()),
                order.getOrderDate(),
                order.getOrderTotal()
        );
    }

    private ClientResponse toResponse(Client client) {
        return new ClientResponse(
                client.getId(),
                client.getEmail(),
                client.getFirstName(),
                client.getLastName(),
                client.getPhoneNumber(),
                client.getRole()
        );
    }

    private AddressResponse toResponse(Address address) {
        return new AddressResponse(
                address.getId(),
                address.getCity(),
                address.getRegion(),
                address.getPostalCode(),
                address.getStreetNumber(),
                address.getFlat()
        );
    }

    private ShippingMethodResponse toResponse(ShippingMethod shippingMethod) {
        return new ShippingMethodResponse(
                shippingMethod.getId(),
                shippingMethod.getName(),
                shippingMethod.getPrice()
        );
    }

    private OrderStatusResponse toResponse(OrderStatus status) {
        return new OrderStatusResponse(status.getId(), status.getName());
    }

    private ResourceNotFoundException orderNotFound() {
        return new ResourceNotFoundException(ApiErrorCode.ORDER_NOT_FOUND, "Nie znaleziono zamówienia");
    }

    private ResourceNotFoundException productNotFound() {
        return new ResourceNotFoundException(ApiErrorCode.PRODUCT_NOT_FOUND, "Nie znaleziono produktu");
    }
}
