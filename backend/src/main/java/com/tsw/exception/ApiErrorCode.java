package com.tsw.exception;

import org.springframework.http.HttpStatus;

public enum ApiErrorCode {
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "Validation failed"),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "Invalid request"),
    INVALID_QUANTITY(HttpStatus.BAD_REQUEST, "Invalid quantity"),
    INVALID_CATEGORY_HIERARCHY(HttpStatus.BAD_REQUEST, "Invalid category hierarchy"),
    NOT_ACCEPTABLE(HttpStatus.NOT_ACCEPTABLE, "Not acceptable"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "Method not allowed"),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported media type"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Invalid credentials"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Unauthorized"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "Forbidden"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found"),
    CLIENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found"),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found"),
    PRODUCT_IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found"),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found"),
    ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found"),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found"),
    ORDER_STATUS_NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found"),
    SHIPPING_METHOD_NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found"),
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found"),
    EMAIL_ALREADY_USED(HttpStatus.CONFLICT, "Email already used"),
    INSUFFICIENT_STOCK(HttpStatus.CONFLICT, "Insufficient stock"),
    INVALID_ORDER_STATUS_TRANSITION(HttpStatus.CONFLICT, "Invalid order status transition"),
    PRODUCT_IN_USE(HttpStatus.CONFLICT, "Product in use"),
    CATEGORY_NOT_EMPTY(HttpStatus.CONFLICT, "Category not empty"),
    DATA_CONFLICT(HttpStatus.CONFLICT, "Data conflict"),
    FILE_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "File too large"),
    STORAGE_OPERATION_FAILED(HttpStatus.BAD_GATEWAY, "Storage operation failed"),
    ORDER_CONFIGURATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Order configuration error"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");

    private final HttpStatus status;
    private final String title;

    ApiErrorCode(HttpStatus status, String title) {
        this.status = status;
        this.title = title;
    }

    public HttpStatus status() {
        return status;
    }

    public String title() {
        return title;
    }
}
