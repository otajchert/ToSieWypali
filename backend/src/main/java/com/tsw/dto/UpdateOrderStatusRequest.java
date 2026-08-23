package com.tsw.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UpdateOrderStatusRequest(
        @NotNull(message = "ORDER_STATUS_REQUIRED") UUID statusId
) {
}
