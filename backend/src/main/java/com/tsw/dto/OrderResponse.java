package com.tsw.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        ClientResponse client,
        AddressResponse shippingAddress,
        ShippingMethodResponse shippingMethod,
        OrderStatusResponse orderStatus,
        LocalDateTime orderDate,
        BigDecimal orderTotal
) {
}
